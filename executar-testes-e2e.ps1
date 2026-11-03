$ErrorActionPreference = "Stop"

$scriptBase = Join-Path $PSScriptRoot "executar-testes-integracao.ps1"
$classeTeste = "com.laurencekl.campushub.CampusHubE2ETest"
$preparacao = "powershell.exe -NoProfile -ExecutionPolicy Bypass " +
    "-File .\preparar-evento-e2e.ps1"

& powershell.exe `
    -NoProfile `
    -ExecutionPolicy Bypass `
    -File $scriptBase `
    -ClasseTeste $classeTeste `
    -ComandoPreparacao $preparacao

if ($LASTEXITCODE -ne 0) {
    throw "Os testes E2E falharam."
}
