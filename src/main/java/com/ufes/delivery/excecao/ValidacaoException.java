package com.ufes.delivery.excecao;

/**
 * Falha de validacao de regra de negocio ou de dado de entrada.
 * A mensagem e exibida ao usuario em portugues, associada ao campo
 * ou operacao que originou a inconsistencia.
 */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
