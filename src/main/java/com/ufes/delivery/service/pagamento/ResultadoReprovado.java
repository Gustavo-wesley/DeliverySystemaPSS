package com.ufes.delivery.service.pagamento;

public class ResultadoReprovado implements IResultadoPagamento {

    public static final String NOME = "Reprovado";

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public boolean isAprovado() {
        return false;
    }

    @Override
    public String toString() {
        return NOME;
    }
}
