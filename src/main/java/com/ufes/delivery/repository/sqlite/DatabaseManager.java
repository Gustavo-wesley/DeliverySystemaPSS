package com.ufes.delivery.repository.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia a conexao com o banco SQLite do dominio (delivery.db),
 * cria o schema na inicializacao e oferece suporte a transacao unica.
 *
 * Nao e singleton: deve ser criado uma vez na composicao da aplicacao
 * e injetado por construtor nos repositorios.
 */
public class DatabaseManager {

    private final String url;

    public DatabaseManager(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            throw new IllegalArgumentException("Caminho do banco de dados deve ser informado");
        }
        this.url = "jdbc:sqlite:" + caminhoArquivo;
        criarSchema();
    }

    public Connection abrirConexao() throws SQLException {
        Connection conexao = DriverManager.getConnection(url);
        try (Statement st = conexao.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conexao;
    }

    /**
     * Executa a operacao dentro de uma transacao unica: commit no sucesso,
     * rollback em qualquer falha. Falha de validacao ou processamento
     * nao pode produzir persistencia parcial (regra transversal).
     */
    public <T> T executarEmTransacao(OperacaoTransacional<T> operacao) {
        try (Connection conexao = abrirConexao()) {
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (Exception e) {
                conexao.rollback();
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new IllegalStateException("Falha na operacao transacional: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha de acesso ao banco de dados", e);
        }
    }

    private void criarSchema() {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS usuario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                senha_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                perfil TEXT NOT NULL,
                situacao TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS cliente (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cpf TEXT NOT NULL UNIQUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS endereco (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL REFERENCES cliente(id) ON DELETE CASCADE,
                logradouro TEXT NOT NULL,
                numero TEXT NOT NULL,
                complemento TEXT,
                bairro TEXT NOT NULL,
                cidade TEXT NOT NULL,
                uf TEXT NOT NULL,
                cep TEXT NOT NULL,
                padrao INTEGER NOT NULL DEFAULT 0
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS produto (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo INTEGER NOT NULL UNIQUE,
                nome TEXT NOT NULL,
                categoria TEXT NOT NULL,
                preco_unitario REAL NOT NULL,
                estoque_atual INTEGER NOT NULL CHECK (estoque_atual >= 0)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS movimentacao_estoque (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produto_id INTEGER NOT NULL REFERENCES produto(id),
                tipo TEXT NOT NULL,
                quantidade INTEGER NOT NULL,
                data TEXT NOT NULL,
                motivo TEXT,
                nota_fiscal TEXT,
                usuario_id INTEGER NOT NULL REFERENCES usuario(id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS pedido (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                cliente_id INTEGER NOT NULL REFERENCES cliente(id),
                endereco_id INTEGER NOT NULL REFERENCES endereco(id),
                estado TEXT NOT NULL,
                data TEXT NOT NULL,
                data_conclusao TEXT,
                cupom_codigo TEXT,
                taxa_entrega REAL NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS item_pedido (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
                produto_id INTEGER NOT NULL REFERENCES produto(id),
                quantidade INTEGER NOT NULL,
                valor_unitario REAL NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS cupom (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                percentual REAL NOT NULL,
                inicio TEXT NOT NULL,
                fim TEXT NOT NULL,
                ativo INTEGER NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS pagamento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL REFERENCES pedido(id),
                resultado TEXT NOT NULL,
                forma TEXT NOT NULL,
                id_transacao TEXT UNIQUE,
                valor_pago REAL NOT NULL,
                data_hora TEXT NOT NULL,
                prazo_entrega TEXT
            )
            """
        };

        try (Connection conexao = abrirConexao(); Statement st = conexao.createStatement()) {
            for (String comando : ddl) {
                st.execute(comando);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao criar o schema do banco de dados", e);
        }
    }
}
