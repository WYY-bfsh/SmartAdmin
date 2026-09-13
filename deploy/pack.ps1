$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$outZip = Join-Path $root "smartadmin-src.zip"
$stage = Join-Path $env:TEMP ("smartadmin-pack-" + [guid]::NewGuid().ToString("N"))

function Copy-Tree($from, $to) {
    if (-not (Test-Path -LiteralPath $from)) {
        throw "Missing folder: $from"
    }
    New-Item -ItemType Directory -Path $to -Force | Out-Null
    & robocopy $from $to /E /XD node_modules .git dist target /NFL /NDL /NJH /NJS /NC /NS /NP | Out-Null
    if ($LASTEXITCODE -ge 8) {
        throw "Copy failed: $from (robocopy $LASTEXITCODE)"
    }
}

New-Item -ItemType Directory -Path $stage | Out-Null
try {
    Copy-Tree (Join-Path $root "deploy") (Join-Path $stage "deploy")
    Copy-Tree (Join-Path $root "smart-admin-api-java17-springboot3") (Join-Path $stage "smart-admin-api-java17-springboot3")
    Copy-Tree (Join-Path $root "smart-admin-web-javascript") (Join-Path $stage "smart-admin-web-javascript")
    Copy-Tree (Join-Path $root "smart-app") (Join-Path $stage "smart-app")

    $sqlFile = Get-ChildItem -Path $root -Recurse -Filter "smart_admin_v3.sql" -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notmatch '\\node_modules\\|\\dist\\|\\.git\\' } |
        Select-Object -First 1
    if (-not $sqlFile) {
        throw "Cannot find smart_admin_v3.sql under $root"
    }
    Copy-Tree $sqlFile.DirectoryName (Join-Path $stage "sql")

    Get-ChildItem -LiteralPath (Join-Path $stage "deploy") -Force -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -eq ".env" -or $_.Name -like "*password*" -or $_.Name -like "*mima*" } |
        Remove-Item -Force -ErrorAction SilentlyContinue

    if (Test-Path $outZip) {
        Remove-Item $outZip -Force
    }
    Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $outZip -Force
    Write-Host ""
    Write-Host "OK zip created:"
    Write-Host $outZip
    Write-Host ""
    Write-Host "Next: upload this zip to server /home/ubuntu/"
}
finally {
    if (Test-Path $stage) {
        Remove-Item $stage -Recurse -Force
    }
}
