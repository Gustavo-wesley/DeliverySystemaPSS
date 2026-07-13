package com.ufes.delivery.auditoria;

import java.util.Objects;

/**
 * Assinante unico de auditoria: escuta os eventos publicados pelos
 * services (Observer) e delega o registro ao Adapter.
 */
public class AuditoriaObserver implements IAuditoriaListener {

    private final AuditoriaAdapter adapter;

    public AuditoriaObserver(AuditoriaAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "Adapter deve ser informado");
    }

    @Override
    public void aoOcorrerEvento(EventoAuditoria evento) {
        adapter.registrar(evento);
    }
}
