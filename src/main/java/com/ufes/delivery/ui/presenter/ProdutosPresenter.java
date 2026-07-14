package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.ui.view.IProdutosView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Presenter da busca de produtos (US07). O criterio selecionado
 * determina a validacao do campo Valor (feita no service).
 */
public class ProdutosPresenter {

    private final IProdutosView view;
    private final ProdutoService produtoService;
    private final INavegador navegador;
    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    public ProdutosPresenter(IProdutosView view, ProdutoService produtoService,
            INavegador navegador) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.produtoService = Objects.requireNonNull(produtoService,
                "Serviço de produtos deve ser informado");
        this.navegador = Objects.requireNonNull(navegador, "Navegador deve ser informado");

        view.setAoBuscar(this::buscar);
        view.setAoNovo(() -> navegador.abrirCadastroProduto(null));
        view.setAoVisualizar(this::visualizarSelecionado);
        view.setAoVisualizarLinha(navegador::abrirCadastroProduto);
        view.setAoFechar(view::fechar);
    }

    public void iniciar() {
        view.abrir();
    }

    private void buscar() {
        try {
            List<Produto> encontrados = new ArrayList<>();

            switch (view.getCriterioBusca()) {
                case IProdutosView.CRITERIO_CODIGO ->
                    produtoService.buscarPorCodigo(view.getValorBusca())
                            .ifPresent(encontrados::add);
                case IProdutosView.CRITERIO_CATEGORIA ->
                    encontrados.addAll(produtoService.buscarPorCategoria(view.getValorBusca()));
                default ->
                    encontrados.addAll(produtoService.buscarPorNome(view.getValorBusca()));
            }

            if (encontrados.isEmpty()) {
                view.mostrarInformacao("Nenhum produto encontrado para a busca informada");
            }
            mostrar(encontrados);
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void visualizarSelecionado() {
        Long id = view.getIdSelecionado();
        if (id == null) {
            view.mostrarErro("Selecione um produto na lista de resultados");
            return;
        }
        navegador.abrirCadastroProduto(id);
    }

    private void mostrar(List<Produto> produtos) {
        List<IProdutosView.LinhaProduto> linhas = produtos.stream()
                .map(p -> new IProdutosView.LinhaProduto(
                        p.getId(),
                        String.valueOf(p.getCodigo()),
                        p.getNome(),
                        p.getCategoria(),
                        formatoMoeda.format(p.getPrecoUnitario()),
                        String.valueOf(p.getEstoqueAtual())))
                .toList();
        view.mostrarResultados(linhas);
    }
}
