package com.ufes.delivery.model;

import com.ufes.delivery.validacao.ValidadorCep;
import java.util.Set;

/**
 * Endereco de entrega do cliente (US06). Logradouro, numero, bairro,
 * cidade, UF e CEP sao obrigatorios; complemento e opcional.
 */
public class Endereco {

    private static final Set<String> UFS_VALIDAS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private Long id;
    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String uf;
    private final String cep;
    private boolean padrao;

    public Endereco(Long id, String logradouro, String numero, String complemento,
            String bairro, String cidade, String uf, String cep, boolean padrao) {

        validarObrigatorio(logradouro, "Logradouro é obrigatório");
        validarObrigatorio(numero, "Número é obrigatório");
        validarObrigatorio(bairro, "Bairro é obrigatório");
        validarObrigatorio(cidade, "Cidade é obrigatória");

        if (uf == null || !UFS_VALIDAS.contains(uf.trim().toUpperCase())) {
            throw new IllegalArgumentException("UF deve corresponder a uma sigla válida com duas letras");
        }

        if (!ValidadorCep.ehValido(cep)) {
            throw new IllegalArgumentException("CEP deve conter oito dígitos");
        }

        this.id = id;
        this.logradouro = logradouro.trim();
        this.numero = numero.trim();
        this.complemento = complemento == null ? "" : complemento.trim();
        this.bairro = bairro.trim();
        this.cidade = cidade.trim();
        this.uf = uf.trim().toUpperCase();
        this.cep = ValidadorCep.normalizar(cep);
        this.padrao = padrao;
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getCep() {
        return cep;
    }

    public boolean isPadrao() {
        return padrao;
    }

    void marcarComoPadrao(boolean padrao) {
        this.padrao = padrao;
    }

    /**
     * Descricao usada na caixa de combinacao da tela de pedido.
     */
    public String descricaoCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        if (!complemento.isEmpty()) {
            sb.append(", ").append(complemento);
        }
        sb.append(", ").append(bairro).append(", ").append(cidade)
                .append(" - ").append(uf)
                .append(", CEP ").append(ValidadorCep.formatar(cep));
        return sb.toString();
    }

    @Override
    public String toString() {
        return descricaoCompleta();
    }
}
