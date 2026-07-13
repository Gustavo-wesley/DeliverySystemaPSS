package com.ufes.delivery.model;

import com.ufes.delivery.configuracao.ConfiguracaoService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public class Pedido {
    private Long id;
    private String codigo;
    private double taxaEntrega = ConfiguracaoService.getTaxaEntregaPadrao();
    private List<Item> itens = new ArrayList<>();
    private Cliente cliente;
    private Endereco enderecoEntrega;
    private EstadoPedido estado = EstadoPedido.NOVO;
    private List<CupomDescontoEntrega> cuponsDescontoEntrega = new ArrayList<>();
    private LocalDateTime data;
    private java.time.LocalDate dataConclusao;

    private CupomDescontoPedido cupomPedidoAplicado;

    public Pedido(LocalDateTime data, Cliente cliente) {
        if (data == null) {
            throw new IllegalArgumentException("Data do pedido deve ser informada");
        }

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente do pedido deve ser informado");
        }

        this.cliente = cliente;
        this.data = data;
    }

    public void adicionarItem(Item objeto) {
        if (objeto == null) {
            throw new IllegalArgumentException("Item do pedido deve ser informado");
        }

        itens.add(objeto);
    }

    public void removerItem(Item item) {
        itens.remove(item);
    }

    public void definirEnderecoEntrega(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço de entrega deve ser informado");
        }
        if (!cliente.getEnderecos().contains(endereco)) {
            throw new IllegalArgumentException("Endereço de entrega deve pertencer ao cliente selecionado");
        }
        this.enderecoEntrega = endereco;
    }

    /**
     * Transicao de estado. A data de conclusao e preenchida somente
     * quando o pedido passa para Entregue.
     */
    public void mudarEstado(EstadoPedido novoEstado, java.time.LocalDate dataOperacional) {
        if (novoEstado == null) {
            throw new IllegalArgumentException("Estado do pedido deve ser informado");
        }
        this.estado = novoEstado;
        if (novoEstado == EstadoPedido.ENTREGUE) {
            this.dataConclusao = dataOperacional;
        } else {
            this.dataConclusao = null;
        }
    }

    /**
     * Validacao para seguir ao pagamento (US09/US10): cliente,
     * endereco e pelo menos um item.
     */
    public void validarParaPagamento() {
        if (enderecoEntrega == null) {
            throw new IllegalStateException("Endereço de entrega é obrigatório");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pelo menos um item é obrigatório");
        }
    }

    public double getValorPedido() {
        double valor = 0;
        for (Item item : itens) {
            valor += item.valorTotal();
        }
        return valor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Item> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public double getTaxaEntrega() {
        return taxaEntrega;
    }

    public double getTotalDescontosTaxaEntrega() {
        double desconto = 0;

        for (CupomDescontoEntrega cupom : cuponsDescontoEntrega) {
            desconto += cupom.getValorDesconto();
        }

        return desconto;
    }

    public List<CupomDescontoEntrega> getCupomDescontoEntrega() {
        return Collections.unmodifiableList(cuponsDescontoEntrega);
    }

    public void limparCuponsDescontoEntrega() {
        cuponsDescontoEntrega.clear();
    }

    public void adicionarCupomDescontoEntrega(CupomDescontoEntrega cupom) {
        if (cupom == null) {
            throw new IllegalArgumentException("Cupom de desconto da entrega deve ser informado");
        }

        double totalDescontosAposAdicionar = getTotalDescontosTaxaEntrega() + cupom.getValorDesconto();
        double limiteAplicavel = taxaEntrega;

        if (cupom.getValorDesconto() < 0) {
            throw new IllegalArgumentException("Desconto na taxa de entrega nao pode ser negativo");
        }

        if (totalDescontosAposAdicionar > limiteAplicavel) {
            throw new IllegalStateException(
                    "Desconto total na taxa de entrega nao pode ultrapassar " + limiteAplicavel);
        }

        cuponsDescontoEntrega.add(cupom);
    }

    public double getTaxaEntregaComDesconto() {
        double taxaComDesconto = taxaEntrega - getTotalDescontosTaxaEntrega();
        if (taxaComDesconto < 0) {
            return 0;
        }
        return taxaComDesconto;
    }

    public double calcularValorTotal() {
        double valorTotal = getValorPedido() + getTaxaEntregaComDesconto();
        Optional<CupomDescontoPedido> cupomAplicado = getCupomAplicado();

        if (cupomAplicado.isPresent()) {
            CupomDescontoPedido cupom = cupomAplicado.get();
            return valorTotal - valorTotal * cupom.getPercentual() / 100;
        }

        return valorTotal;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public java.time.LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public void removerCupomAplicado() {
        this.cupomPedidoAplicado = null;
    }

    public Optional<CupomDescontoPedido> getCupomAplicado() {
        return Optional.ofNullable(cupomPedidoAplicado);
    }

    public void setCupomAplicado(CupomDescontoPedido cupomPedidoAplicado) {
        if (cupomPedidoAplicado == null) {
            throw new IllegalArgumentException("Cupom do pedido deve ser informado");
        }

        this.cupomPedidoAplicado = cupomPedidoAplicado;
    }

    @Override
    public String toString() {
        return "Pedido{"
                + "data=" + data
                + ", cliente=" + cliente
                + ", itens=" + itens
                + ", taxaEntrega=" + taxaEntrega
                + ", cuponsDescontoEntrega=" + cuponsDescontoEntrega
                + ", cupomPedidoAplicado=" + cupomPedidoAplicado
                + ", valorPedido=" + getValorPedido()
                + ", totalDescontosTaxaEntrega=" + getTotalDescontosTaxaEntrega()
                + ", valorTotal=" + calcularValorTotal()
                + "}";
    }
}
