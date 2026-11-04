package br.pucminas.aluguelcarros.exception;

/** Login ou senha incorretos (HTTP 401). */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Usuário ou senha inválidos.");
    }
}
