package com.ufes.delivery.model;

/**
 * Situacao de acesso do usuario. Enum apenas como rotulo fechado,
 * sem comportamento (dominio fixado pelo enunciado).
 */
public enum SituacaoUsuario {
    AUTORIZADO("Autorizado"),
    PENDENTE("Pendente"),
    NAO_AUTORIZADO("Não autorizado");

    private final String descricao;

    SituacaoUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static SituacaoUsuario porDescricao(String descricao) {
        for (SituacaoUsuario situacao : values()) {
            if (situacao.descricao.equalsIgnoreCase(descricao) || situacao.name().equalsIgnoreCase(descricao)) {
                return situacao;
            }
        }
        throw new IllegalArgumentException("Situação de usuário desconhecida: " + descricao);
    }
}
