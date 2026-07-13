package com.ufes.delivery.service;

import com.ufes.delivery.service.pagamento.IGeradorAleatorio;

/**
 * Fonte de aleatoriedade deterministica para os testes (DoD da US11).
 */
class GeradorAleatorioDeterministico implements IGeradorAleatorio {

    private final double valorDouble;
    private final int valorInt;

    GeradorAleatorioDeterministico(double valorDouble, int valorInt) {
        this.valorDouble = valorDouble;
        this.valorInt = valorInt;
    }

    @Override
    public double proximoDouble() {
        return valorDouble;
    }

    @Override
    public int proximoInt(int limite) {
        return Math.min(valorInt, limite - 1);
    }
}
