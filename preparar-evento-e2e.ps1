$ErrorActionPreference = "Stop"

$uri = "http://127.0.0.1:8080/v1/projects/" +
    "campushub-1d2a4/databases/(default)/documents/eventos/evento-e2e"
$cabecalho = @{ Authorization = "Bearer owner" }
$documento = @{
    fields = @{
        titulo = @{ stringValue = "Evento E2E CampusHub" }
        descricao = @{ stringValue = "Evento criado para o teste ponta a ponta" }
        data = @{ stringValue = "31/12/2099" }
        horario = @{ stringValue = "10:00" }
        local = @{ stringValue = "Campus Universitario" }
        categoria = @{ stringValue = "Tecnologia" }
        endereco = @{ stringValue = "Universidade" }
        limiteVagas = @{ integerValue = "20" }
        inscritos = @{ integerValue = "0" }
    }
} | ConvertTo-Json -Depth 8

Invoke-RestMethod `
    -Method Patch `
    -Uri $uri `
    -Headers $cabecalho `
    -ContentType "application/json" `
    -Body $documento | Out-Null

Write-Output "Evento temporario criado no Firestore Emulator."
