package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.ui.view.IPagamentoView;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Presenter do resultado simulado do pagamento (US11, Figura 12).
 * Tela somente leitura: apenas formata os dados da tentativa,
 * derivados do pedido e da simulacao.
 */
public class PagamentoPresenter {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IPagamentoView view;
    private final Pagamento pagamento;
    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    public PagamentoPresenter(IPagamentoView view, Pagamento pagamento) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.pagamento = Objects.requireNonNull(pagamento, "Pagamento deve ser informado");

        view.setAoFechar(view::fechar);
    }

    public void iniciar() {
        boolean aprovado = pagamento.getResultado().isAprovado();

        view.setResultado(aprovado ? "Pagamento aprovado" : "Pagamento reprovado", aprovado);
        view.setSituacaoOperacional(aprovado
                ? "Pedido pronto para entrega"
                : "Pedido preservado para nova tentativa", aprovado);

        // ---- Resumo do Pedido ----
        view.setCodigoPedido(pagamento.getPedido().getCodigo());
        view.setNomeCliente(pagamento.getPedido().getCliente().getNome());
        Endereco endereco = pagamento.getPedido().getEnderecoEntrega();
        view.setEnderecoEntrega(endereco == null ? "-" : endereco.descricaoCompleta());
        view.setTotalPedido(formatoMoeda.format(pagamento.getPedido().calcularValorTotal()));

        // ---- Informacoes do Pagamento ----
        view.setSituacaoPagamento(pagamento.getResultado().getNome(), aprovado);
        view.setForma(pagamento.getForma().getNome());
        view.setDataHoraPagamento(FORMATO_DATA_HORA.format(pagamento.getDataHora()));
        view.setIdTransacao(aprovado ? pagamento.getIdTransacao() : "-");
        view.setValorPago(aprovado ? formatoMoeda.format(pagamento.getValorPago()) : "-");

        // ---- Entrega ----
        view.setSituacaoPedido(aprovado
                ? "Pronto para entrega"
                : "Em elaboração, disponível para nova tentativa", aprovado);
        view.setPrazoEntrega(aprovado
                ? FORMATO_DATA_HORA.format(pagamento.getPrazoEntrega())
                : "-");
        view.setObservacao("Prazo gerado de forma simulada para o MVP");

        view.abrir();
    }
}
