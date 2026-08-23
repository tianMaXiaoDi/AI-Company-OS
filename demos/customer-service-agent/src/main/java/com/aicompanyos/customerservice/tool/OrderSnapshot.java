package com.aicompanyos.customerservice.tool;

public record OrderSnapshot(String id, String product, String status, String carrier, String trackingNumber, String estimatedDelivery) {
    public String shippingReply() {
        if ("SHIPPED".equals(status)) {
            return "订单 " + id + " 已由 " + carrier + " 发货，运单号 " + trackingNumber + "，预计 " + estimatedDelivery + " 送达。";
        }
        return "订单 " + id + " 当前状态为 " + status + "，预计 " + estimatedDelivery + " 发货或送达。";
    }
}
