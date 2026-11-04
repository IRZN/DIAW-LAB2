package br.pucminas.aluguelcarros.model.enums;

/**
 * Ciclo de vida de um pedido de aluguel.
 * PENDENTE -> (avaliação financeira do agente) -> APROVADO | REPROVADO
 * APROVADO -> (execução do contrato) -> CONTRATADO
 * PENDENTE | APROVADO -> (cliente) -> CANCELADO
 */
public enum StatusPedido {
    PENDENTE,
    APROVADO,
    REPROVADO,
    CANCELADO,
    CONTRATADO
}
