package com.ufes.delivery.model;

import com.ufes.delivery.validacao.ValidadorCpf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Agregado Cliente (US06): nome, CPF unico e de um a tres enderecos
 * de entrega, com exatamente um endereco padrao.
 *
 * Os campos tipo e fidelidade do CR1 sao mantidos porque as strategies
 * de desconto da taxa de entrega dependem deles.
 */
public class Cliente {

    public static final int MAXIMO_ENDERECOS = 3;
    public static final String MSG_LIMITE_ENDERECOS =
            "O cliente pode ter no máximo três endereços de entrega";
    public static final String MSG_ENDERECO_PADRAO_OBRIGATORIO =
            "Um endereço padrão é obrigatório";

    private Long id;
    private String nome;
    private String cpf;
    private String tipo;
    private double fidelidade;
    private final List<Endereco> enderecos = new ArrayList<>();

    public Cliente(Long id, String nome, String cpf, String tipo, double fidelidade) {
        validarNome(nome);

        if (!ValidadorCpf.ehValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido");
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Tipo do cliente não pode ser vazio");
        }

        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente não pode ser negativa");
        }

        this.id = id;
        this.nome = nome.trim();
        this.cpf = ValidadorCpf.normalizar(cpf);
        this.tipo = tipo;
        this.fidelidade = fidelidade;
    }

    public Cliente(Long id, String nome, String cpf) {
        this(id, nome, cpf, "Comum", 0);
    }

    public static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.length() < 2 || nomeLimpo.length() > 120) {
            throw new IllegalArgumentException("Nome deve conter de 2 a 120 caracteres");
        }
        if (!nomeLimpo.matches("^[\\p{L}' \\-]+$")) {
            throw new IllegalArgumentException("Nome aceita apenas letras, espaços, apóstrofos e hífens");
        }
    }

    public void adicionarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço deve ser informado");
        }
        if (enderecos.size() >= MAXIMO_ENDERECOS) {
            throw new IllegalStateException(MSG_LIMITE_ENDERECOS);
        }
        if (endereco.isPadrao()) {
            enderecos.forEach(e -> e.marcarComoPadrao(false));
        }
        enderecos.add(endereco);
    }

    public void removerEndereco(Endereco endereco) {
        enderecos.remove(endereco);
    }

    public void definirEnderecoPadrao(Endereco endereco) {
        if (!enderecos.contains(endereco)) {
            throw new IllegalArgumentException("Endereço não pertence ao cliente");
        }
        enderecos.forEach(e -> e.marcarComoPadrao(false));
        endereco.marcarComoPadrao(true);
    }

    public Optional<Endereco> getEnderecoPadrao() {
        return enderecos.stream().filter(Endereco::isPadrao).findFirst();
    }

    public List<Endereco> getEnderecos() {
        return Collections.unmodifiableList(enderecos);
    }

    /**
     * Valida as invariantes do agregado antes da persistencia:
     * de um a tres enderecos e exatamente um padrao.
     */
    public void validarParaSalvar() {
        if (enderecos.isEmpty()) {
            throw new IllegalStateException("O cliente deve ter pelo menos um endereço de entrega");
        }
        if (enderecos.size() > MAXIMO_ENDERECOS) {
            throw new IllegalStateException(MSG_LIMITE_ENDERECOS);
        }

        long quantidadePadrao = enderecos.stream().filter(Endereco::isPadrao).count();
        if (quantidadePadrao == 0) {
            throw new IllegalStateException(MSG_ENDERECO_PADRAO_OBRIGATORIO);
        }
        if (quantidadePadrao > 1) {
            throw new IllegalStateException("Deve existir exatamente um endereço padrão");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome.trim();
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        if (!ValidadorCpf.ehValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido");
        }
        this.cpf = ValidadorCpf.normalizar(cpf);
    }

    public String getTipo() {
        return tipo;
    }

    public double getFidelidade() {
        return fidelidade;
    }

    public void setFidelidade(double fidelidade) {
        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente não pode ser negativa");
        }
        this.fidelidade = fidelidade;
    }

    /**
     * Bairro do endereco padrao — mantido para as strategies de desconto
     * do CR1 que consultam o bairro do cliente.
     */
    public String getBairro() {
        return getEnderecoPadrao().map(Endereco::getBairro).orElse("");
    }

    @Override
    public String toString() {
        return "Cliente{nome='" + nome + "', cpf='" + ValidadorCpf.formatar(cpf)
                + "', tipo='" + tipo + "', enderecos=" + enderecos.size() + "}";
    }
}
