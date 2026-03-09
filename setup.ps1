param (
    [string]$RepoUrl = "https://github.com/Adi-Codes-well/Java-Shell.git"
)

Write-Host "Starting automated My-Shell-beta installation..." -ForegroundColor Cyan

# Ensure Scoop is installed
if (-not (Get-Command scoop -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Scoop..." -ForegroundColor Yellow
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
    Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression
} else {
    Write-Host "Scoop is already installed." -ForegroundColor Green
}

# Add java bucket
Write-Host "Adding java bucket to scoop..." -ForegroundColor Yellow
scoop bucket add java

Write-Host "Updating Scoop..." -ForegroundColor Yellow
scoop update

# Handle Java installation
$javaInstalled = $false
if (Get-Command java -ErrorAction SilentlyContinue) {
    $javaVersionOutput = java -version 2>&1
    if ($javaVersionOutput[0] -match 'version "(\d+)\.') {
        $version = [int]$matches[1]
        if ($version -ge 21) {
            Write-Host "Java 21 or higher is already installed." -ForegroundColor Green
            $javaInstalled = $true
        } else {
            Write-Host "Old Java version detected ($version). Installing OpenJDK 21 via Scoop..." -ForegroundColor Yellow
        }
    }
}

if (-not $javaInstalled) {
    Write-Host "Installing OpenJDK 21..." -ForegroundColor Yellow
    scoop install openjdk21
}

# Ensure Maven is installed
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Maven..." -ForegroundColor Yellow
    scoop install maven
} else {
    Write-Host "Maven is already installed." -ForegroundColor Green
}

# Ensure Git is installed
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Git..." -ForegroundColor Yellow
    scoop install git
} else {
    Write-Host "Git is already installed." -ForegroundColor Green
}

$InstallDir = "$env:USERPROFILE\Java-Shell"

if (-not (Test-Path -Path $InstallDir)) {
    Write-Host "Cloning repository..." -ForegroundColor Yellow
    git clone $RepoUrl $InstallDir
} else {
    Write-Host "Repository already exists at $InstallDir. Pulling latest..." -ForegroundColor Yellow
    Push-Location $InstallDir
    git pull
    Pop-Location
}

Write-Host "Running installation script..." -ForegroundColor Yellow
Push-Location $InstallDir
cmd.exe /c ".\install.bat"
Pop-Location

Write-Host "All done! You can now run mini-shell from your terminal." -ForegroundColor Green
