package com.ufes.delivery.service;

import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.model.EstadoPedido;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PainelServiceTest {

    private static final LocalDate DATA_OPERACAO = LocalDate.of(2026, 6, 20);

    private PedidoRepositoryEmMemoria repositorio;
    private PainelService service;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        repositorio = new PedidoRepositoryEmMemoria();
        service = new PainelService(repositorio);

        cliente = new Cliente(null, "Fulano de Tal", "000.000.000-00");
        cliente.adicionarEndereco(new Endereco(null, "Rua Fulano", "123", "",
                "Centro", "Cidade Exemplo", "ES", "29000-000", true));
    }

    private Pedido novoPedido(EstadoPedido estado) {
        Pedido pedido = new Pedido(DATA_OPERACAO.atTime(9, 0), cliente);
        pedido.adicionarItem(new Item("Caderno", 1, 18.50, "Papelaria"));
        pedido.mudarEstado(estado, DATA_OPERACAO);
        return repositorio.salvar(pedido);
    }

    @Test
    void metricasCoerentesComAListaDePedidos() {
        novoPedido(EstadoPedido.NOVO);
        novoPedido(EstadoPedido.NOVO);
        novoPedido(EstadoPedido.AGUARDANDO_PAGAMENTO);
        novoPedido(EstadoPedido.EM_PREPARO);
        novoPedido(EstadoPedido.AGUARDANDO_ENTREGA);
        novoPedido(EstadoPedido.EM_TRANSITO);
        novoPedido(EstadoPedido.ENTREGUE);
        novoPedido(EstadoPedido.ENTREGUE);

        PainelViewModel painel = service.montarPainel(DATA_OPERACAO);

        assertEquals(8, painel.getPedidosDoDia());
        assertEquals(2, painel.contagem(EstadoPedido.NOVO));
        assertEquals(2, painel.contagem(EstadoPedido.ENTREGUE));
        assertEquals(1, painel.contagem(EstadoPedido.AGUARDANDO_PAGAMENTO));
        assertEquals(8, painel.getLinhas().size());
    }

    @Test
    void pedidoNaoEntregueFicaSemDataDeConclusao() {
        novoPedido(EstadoPedido.EM_TRANSITO);

        PainelViewModel painel = service.montarPainel(DATA_OPERACAO);

        assertNull(painel.getLinhas().get(0).dataConclusao());
    }

    @Test
    void pedidoEntregueRecebeDataDeConclusao() {
        novoPedido(EstadoPedido.ENTREGUE);

        PainelViewModel painel = service.montarPainel(DATA_OPERACAO);

        assertEquals(DATA_OPERACAO, painel.getLinhas().get(0).dataConclusao());
    }

    @Test
    void painelDeOutraDataNaoContaOsPedidos() {
        novoPedido(EstadoPedido.NOVO);

        PainelViewModel painel = service.montarPainel(DATA_OPERACAO.plusDays(1));

        assertEquals(0, painel.getPedidosDoDia());
        assertTrue(painel.getLinhas().isEmpty());
    }
}
