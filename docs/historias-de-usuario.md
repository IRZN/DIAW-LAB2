# 📋 Histórias de Usuário — Sistema de Aluguel de Carros

Formato: **Como** <ator>, **quero** <ação>, **para** <benefício>. Cada história tem critérios de aceitação e o caso de uso (UC) e endpoint correspondentes, garantindo o rastreamento **requisito → modelo → código**.

| ID | Ator | UC | Prioridade |
|----|------|----|------------|
| US01 | Usuário | UC01 | Alta |
| US02 | Usuário | UC02 | Alta |
| US03 | Cliente | UC03 | Alta |
| US04 | Cliente | UC04 | Alta |
| US05 | Cliente | UC06 | Alta |
| US06 | Cliente | UC05 | Média |
| US07 | Cliente | UC07 | Média |
| US08 | Agente | UC06 / UC12 | Alta |
| US09 | Agente | UC08 | Alta |
| US10 | Agente | UC05 | Média |
| US11 | Agente | UC09 | Alta |
| US12 | Banco | UC10 | Média |
| US13 | Agente | UC11 | Média |

---

### US01 — Cadastro prévio
**Como** usuário (cliente ou agente), **quero** me cadastrar no sistema, **para** poder utilizá-lo.
- O cliente informa usuário, senha, RG, CPF, nome, endereço, profissão e até **3 rendimentos** (entidade empregadora + valor).
- O agente informa usuário, senha, nome, CNPJ e tipo (**Empresa** ou **Banco**).
- Login, CPF e CNPJ são únicos; senha com no mínimo 6 caracteres, armazenada com BCrypt.
- Endpoints: `POST /api/auth/registro/cliente`, `POST /api/auth/registro/agente`.

### US02 — Login obrigatório
**Como** usuário cadastrado, **quero** entrar com usuário e senha, **para** acessar as funcionalidades do meu perfil.
- Credenciais inválidas retornam mensagem "Usuário ou senha inválidos." (HTTP 401).
- Após o login é emitido um token JWT com o perfil (`CLIENTE`/`AGENTE`) e validade de 8h.
- Qualquer rota (exceto login, cadastro e `/api/status`) exige token válido.
- Endpoint: `POST /api/auth/login`.

### US03 — Manter meu cadastro (CRUD de cliente)
**Como** cliente, **quero** consultar, alterar e excluir meu cadastro, **para** manter meus dados corretos.
- O cliente só acessa/altera o próprio cadastro (HTTP 403 caso contrário).
- A senha é opcional na edição (em branco = mantém a atual).
- Não é permitido mais de 3 rendimentos.
- Não é possível excluir a conta se houver pedidos vinculados.
- Endpoints: `GET /api/clientes/me`, `PUT /api/clientes/{id}`, `DELETE /api/clientes/{id}`.

### US04 — Introduzir pedido de aluguel
**Como** cliente, **quero** criar um pedido escolhendo um automóvel e o período, **para** solicitar o aluguel.
- Somente automóveis **disponíveis** podem ser escolhidos.
- A data de início não pode estar no passado e a data de fim não pode ser anterior à de início.
- O cliente pode indicar que deseja **crédito bancário** e o valor desejado.
- Todo pedido nasce com status **PENDENTE**.
- Endpoint: `POST /api/pedidos`.

### US05 — Consultar meus pedidos e status
**Como** cliente, **quero** listar meus pedidos e ver o status de cada um, **para** acompanhar o andamento.
- O cliente vê apenas os próprios pedidos; pode filtrar por status.
- O detalhe exibe linha do tempo (Pendente → Aprovado → Contratado), parecer e contrato.
- Endpoints: `GET /api/pedidos`, `GET /api/pedidos/{id}`.

### US06 — Modificar pedido
**Como** cliente, **quero** alterar automóvel, datas ou observações de um pedido, **para** corrigir minha solicitação.
- Permitido apenas enquanto o pedido estiver **PENDENTE**.
- Endpoint: `PUT /api/pedidos/{id}`.

### US07 — Cancelar pedido
**Como** cliente, **quero** cancelar um pedido, **para** desistir do aluguel.
- Permitido para pedidos **PENDENTES** ou **APROVADOS**; status passa a **CANCELADO**.
- Endpoint: `PATCH /api/pedidos/{id}/cancelar`.

### US08 — Consultar pedidos e contratantes
**Como** agente, **quero** listar todos os pedidos e consultar os dados dos clientes, **para** realizar a análise financeira.
- Agente vê todos os pedidos e a ficha do cliente (profissão, rendimentos e renda total).
- Endpoints: `GET /api/pedidos?status=`, `GET /api/clientes`, `GET /api/clientes/{id}`.

### US09 — Avaliar pedido (análise financeira)
**Como** agente, **quero** registrar um parecer positivo ou negativo, **para** decidir se o pedido segue para contrato.
- Só pedidos **PENDENTES** podem ser avaliados; o parecer textual é obrigatório.
- Parecer positivo → **APROVADO**; negativo → **REPROVADO**. O agente avaliador fica registrado.
- Endpoint: `PATCH /api/pedidos/{id}/avaliar`.

### US10 — Modificar pedido (agente)
**Como** agente, **quero** ajustar um pedido, **para** adequá-lo às condições da análise.
- Permitido para pedidos **PENDENTES** ou **APROVADOS**.
- Endpoint: `PUT /api/pedidos/{id}`.

### US11 — Executar contrato
**Como** agente, **quero** executar o contrato de um pedido aprovado, **para** concretizar o aluguel.
- Só pedidos **APROVADOS** com automóvel ainda disponível.
- Define a propriedade do automóvel: **Cliente**, **Empresa** (se o agente for empresa) ou **Banco** (se o agente for banco).
- Pedido passa a **CONTRATADO** e o automóvel fica indisponível.
- Endpoint: `POST /api/pedidos/{id}/contrato`.

### US12 — Conceder contrato de crédito
**Como** banco agente, **quero** associar um contrato de crédito ao aluguel, **para** financiar o cliente.
- Somente agentes do tipo **BANCO** concedem crédito (valor, juros a.m., nº de parcelas).
- Pedidos que solicitaram crédito só podem ser contratados por um banco.
- O sistema calcula a parcela pela Tabela Price.
- Endpoint: `POST /api/pedidos/{id}/contrato` (campo `credito`).

### US13 — Manter automóveis
**Como** agente, **quero** cadastrar, editar e excluir automóveis (matrícula, ano, marca, modelo e placa), **para** manter a frota disponível para aluguel.
- Placa e matrícula são únicas; não é possível excluir automóvel com pedidos vinculados.
- Endpoints: `GET/POST /api/automoveis`, `PUT/DELETE /api/automoveis/{id}`.
