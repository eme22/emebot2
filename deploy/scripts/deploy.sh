#!/bin/bash
set -e

echo -e "\e[36m=== 1. Iniciando compilación de Docker ===\e[0m"
docker build -f deploy/docker/Dockerfile.prod -t emebot:prod .

echo -e "\e[36m=== 2. Guardando y subiendo imagen al servidor ===\e[0m"
echo -e "\e[33mExportando emebot:prod y cargándolo en el servidor rozskin@10.241.251.82...\e[0m"

docker save emebot:prod | ssh rozskin@10.241.251.82 "docker load"

echo -e "\e[32m=== ¡Despliegue completado con éxito! ===\e[0m"
