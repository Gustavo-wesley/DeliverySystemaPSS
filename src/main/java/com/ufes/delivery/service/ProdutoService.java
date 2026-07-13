package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.repository.IProdutoRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Busca e cadastro de produtos (US07). O cadastro e restrito
 * ao Administrador; a busca e liberada ao atendimento porque
 * apoia a tela de pedido.
 */
public class ProdutoService {

    public static final String MSG_CODIGO_EM_USO = "Código já está em uso";
    public static final String MSG_NOME_DUPLICADO_NA_CATEGORIA =
            "Já existe produto com esse nome na mesma categoria";

    private final IProdutoRepository produtoRepository;
    private final GuardaAcessoAdministrativo guarda;

    public ProdutoService(IProdutoRepository produtoRepository,
            GuardaAcessoAdministrativo guarda) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository,
                "Repositório de produtos deve ser informado");
        this.guarda = Objects.requireNonNull(guarda, "Guarda de acesso deve ser informado");
    }

    public Optional<Produto> buscarPorCodigo(String valor) {
        String informado = exigirValor(valor);

        int codigo;
        try {
            codigo = Integer.parseInt(informado);
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Código deve ser um número inteiro positivo");
        }

        if (codigo <= 0) {
            throw new ValidacaoException("Código deve ser um número inteiro positivo");
        }

        return produtoRepository.buscarPorCodigo(codigo);
    }

    public List<Produto> buscarPorNome(String valor) {
        return produtoRepository.buscarPorNome(exigirValor(valor));
    }

    public List<Produto> buscarPorCategoria(String valor) {
        return produtoRepository.buscarPorCategoria(exigirValor(valor));
    }

    public List<String> categoriasDisponiveis() {
        return produtoRepository.buscarCategorias();
    }

    public Produto salvar(Produto produto) {
        guarda.exigirAdministrador();
        Objects.requireNonNull(produto, "Produto deve ser informado");

        exigirCodigoUnico(produto);
        exigirNomeUnicoNaCategoria(produto);

        if (produto.getId() == null) {
            return produtoRepository.salvar(produto);
        }

        produtoRepository.atualizar(produto);
        return produto;
    }

    private void exigirCodigoUnico(Produto produto) {
        Optional<Produto> existente = produtoRepository.buscarPorCodigo(produto.getCodigo());
        if (existente.isPresent() && !existente.get().getId().equals(produto.getId())) {
            throw new ValidacaoException(MSG_CODIGO_EM_USO);
        }
    }

    private void exigirNomeUnicoNaCategoria(Produto produto) {
        boolean duplicado = produtoRepository.buscarPorCategoria(produto.getCategoria()).stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase(produto.getNome())
                        && !p.getId().equals(produto.getId()));

        if (duplicado) {
            throw new ValidacaoException(MSG_NOME_DUPLICADO_NA_CATEGORIA);
        }
    }

    private String exigirValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacaoException("O valor da busca é obrigatório");
        }
        return valor.trim();
    }
}
