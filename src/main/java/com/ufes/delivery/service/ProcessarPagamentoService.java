package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.EstadoPedido;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.repository.IPedidoRepository;
import com.ufes.delivery.repository.IProdutoRepository;
import com.ufes.delivery.service.pagamento.GeradorIdTransacao;
import com.ufes.delivery.service.pagamento.IFormaPagamento;
import com.ufes.delivery.service.pagamento.IGeradorPrazoEntrega;
import com.ufes.delivery.service.pagamento.IResultadoPagamento;
import com.ufes.delivery.service.pagamento.SimuladorPagamento;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Processa a tentativa de pagamento simulada (US10/US11), tudo-ou-nada:
 * 1. valida o pedido (cliente, endereco, itens);
 * 2. consulta a disponibilidade de cada item NO INSTANTE da confirmacao;
 * 3. sorteia resultado e forma pela fonte de aleatoriedade injetada;
 * 4. aprovado: baixa estoque + estado Aguardando entrega + pagamento,
 *    em transacao unica no repositorio;
 * 5. reprovado: registra a tentativa e preserva o pedido intacto.
 */
public class ProcessarPagamentoService {

    private final IPedidoRepository pedidoRepository;
    private final IProdutoRepository produtoRepository;
    private final SimuladorPagamento simulador;
    private final IGeradorPrazoEntrega geradorPrazoEntrega;
    private final GeradorIdTransacao geradorIdTransacao;

    public ProcessarPagamentoService(IPedidoRepository pedidoRepository,
            IProdutoRepository produtoRepository, SimuladorPagamento simulador,
            IGeradorPrazoEntrega geradorPrazoEntrega, GeradorIdTransacao geradorIdTransacao) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository,
                "Repositório de pedidos deve ser informado");
        this.produtoRepository = Objects.requireNonNull(produtoRepository,
                "Repositório de produtos deve ser informado");
        this.simulador = Objects.requireNonNull(simulador,
                "Simulador de pagamento deve ser informado");
        this.geradorPrazoEntrega = Objects.requireNonNull(geradorPrazoEntrega,
                "Gerador de prazo de entrega deve ser informado");
        this.geradorIdTransacao = Objects.requireNonNull(geradorIdTransacao,
                "Gerador de id de transação deve ser informado");
    }

    public Pagamento processar(Pedido pedido, LocalDateTime instante) {
        Objects.requireNonNull(pedido, "Pedido deve ser informado");
        Objects.requireNonNull(instante, "Instante da tentativa deve ser informado");

        try {
            pedido.validarParaPagamento();
        } catch (IllegalStateException e) {
            throw new ValidacaoException(e.getMessage());
        }

        // disponibilidade consultada no instante da confirmacao,
        // mesmo que os itens tenham sido validados antes
        validarDisponibilidade(pedido);

        IResultadoPagamento resultado = simulador.sortearResultado();
        IFormaPagamento forma = simulador.sortearForma();
        double valorTotal = pedido.calcularValorTotal();

        if (!resultado.isAprovado()) {
            Pagamento reprovado = new Pagamento(null, pedido, resultado, forma,
                    null, valorTotal, instante, null);
            pedidoRepository.registrarPagamentoReprovado(reprovado);
            return reprovado;
        }

        String idTransacao = geradorIdTransacao.gerar(instante);
        LocalDateTime prazoEntrega = geradorPrazoEntrega.gerar(instante);

        Pagamento aprovado = new Pagamento(null, pedido, resultado, forma,
                idTransacao, valorTotal, instante, prazoEntrega);

        // baixa de estoque + mudanca de estado + pagamento em transacao unica
        for (Item item : pedido.getItens()) {
            item.getProduto().aplicarVariacaoEstoque(-item.getQuantidade());
        }
        pedido.mudarEstado(EstadoPedido.AGUARDANDO_ENTREGA, null);

        pedidoRepository.confirmarPagamentoAprovado(pedido, aprovado);
        return aprovado;
    }

    private void validarDisponibilidade(Pedido pedido) {
        for (Item item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (produto == null) {
                throw new ValidacaoException(
                        "Item sem produto vinculado: " + item.getNome());
            }

            int estoqueAtual = produtoRepository.buscarPorCodigo(produto.getCodigo())
                    .map(Produto::getEstoqueAtual)
                    .orElseThrow(() -> new ValidacaoException(
                            "Produto não encontrado: " + item.getNome()));

            if (estoqueAtual < item.getQuantidade()) {
                throw new ValidacaoException("Estoque insuficiente para o item "
                        + item.getNome() + ". Quantidade disponível: " + estoqueAtual);
            }
        }
    }
}
