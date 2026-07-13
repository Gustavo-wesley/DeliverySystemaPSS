package com.ufes.delivery.service.pagamento;

import java.util.Random;

public class GeradorAleatorioPadrao implements IGeradorAleatorio {

    private final Random random = new Random();

    @Override
    public double proximoDouble() {
        return random.nextDouble();
    }

    @Override
    public int proximoInt(int limite) {
        return random.nextInt(limite);
    }
}
