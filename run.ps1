Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Iniciando compilacion y arranque (UNLa) " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Matar procesos Java y protoc colgados
Write-Host "`n[1/3] Limpiando procesos previos..." -ForegroundColor Yellow
Get-Process -Name java, protoc -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

# 2. Limpieza forzada de target en Windows
Write-Host "`n[2/3] Limpiando target..." -ForegroundColor Yellow
if (Test-Path .\target) {
    Remove-Item -Recurse -Force .\target -ErrorAction SilentlyContinue
}

# 3. Compilacion completa (ejecuta protobuf y compila Java en una sola pasada)
Write-Host "`n[3/3] Compilando Protobuf y clases Java..." -ForegroundColor Yellow
.\mvnw.cmd compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error durante la compilacion. Abortando." -ForegroundColor Red
    exit $LASTEXITCODE
}

# 4. Levantar Spring Boot
Write-Host "`n=========================================" -ForegroundColor Green
Write-Host " Levantando Spring Boot... " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
.\mvnw.cmd spring-boot:run