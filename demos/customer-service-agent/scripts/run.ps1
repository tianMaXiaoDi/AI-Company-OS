param(
    [ValidateRange(1, 65535)]
    [int]$Port = 8080
)

$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -eq $maven) {
    throw 'Maven 3.6.3+ is required. Install Maven or run the service with Docker Compose.'
}

& $maven.Source spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Port"
