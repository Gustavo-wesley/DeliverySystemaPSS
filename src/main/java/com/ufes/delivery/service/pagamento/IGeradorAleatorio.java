package com.ufes.delivery.service.pagamento;

/**
 * Fonte de aleatoriedade injetavel: os testes substituem por
 * implementacao deterministica para verificar aprovacao, reprovacao
 * e cada forma de pagamento (DoD da US11).
 */
public interface IGeradorAleatorio {

    /**
     * Valor uniforme em [0, 1).
     */
    double proximoDouble();

    /**
     * Inteiro uniforme em [0, limite).
     */
    int proximoInt(int limite);
}
