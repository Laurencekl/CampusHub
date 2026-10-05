# CampusHub

Aplicativo Android para alunos encontrarem eventos da universidade, organizarem seus favoritos, compartilharem experiências e acompanharem suas inscrições.

## Funcionalidades

- Criação de conta, login e logout.
- Recuperação de senha por e-mail.
- Visualização e edição do perfil do usuário.
- Listagem e detalhes dos eventos.
- Pesquisa de eventos pelo título.
- Filtros por categoria e situação: próximos ou encerrados.
- Identificação da situação em cada cartão de evento.
- Controle de vagas disponíveis e bloqueio de inscrições em eventos lotados.
- Inscrição e cancelamento de inscrição.
- Tela **Meus eventos** com as inscrições do aluno.
- Favoritar e desfavoritar eventos.
- Tela **Meus favoritos**.
- Comentários com nome do autor e data de publicação.
- Edição e exclusão dos próprios comentários.
- Avaliação de eventos encerrados com notas de 1 a 5.
- Exibição da média das avaliações.
- Compartilhamento das informações do evento.
- Adição do evento ao calendário do celular.

## Tecnologias

- Kotlin.
- XML, AndroidX e Material Components.
- Firebase Authentication.
- Cloud Firestore.
- Gradle Kotlin DSL.

## Organização dos dados

Os eventos são armazenados na coleção `eventos` do Cloud Firestore. Cada usuário possui suas próprias inscrições e seus favoritos. Os comentários e as avaliações ficam associados ao evento correspondente.

## Como executar

1. Abra o projeto no Android Studio.
2. Crie um projeto no Firebase com o pacote `com.laurencekl.campushub`.
3. Ative a autenticação por e-mail e senha e crie o banco Cloud Firestore.
4. Adicione o arquivo `google-services.json` dentro da pasta `app`.
5. Publique no Firestore as regras disponíveis no arquivo `firestore.rules`.
6. Sincronize o projeto com o Gradle.
7. Execute em um emulador ou aparelho Android.

## Formato dos eventos

Cada documento da coleção `eventos` utiliza os campos `titulo`, `descricao`, `data`, `horario`, `local`, `categoria`, `limiteVagas` e `inscritos`. A data deve seguir o formato `dd/MM/yyyy` e o horário o formato `HH:mm`.
