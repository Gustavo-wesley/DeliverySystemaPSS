package com.ufes.delivery.model.perfil;

public class Administrador implements PerfilUsuario {

    public static final String NOME = "Administrador";

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public boolean podeExecutarOperacoesAdministrativas() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Administrador;
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
