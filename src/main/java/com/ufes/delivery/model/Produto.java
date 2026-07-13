package com.ufes.delivery.model;

/**
 * Produto do catalogo (US07): codigo inteiro positivo unico, nome,
 * categoria controlada, preco unitario e estoque atual (nunca negativo).
 */
public class Produto {

    private Long id;
    private final int codigo;
    private String nome;
    private String categoria;
    private double precoUnitario;
    private int estoqueAtual;

    public Produto(Long id, int codigo, String nome, String categoria,
            double precoUnitario, int estoqueAtual) {

        if (codigo <= 0) {
            throw new IllegalArgumentException("Código deve ser um inteiro positivo");
        }

        validarNome(nome);
        validarCategoria(categoria);
        validarPreco(precoUnitario);

        if (estoqueAtual < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo");
        }

        this.id = id;
        this.codigo = codigo;
        this.nome = nome.trim();
        this.categoria = categoria.trim();
        this.precoUnitario = precoUnitario;
        this.estoqueAtual = estoqueAtual;
    }

    public static void validarNome(String nome) {
        if (nome == null || nome.trim().length() < 2 || nome.trim().length() > 120) {
            throw new IllegalArgumentException("Nome deve conter de 2 a 120 caracteres");
        }
    }

    public static void validarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
    }

    public static void validarPreco(double preco) {
        if (preco <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que R$ 0,00");
        }

        double centavos = preco * 100;
        if (Math.abs(centavos - Math.round(centavos)) > 0.000001) {
            throw new IllegalArgumentException("Preço unitário deve possuir no máximo duas casas decimais");
        }
    }

    /**
     * Aplica uma variacao de estoque (positiva ou negativa),
     * impedindo resultado negativo.
     */
    public void aplicarVariacaoEstoque(int variacao) {
        int resultado = estoqueAtual + variacao;
        if (resultado < 0) {
            throw new IllegalStateException("Estoque resultante não pode ser negativo. Quantidade disponível: " + estoqueAtual);
        }
        this.estoqueAtual = resultado;
    }

    public boolean temEstoqueDisponivel(int quantidade) {
        return estoqueAtual >= quantidade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome.trim();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        validarCategoria(categoria);
        this.categoria = categoria.trim();
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        validarPreco(precoUnitario);
        this.precoUnitario = precoUnitario;
    }

    public int getEstoqueAtual() {
        return estoqueAtual;
    }

    @Override
    public String toString() {
        return "Produto{codigo=" + codigo + ", nome='" + nome + "', categoria='" + categoria
                + "', preco=" + precoUnitario + ", estoque=" + estoqueAtual + "}";
    }
}
