# PowerShell Deployment Script for EmeBot
$ErrorActionPreference = "Stop"

Write-Host "=== 1. Iniciando compilación de Docker ===" -ForegroundColor Cyan
docker build -f src/main/docker/Dockerfile.prod -t emebot:prod .

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: La compilación de Docker falló." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "=== 2. Guardando y subiendo imagen al servidor ===" -ForegroundColor Cyan
Write-Host "Exportando emebot:prod y cargándolo en el servidor rozskin@10.241.251.82..." -ForegroundColor Yellow

# Ejecutar el docker save y enviarlo por SSH
docker save emebot:prod | ssh rozskin@10.241.251.82 "docker load"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Falló la subida de la imagen por SSH." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "=== ¡Despliegue completado con éxito! ===" -ForegroundColor Green
