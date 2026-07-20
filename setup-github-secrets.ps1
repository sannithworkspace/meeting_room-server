# ============================================================
# GitHub Repository Secrets Single-Go Injection Script
# Requirement: GitHub CLI (`gh auth login` must be completed)
# ============================================================

param (
    [string]$DockerHubUser,
    [string]$DockerHubToken,
    [string]$EC2Host,
    [string]$EC2User = "ubuntu",
    [string]$EC2KeyPath,
    [string]$JwtSecret = "MeetingRoomProjectSuperSecretSigningKey2026With256BitsMinimumLength!",
    [string]$RDSEndpoint,
    [string]$RDSPort = "5432",
    [string]$RDSDBName = "postgres",
    [string]$RDSUser = "postgres",
    [string]$RDSPassword
)

if (-not $DockerHubUser -or -not $DockerHubToken -or -not $EC2Host -or -not $EC2KeyPath -or -not $RDSEndpoint -or -not $RDSPassword) {
    Write-Host "Error: Mandatory parameters missing!" -ForegroundColor Red
    Write-Host "Usage example:" -ForegroundColor Yellow
    Write-Host ".\setup-github-secrets.ps1 -DockerHubUser 'myuser' -DockerHubToken 'dckr_pat_...' -EC2Host '54.210.x.x' -EC2KeyPath 'C:\keys\id_rsa' -RDSEndpoint 'db.rds.amazonaws.com' -RDSPassword 'dbpass123'"
    exit 1
}

$SSHKeyContent = Get-Content -Raw $EC2KeyPath

Write-Host "Setting GitHub Repository Secrets..." -ForegroundColor Cyan

gh secret set DOCKER_HUB_USERNAME --body "$DockerHubUser"
gh secret set DOCKER_HUB_TOKEN --body "$DockerHubToken"
gh secret set EC2_HOST --body "$EC2Host"
gh secret set EC2_USERNAME --body "$EC2User"
gh secret set EC2_SSH_KEY --body "$SSHKeyContent"
gh secret set EUREKA_USERNAME --body "admin"
gh secret set EUREKA_PASSWORD --body "admin123"
gh secret set JWT_SECRET --body "$JwtSecret"
gh secret set RDS_ENDPOINT --body "$RDSEndpoint"
gh secret set RDS_PORT --body "$RDSPort"
gh secret set RDS_DB_NAME --body "$RDSDBName"
gh secret set RDS_USERNAME --body "$RDSUser"
gh secret set RDS_PASSWORD --body "$RDSPassword"

Write-Host "ALL 12 GITHUB SECRETS CONFIGURED SUCCESSFULLY IN A SINGLE GO!" -ForegroundColor Green
