package com.ufes.logger;

import com.ufes.model.LogEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class CsvLogger implements ILogger {

    private final String caminhoArquivo;

    public CsvLogger(String caminhoArquivo) {

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

        File arquivo = new File(caminhoArquivo);
        boolean arquivoNovo = !arquivo.exists();

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(arquivo, true))) {

            if (arquivoNovo) {
                writer.write(
                        "usuario,data,hora,codigoPedido,operacao,cliente,mensagem");
                writer.newLine();
            }

            writer.write(
                    String.format(
                            "%s,%s,%s,%s,%s,%s,%s",
                            logEntry.getNomeUsuario(),
                            logEntry.getData().format(formatoData),
                            logEntry.getHora().format(formatoHora),
                            logEntry.getCodigoPedido(),
                            logEntry.getNomeOperacao(),
                            logEntry.getNomeCliente(),
                            logEntry.getMensagem()
                    ));

            writer.newLine();

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Erro ao gravar log CSV",
                    ex);
        }
    }
}