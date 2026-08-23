$projectRoot = Split-Path -Parent $PSScriptRoot
$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -eq $maven) {
    throw 'Maven 3.6.3+ is required. Install Maven or run `mvn test` from a Maven-enabled terminal.'
}

Push-Location $projectRoot
try {
    & $maven.Source test
} finally {
    Pop-Location
}
