package com.ufes.delivery.auditoria;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject do padrao Observer: os services de dominio publicam eventos
 * de auditoria sem conhecer quem os registra. O assinante unico da
 * aplicacao e o {@link AuditoriaObserver}.
 */
public class AuditoriaPublisher {

    private final List<IAuditoriaListener> listeners = new ArrayList<>();

    public void registrar(IAuditoriaListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void remover(IAuditoriaListener listener) {
        listeners.remove(listener);
    }

    public void publicar(EventoAuditoria evento) {
        if (evento == null) {
            return;
        }
        for (IAuditoriaListener listener : listeners) {
            listener.aoOcorrerEvento(evento);
        }
    }
}
