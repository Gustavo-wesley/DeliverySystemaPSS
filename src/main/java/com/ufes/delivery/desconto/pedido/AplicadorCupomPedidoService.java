package com.ufes.delivery.desconto.pedido;

import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.repository.ICupomRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class AplicadorCupomPedidoService {
    private ICupomRepository cupomRepository;

    public AplicadorCupomPedidoService(ICupomRepository cupomRepository) {
        this.cupomRepository = Objects.requireNonNull(cupomRepository, "Repositorio de cupons nao pode ser nulo");
    }

    public void aplicarCupom(Pedido pedido, String codigoCupom, LocalDateTime dataHoraAplicacao) {
        Objects.requireNonNull(pedido, "Pedido nao pode ser nulo");
        Objects.requireNonNull(dataHoraAplicacao, "Data e hora de aplicacao nao podem ser nulas");

        if (codigoCupom == null || codigoCupom.isBlank()) {
            throw new IllegalArgumentException("Codigo do cupom nao pode ser vazio");
        }

        Optional<CupomDescontoPedido> cupomEncontrado = cupomRepository.buscarCupom(codigoCupom);

        if (cupomEncontrado.isEmpty()) {
            throw new IllegalArgumentException("Cupom inexistente: " + codigoCupom);
        }

        CupomDescontoPedido cupom = cupomEncontrado.get();

        if (dataHoraAplicacao.isBefore(cupom.getDataHoraInicio())
                || dataHoraAplicacao.isAfter(cupom.getDataHoraFim())) {
            throw new IllegalStateException("O pedido nao esta dentro da validade do cupom");
        }

        Optional<CupomDescontoPedido> cupomAtual = pedido.getCupomAplicado();

        if (cupomAtual.isPresent()) {
            if (cupom.getPercentual() <= cupomAtual.get().getPercentual()) {
                throw new IllegalStateException(
                        "O cupom " + codigoCupom + " nao tem um percentual maior que o cupom atual");
            }
        }

        pedido.setCupomAplicado(cupom);
    }
}
