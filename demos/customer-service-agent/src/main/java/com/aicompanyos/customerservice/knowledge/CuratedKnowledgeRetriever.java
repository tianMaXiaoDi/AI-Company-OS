package com.aicompanyos.customerservice.knowledge;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * A citation-first retrieval baseline. It returns only curated source text from published
 * document chunks, so it remains safe before an embedding model and LLM are selected.
 */
@Service
public class CuratedKnowledgeRetriever implements KnowledgeRetriever {
    private static final Set<String> COMMON_CHINESE_BIGRAMS = Set.of(
            "什么", "怎么", "可以", "我们", "你们", "请问", "一下", "是否", "需要", "这个", "那个");

    private final KnowledgeChunkRepository chunks;

    public CuratedKnowledgeRetriever(KnowledgeChunkRepository chunks) {
        this.chunks = chunks;
    }

    @Override
    public Optional<KnowledgeAnswer> retrieve(String question) {
        Set<String> terms = searchTerms(question);
        if (terms.isEmpty()) {
            return Optional.empty();
        }

        return chunks.findByDocumentStatus(KnowledgeDocumentStatus.PUBLISHED).stream()
                .map(chunk -> new ScoredChunk(chunk, score(chunk, terms)))
                .filter(candidate -> candidate.score() > 0)
                .max(Comparator.comparingInt(ScoredChunk::score)
                        .thenComparing(candidate -> candidate.chunk().document().verifiedAt()))
                .map(candidate -> answerFor(candidate.chunk()));
    }

    private static KnowledgeAnswer answerFor(KnowledgeChunk chunk) {
        KnowledgeDocument document = chunk.document();
        return new KnowledgeAnswer(chunk.content(), List.of(new KnowledgeCitation(
                document.sourceKey(),
                document.title(),
                document.sourceUri() + "#" + chunk.citationAnchor(),
                document.verifiedAt())));
    }

    private static int score(KnowledgeChunk chunk, Set<String> terms) {
        String searchable = (chunk.document().title() + " " + chunk.content()).toLowerCase(Locale.ROOT);
        return (int) terms.stream().filter(searchable::contains).count();
    }

    private static Set<String> searchTerms(String question) {
        String normalized = question.toLowerCase(Locale.ROOT);
        Set<String> terms = new LinkedHashSet<>();
        addKnownTerms(normalized, terms);

        StringBuilder chineseRun = new StringBuilder();
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (Character.UnicodeScript.of(character) == Character.UnicodeScript.HAN) {
                chineseRun.append(character);
            } else {
                addChineseBigrams(chineseRun, terms);
                chineseRun.setLength(0);
            }
        }
        addChineseBigrams(chineseRun, terms);

        for (String token : normalized.split("[^a-z0-9]+")) {
            if (token.length() >= 3) {
                terms.add(token);
            }
        }
        return terms;
    }

    private static void addKnownTerms(String question, Set<String> terms) {
        List<String> supportedTerms = List.of("退款", "取消", "地址", "补偿", "支付", "发票", "人工", "工单", "物流", "订单");
        for (String term : supportedTerms) {
            if (question.contains(term)) {
                terms.add(term);
            }
        }
    }

    private static void addChineseBigrams(StringBuilder chineseRun, Set<String> terms) {
        for (int index = 0; index < chineseRun.length() - 1; index++) {
            String bigram = chineseRun.substring(index, index + 2);
            if (!COMMON_CHINESE_BIGRAMS.contains(bigram)) {
                terms.add(bigram);
            }
        }
    }

    private record ScoredChunk(KnowledgeChunk chunk, int score) {
    }
}
