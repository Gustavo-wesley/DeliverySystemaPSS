package com.ufes.logger;

public class LoggerFactory {

    private LoggerFactory() {
        throw new IllegalStateException(
                "Classe utilitaria nao deve ser instanciada");
    }

    public static ILogger criarLogger(
            TipoLogger tipoLogger,
            String caminhoArquivo) {

        if (tipoLogger == null) {
            throw new IllegalArgumentException(
                    "Tipo de logger deve ser informado");
        }

        return switch (tipoLogger) {

            case JSONL ->
                    new JsonLogger(caminhoArquivo);

            case CSV ->
                    new CsvLogger(caminhoArquivo);

            case XML ->
                    new XmlLogger(caminhoArquivo);
        };
    }
}