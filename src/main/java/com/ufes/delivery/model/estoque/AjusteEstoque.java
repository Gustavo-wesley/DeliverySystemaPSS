package com.ufes.delivery.model.estoque;

import com.ufes.delivery.excecao.ValidacaoException;

/**
 * Ajuste de estoque: quantidade com sinal (positiva ou negativa,
 * diferente de zero) e motivo obrigatorio.
 */
public class AjusteEstoque implements ITipoMovimentacao {

    public static final String NOME = "Ajuste de estoque";
    public static final String MSG_MOTIVO_OBRIGATORIO =
            "O motivo do ajuste é obrigatório";

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public void validar(int quantidade, String motivo, String notaFiscal) {
        if (quantidade == 0) {
            throw new ValidacaoException("Quantidade do ajuste deve ser um inteiro diferente de zero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ValidacaoException(MSG_MOTIVO_OBRIGATORIO);
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
