package com.ufes.delivery.ui.view;

import java.util.List;
import java.util.function.Consumer;

/**
 * View passiva da busca de produtos (US07, Figura 7): criterio
 * Codigo, Nome ou Categoria, resultados com acao Visualizar por linha.
 */
public interface IProdutosView {

    String CRITERIO_CODIGO = "Código";
    String CRITERIO_NOME = "Nome";
    String CRITERIO_CATEGORIA = "Categoria";

    record LinhaProduto(
            Long id,
            String codigo,
            String nome,
            String categoria,
            String precoUnitario,
            String estoqueAtual) {
    }

    String getCriterioBusca();

    String getValorBusca();

    void mostrarResultados(List<LinhaProduto> linhas);

    /**
     * Id do produto selecionado na tabela, ou null.
     */
    Long getIdSelecionado();

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoBuscar(Runnable acao);

    void setAoNovo(Runnable acao);

    void setAoVisualizar(Runnable acao);

    void setAoFechar(Runnable acao);

    /**
     * Acao Visualizar da propria linha (recebe o id do produto da linha).
     */
    void setAoVisualizarLinha(Consumer<Long> acao);
}
