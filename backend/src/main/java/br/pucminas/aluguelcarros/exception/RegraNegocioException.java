package br.pucminas.aluguelcarros.exception;

/** Violação de regra de negócio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
