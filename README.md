# POC Delivery — Sistema de Delivery Desktop

Trabalho acadêmico da disciplina de Projeto de Software (UFES). Evolução do
POC de delivery (Delivery_CR1) implementando as 12 histórias de usuário
(US01–US12) da especificação, com interface gráfica Swing, persistência em
SQLite e auditoria transversal consumindo a biblioteca de log publicada no
JitPack.

## Desenvolvedores

| Nome                    | Matrícula  |
|-------------------------|------------|
| Gustavo Wesley de Souza | 2023200945 |
| Hiago do Carmo Lopes    | 2022200636 |

## Stack

- Java 21
- Maven
- Swing (interface gráfica)
- SQLite (persistência de domínio e modalidade de auditoria)
- [Log-library](https://github.com/Gustavo-wesley/Log-library) via JitPack
  (biblioteca de auditoria — consumida como está, sem alterações)
- JUnit 5 (testes)

## Como executar

Requisitos: JDK 21 e Maven.

```bash
mvn exec:java
```

No primeiro uso, clique em **Cadastrar usuário** na tela de login: o primeiro
usuário cadastrado recebe perfil **Administrador** com acesso **Autorizado**.
Os cadastros seguintes entram como **Atendente/Pendente** e dependem de
autorização do administrador (menu Administração > Gestão de usuários).

Para rodar os testes:

```bash
mvn test
```

## Funcionalidades (US01–US12)

1. **US01 — Login**: autenticação com hash de senha (SHA-256 + salt); a sessão
   alimenta a auditoria.
2. **US02 — Cadastro de usuário**: primeiro usuário vira Admin/Autorizado,
   demais Atendente/Pendente.
3. **US03 — Gestão de usuários**: busca, autorizar/desautorizar/excluir em
   lote, troca de perfil (restrito ao Administrador).
4. **US04 — Painel operacional**: métricas por estado do pedido na data de
   operação, lista de pedidos com ação Visualizar e barra de status.
5. **US05 — Busca de clientes**: por nome ou CPF (validado pelos dígitos
   verificadores).
6. **US06 — Cadastro de cliente**: 1 a 3 endereços de entrega, exatamente um
   padrão.
7. **US07 — Produtos**: busca por código/nome/categoria e cadastro (Admin).
8. **US08 — Movimentação de estoque**: Entrada (exige nota fiscal) e Ajuste
   (exige motivo), com prévia sem persistência; nunca estoque negativo.
9. **US09 — Pedido**: cliente, endereço vinculado, itens com quantidade
   editável, exclusão por menu de contexto e cupom de desconto.
10. **US10 — Pagamento**: validação de disponibilidade no instante da
    confirmação; baixa de estoque em transação única somente na aprovação.
11. **US11 — Resultado simulado**: aprovado/reprovado 50/50, forma sorteada
    (25% cada), id de transação e prazo estimado; reprovação preserva o pedido.
12. **US12 — Auditoria**: todos os eventos relevantes registrados via
    biblioteca de log (arquivo JSONL/CSV/XML) ou SQLite local.

## Arquitetura

- **MVP Passive View**: cada tela tem uma interface de View (implementada por
  `JFrame`/`JDialog` passivo, sem regra de negócio) e um Presenter que não
  conhece Swing. A navegação entre telas passa pela interface `INavegador`.
- **Repository**: uma interface por agregado (`IUsuarioRepository`,
  `IClienteRepository`, `IProdutoRepository`, `IPedidoRepository`,
  `ICupomRepository`) com implementações SQLite. Toda escrita ocorre em
  transação única (tudo-ou-nada).
- **Strategy**: descontos da taxa de entrega (CR1), perfil de usuário, tipo de
  movimentação de estoque, forma e resultado do pagamento simulado. A fonte de
  aleatoriedade (`IGeradorAleatorio`) é injetável para testes determinísticos.
- **Adapter + Observer + Factory (auditoria)**: os services publicam eventos
  ricos (`AuditoriaPublisher`); o `AuditoriaObserver` escuta e delega ao
  `AuditoriaAdapter`, que traduz para o `LogEntry` da lib (sentinela `"-"`
  quando não há pedido/cliente). A `AuditoriaLoggerFactory` decide a
  modalidade da execução: loggers de arquivo da lib ou o `SqliteLogger` local.
- **Injeção de dependência manual** por construtor, concentrada na composição
  `AplicacaoDelivery` (sem framework).

```
src/main/java/com/ufes/delivery/
├── auditoria/      Adapter, Observer, factory e SqliteLogger (US12)
├── configuracao/   parâmetros da aplicação
├── desconto/       strategies de desconto (CR1) e aplicador de cupom
├── excecao/        ValidacaoException
├── model/          agregados de domínio (+ perfil/ e estoque/)
├── repository/     interfaces + implementações sqlite/
├── seguranca/      hash de senha
├── service/        services de domínio (+ pagamento/)
├── ui/
│   ├── view/       interfaces das Views (MVP)
│   ├── presenter/  Presenters (sem Swing) e INavegador
│   └── swing/      JFrames/JDialogs passivos
└── validacao/      ValidadorCpf e ValidadorCep
```

## Configuração da auditoria

Por padrão a auditoria grava em SQLite (`auditoria.db`). Para trocar a
modalidade, crie um arquivo `auditoria.properties` no diretório de execução:

```properties
# JSONL, CSV, XML ou SQLITE (modalidade única por execução)
auditoria.modalidade=CSV
auditoria.caminho=auditoria.csv
```

Indisponibilidade da auditoria é tratada como falha operacional, sem vazar
dados sensíveis (senhas nunca aparecem nos registros).

## Bancos de dados

- `delivery.db` — domínio (usuários, clientes, produtos, pedidos, cupons,
  movimentações e pagamentos). Criado automaticamente na primeira execução.
- `auditoria.db` — registros de auditoria (modalidade padrão).

Ambos ficam no diretório de execução e não são versionados. O cupom
**EDUCAR10** (10%) é carregado automaticamente para os cenários de aceite.
