package com.ufes.delivery.model.perfil;

/**
 * Perfil de usuario modelado como Strategy (nao enum): um novo perfil
 * entra como nova classe, sem editar switch existente (OCP).
 */
public interface PerfilUsuario {

    String getNome();

    /**
     * Indica se o perfil pode executar operacoes administrativas
     * (gestao de usuarios, movimentacao de estoque, cadastro de produto).
     */
    boolean podeExecutarOperacoesAdministrativas();
}
