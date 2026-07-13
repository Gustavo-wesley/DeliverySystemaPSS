package com.ufes.delivery.repository.sqlite;

import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.repository.IClienteRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClienteRepositorySqlite implements IClienteRepository {

    private final DatabaseManager db;

    public ClienteRepositorySqlite(DatabaseManager db) {
        this.db = Objects.requireNonNull(db, "DatabaseManager deve ser informado");
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return db.executarEmTransacao(conexao -> {
            String sql = "INSERT INTO cliente (nome, cpf) VALUES (?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cliente.getNome());
                ps.setString(2, cliente.getCpf());
                ps.executeUpdate();

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (chaves.next()) {
                        cliente.setId(chaves.getLong(1));
                    }
                }
            }
            inserirEnderecos(conexao, cliente);
            return cliente;
        });
    }

    @Override
    public void atualizar(Cliente cliente) {
        db.executarEmTransacao(conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "UPDATE cliente SET nome = ?, cpf = ? WHERE id = ?")) {
                ps.setString(1, cliente.getNome());
                ps.setString(2, cliente.getCpf());
                ps.setLong(3, cliente.getId());
                ps.executeUpdate();
            }

            // substitui os enderecos na mesma transacao
            try (PreparedStatement ps = conexao.prepareStatement(
                    "DELETE FROM endereco WHERE cliente_id = ?")) {
                ps.setLong(1, cliente.getId());
                ps.executeUpdate();
            }
            inserirEnderecos(conexao, cliente);
            return null;
        });
    }

    private void inserirEnderecos(Connection conexao, Cliente cliente) throws SQLException {
        String sql = "INSERT INTO endereco (cliente_id, logradouro, numero, complemento, "
                + "bairro, cidade, uf, cep, padrao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        for (Endereco endereco : cliente.getEnderecos()) {
            try (PreparedStatement ps = conexao.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, cliente.getId());
                ps.setString(2, endereco.getLogradouro());
                ps.setString(3, endereco.getNumero());
                ps.setString(4, endereco.getComplemento());
                ps.setString(5, endereco.getBairro());
                ps.setString(6, endereco.getCidade());
                ps.setString(7, endereco.getUf());
                ps.setString(8, endereco.getCep());
                ps.setInt(9, endereco.isPadrao() ? 1 : 0);
                ps.executeUpdate();

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (chaves.next()) {
                        endereco.setId(chaves.getLong(1));
                    }
                }
            }
        }
    }

    @Override
    public void excluir(Long id) {
        db.executarEmTransacao(conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "DELETE FROM cliente WHERE id = ?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        List<Cliente> resultado = buscarVarios(
                "SELECT * FROM cliente WHERE id = ?", ps -> ps.setLong(1, id));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        List<Cliente> resultado = buscarVarios(
                "SELECT * FROM cliente WHERE cpf = ?", ps -> ps.setString(1, cpf));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<Cliente> buscarPorNome(String trecho) {
        return buscarVarios("SELECT * FROM cliente WHERE lower(nome) LIKE ? ORDER BY nome",
                ps -> ps.setString(1, "%" + trecho.toLowerCase() + "%"));
    }

    @Override
    public List<Cliente> buscarTodos() {
        return buscarVarios("SELECT * FROM cliente ORDER BY nome", ps -> {
        });
    }

    private List<Cliente> buscarVarios(String sql, PreparadorSql preparador) {
        return db.executarEmTransacao(conexao -> {
            List<Cliente> clientes = new ArrayList<>();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                preparador.preparar(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        clientes.add(mapear(conexao, rs));
                    }
                }
            }
            return clientes;
        });
    }

    private Cliente mapear(Connection conexao, ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("cpf"));

        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT * FROM endereco WHERE cliente_id = ? ORDER BY id")) {
            ps.setLong(1, cliente.getId());
            try (ResultSet enderecos = ps.executeQuery()) {
                while (enderecos.next()) {
                    cliente.adicionarEndereco(new Endereco(
                            enderecos.getLong("id"),
                            enderecos.getString("logradouro"),
                            enderecos.getString("numero"),
                            enderecos.getString("complemento"),
                            enderecos.getString("bairro"),
                            enderecos.getString("cidade"),
                            enderecos.getString("uf"),
                            enderecos.getString("cep"),
                            enderecos.getInt("padrao") == 1));
                }
            }
        }
        return cliente;
    }
}
