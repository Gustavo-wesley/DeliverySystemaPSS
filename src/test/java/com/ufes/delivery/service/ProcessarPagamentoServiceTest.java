package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.model.EstadoPedido;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.service.pagamento.GeradorIdTransacao;
import com.ufes.delivery.service.pagamento.GeradorPrazoEntregaSimulado;
import com.ufes.delivery.service.pagamento.IGeradorAleatorio;
import com.ufes.delivery.service.pagamento.SimuladorPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessarPagamentoServiceTest {

    private static final LocalDateTime INSTANTE =
            LocalDateTime.of(2026, 6, 20, 10, 24);

    private PedidoRepositoryEmMemoria pedidoRepositorio;
    private ProdutoRepositoryEmMemoria produtoRepositorio;
    private Produto caderno;
    private Produto xadrez;
    private Pedido pedido;

    @BeforeEach
    void preparar() {
        pedidoRepositorio = new PedidoRepositoryEmMemoria();
        produtoRepositorio = new ProdutoRepositoryEmMemoria();

        caderno = produtoRepositorio.salvar(
                new Produto(null, 2001, "Caderno Universitário", "Papelaria", 18.50, 120));
        xadrez = produtoRepositorio.salvar(
                new Produto(null, 2003, "Jogo de Xadrez", "Lazer", 32.90, 18));

        Cliente cliente = new Cliente(null, "Fulano de Tal", "000.000.000-00");
        Endereco endereco = new Endereco(null, "Rua Fulano", "123", "Apto 101",
                "Sem Desconto", "Cidade Exemplo", "ES", "29000-000", true);
        cliente.adicionarEndereco(endereco);

        pedido = new Pedido(INSTANTE, cliente);
        pedido.definirEnderecoEntrega(endereco);
        pedido.adicionarItem(new Item(caderno, 2));
        pedido.adicionarItem(new Item(xadrez, 1));
        pedidoRepositorio.salvar(pedido);
    }

    private ProcessarPagamentoService servico(IGeradorAleatorio rng) {
        return new ProcessarPagamentoService(pedidoRepositorio, produtoRepositorio,
                new SimuladorPagamento(rng), new GeradorPrazoEntregaSimulado(rng),
                new GeradorIdTransacao(rng));
    }

    @Test
    void aprovadoBaixaEstoqueEMudaEstadoParaAguardandoEntrega() {
        // proximoDouble = 0.0 < 0.5 -> Aprovado; proximoInt = 2 -> PIX QR Code
        ProcessarPagamentoService service = servico(new GeradorAleatorioDeterministico(0.0, 2));

        Pagamento pagamento = service.processar(pedido, INSTANTE);

        assertTrue(pagamento.getResultado().isAprovado());
        assertEquals("PIX QR Code", pagamento.getForma().getNome());
        assertEquals(118, caderno.getEstoqueAtual());
        assertEquals(17, xadrez.getEstoqueAtual());
        assertEquals(EstadoPedido.AGUARDANDO_ENTREGA, pedido.getEstado());
        assertNotNull(pagamento.getIdTransacao());
        assertTrue(pagamento.getIdTransacao().startsWith("TXN-20260620-"));
        assertEquals(pedido.calcularValorTotal(), pagamento.getValorPago(), 0.001);
        assertEquals(1, pedidoRepositorio.pagamentos.size());
    }

    @Test
    void reprovadoPreservaPedidoEEstoque() {
        // proximoDouble = 0.9 >= 0.5 -> Reprovado
        ProcessarPagamentoService service = servico(new GeradorAleatorioDeterministico(0.9, 0));

        Pagamento pagamento = service.processar(pedido, INSTANTE);

        assertFalse(pagamento.getResultado().isAprovado());
        assertEquals(120, caderno.getEstoqueAtual());
        assertEquals(18, xadrez.getEstoqueAtual());
        assertEquals(EstadoPedido.NOVO, pedido.getEstado());
        assertNull(pagamento.getIdTransacao());
        assertNull(pagamento.getPrazoEntrega());
    }

    @Test
    void bloqueiaTentativaComEstoqueInsuficienteSemAlterarNada() {
        pedido.adicionarItem(new Item(xadrez, 100));
        ProcessarPagamentoService service = servico(new GeradorAleatorioDeterministico(0.0, 0));

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.processar(pedido, INSTANTE));

        assertTrue(erro.getMessage().contains("Jogo de Xadrez"));
        assertTrue(erro.getMessage().contains("18"));
        assertEquals(120, caderno.getEstoqueAtual());
        assertEquals(18, xadrez.getEstoqueAtual());
        assertEquals(EstadoPedido.NOVO, pedido.getEstado());
        assertTrue(pedidoRepositorio.pagamentos.isEmpty());
    }

    @Test
    void rejeitaPedidoSemItem() {
        Pedido vazio = new Pedido(INSTANTE, pedido.getCliente());
        vazio.definirEnderecoEntrega(pedido.getEnderecoEntrega());
        ProcessarPagamentoService service = servico(new GeradorAleatorioDeterministico(0.0, 0));

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.processar(vazio, INSTANTE));

        assertEquals("Pelo menos um item é obrigatório", erro.getMessage());
    }

    @Test
    void cadaFormaDePagamentoPodeSerSelecionadaPelaFonteDeTeste() {
        String[] esperadas = {"Open Finance", "PIX chave", "PIX QR Code", "Cartão de crédito"};

        for (int i = 0; i < esperadas.length; i++) {
            SimuladorPagamento simulador = new SimuladorPagamento(
                    new GeradorAleatorioDeterministico(0.0, i));
            assertEquals(esperadas[i], simulador.sortearForma().getNome());
        }
    }

    @Test
    void prazoDeEntregaFicaEntreAprovacaoEOMesmoDiaDoMesSeguinte() {
        GeradorPrazoEntregaSimulado inicio = new GeradorPrazoEntregaSimulado(
                new GeradorAleatorioDeterministico(0.0, 0));
        GeradorPrazoEntregaSimulado fim = new GeradorPrazoEntregaSimulado(
                new GeradorAleatorioDeterministico(0.999999, 0));

        LocalDateTime prazoMinimo = inicio.gerar(INSTANTE);
        LocalDateTime prazoMaximo = fim.gerar(INSTANTE);

        assertFalse(prazoMinimo.isBefore(INSTANTE.withSecond(0).withNano(0)));
        assertFalse(prazoMaximo.isAfter(LocalDateTime.of(2026, 7, 20, 10, 24)));
    }
}
