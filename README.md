# Avaliações API

Este é um projeto Java utilizando [Quarkus](https://quarkus.io/) integrado com **Azure Functions**, criado para gerenciar e processar avaliações e feedbacks.

## Tecnologias Utilizadas
- **Java 21**
- **Quarkus** (Framework principal)
- **Azure Functions** (Gatilhos HTTP, Queue e Timer)
- **Maven** (Gerenciador de dependências e build)
- **JUnit 5 / RestAssured / Mockito** (Testes)

## Arquitetura
O projeto adota uma estrutura em camadas, separando o domínio da aplicação da infraestrutura:
- `org.adjt.application.services`: Contém as regras de negócio e serviços da aplicação.
- `org.adjt.infrastructure.entrypoints`: Contém os pontos de entrada das funções do Azure.

### Funções do Azure (Azure Functions)

O projeto possui os seguintes gatilhos (Triggers) implementados como Azure Functions:

1. **ReceberFeedback (HTTP Trigger)**
   - **Descrição:** Endpoint para receber um novo feedback.
   - **Método:** `POST`
   - **Parâmetros aceitos:** Recebe o parâmetro `grade` pela Query String ou via Corpo da requisição (Body).
   - **Autorização:** Anonymous

2. **NotificarCriticos (Queue Trigger)**
   - **Descrição:** Ouve a fila `feedbacks-criticos` e processa mensagens de avaliações que demandam atenção crítica.
   - **Conexão:** `AzureWebJobsStorage`

3. **GerarRelatorioSemanal (Timer Trigger)**
   - **Descrição:** Função engatilhada por tempo para gerar relatórios. Atualmente agendada para rodar a cada 30 segundos (`*/30 * * * * *`).

## CI/CD (GitHub Actions)

O projeto inclui um pipeline de CI/CD automatizado através do **GitHub Actions**. O arquivo de workflow (`.github/workflows/main_avaliacoes-tc-4.yml`) está configurado para realizar o build e o deploy do projeto para a nuvem da Azure.

- **Trigger:** O pipeline é executado a cada `push` na branch `main` ou manualmente via `workflow_dispatch`.
- **Funcionalidades:**
    - Faz o checkout do código fonte.
    - Configura a distribuição Microsoft do **Java SDK 21** com suporte a cache do Maven.
    - Realiza o build (compilação e empacotamento) do código via Maven.
    - Localiza dinamicamente a pasta de destino dos pacotes empacotados pelo Quarkus.
    - Realiza a autenticação segura com a Azure via OIDC (Token JWT).
    - Executa a action do Azure Functions para implantar a aplicação de forma automatizada no Function App (`avaliacoes-tc-4`) em ambiente de Produção.

## Como executar localmente

1. Certifique-se de ter o **JDK 21** e **Maven** instalados na sua máquina.
2. Você também precisará do **Azure Functions Core Tools** instalado para rodar as funções localmente.
3. No terminal, execute o seguinte comando na raiz do projeto:

```bash
./mvnw clean package
./mvnw quarkus:dev
```

Para rodar diretamente as Azure Functions localmente usando o Maven:
```bash
./mvnw azure-functions:run
```

## Como empacotar para a nuvem

Para compilar o projeto e gerar o pacote de deploy para a Azure, utilize o comando:

```bash
./mvnw clean package
```

Isto irá preparar os artefatos necessários na pasta `target/azure-functions/`.

## Testes
Os testes da aplicação podem ser executados com:
```bash
./mvnw test
```
