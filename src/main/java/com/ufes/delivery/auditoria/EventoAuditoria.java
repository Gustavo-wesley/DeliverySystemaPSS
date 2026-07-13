package com.ufes.delivery.auditoria;

/**
 * Evento rico de auditoria publicado pelos services de dominio (US12).
 * Quando nao houver pedido/cliente (login, cadastro de usuario,
 * estoque, autorizacao), os campos ficam nulos e o Adapter aplica
 * a sentinela "-" exigida pela lib.
 *
 * Nunca carregue senha ou dado financeiro sensivel na mensagem.
 */
public record EventoAuditoria(
        String nomeOperacao,
        String codigoPedido,
        String nomeCliente,
        String recursoAfetado,
        String resultado,
        String justificativa) {

    public static EventoAuditoria dePedido(String nomeOperacao, String codigoPedido,
            String nomeCliente, String resultado, String justificativa) {
        return new EventoAuditoria(nomeOperacao, codigoPedido, nomeCliente,
                "Pedido " + codigoPedido, resultado, justificativa);
    }

    public static EventoAuditoria semPedido(String nomeOperacao, String recursoAfetado,
            String resultado, String justificativa) {
        return new EventoAuditoria(nomeOperacao, null, null,
                recursoAfetado, resultado, justificativa);
    }

    /**
     * Mensagem estruturada com recurso, resultado e justificativa.
     */
    public String mensagemEstruturada() {
        StringBuilder sb = new StringBuilder();
        sb.append("recurso=").append(vazioSeNulo(recursoAfetado));
        sb.append("; resultado=").append(vazioSeNulo(resultado));
        if (justificativa != null && !justificativa.isBlank()) {
            sb.append("; justificativa=").append(justificativa);
        }
        return sb.toString();
    }

    private String vazioSeNulo(String valor) {
        return valor == null ? "" : valor;
    }
}
