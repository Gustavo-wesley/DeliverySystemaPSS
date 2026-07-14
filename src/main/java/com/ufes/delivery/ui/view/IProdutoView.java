package com.ufes.delivery.ui.view;

import java.util.List;

/**
 * View passiva do cadastro de produto (US07, Figura 8).
 */
public interface IProdutoView {

    String getCodigo();

    String getNome();

    String getCategoria();

    String getPrecoUnitario();

    String getQuantidadeInicial();

    void setCodigo(String codigo);

    void setNome(String nome);

    void setCategoria(String categoria);

    void setPrecoUnitario(String preco);

    void setQuantidadeInicial(String quantidade);

    void setCategoriasDisponiveis(List<String> categorias);

    /**
     * Em edicao o codigo e a quantidade ficam travados: o codigo e
     * imutavel e o estoque so muda por movimentacao (US08).
     */
    void setModoEdicao(boolean edicao);

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoSalvar(Runnable acao);

    void setAoCancelar(Runnable acao);
}
