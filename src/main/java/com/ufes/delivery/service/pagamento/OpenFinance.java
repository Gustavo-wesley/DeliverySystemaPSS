package com.ufes.delivery.service.pagamento;

public class OpenFinance implements IFormaPagamento {

    @Override
    public String getNome() {
        return "Open Finance";
    }

    @Override
    public String toString() {
        return getNome();
    }
}
