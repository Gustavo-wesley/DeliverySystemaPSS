package com.ufes.delivery.ui.view;

import java.util.List;

/**
 * View passiva da movimentacao de estoque (US08, Figura 9): busca e
 * selecao de produto, campos de leitura do produto selecionado,
 * dados da movimentacao e previa do estoque resultante.
 */
public interface IMovimentacaoEstoqueView {

    record LinhaProdutoBusca(
            Long id,
            String codigo,
            String nome,
            String categoria,
            String estoqueAtual) {
    }

    String getValorBuscaProduto();

    void mostrarResultadosBusca(List<LinhaProdutoBusca> linhas);

    Long getIdProdutoSelecionadoNaBusca();

    void setProdutoSelecionado(String nome, String estoqueAtual);

    String getDataMovimentacao();

    void setDataMovimentacao(String data);

    String getTipoMovimentacao();

    void setTiposDisponiveis(List<String> tipos);

    String getQuantidade();

    String getMotivo();

    String getNotaFiscal();

    void limparCamposMovimentacao();

    /**
     * Previa do estoque apos a movimentacao, somente exibicao.
     */
    void setPrevia(String valor);

    void setMotivoHabilitado(boolean habilitado);

    void setNotaFiscalHabilitada(boolean habilitada);

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoBuscar(Runnable acao);

    void setAoSelecionarProduto(Runnable acao);

    /**
     * Disparado quando tipo ou quantidade mudam, para atualizar a previa.
     */
    void setAoDadosAlterados(Runnable acao);

    void setAoConfirmar(Runnable acao);

    void setAoCancelar(Runnable acao);
}
