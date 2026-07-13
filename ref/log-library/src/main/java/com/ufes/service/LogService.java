package com.ufes.service;

import com.ufes.logger.ILogger;
import com.ufes.model.LogEntry;
import com.ufes.util.UsuarioLogadoService;

import java.time.LocalDate;
import java.time.LocalTime;

public class LogService {

    private final ILogger logger;

    public LogService(ILogger logger) {

        if (logger == null) {
            throw new IllegalArgumentException(
                    "Logger deve ser informado");
        }

        this.logger = logger;
    }

    public void registrarOperacao(
            String codigoPedido,
            String nomeOperacao,
            String nomeCliente,
            String mensagem) {

        LogEntry logEntry = new LogEntry(
                UsuarioLogadoService.getNomeUsuario(),
                LocalDate.now(),
                LocalTime.now(),
                codigoPedido,
                nomeOperacao,
                nomeCliente,
                mensagem
        );

        logger.registrar(logEntry);
    }

    public void registrarErro(
            String codigoPedido,
            String nomeOperacao,
            String nomeCliente,
            Exception ex) {

        LogEntry logEntry = new LogEntry(
                UsuarioLogadoService.getNomeUsuario(),
                LocalDate.now(),
                LocalTime.now(),
                codigoPedido,
                nomeOperacao,
                nomeCliente,
                ex.getMessage()
        );

        logger.registrar(logEntry);
    }
}