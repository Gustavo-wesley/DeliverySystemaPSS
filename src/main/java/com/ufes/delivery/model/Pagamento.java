package com.ufes.delivery.model;

import com.ufes.delivery.service.pagamento.IFormaPagamento;
import com.ufes.delivery.service.pagamento.IResultadoPagamento;
import java.time.LocalDateTime;

/**
 * Tentativa de pagamento simulada (US11). O identificador de transacao
 * e o prazo de entrega existem somente para resultado aprovado.
 */
public class Pagamento {

    private Long id;
    private final Pedido pedido;
    private final IResultadoPagamento resultado;
    private final IFormaPagamento forma;
    private final String idTransacao;
    private final double valorPago;
    private final LocalDateTime dataHora;
    private final LocalDateTime prazoEntrega;

    public Pagamento(Long id, Pedido pedido, IResultadoPagamento resultado,
            IFormaPagamento forma, String idTransacao, double valorPago,
            LocalDateTime dataHora, LocalDateTime prazoEntrega) {

        if (pedido == null) {
            throw new IllegalArgumentException("Pedido do pagamento deve ser informado");
        }
        if (resultado == null) {
            throw new IllegalArgumentException("Resultado do pagamento deve ser informado");
        }
        if (forma == null) {
            throw new IllegalArgumentException("Forma de pagamento deve ser informada");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException("Data e hora do pagamento devem ser informadas");
        }

        if (resultado.isAprovado()) {
            if (idTransacao == null || idTransacao.isBlank()) {
                throw new IllegalArgumentException(
                        "Identificador da transação é obrigatório para resultado aprovado");
            }
            if (prazoEntrega == null) {
                throw new IllegalArgumentException(
                        "Prazo estimado de entrega é obrigatório para resultado aprovado");
            }
            if (prazoEntrega.isBefore(dataHora)) {
                throw new IllegalArgumentException(
                        "Prazo de entrega deve ser posterior ou igual ao instante da aprovação");
            }
        }

        this.id = id;
        this.pedido = pedido;
        this.resultado = resultado;
        this.forma = forma;
        this.idTransacao = idTransacao;
        this.valorPago = valorPago;
        this.dataHora = dataHora;
        this.prazoEntrega = prazoEntrega;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public IResultadoPagamento getResultado() {
        return resultado;
    }

    public IFormaPagamento getForma() {
        return forma;
    }

    public String getIdTransacao() {
        return idTransacao;
    }

    public double getValorPago() {
        return valorPago;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public LocalDateTime getPrazoEntrega() {
        return prazoEntrega;
    }
}
