package com.ufes.delivery.auditoria;

import com.ufes.logger.ILogger;
import com.ufes.model.LogEntry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

/**
 * Modalidade SQLite de auditoria, criada no lado do Delivery
 * (a Log-library permanece intacta). Implementa a mesma interface
 * ILogger dos loggers de arquivo da lib e grava em banco proprio
 * (auditoria.db), separado do banco de dominio.
 */
public class SqliteLogger implements ILogger {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String url;

    public SqliteLogger(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            throw new IllegalArgumentException("Caminho do banco de auditoria deve ser informado");
        }
        this.url = "jdbc:sqlite:" + caminhoArquivo;
        criarTabela();
    }

    @Override
    public void registrar(LogEntry logEntry) {
        if (logEntry == null) {
            throw new IllegalArgumentException("LogEntry deve ser informado");
        }

        String sql = "INSERT INTO log_auditoria "
                + "(nome_usuario, data, hora, codigo_pedido, nome_operacao, nome_cliente, mensagem) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexao = DriverManager.getConnection(url);
                PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, logEntry.getNomeUsuario());
            ps.setString(2, logEntry.getData().format(FORMATO_DATA));
            ps.setString(3, logEntry.getHora().format(FORMATO_HORA));
            ps.setString(4, logEntry.getCodigoPedido());
            ps.setString(5, logEntry.getNomeOperacao());
            ps.setString(6, logEntry.getNomeCliente());
            ps.setString(7, logEntry.getMensagem());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao registrar auditoria em SQLite", e);
        }
    }

    private void criarTabela() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS log_auditoria (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome_usuario TEXT NOT NULL,
                    data TEXT NOT NULL,
                    hora TEXT NOT NULL,
                    codigo_pedido TEXT NOT NULL,
                    nome_operacao TEXT NOT NULL,
                    nome_cliente TEXT NOT NULL,
                    mensagem TEXT
                )
                """;
        try (Connection conexao = DriverManager.getConnection(url);
                Statement st = conexao.createStatement()) {
            st.execute(ddl);
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao preparar o banco de auditoria", e);
        }
    }
}
