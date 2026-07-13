package com.ufes.delivery.service.pagamento;

public class PixChave implements IFormaPagamento {

    @Override
    public String getNome() {
        return "PIX chave";
    }

    @Override
    public String toString() {
        return getNome();
    }
}
