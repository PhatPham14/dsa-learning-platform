# Load .env file into current PowerShell session
# Usage: .\scripts\set-dev-env.ps1
$envFile = Join-Path $PSScriptRoot '..\.env'
if (-Not (Test-Path $envFile)) {
    Write-Error ".env file not found at $envFile"
    exit 1
}
Get-Content $envFile | ForEach-Object {
    if ($_ -and -not $_.StartsWith('#')) {
        $parts = $_ -split '=', 2
        if ($parts.Length -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            # Set environment variable for current process only
            $env:$name = $value
            Write-Output "Set $name"
        }
    }
}
Write-Output 'Environment variables loaded into current session.'
