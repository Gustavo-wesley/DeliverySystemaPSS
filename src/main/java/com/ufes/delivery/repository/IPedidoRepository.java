package com.ufes.delivery.repository;

import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Pedido;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IPedidoRepository {

    /**
     * Persiste o pedido e seus itens em transacao unica,
     * atribuindo o codigo sequencial.
     */
    Pedido salvar(Pedido pedido);

    void atualizar(Pedido pedido);

    Optional<Pedido> buscarPorId(Long id);

    Optional<Pedido> buscarPorCodigo(String codigo);

    /**
     * Pedidos cuja data (ou data de conclusao, para entregues)
     * corresponde a data de operacao do painel (US04).
     */
    List<Pedido> buscarPorDataDeOperacao(LocalDate dataOperacao);

    /**
     * Registra uma tentativa de pagamento reprovada, sem alterar
     * pedido ou estoque.
     */
    void registrarPagamentoReprovado(Pagamento pagamento);

    /**
     * Confirma o pagamento aprovado em transacao unica: baixa o
     * estoque de todos os itens, muda o estado do pedido para
     * Aguardando entrega e grava o pagamento. Tudo-ou-nada.
     */
    void confirmarPagamentoAprovado(Pedido pedido, Pagamento pagamento);
}
