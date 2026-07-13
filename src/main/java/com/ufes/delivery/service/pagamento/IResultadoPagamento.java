package com.ufes.delivery.service.pagamento;

/**
 * Resultado simulado da tentativa de pagamento como Strategy
 * (US10/US11): Aprovado ou Reprovado.
 */
public interface IResultadoPagamento {

    String getNome();

    boolean isAprovado();
}
