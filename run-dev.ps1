# Load .env variables into current PowerShell session
Write-Host "Loading .env variables..." -ForegroundColor Cyan

if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            $key = $matches[1]
            $value = $matches[2]
            [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
            Write-Host "  ✓ $key" -ForegroundColor Green
        }
    }
} else {
    Write-Host "  ⚠ .env file not found!" -ForegroundColor Yellow
}

Write-Host "`nStarting Spring Boot application..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run
