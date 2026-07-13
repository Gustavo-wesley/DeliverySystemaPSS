package com.ufes.delivery.auditoria;

import com.ufes.delivery.service.SessaoService;
import com.ufes.logger.ILogger;
import com.ufes.model.LogEntry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Adapter (US12): traduz o evento rico do dominio para o LogEntry
 * exigido pela Log-library, que nao pode ser alterada.
 *
 * - codigoPedido e nomeCliente sao obrigatorios na lib; eventos sem
 *   pedido/cliente usam a sentinela "-".
 * - O usuario vem do SessaoService do Delivery (substitui o mock
 *   UsuarioLogadoService da lib), por isso o LogEntry e montado aqui,
 *   sem passar pelo LogService da lib.
 * - Indisponibilidade da auditoria e tratada como falha operacional,
 *   sem expor dado sensivel na mensagem propagada.
 */
public class AuditoriaAdapter {

    public static final String SENTINELA = "-";
    public static final String USUARIO_ANONIMO = "anonimo";

    private final ILogger logger;
    private final SessaoService sessaoService;

    public AuditoriaAdapter(ILogger logger, SessaoService sessaoService) {
        this.logger = Objects.requireNonNull(logger, "Logger deve ser informado");
        this.sessaoService = Objects.requireNonNull(sessaoService,
                "Serviço de sessão deve ser informado");
    }

    public void registrar(EventoAuditoria evento) {
        Objects.requireNonNull(evento, "Evento de auditoria deve ser informado");

        LocalDateTime agora = LocalDateTime.now();
        LogEntry logEntry = new LogEntry(
                nomeUsuario(),
                LocalDate.from(agora),
                LocalTime.from(agora),
                sentinelaSeVazio(evento.codigoPedido()),
                evento.nomeOperacao(),
                sentinelaSeVazio(evento.nomeCliente()),
                evento.mensagemEstruturada());

        try {
            logger.registrar(logEntry);
        } catch (Exception e) {
            // falha operacional tratada: nao propaga conteudo do registro
            throw new AuditoriaIndisponivelException(
                    "O registro de auditoria está indisponível no momento", e);
        }
    }

    private String nomeUsuario() {
        // antes do login (tentativas de autenticacao, primeiro cadastro)
        // ainda nao ha sessao ativa
        return sessaoService.haSessaoAtiva()
                ? sessaoService.getNomeUsuario()
                : USUARIO_ANONIMO;
    }

    private String sentinelaSeVazio(String valor) {
        return (valor == null || valor.isBlank()) ? SENTINELA : valor;
    }
}
