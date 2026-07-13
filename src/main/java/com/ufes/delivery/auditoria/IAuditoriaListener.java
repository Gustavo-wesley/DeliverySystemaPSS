package com.ufes.delivery.auditoria;

/**
 * Observer dos eventos de auditoria publicados pelos services.
 */
public interface IAuditoriaListener {

    void aoOcorrerEvento(EventoAuditoria evento);
}
