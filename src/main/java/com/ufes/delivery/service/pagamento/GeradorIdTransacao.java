package com.ufes.delivery.service.pagamento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Gera identificador legivel de transacao aprovada,
 * no formato TXN-AAAAMMDD-NNNN.
 */
public class GeradorIdTransacao {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IGeradorAleatorio geradorAleatorio;

    public GeradorIdTransacao(IGeradorAleatorio geradorAleatorio) {
        this.geradorAleatorio = Objects.requireNonNull(geradorAleatorio,
                "Gerador aleatório deve ser informado");
    }

    public String gerar(LocalDateTime instante) {
        int sufixo = 1000 + geradorAleatorio.proximoInt(9000);
        return "TXN-" + instante.format(FORMATO_DATA) + "-" + sufixo;
    }
}
