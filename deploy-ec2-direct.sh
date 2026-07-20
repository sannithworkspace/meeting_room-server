#!/bin/bash
# ============================================================
# Direct EC2 Deployment Script (Without GitHub Actions)
# ============================================================

set -e

echo "============================================================"
echo " Starting Direct EC2 Deployment for Meeting Room Platform"
echo "============================================================"

# Ensure meetingroom-network exists
sudo docker network create meetingroom-network 2>/dev/null || true

# Ensure .env file exists
if [ ! -f .env ]; then
    echo "[ERROR] .env file not found! Please create a .env file with your RDS & JWT credentials."
    exit 1
fi

echo "[1/2] Pulling latest production Docker images..."
sudo docker compose -f docker-compose.prod.yml pull

echo "[2/2] Launching containers..."
sudo docker compose -f docker-compose.prod.yml up -d

echo ""
echo "============================================================"
echo " DEPLOYMENT COMPLETE!"
echo "============================================================"
echo " Frontend Web App   : http://$(curl -s ifconfig.me)"
echo " API Gateway Ingress : http://$(curl -s ifconfig.me):8080"
echo " Eureka Registry     : http://$(curl -s ifconfig.me):8761"
echo "============================================================"
