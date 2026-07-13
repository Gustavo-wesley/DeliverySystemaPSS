package com.ufes.delivery.service.pagamento;

public class ResultadoAprovado implements IResultadoPagamento {

    public static final String NOME = "Aprovado";

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public boolean isAprovado() {
        return true;
    }

    @Override
    public String toString() {
        return NOME;
    }
}
