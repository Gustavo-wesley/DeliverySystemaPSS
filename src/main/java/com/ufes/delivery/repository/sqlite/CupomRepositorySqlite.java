package com.ufes.delivery.repository.sqlite;

import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.repository.ICupomRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacao SQLite do repositorio de cupons. Somente cupons
 * ativos sao retornados para aplicacao (US09).
 */
public class CupomRepositorySqlite implements ICupomRepository {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DatabaseManager db;

    public CupomRepositorySqlite(DatabaseManager db) {
        this.db = Objects.requireNonNull(db, "DatabaseManager deve ser informado");
    }

    @Override
    public Optional<CupomDescontoPedido> buscarCupom(String codigo) {
        return db.executarEmTransacao(conexao -> {
            String sql = "SELECT * FROM cupom WHERE lower(codigo) = ? AND ativo = 1";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setString(1, codigo.toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new CupomDescontoPedido(
                            rs.getString("codigo"),
                            rs.getDouble("percentual"),
                            LocalDateTime.parse(rs.getString("inicio"), FORMATO),
                            LocalDateTime.parse(rs.getString("fim"), FORMATO)));
                }
            }
        });
    }

    /**
     * Insercao utilitaria para carga inicial de cupons.
     */
    public void adicionarCupom(CupomDescontoPedido cupom) {
        db.executarEmTransacao(conexao -> {
            String sql = "INSERT OR IGNORE INTO cupom (codigo, percentual, inicio, fim, ativo) "
                    + "VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setString(1, cupom.getCodigo());
                ps.setDouble(2, cupom.getPercentual());
                ps.setString(3, cupom.getDataHoraInicio().format(FORMATO));
                ps.setString(4, cupom.getDataHoraFim().format(FORMATO));
                ps.executeUpdate();
            }
            return null;
        });
    }
}
