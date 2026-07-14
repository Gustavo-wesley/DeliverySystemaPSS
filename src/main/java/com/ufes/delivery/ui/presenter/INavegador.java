package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.model.Pagamento;

/**
 * Navegacao entre telas, para o Presenter abrir outra tela sem
 * conhecer Swing. Quem implementa e a composicao da aplicacao.
 */
public interface INavegador {

    void abrirTelaInicio();

    void abrirCadastroUsuario();

    void abrirGestaoUsuarios();

    void abrirBuscaClientes();

    /**
     * Abre o cadastro de cliente; id nulo significa cliente novo.
     */
    void abrirCadastroCliente(Long clienteId);

    void abrirBuscaProdutos();

    /**
     * Abre o cadastro de produto; id nulo significa produto novo.
     */
    void abrirCadastroProduto(Long produtoId);

    void abrirMovimentacaoEstoque();

    void abrirNovoPedido();

    /**
     * Abre a tela de pedido com os dados do pedido informado
     * (acao Visualizar do painel, US04).
     */
    void abrirPedido(String codigoPedido);

    /**
     * Exibe a tela somente leitura do resultado da tentativa (US11).
     */
    void mostrarResultadoPagamento(Pagamento pagamento);
}
