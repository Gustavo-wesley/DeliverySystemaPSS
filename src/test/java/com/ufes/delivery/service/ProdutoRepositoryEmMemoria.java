package com.ufes.delivery.service;

import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.estoque.MovimentacaoEstoque;
import com.ufes.delivery.repository.IProdutoRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Fake em memoria para testes dos services de produto e estoque.
 */
class ProdutoRepositoryEmMemoria implements IProdutoRepository {

    private final Map<Long, Produto> dados = new HashMap<>();
    final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
    private long proximoId = 1;

    @Override
    public Produto salvar(Produto produto) {
        produto.setId(proximoId++);
        dados.put(produto.getId(), produto);
        return produto;
    }

    @Override
    public void atualizar(Produto produto) {
        dados.put(produto.getId(), produto);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Produto> buscarPorCodigo(int codigo) {
        return dados.values().stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();
    }

    @Override
    public List<Produto> buscarPorNome(String trecho) {
        String filtro = trecho.toLowerCase(Locale.ROOT);
        return dados.values().stream()
                .filter(p -> p.getNome().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }

    @Override
    public List<Produto> buscarPorCategoria(String categoria) {
        return dados.values().stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                .toList();
    }

    @Override
    public List<Produto> buscarTodos() {
        return new ArrayList<>(dados.values());
    }

    @Override
    public List<String> buscarCategorias() {
        return dados.values().stream()
                .map(Produto::getCategoria)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public void registrarMovimentacao(MovimentacaoEstoque movimentacao) {
        movimentacoes.add(movimentacao);
        atualizar(movimentacao.getProduto());
    }
}
