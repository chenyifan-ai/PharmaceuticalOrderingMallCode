# Create GitHub repo and push (requires GitHub CLI: gh auth login)
param(
    [string]$RepoName = 'chenyifan-ai/PharmaceuticalOrderingMallCode',
    [string]$Visibility = 'private',
    [string]$Description = '医药订货系统 - Spring Boot + Vue3 B2B 订货平台'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

$gh = Get-Command gh -ErrorAction SilentlyContinue
if (-not $gh) {
    Write-Host '未找到 gh CLI。请先安装: https://cli.github.com/' -ForegroundColor Yellow
    Write-Host '安装后执行: gh auth login' -ForegroundColor Yellow
    exit 1
}

gh auth status 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host '请先登录 GitHub: gh auth login' -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path '.git')) {
    git init
    git add -A
    git commit -m 'Initial commit: pharmacy mall platform'
}

$exists = gh repo view $RepoName 2>$null
if ($LASTEXITCODE -ne 0) {
    if ($Visibility -eq 'public') {
        gh repo create $RepoName --public --source=. --remote=origin --description $Description --push
    } else {
        gh repo create $RepoName --private --source=. --remote=origin --description $Description --push
    }
    Write-Host "已创建并推送: $(gh repo view $RepoName --json url -q .url)"
} else {
    $url = gh repo view $RepoName --json url -q .url
    git remote remove origin 2>$null
    git remote add origin $url
    git branch -M main
    git push -u origin main
    Write-Host "已推送到: $url"
}
