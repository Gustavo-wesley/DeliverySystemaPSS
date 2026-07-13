package com.ufes.delivery.service.pagamento;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class GeradorPrazoEntregaSimulado implements IGeradorPrazoEntrega {

    private final IGeradorAleatorio geradorAleatorio;

    public GeradorPrazoEntregaSimulado(IGeradorAleatorio geradorAleatorio) {
        this.geradorAleatorio = Objects.requireNonNull(geradorAleatorio,
                "Gerador aleatório deve ser informado");
    }

    @Override
    public LocalDateTime gerar(LocalDateTime instanteAprovacao) {
        Objects.requireNonNull(instanteAprovacao, "Instante da aprovação deve ser informado");

        LocalDateTime limite = instanteAprovacao.plusMonths(1);
        long minutosDisponiveis = Duration.between(instanteAprovacao, limite).toMinutes();

        long deslocamento = (long) (geradorAleatorio.proximoDouble() * minutosDisponiveis);
        return instanteAprovacao.plusMinutes(deslocamento).withSecond(0).withNano(0);
    }
}
