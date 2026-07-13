package com.ufes.logger;

import com.ufes.model.LogEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class XmlLogger implements ILogger {

    private final String caminhoArquivo;

    public XmlLogger(String caminhoArquivo) {

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

        String xml =
                "<registro>" +
                        "<usuario>" + logEntry.getNomeUsuario() + "</usuario>" +
                        "<data>" + logEntry.getData().format(formatoData) + "</data>" +
                        "<hora>" + logEntry.getHora().format(formatoHora) + "</hora>" +
                        "<codigoPedido>" + logEntry.getCodigoPedido() + "</codigoPedido>" +
                        "<operacao>" + logEntry.getNomeOperacao() + "</operacao>" +
                        "<cliente>" + logEntry.getNomeCliente() + "</cliente>" +
                        "<mensagem>" + logEntry.getMensagem() + "</mensagem>" +
                        "</registro>";

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(caminhoArquivo, true))) {

            writer.write(xml);
            writer.newLine();

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Erro ao gravar log XML",
                    ex);
        }
    }
}