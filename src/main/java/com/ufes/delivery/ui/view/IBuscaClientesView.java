package com.ufes.delivery.ui.view;

import java.util.List;

/**
 * View passiva da busca de clientes (US05, Figura 4).
 */
public interface IBuscaClientesView {

    String CRITERIO_NOME = "Nome";
    String CRITERIO_CPF = "CPF";

    record LinhaCliente(Long id, String nome, String cpf) {
    }

    String getCriterioBusca();

    String getValorBusca();

    void mostrarResultados(List<LinhaCliente> linhas);

    /**
     * Id do cliente selecionado na tabela, ou null se nada selecionado.
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
}
