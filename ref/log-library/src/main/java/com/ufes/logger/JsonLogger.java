package com.ufes.logger;

import com.ufes.model.LogEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class JsonLogger implements ILogger {

    private final String caminhoArquivo;

    public JsonLogger(String caminhoArquivo) {

        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Caminho do arquivo deve ser informado");
        }

        this.caminhoArquivo = caminhoArquivo;
    }

    @Override
    public void registrar(LogEntry logEntry) {

        if (logEntry == null) {
            throw new IllegalArgumentException(
                    "LogEntry deve ser informado");
        }

        DateTimeFormatter formatoData =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        DateTimeFormatter formatoHora =
                DateTimeFormatter.ofPattern("HH:mm:ss");

        String json = String.format(
                "{\"usuario\":\"%s\","
                        + "\"data\":\"%s\","
                        + "\"hora\":\"%s\","
                        + "\"codigoPedido\":\"%s\","
                        + "\"operacao\":\"%s\","
                        + "\"cliente\":\"%s\","
                        + "\"mensagem\":\"%s\"}",
                logEntry.getNomeUsuario(),
                logEntry.getData().format(formatoData),
                logEntry.getHora().format(formatoHora),
                logEntry.getCodigoPedido(),
                logEntry.getNomeOperacao(),
                logEntry.getNomeCliente(),
                logEntry.getMensagem()
        );

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(caminhoArquivo, true))) {

            writer.write(json);
            writer.newLine();

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Erro ao gravar log JSONL",
                    ex);
        }
    }
}