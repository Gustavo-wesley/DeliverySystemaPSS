package com.ufes.delivery.model.perfil;

import java.util.List;

/**
 * Ponto unico de resolucao de perfil a partir do nome persistido.
 * Para adicionar um perfil novo, basta registra-lo na lista.
 */
public final class Perfis {

    private static final List<PerfilUsuario> PERFIS_DISPONIVEIS =
            List.of(new Administrador(), new Atendente());

    private Perfis() {
    }

    public static PerfilUsuario porNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do perfil deve ser informado");
        }

        return PERFIS_DISPONIVEIS.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil desconhecido: " + nome));
    }

    public static List<PerfilUsuario> disponiveis() {
        return PERFIS_DISPONIVEIS;
    }
}
