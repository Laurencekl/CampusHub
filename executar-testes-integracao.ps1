$ErrorActionPreference = "Stop"

$pastaProjeto = Split-Path -Parent $MyInvocation.MyCommand.Path
$arquivoLocal = Join-Path $pastaProjeto "local.properties"
$javaAndroidStudio = "C:\Program Files\Android\Android Studio\jbr"

if (Test-Path -LiteralPath (Join-Path $javaAndroidStudio "bin\java.exe")) {
    $env:JAVA_HOME = $javaAndroidStudio
    $env:Path = "$javaAndroidStudio\bin;$env:Path"
}

if (-not (Test-Path -LiteralPath $arquivoLocal)) {
    throw "Arquivo local.properties nao encontrado. Abra o projeto no Android Studio primeiro."
}

$linhaSdk = Get-Content -LiteralPath $arquivoLocal |
    Where-Object { $_ -like "sdk.dir=*" } |
    Select-Object -First 1
$pastaSdk = ($linhaSdk -split "=", 2)[1]
$pastaSdk = $pastaSdk -replace "\\:", ":" -replace "\\\\", "\"
$adb = Join-Path $pastaSdk "platform-tools\adb.exe"

if (-not (Test-Path -LiteralPath $adb)) {
    throw "ADB nao encontrado em $adb"
}

$dispositivos = & $adb devices
$conectado = $dispositivos | Where-Object { $_ -match "\tdevice$" }

if (-not $conectado) {
    throw "Conecte e autorize o celular antes de executar os testes."
}

& $adb reverse tcp:8080 tcp:8080
& $adb reverse tcp:9099 tcp:9099

$classeTeste = "com.laurencekl.campushub.FirebaseIntegracaoTest"
$comandoGradle = ".\gradlew.bat connectedDebugAndroidTest " +
    "-Pandroid.testInstrumentationRunnerArguments.class=$classeTeste"

Push-Location $pastaProjeto
try {
    & npx.cmd --yes firebase-tools@15.34.2 emulators:exec `
        --project "campushub-1d2a4" `
        --only "auth,firestore" `
        $comandoGradle

    if ($LASTEXITCODE -ne 0) {
        throw "Os testes de integracao falharam."
    }
}
finally {
    Pop-Location
}
