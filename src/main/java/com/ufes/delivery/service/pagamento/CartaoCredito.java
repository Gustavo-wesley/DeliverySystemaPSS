package com.ufes.delivery.service.pagamento;

public class CartaoCredito implements IFormaPagamento {

    @Override
    public String getNome() {
        return "Cartão de crédito";
    }

    @Override
    public String toString() {
        return getNome();
    }
}
