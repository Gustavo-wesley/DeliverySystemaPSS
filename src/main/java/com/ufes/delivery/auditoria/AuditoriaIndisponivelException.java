package com.ufes.delivery.auditoria;

/**
 * Indisponibilidade do mecanismo de auditoria, comunicada sem
 * exposicao de dados sensiveis.
 */
public class AuditoriaIndisponivelException extends RuntimeException {

    public AuditoriaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
