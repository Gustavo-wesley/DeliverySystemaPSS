package com.ufes.delivery.repository.sqlite;

import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.estoque.MovimentacaoEstoque;
import com.ufes.delivery.repository.IProdutoRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProdutoRepositorySqlite implements IProdutoRepository {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DatabaseManager db;

    public ProdutoRepositorySqlite(DatabaseManager db) {
        this.db = Objects.requireNonNull(db, "DatabaseManager deve ser informado");
    }

    @Override
    public Produto salvar(Produto produto) {
        return db.executarEmTransacao(conexao -> {
            String sql = "INSERT INTO produto (codigo, nome, categoria, preco_unitario, estoque_atual) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, produto.getCodigo());
                ps.setString(2, produto.getNome());
                ps.setString(3, produto.getCategoria());
                ps.setDouble(4, produto.getPrecoUnitario());
                ps.setInt(5, produto.getEstoqueAtual());
                ps.executeUpdate();

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (chaves.next()) {
                        produto.setId(chaves.getLong(1));
                    }
                }
            }
            return produto;
        });
    }

    @Override
    public void atualizar(Produto produto) {
        db.executarEmTransacao(conexao -> {
            atualizarNaConexao(conexao, produto);
            return null;
        });
    }

    static void atualizarNaConexao(Connection conexao, Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, categoria = ?, preco_unitario = ?, "
                + "estoque_atual = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getCategoria());
            ps.setDouble(3, produto.getPrecoUnitario());
            ps.setInt(4, produto.getEstoqueAtual());
            ps.setLong(5, produto.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        List<Produto> resultado = buscarVarios(
                "SELECT * FROM produto WHERE id = ?", ps -> ps.setLong(1, id));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<Produto> buscarPorCodigo(int codigo) {
        List<Produto> resultado = buscarVarios(
                "SELECT * FROM produto WHERE codigo = ?", ps -> ps.setInt(1, codigo));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<Produto> buscarPorNome(String trecho) {
        return buscarVarios("SELECT * FROM produto WHERE lower(nome) LIKE ? ORDER BY codigo",
                ps -> ps.setString(1, "%" + trecho.toLowerCase() + "%"));
    }

    @Override
    public List<Produto> buscarPorCategoria(String categoria) {
        return buscarVarios("SELECT * FROM produto WHERE lower(categoria) = ? ORDER BY codigo",
                ps -> ps.setString(1, categoria.toLowerCase()));
    }

    @Override
    public List<Produto> buscarTodos() {
        return buscarVarios("SELECT * FROM produto ORDER BY codigo", ps -> {
        });
    }

    @Override
    public List<String> buscarCategorias() {
        return db.executarEmTransacao(conexao -> {
            List<String> categorias = new ArrayList<>();
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT DISTINCT categoria FROM produto ORDER BY categoria");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(rs.getString(1));
                }
            }
            return categorias;
        });
    }

    @Override
    public void registrarMovimentacao(MovimentacaoEstoque movimentacao) {
        db.executarEmTransacao(conexao -> {
            // novo estoque + registro da movimentacao em transacao unica
            atualizarNaConexao(conexao, movimentacao.getProduto());

            String sql = "INSERT INTO movimentacao_estoque "
                    + "(produto_id, tipo, quantidade, data, motivo, nota_fiscal, usuario_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, movimentacao.getProduto().getId());
                ps.setString(2, movimentacao.getTipo().getNome());
                ps.setInt(3, movimentacao.getQuantidade());
                ps.setString(4, movimentacao.getData().format(FORMATO_DATA));
                ps.setString(5, movimentacao.getMotivo());
                ps.setString(6, movimentacao.getNotaFiscal());
                ps.setLong(7, movimentacao.getResponsavel().getId());
                ps.executeUpdate();
            }
            return null;
        });
    }

    private List<Produto> buscarVarios(String sql, PreparadorSql preparador) {
        return db.executarEmTransacao(conexao -> {
            List<Produto> produtos = new ArrayList<>();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                preparador.preparar(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        produtos.add(mapear(rs));
                    }
                }
            }
            return produtos;
        });
    }

    static Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getLong("id"),
                rs.getInt("codigo"),
                rs.getString("nome"),
                rs.getString("categoria"),
                rs.getDouble("preco_unitario"),
                rs.getInt("estoque_atual"));
    }
}
