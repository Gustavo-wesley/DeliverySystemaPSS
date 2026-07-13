package com.ufes.delivery.model.estoque;

import java.util.List;

/**
 * Resolucao de tipo de movimentacao a partir do nome persistido
 * ou selecionado na tela. "Saida" nao esta registrada e por isso
 * e rejeitada naturalmente.
 */
public final class TiposMovimentacao {

    private static final List<ITipoMovimentacao> TIPOS_DISPONIVEIS =
            List.of(new Entrada(), new AjusteEstoque());

    private TiposMovimentacao() {
    }

    public static ITipoMovimentacao porNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Tipo de movimentação deve ser informado");
        }

        return TIPOS_DISPONIVEIS.stream()
                .filter(t -> t.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de movimentação inválido: " + nome));
    }

    public static List<ITipoMovimentacao> disponiveis() {
        return TIPOS_DISPONIVEIS;
    }
}
