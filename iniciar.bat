cd /d "%-dp0"

Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

.\mvnw.cmd spring-boot:run