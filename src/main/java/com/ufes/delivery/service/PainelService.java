package com.ufes.delivery.service;

import com.ufes.delivery.model.EstadoPedido;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.repository.IPedidoRepository;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Monta o PainelViewModel da data de operacao (US04). As metricas
 * refletem exatamente a lista de pedidos retornada pelo repositorio.
 */
public class PainelService {

    private final IPedidoRepository pedidoRepository;

    public PainelService(IPedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository,
                "Repositório de pedidos deve ser informado");
    }

    public PainelViewModel montarPainel(LocalDate dataOperacao) {
        Objects.requireNonNull(dataOperacao, "Data de operação deve ser informada");

        List<Pedido> pedidos = pedidoRepository.buscarPorDataDeOperacao(dataOperacao);

        Map<EstadoPedido, Long> contagem = new EnumMap<>(EstadoPedido.class);
        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() == EstadoPedido.ENTREGUE
                    && !dataOperacao.equals(pedido.getDataConclusao())) {
                // entregue em outra data nao conta na metrica "Entregues hoje"
                continue;
            }
            contagem.merge(pedido.getEstado(), 1L, Long::sum);
        }

        List<PainelViewModel.LinhaPedido> linhas = pedidos.stream()
                .map(p -> new PainelViewModel.LinhaPedido(
                        p.getCodigo(),
                        p.getCliente().getNome(),
                        p.getData().toLocalDate(),
                        p.getDataConclusao(),
                        p.getEstado().getDescricao(),
                        p.calcularValorTotal()))
                .toList();

        return new PainelViewModel(dataOperacao, pedidos.size(), contagem, linhas);
    }
}
