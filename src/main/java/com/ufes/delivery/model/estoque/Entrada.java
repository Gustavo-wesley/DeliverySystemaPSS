package com.ufes.delivery.model.estoque;

import com.ufes.delivery.excecao.ValidacaoException;

/**
 * Entrada de estoque: quantidade positiva e nota fiscal obrigatoria.
 */
public class Entrada implements ITipoMovimentacao {

    public static final String NOME = "Entrada";
    public static final String MSG_NOTA_FISCAL_OBRIGATORIA =
            "O número da nota fiscal de entrada é obrigatório";

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public void validar(int quantidade, String motivo, String notaFiscal) {
        if (quantidade <= 0) {
            throw new ValidacaoException("Quantidade da entrada deve ser um inteiro maior que zero");
        }
        if (notaFiscal == null || notaFiscal.isBlank()) {
            throw new ValidacaoException(MSG_NOTA_FISCAL_OBRIGATORIA);
        }
    }

    @Override
    public int variacaoDeEstoque(int quantidade) {
        return quantidade;
    }

    @Override
    public String toString() {
        return NOME;
    }
}
