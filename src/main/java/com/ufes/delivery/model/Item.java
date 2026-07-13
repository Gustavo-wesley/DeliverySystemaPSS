package com.ufes.delivery.model;

public class Item {
    private String nome;
    private int quantidade;
    private double valorUnitario;
    private String tipo;
    private Produto produto;

    /**
     * Item vinculado a um produto do catalogo (US09): nome, valor
     * e categoria derivam do produto, e a baixa de estoque na
     * aprovacao do pagamento usa esse vinculo.
     */
    public Item(Produto produto, int quantidade) {
        this(produto.getNome(), quantidade, produto.getPrecoUnitario(), produto.getCategoria());
        this.produto = produto;
    }

    /**
     * Reconstrucao a partir da persistencia, preservando o valor
     * unitario praticado no momento do pedido.
     */
    public Item(Produto produto, int quantidade, double valorUnitarioPraticado) {
        this(produto.getNome(), quantidade, valorUnitarioPraticado, produto.getCategoria());
        this.produto = produto;
    }

    public Item(String nome, int quantidade, double valorUnitario, String tipo) {
        validarTextoObrigatorio(nome, "Nome do item nao pode ser vazio");
        validarTextoObrigatorio(tipo, "Tipo do item nao pode ser vazio");

        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero");
        }

        if (valorUnitario < 0) {
            throw new IllegalArgumentException("Valor unitario do item nao pode ser negativo");
        }

        this.nome = nome;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.tipo = tipo;
    }

    public double valorTotal() {
        return valorUnitario * quantidade;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public String getTipo() {
        return tipo;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero");
        }
        this.quantidade = quantidade;
    }

    private void validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    @Override
    public String toString() {
        return "Item{"
                + "nome='" + nome + '\''
                + ", quantidade=" + quantidade
                + ", valorUnitario=" + valorUnitario
                + ", tipo='" + tipo + '\''
                + ", valorTotal=" + valorTotal()
                + "}";
    }
}
