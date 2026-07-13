package com.ufes.delivery.model;

/**
 * Estados do ciclo do pedido. O dominio e fechado pelo enunciado,
 * por isso enum apenas como rotulo, sem comportamento de transicao.
 */
public enum EstadoPedido {
    NOVO("Novo"),
    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    EM_PREPARO("Em preparo"),
    AGUARDANDO_ENTREGA("Aguardando entrega"),
    EM_TRANSITO("Em trânsito"),
    ENTREGUE("Entregue");

    private final String descricao;

    EstadoPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static EstadoPedido porDescricao(String descricao) {
        for (EstadoPedido estado : values()) {
            if (estado.descricao.equalsIgnoreCase(descricao) || estado.name().equalsIgnoreCase(descricao)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido desconhecido: " + descricao);
    }
}
