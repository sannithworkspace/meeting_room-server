#!/bin/bash
# ============================================================
# Complete AWS EC2 Installer & Environment Bootstrapper
# Works on: Ubuntu 20.04/22.04/24.04 & Debian
# ============================================================

set -e

echo "============================================================"
echo " Starting Full EC2 Provisioning & Dependency Installation"
echo "============================================================"

# 1. Update OS packages & base tools
echo "[1/6] Updating system repositories..."
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release wget ufw htop unzip git

# 2. Install Docker & Docker Compose Plugin
echo "[2/6] Installing Docker Engine & Docker Compose..."
sudo apt-get install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker $USER

# Grant passwordless sudo for docker binary
echo "$USER ALL=(ALL) NOPASSWD: /usr/bin/docker, /usr/libexec/docker/cli-plugins/docker-compose" | sudo tee /etc/sudoers.d/docker-github-actions > /dev/null
sudo chmod 0440 /etc/sudoers.d/docker-github-actions

# 3. Create 2GB Swap Memory (prevents out-of-memory crashes on t2.micro/t3.small)
echo "[3/6] Configuring 2GB Swap Memory..."
if [ ! -f /swapfile ]; then
    sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    echo "Swap created successfully."
else
    echo "Swapfile already exists."
fi

# 4. Install OpenJDK 21 (for direct local execution if needed)
echo "[4/6] Installing Java 21 JDK..."
sudo apt-get install -y openjdk-21-jdk-headless || sudo apt-get install -y openjdk-17-jdk-headless

# 5. Fix SSH permissions for GitHub Actions
echo "[5/6] Ensuring secure SSH directory permissions..."
mkdir -p ~/.ssh
chmod 700 ~/.ssh
if [ -f ~/.ssh/authorized_keys ]; then
    chmod 600 ~/.ssh/authorized_keys
fi

# 6. Create shared Docker overlay network
echo "[6/6] Initializing Docker network ('meetingroom-network')..."
sudo docker network create meetingroom-network 2>/dev/null || true

echo ""
echo "============================================================"
echo " FULL EC2 ENVIRONMENT PROVISIONED SUCCESSFULLY!"
echo "============================================================"
echo " Docker Version : $(docker --version)"
echo " Docker Compose : $(docker compose version)"
echo " Java Version   : $(java -version 2>&1 | head -n 1)"
echo " Swap Space     : $(free -h | grep Swap)"
echo " Docker Network : meetingroom-network (Active)"
echo "============================================================"
