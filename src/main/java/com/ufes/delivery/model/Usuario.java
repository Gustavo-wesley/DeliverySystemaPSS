package com.ufes.delivery.model;

import com.ufes.delivery.model.perfil.PerfilUsuario;
import java.util.regex.Pattern;

/**
 * Usuario do sistema. A senha nunca e mantida em texto aberto:
 * o agregado guarda apenas hash e salt.
 */
public class Usuario {

    private static final Pattern FORMATO_USERNAME = Pattern.compile("^[a-z0-9]{3,30}$");

    private Long id;
    private final String nome;
    private final String username;
    private final String senhaHash;
    private final String salt;
    private PerfilUsuario perfil;
    private SituacaoUsuario situacao;

    public Usuario(Long id, String nome, String username, String senhaHash, String salt,
            PerfilUsuario perfil, SituacaoUsuario situacao) {

        if (nome == null || nome.trim().length() < 2 || nome.trim().length() > 120) {
            throw new IllegalArgumentException("Nome deve conter de 2 a 120 caracteres");
        }

        validarUsername(username);

        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha deve ser informado");
        }

        if (salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("Salt da senha deve ser informado");
        }

        if (perfil == null) {
            throw new IllegalArgumentException("Perfil do usuário deve ser informado");
        }

        if (situacao == null) {
            throw new IllegalArgumentException("Situação do usuário deve ser informada");
        }

        this.id = id;
        this.nome = nome.trim();
        this.username = username;
        this.senhaHash = senhaHash;
        this.salt = salt;
        this.perfil = perfil;
        this.situacao = situacao;
    }

    public static void validarUsername(String username) {
        if (username == null || !FORMATO_USERNAME.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Nome de usuário deve conter de 3 a 30 caracteres, "
                    + "somente letras minúsculas e algarismos, sem espaços");
        }
    }

    public boolean podeIniciarSessao() {
        return situacao == SituacaoUsuario.AUTORIZADO;
    }

    public void autorizar() {
        this.situacao = SituacaoUsuario.AUTORIZADO;
    }

    public void desautorizar() {
        this.situacao = SituacaoUsuario.NAO_AUTORIZADO;
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

    public String getUsername() {
        return username;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getSalt() {
        return salt;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil do usuário deve ser informado");
        }
        this.perfil = perfil;
    }

    public SituacaoUsuario getSituacao() {
        return situacao;
    }

    @Override
    public String toString() {
        return "Usuario{username='" + username + "', nome='" + nome
                + "', perfil=" + perfil.getNome() + ", situacao=" + situacao.getDescricao() + "}";
    }
}
