package com.ufes.delivery.ui.view;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * View passiva da tela de pedido (US09/US10, Figuras 10 e 11):
 * cliente, endereco de entrega, itens com quantidade editavel,
 * exclusao por menu de contexto, cupom e resumo financeiro de leitura.
 */
public interface IPedidoView {

    record OpcaoCliente(Long id, String nome) {
    }

    record LinhaItem(
            String categoria,
            String nome,
            String precoUnitario,
            String quantidade,
            String precoTotal) {
    }

    void setClientesDisponiveis(List<OpcaoCliente> clientes);

    Long getIdClienteSelecionado();

    void setEnderecosDisponiveis(List<String> enderecos);

    int getIndiceEnderecoSelecionado();

    void setIndiceEnderecoSelecionado(int indice);

    void mostrarItens(List<LinhaItem> linhas);

    String getProdutoParaAdicionar();

    String getQuantidadeParaAdicionar();

    void limparCamposDeItem();

    String getCupom();

    void setTotalDescontos(String valor);

    void setDescontoTaxaEntrega(String valor);

    void setTaxaEntregaFinal(String valor);

    void setTotalPedido(String valor);

    boolean confirmarCancelamento();

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoSelecionarCliente(Runnable acao);

    void setAoNovoCliente(Runnable acao);

    void setAoSelecionarEndereco(Runnable acao);

    void setAoAdicionarItem(Runnable acao);

    /**
     * Acao Excluir do menu de contexto; recebe o indice da linha.
     */
    void setAoExcluirItem(IntConsumer acao);

    /**
     * Quantidade editada na tabela: (indice da linha, valor digitado).
     */
    void setAoAlterarQuantidade(BiConsumer<Integer, String> acao);

    void setAoAplicarCupom(Runnable acao);

    void setAoPagar(Runnable acao);

    void setAoCancelar(Runnable acao);
}
