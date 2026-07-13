package com.ufes.delivery.service;

import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.repository.IPedidoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fake em memoria para testes de pagamento e painel.
 */
class PedidoRepositoryEmMemoria implements IPedidoRepository {

    private final Map<Long, Pedido> dados = new HashMap<>();
    final List<Pagamento> pagamentos = new ArrayList<>();
    private long proximoId = 1;
    private long proximoCodigo = 1001;

    @Override
    public Pedido salvar(Pedido pedido) {
        pedido.setId(proximoId++);
        pedido.setCodigo(String.valueOf(proximoCodigo++));
        dados.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public void atualizar(Pedido pedido) {
        dados.put(pedido.getId(), pedido);
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Pedido> buscarPorCodigo(String codigo) {
        return dados.values().stream()
                .filter(p -> codigo.equals(p.getCodigo()))
                .findFirst();
    }

    @Override
    public List<Pedido> buscarPorDataDeOperacao(LocalDate dataOperacao) {
        return dados.values().stream()
                .filter(p -> p.getData().toLocalDate().equals(dataOperacao)
                        || dataOperacao.equals(p.getDataConclusao()))
                .toList();
    }

    @Override
    public void registrarPagamentoReprovado(Pagamento pagamento) {
        pagamentos.add(pagamento);
    }

    @Override
    public void confirmarPagamentoAprovado(Pedido pedido, Pagamento pagamento) {
        pagamentos.add(pagamento);
        atualizar(pedido);
    }
}
