package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.service.GuardaAcessoAdministrativo;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.ui.view.IProdutoView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Presenter do cadastro de produto (US07): parse dos campos no
 * padrao brasileiro na borda da UI e regras no service/dominio.
 */
public class ProdutoPresenter {

    // categorias base do catalogo, unidas as ja persistidas
    private static final List<String> CATEGORIAS_BASE = List.of(
            "Alimentação", "Educação", "Entretenimento", "Lazer", "Papelaria");

    private final IProdutoView view;
    private final ProdutoService produtoService;
    private final GuardaAcessoAdministrativo guarda;
    private final Produto produtoEmEdicao; // null quando e cadastro novo

    public ProdutoPresenter(IProdutoView view, ProdutoService produtoService,
            GuardaAcessoAdministrativo guarda, Produto produtoEmEdicao) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.produtoService = Objects.requireNonNull(produtoService,
                "Serviço de produtos deve ser informado");
        this.guarda = Objects.requireNonNull(guarda, "Guarda de acesso deve ser informado");
        this.produtoEmEdicao = produtoEmEdicao;

        view.setAoSalvar(this::salvar);
        view.setAoCancelar(view::fechar);
    }

    public void iniciar() {
        TreeSet<String> categorias = new TreeSet<>(CATEGORIAS_BASE);
        categorias.addAll(produtoService.categoriasDisponiveis());
        view.setCategoriasDisponiveis(new ArrayList<>(categorias));

        if (produtoEmEdicao != null) {
            view.setCodigo(String.valueOf(produtoEmEdicao.getCodigo()));
            view.setNome(produtoEmEdicao.getNome());
            view.setCategoria(produtoEmEdicao.getCategoria());
            view.setPrecoUnitario(String.format("%.2f", produtoEmEdicao.getPrecoUnitario())
                    .replace('.', ','));
            view.setQuantidadeInicial(String.valueOf(produtoEmEdicao.getEstoqueAtual()));
            view.setModoEdicao(true);
        }
        view.abrir();
    }

    private void salvar() {
        try {
            guarda.exigirAdministrador();

            int codigo = parseInteiro(view.getCodigo(),
                    "Código deve ser um número inteiro positivo");
            double preco = parsePreco(view.getPrecoUnitario());

            int estoque;
            Long id;
            if (produtoEmEdicao == null) {
                estoque = parseInteiro(view.getQuantidadeInicial(),
                        "Quantidade inicial em estoque deve ser um número inteiro");
                id = null;
            } else {
                // estoque so muda por movimentacao (US08)
                estoque = produtoEmEdicao.getEstoqueAtual();
                id = produtoEmEdicao.getId();
            }

            Produto produto = new Produto(id, codigo, view.getNome(),
                    view.getCategoria(), preco, estoque);
            produtoService.salvar(produto);

            view.mostrarInformacao("Produto salvo com sucesso");
            view.fechar();
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private int parseInteiro(String valor, String mensagem) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException(mensagem);
        }
    }

    private double parsePreco(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacaoException("Preço unitário é obrigatório");
        }
        try {
            // aceita virgula decimal do padrao brasileiro
            return Double.parseDouble(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Preço unitário inválido");
        }
    }
}
