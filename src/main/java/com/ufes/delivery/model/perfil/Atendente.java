package com.ufes.delivery.model.perfil;

public class Atendente implements PerfilUsuario {

    public static final String NOME = "Atendente";

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public boolean podeExecutarOperacoesAdministrativas() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Atendente;
    }

    @Override
    public int hashCode() {
        return NOME.hashCode();
    }

    @Override
    public String toString() {
        return NOME;
    }
}
