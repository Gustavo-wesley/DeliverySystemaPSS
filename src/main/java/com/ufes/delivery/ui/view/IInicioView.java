package com.ufes.delivery.ui.view;

import java.util.List;
import java.util.function.Consumer;

/**
 * View passiva do painel operacional (US04, Figura 3): menu Operacao,
 * data de operacao, metricas por estado, lista de pedidos e barra de status.
 */
public interface IInicioView {

    /**
     * Linha da lista de pedidos, com os valores ja formatados pelo Presenter.
     */
    record LinhaPedidoPainel(
            String codigo,
            String nomeCliente,
            String dataPedido,
            String dataConclusao,
            String estado,
            String valorTotal) {
    }

    void setDataOperacao(String texto);

    void setMetricaPedidosDoDia(String valor);

    void setMetricaNovos(String valor);

    void setMetricaAguardandoPagamento(String valor);

    void setMetricaEmPreparo(String valor);

    void setMetricaAguardandoEntrega(String valor);

    void setMetricaEmTransito(String valor);

    void setMetricaEntreguesHoje(String valor);

    void mostrarPedidos(List<LinhaPedidoPainel> linhas);

    void setUsuarioLogado(String texto);

    void setInformacaoLogin(String texto);

    void setTipoPerfil(String texto);

    void setMenuAdministracaoVisivel(boolean visivel);

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoNovoPedido(Runnable acao);

    void setAoBuscarProdutos(Runnable acao);

    void setAoNovoProduto(Runnable acao);

    void setAoMovimentacaoEstoque(Runnable acao);

    void setAoNovoCliente(Runnable acao);

    void setAoBuscarClientes(Runnable acao);

    void setAoGestaoUsuarios(Runnable acao);

    /**
     * Disparado ao clicar em Visualizar numa linha; recebe o codigo do pedido.
     */
    void setAoVisualizarPedido(Consumer<String> acao);

    /**
     * Disparado quando a janela volta ao foco, para o painel se atualizar.
     */
    void setAoAtualizar(Runnable acao);
}
