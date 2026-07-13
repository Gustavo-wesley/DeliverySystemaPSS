package com.ufes.delivery.model.estoque;

/**
 * Tipo de movimentacao de estoque como Strategy (US08).
 * "Saida" nao existe neste dominio: a baixa por venda ocorre
 * exclusivamente na aprovacao do pagamento (US10/US11).
 */
public interface ITipoMovimentacao {

    String getNome();

    /**
     * Valida os dados condicionais do tipo (motivo ou nota fiscal)
     * e a quantidade informada.
     */
    void validar(int quantidade, String motivo, String notaFiscal);

    /**
     * Variacao (com sinal) aplicada ao estoque atual.
     */
    int variacaoDeEstoque(int quantidade);
}
