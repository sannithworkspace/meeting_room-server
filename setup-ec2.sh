#!/bin/bash
# ============================================================
# Fail-Proof AWS EC2 Provisioning Script for GitHub Actions
# Author: MeetingRoom Microservices Team
# ============================================================

set -e

echo "============================================================"
echo " Starting Complete AWS EC2 Provisioning for GitHub Actions"
echo "============================================================"

# 1. Update OS packages & install prerequisites
echo "[1/5] Updating OS packages and installing essential tools..."
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release wget ufw htop

# 2. Install Docker & Docker Compose
echo "[2/5] Installing Docker Engine & Docker Compose Plugin..."
if ! command -v docker &> /dev/null; then
    sudo apt-get install -y docker.io docker-compose-plugin
else
    echo "Docker is already installed."
fi

# 3. Enable Docker service & set passwordless permissions for GitHub Actions runner
echo "[3/5] Setting up Docker background service & permissions..."
sudo systemctl enable --now docker
sudo usermod -aG docker $USER

# Grant passwordless sudo for docker commands to ensure GitHub Actions SSH step never prompts for a password
echo "$USER ALL=(ALL) NOPASSWD: /usr/bin/docker, /usr/libexec/docker/cli-plugins/docker-compose" | sudo tee /etc/sudoers.d/docker-github-actions > /dev/null
sudo chmod 0440 /etc/sudoers.d/docker-github-actions

# 4. Set strict SSH directory permissions for GitHub SSH Action
echo "[4/5] Securing SSH directory permissions (~/.ssh)..."
mkdir -p ~/.ssh
chmod 700 ~/.ssh
if [ -f ~/.ssh/authorized_keys ]; then
    chmod 600 ~/.ssh/authorized_keys
fi

# 5. Create shared Docker overlay network for microservices communication
echo "[5/5] Creating shared Docker bridge network ('meetingroom-network')..."
sudo docker network create meetingroom-network 2>/dev/null || true

echo ""
echo "============================================================"
echo " EC2 PROVISIONING COMPLETE! VERIFICATION SUMMARY:"
echo "============================================================"
echo " Docker Version       : $(docker --version)"
echo " Docker Compose       : $(docker compose version)"
echo " Active Docker Net    : $(sudo docker network ls | grep meetingroom-network)"
echo " Passwordless Sudo    : Configured for user '$USER'"
echo " SSH Permissions      : Verified (700 for ~/.ssh, 600 for authorized_keys)"
echo "============================================================"
echo " SECURITY GROUP REMINDER: Ensure ports 22, 80, 8080, 8761, 9411 are open."
echo "============================================================"
