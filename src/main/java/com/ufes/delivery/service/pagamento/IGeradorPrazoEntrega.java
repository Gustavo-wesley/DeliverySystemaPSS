package com.ufes.delivery.service.pagamento;

import java.time.LocalDateTime;

/**
 * Modulo especifico de geracao do prazo estimado de entrega (US11).
 * Simulado no MVP; a interface permite mock deterministico nos testes.
 */
public interface IGeradorPrazoEntrega {

    /**
     * Data e hora entre o instante da aprovacao (inclusive) e o mesmo
     * dia do mes subsequente (inclusive).
     */
    LocalDateTime gerar(LocalDateTime instanteAprovacao);
}
