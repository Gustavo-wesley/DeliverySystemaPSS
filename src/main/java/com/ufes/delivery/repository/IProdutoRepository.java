package com.ufes.delivery.repository;

import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.estoque.MovimentacaoEstoque;
import java.util.List;
import java.util.Optional;

public interface IProdutoRepository {

    Produto salvar(Produto produto);

    void atualizar(Produto produto);

    Optional<Produto> buscarPorId(Long id);

    Optional<Produto> buscarPorCodigo(int codigo);

    List<Produto> buscarPorNome(String trecho);

    List<Produto> buscarPorCategoria(String categoria);

    List<Produto> buscarTodos();

    List<String> buscarCategorias();

    /**
     * Persiste a movimentacao e o novo estoque do produto
     * em transacao unica.
     */
    void registrarMovimentacao(MovimentacaoEstoque movimentacao);
}
