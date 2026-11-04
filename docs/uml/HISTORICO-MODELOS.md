# 🗂️ Histórico de versões dos modelos UML

Este arquivo registra a evolução dos modelos ao longo do laboratório, comparando a modelagem inicial com o protótipo final (requisito da apresentação final).

| Versão | Pasta / arquivo | Descrição |
|--------|-----------------|-----------|
| v1 | [`v1-inicial/diagrama-classes-v1.mmd`](v1-inicial/diagrama-classes-v1.mmd) | Modelo de classes inicial, derivado diretamente do enunciado. |
| v2 (atual) | [`diagrama-classes.mmd`](diagrama-classes.mmd), [`plantuml/classes.puml`](plantuml/classes.puml) | Modelo alinhado ao código (`br.pucminas.aluguelcarros.model`). |
| v2 (atual) | [`diagrama-casos-de-uso.mmd`](diagrama-casos-de-uso.mmd), [`plantuml/casos-de-uso.puml`](plantuml/casos-de-uso.puml) | 12 casos de uso, atores Cliente, Agente (Empresa/Banco). |
| v2 (atual) | [`diagrama-pacotes.mmd`](diagrama-pacotes.mmd), [`plantuml/pacotes.puml`](plantuml/pacotes.puml) | Visão lógica: 2 subsistemas e camadas MVC. |
| v2 (atual) | [`diagrama-estados-pedido.mmd`](diagrama-estados-pedido.mmd) | Ciclo de vida do pedido (adicionado durante a implementação). |

## Modificações da v1 para a v2 (e o motivo)

1. **Rendimentos viraram a classe `Rendimento` (composição 0..3).**
   Na v1 havia dois vetores paralelos (`empregadores[]`, `rendimentos[]`). Como cada rendimento pertence a uma entidade empregadora, eles foram unidos em uma classe; o limite de 3 é garantido em `Cliente.definirRendimentos()`.

2. **`Empresa` e `Banco` deixaram de ser subclasses e viraram o enum `TipoAgente`.**
   Não há atributos ou comportamentos distintos entre eles além de "bancos concedem crédito". Uma única tabela `agentes` com o campo `tipo` simplificou a persistência; a regra fica em `PedidoService.executarContrato()`.

3. **`status` passou de `String` para o enum `StatusPedido`** e as transições foram encapsuladas na entidade `PedidoAluguel` (`avaliar`, `cancelar`, `marcarComoContratado`), com o diagrama de estados documentando o ciclo.

4. **Inclusão do perfil (`Perfil`) e do hash da senha em `Usuario`** para atender ao requisito de login obrigatório com autorização por papel (JWT + Spring Security).

5. **Propriedade do automóvel modelada explicitamente** (`Automovel.tipoProprietario` + associação com `Usuario`), atendendo a "os automóveis podem ser registrados como propriedade dos clientes, empresas ou bancos". O registro ocorre na execução do contrato.

6. **`Contrato` passou a guardar o agente que o executou e a data de assinatura**; `ContratoCredito` ganhou taxa de juros e número de parcelas (cálculo da parcela pela Tabela Price).

7. **Pedido com período (`dataInicio`/`dataFim`) e solicitação de crédito** (`solicitaCredito`, `valorCreditoSolicitado`), necessários para a análise financeira.

8. **Diagrama de pacotes refinado** para refletir a estrutura real do código: `controller`, `service`, `repository`, `model`, `dto`, `security`, `config`, `exception` no back-end e `pages`, `components`, `context`, `services`, `utils` no front-end.
