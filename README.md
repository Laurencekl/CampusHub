# CampusHub

Aplicativo Android para alunos encontrarem eventos universitários, realizarem inscrições e compartilharem suas experiências. O sistema também oferece uma área para organizadores criarem e acompanharem seus próprios eventos.

## Funcionalidades dos alunos

- Criação de conta, login, logout e recuperação de senha.
- Visualização e edição do perfil.
- Listagem, pesquisa e detalhes dos eventos.
- Filtros por categoria e situação: próximos ou encerrados.
- Inscrição, cancelamento e consulta da tela **Meus eventos**.
- Controle de vagas e entrada na lista de espera quando o evento estiver lotado.
- Confirmação da inscrição pelo primeiro aluno da lista quando surgir uma vaga.
- Favoritos e tela **Meus favoritos**.
- Comentários com autor e data, permitindo editar ou excluir apenas os próprios comentários.
- Avaliação de eventos encerrados com nota de 1 a 5 e exibição da média.
- Exibição do evento no mapa, compartilhamento e adição ao calendário.
- Publicação local de fotos, áudios e vídeos relacionados ao evento.
- QR Code individual para o check-in de cada inscrição confirmada.

## Funcionalidades dos organizadores

- Criação de eventos como rascunho ou já publicados.
- Edição dos próprios eventos.
- Publicação e encerramento dos eventos sob sua responsabilidade.
- Cadastro de título, descrição, categoria, data, horário, local, endereço, capacidade e imagem por URL.
- Acompanhamento das vagas ocupadas, lista de espera, participantes e check-ins.
- Check-in manual ou por leitura do QR Code do aluno.
- Bloqueio de uma segunda validação do mesmo QR Code.

## Tecnologias

- Kotlin e layouts XML.
- AndroidX e Material Components.
- Firebase Authentication e Cloud Firestore.
- Glide para carregar imagens dos eventos.
- ZXing para gerar e ler QR Codes.
- JUnit, AndroidX Test, Espresso e UI Automator.
- JaCoCo para o relatório de cobertura.
- Gradle Kotlin DSL.

## Organização dos dados

- `usuarios`: perfil e dados básicos de cada conta.
- `eventos`: informações, organizador, situação, vagas e lista de espera.
- `eventos/{eventoId}/inscritos`: participantes e situação do check-in.
- `eventos/{eventoId}/comentarios`: comentários publicados.
- `eventos/{eventoId}/avaliacoes`: avaliações dos alunos inscritos.
- `usuarios/{uid}/inscricoes`: eventos em que o aluno está inscrito.
- `usuarios/{uid}/favoritos`: eventos favoritos do aluno.

As fotos, os áudios e os vídeos publicados ficam armazenados apenas no aparelho utilizado. Os demais dados são sincronizados pelo Firestore.

## Como executar

1. Abra o projeto no Android Studio.
2. Crie um projeto no Firebase com o pacote `com.laurencekl.campushub`.
3. Ative a autenticação por e-mail e senha e crie o banco Cloud Firestore.
4. Adicione o arquivo `google-services.json` dentro da pasta `app`.
5. Publique no Firestore as regras disponíveis no arquivo `firestore.rules`.
6. Sincronize o projeto com o Gradle.
7. Execute em um emulador ou aparelho Android.

Toda conta nova recebe o perfil `aluno`. Para testar a administração, altere no documento `usuarios/{uid}` do Firestore o campo `perfil` para `organizador`.

## Testes automatizados

Execute os testes unitários e verifique a cobertura:

```powershell
.\gradlew.bat testDebugUnitTest relatorioCobertura verificarCobertura
```

Com um emulador ou aparelho conectado, execute os testes de integração com os emuladores do Firebase:

```powershell
powershell -ExecutionPolicy Bypass -File .\executar-testes-integracao.ps1
```

Os relatórios ficam dentro de `app/build/reports`.

## Formato dos eventos

Os documentos da coleção `eventos` utilizam campos como `titulo`, `descricao`, `data`, `horario`, `local`, `endereco`, `categoria`, `imagemUrl`, `limiteVagas`, `inscritos`, `filaEspera`, `organizadorId` e `status`.

A data segue o formato `dd/MM/yyyy`, o horário utiliza `HH:mm` e a situação do evento pode ser `rascunho`, `publicado` ou `encerrado`.
