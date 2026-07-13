package com.ufes.delivery.service;

import com.ufes.delivery.model.EstadoPedido;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ViewModel do painel operacional (US04): dados prontos para a View,
 * sem regra de negocio na tela.
 */
public class PainelViewModel {

    /**
     * Linha da lista de pedidos, ja formatavel pela View.
     */
    public record LinhaPedido(
            String codigo,
            String nomeCliente,
            LocalDate dataPedido,
            LocalDate dataConclusao,
            String estado,
            double valorTotal) {
    }

    private final LocalDate dataOperacao;
    private final int pedidosDoDia;
    private final Map<EstadoPedido, Long> contagemPorEstado;
    private final List<LinhaPedido> linhas;

    public PainelViewModel(LocalDate dataOperacao, int pedidosDoDia,
            Map<EstadoPedido, Long> contagemPorEstado, List<LinhaPedido> linhas) {
        this.dataOperacao = dataOperacao;
        this.pedidosDoDia = pedidosDoDia;
        this.contagemPorEstado = Collections.unmodifiableMap(contagemPorEstado);
        this.linhas = Collections.unmodifiableList(linhas);
    }

    public LocalDate getDataOperacao() {
        return dataOperacao;
    }

    public int getPedidosDoDia() {
        return pedidosDoDia;
    }

    public long contagem(EstadoPedido estado) {
        return contagemPorEstado.getOrDefault(estado, 0L);
    }

    public List<LinhaPedido> getLinhas() {
        return linhas;
    }
}
