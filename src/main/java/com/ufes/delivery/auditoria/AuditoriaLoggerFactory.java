package com.ufes.delivery.auditoria;

import com.ufes.logger.ILogger;
import com.ufes.logger.LoggerFactory;
import com.ufes.logger.TipoLogger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Factory propria do Delivery (US12): decide a modalidade de auditoria
 * da execucao — loggers de arquivo da lib (JSONL, CSV, XML) ou o
 * SqliteLogger local. Modalidade unica por execucao, definida em
 * auditoria.properties.
 */
public final class AuditoriaLoggerFactory {

    public static final String ARQUIVO_CONFIGURACAO = "auditoria.properties";
    public static final String CHAVE_MODALIDADE = "auditoria.modalidade";
    public static final String CHAVE_CAMINHO = "auditoria.caminho";

    private AuditoriaLoggerFactory() {
    }

    /**
     * Le auditoria.properties do diretorio de execucao; na ausencia,
     * usa a modalidade padrao SQLITE em auditoria.db.
     */
    public static ILogger criarLoggerConfigurado() {
        Properties props = new Properties();
        Path arquivo = Path.of(ARQUIVO_CONFIGURACAO);

        if (Files.exists(arquivo)) {
            try (InputStream in = new FileInputStream(arquivo.toFile())) {
                props.load(in);
            } catch (IOException e) {
                throw new AuditoriaIndisponivelException(
                        "Não foi possível ler a configuração de auditoria", e);
            }
        }

        String modalidade = props.getProperty(CHAVE_MODALIDADE, "SQLITE");
        String caminho = props.getProperty(CHAVE_CAMINHO, caminhoPadrao(modalidade));
        return criarLogger(modalidade, caminho);
    }

    public static ILogger criarLogger(String modalidade, String caminho) {
        if (modalidade == null || modalidade.isBlank()) {
            throw new IllegalArgumentException("Modalidade de auditoria deve ser informada");
        }

        String valor = modalidade.trim().toUpperCase(Locale.ROOT);

        if ("SQLITE".equals(valor)) {
            return new SqliteLogger(caminho);
        }

        try {
            TipoLogger tipo = TipoLogger.valueOf(valor);
            return LoggerFactory.criarLogger(tipo, caminho);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Modalidade de auditoria inválida: " + modalidade
                    + " (esperado JSONL, CSV, XML ou SQLITE)");
        }
    }

    private static String caminhoPadrao(String modalidade) {
        return switch (modalidade.trim().toUpperCase(Locale.ROOT)) {
            case "CSV" -> "auditoria.csv";
            case "XML" -> "auditoria.xml";
            case "JSONL" -> "auditoria.jsonl";
            default -> "auditoria.db";
        };
    }
}
