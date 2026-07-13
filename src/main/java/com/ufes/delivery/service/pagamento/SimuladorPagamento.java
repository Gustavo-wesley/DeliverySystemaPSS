package com.ufes.delivery.service.pagamento;

import java.util.List;
import java.util.Objects;

/**
 * Sorteia o resultado (50% aprovado / 50% reprovado) e a forma de
 * pagamento (25% cada) a partir da fonte de aleatoriedade injetada.
 */
public class SimuladorPagamento {

    private final IGeradorAleatorio geradorAleatorio;
    private final List<IFormaPagamento> formasDisponiveis;

    public SimuladorPagamento(IGeradorAleatorio geradorAleatorio) {
        this.geradorAleatorio = Objects.requireNonNull(geradorAleatorio,
                "Gerador aleatório deve ser informado");
        this.formasDisponiveis = List.of(
                new OpenFinance(), new PixChave(), new PixQrCode(), new CartaoCredito());
    }

    public IResultadoPagamento sortearResultado() {
        return geradorAleatorio.proximoDouble() < 0.5
                ? new ResultadoAprovado()
                : new ResultadoReprovado();
    }

    public IFormaPagamento sortearForma() {
        int indice = geradorAleatorio.proximoInt(formasDisponiveis.size());
        return formasDisponiveis.get(indice);
    }

    public List<IFormaPagamento> getFormasDisponiveis() {
        return formasDisponiveis;
    }
}
