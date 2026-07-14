package com.ufes.delivery.ui.view;

/**
 * View passiva do resultado simulado do pagamento (US11, Figura 12).
 * Somente leitura; o unico comando e Fechar. Os destaques visuais
 * distinguem resultado, valor efetivado e situacao operacional.
 */
public interface IPagamentoView {

    /**
     * Faixa principal com o resultado ("Pagamento aprovado/reprovado").
     */
    void setResultado(String texto, boolean aprovado);

    /**
     * Faixa da situacao operacional ("Pedido pronto para entrega").
     */
    void setSituacaoOperacional(String texto, boolean destaque);

    // ---- Resumo do Pedido ----
    void setCodigoPedido(String texto);

    void setNomeCliente(String texto);

    void setEnderecoEntrega(String texto);

    void setTotalPedido(String texto);

    // ---- Informacoes do Pagamento ----
    void setSituacaoPagamento(String texto, boolean aprovado);

    void setForma(String texto);

    void setDataHoraPagamento(String texto);

    void setIdTransacao(String texto);

    void setValorPago(String texto);

    // ---- Entrega ----
    void setSituacaoPedido(String texto, boolean destaque);

    void setPrazoEntrega(String texto);

    void setObservacao(String texto);

    void abrir();

    void fechar();

    void setAoFechar(Runnable acao);
}
