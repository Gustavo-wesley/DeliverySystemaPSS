package com.ufes.delivery.service.pagamento;

/**
 * Forma de pagamento simulada como Strategy (US11). Uma nova forma
 * entra como nova classe registrada no sorteador, sem switch.
 */
public interface IFormaPagamento {

    String getNome();
}
