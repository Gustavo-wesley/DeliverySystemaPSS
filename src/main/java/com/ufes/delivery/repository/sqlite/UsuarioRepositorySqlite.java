package com.ufes.delivery.repository.sqlite;

import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.perfil.Perfis;
import com.ufes.delivery.repository.IUsuarioRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UsuarioRepositorySqlite implements IUsuarioRepository {

    private final DatabaseManager db;

    public UsuarioRepositorySqlite(DatabaseManager db) {
        this.db = Objects.requireNonNull(db, "DatabaseManager deve ser informado");
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return db.executarEmTransacao(conexao -> {
            String sql = "INSERT INTO usuario (nome, username, senha_hash, salt, perfil, situacao) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNome());
                ps.setString(2, usuario.getUsername());
                ps.setString(3, usuario.getSenhaHash());
                ps.setString(4, usuario.getSalt());
                ps.setString(5, usuario.getPerfil().getNome());
                ps.setString(6, usuario.getSituacao().name());
                ps.executeUpdate();

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (chaves.next()) {
                        usuario.setId(chaves.getLong(1));
                    }
                }
            }
            return usuario;
        });
    }

    @Override
    public void atualizar(Usuario usuario) {
        db.executarEmTransacao(conexao -> {
            atualizarNaConexao(conexao, usuario);
            return null;
        });
    }

    @Override
    public void atualizarTodos(List<Usuario> usuarios) {
        db.executarEmTransacao(conexao -> {
            for (Usuario usuario : usuarios) {
                atualizarNaConexao(conexao, usuario);
            }
            return null;
        });
    }

    private void atualizarNaConexao(Connection conexao, Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nome = ?, senha_hash = ?, salt = ?, "
                + "perfil = ?, situacao = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getSenhaHash());
            ps.setString(3, usuario.getSalt());
            ps.setString(4, usuario.getPerfil().getNome());
            ps.setString(5, usuario.getSituacao().name());
            ps.setLong(6, usuario.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void excluir(Long id) {
        db.executarEmTransacao(conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "DELETE FROM usuario WHERE id = ?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return buscarUm("SELECT * FROM usuario WHERE username = ?",
                ps -> ps.setString(1, username));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return buscarUm("SELECT * FROM usuario WHERE id = ?",
                ps -> ps.setLong(1, id));
    }

    @Override
    public List<Usuario> buscarPorNome(String trecho) {
        String filtro = "%" + trecho.toLowerCase() + "%";
        return buscarVarios("SELECT * FROM usuario WHERE lower(nome) LIKE ? "
                + "OR lower(username) LIKE ? ORDER BY username", ps -> {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
        });
    }

    @Override
    public List<Usuario> buscarTodos() {
        return buscarVarios("SELECT * FROM usuario ORDER BY username", ps -> {
        });
    }

    @Override
    public long contar() {
        return db.executarEmTransacao(conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM usuario");
                    ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });
    }

    private Optional<Usuario> buscarUm(String sql, PreparadorSql preparador) {
        List<Usuario> resultado = buscarVarios(sql, preparador);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    private List<Usuario> buscarVarios(String sql, PreparadorSql preparador) {
        return db.executarEmTransacao(conexao -> {
            List<Usuario> usuarios = new ArrayList<>();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                preparador.preparar(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        usuarios.add(mapear(rs));
                    }
                }
            }
            return usuarios;
        });
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("username"),
                rs.getString("senha_hash"),
                rs.getString("salt"),
                Perfis.porNome(rs.getString("perfil")),
                SituacaoUsuario.porDescricao(rs.getString("situacao")));
    }
}
