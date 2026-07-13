package com.ufes.delivery.repository.sqlite;

import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.model.EstadoPedido;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.model.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PedidoRepositorySqlite implements com.ufes.delivery.repository.IPedidoRepository {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DatabaseManager db;

    public PedidoRepositorySqlite(DatabaseManager db) {
        this.db = Objects.requireNonNull(db, "DatabaseManager deve ser informado");
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        return db.executarEmTransacao(conexao -> {
            String codigo = proximoCodigo(conexao);
            pedido.setCodigo(codigo);

            String sql = "INSERT INTO pedido (codigo, cliente_id, endereco_id, estado, data, "
                    + "data_conclusao, cupom_codigo, taxa_entrega) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                preencherCampos(ps, pedido);
                ps.executeUpdate();

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (chaves.next()) {
                        pedido.setId(chaves.getLong(1));
                    }
                }
            }

            inserirItens(conexao, pedido);
            return pedido;
        });
    }

    @Override
    public void atualizar(Pedido pedido) {
        db.executarEmTransacao(conexao -> {
            atualizarNaConexao(conexao, pedido);

            try (PreparedStatement ps = conexao.prepareStatement(
                    "DELETE FROM item_pedido WHERE pedido_id = ?")) {
                ps.setLong(1, pedido.getId());
                ps.executeUpdate();
            }
            inserirItens(conexao, pedido);
            return null;
        });
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        List<Pedido> resultado = buscarVarios(
                "SELECT * FROM pedido WHERE id = ?", ps -> ps.setLong(1, id));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<Pedido> buscarPorCodigo(String codigo) {
        List<Pedido> resultado = buscarVarios(
                "SELECT * FROM pedido WHERE codigo = ?", ps -> ps.setString(1, codigo));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<Pedido> buscarPorDataDeOperacao(LocalDate dataOperacao) {
        String dia = dataOperacao.format(FORMATO_DATA);
        return buscarVarios("SELECT * FROM pedido WHERE substr(data, 1, 10) = ? "
                + "OR data_conclusao = ? ORDER BY codigo", ps -> {
            ps.setString(1, dia);
            ps.setString(2, dia);
        });
    }

    @Override
    public void registrarPagamentoReprovado(Pagamento pagamento) {
        db.executarEmTransacao(conexao -> {
            inserirPagamento(conexao, pagamento);
            return null;
        });
    }

    @Override
    public void confirmarPagamentoAprovado(Pedido pedido, Pagamento pagamento) {
        // baixa de estoque + estado do pedido + pagamento: transacao unica
        db.executarEmTransacao(conexao -> {
            for (Item item : pedido.getItens()) {
                if (item.getProduto() != null) {
                    ProdutoRepositorySqlite.atualizarNaConexao(conexao, item.getProduto());
                }
            }
            atualizarNaConexao(conexao, pedido);
            inserirPagamento(conexao, pagamento);
            return null;
        });
    }

    private String proximoCodigo(Connection conexao) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT COALESCE(MAX(CAST(codigo AS INTEGER)), 1000) + 1 FROM pedido");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.valueOf(rs.getInt(1));
        }
    }

    private void preencherCampos(PreparedStatement ps, Pedido pedido) throws SQLException {
        ps.setString(1, pedido.getCodigo());
        ps.setLong(2, pedido.getCliente().getId());
        ps.setLong(3, pedido.getEnderecoEntrega().getId());
        ps.setString(4, pedido.getEstado().name());
        ps.setString(5, pedido.getData().format(FORMATO_DATA_HORA));
        ps.setString(6, pedido.getDataConclusao() == null
                ? null : pedido.getDataConclusao().format(FORMATO_DATA));
        ps.setString(7, pedido.getCupomAplicado()
                .map(CupomDescontoPedido::getCodigo).orElse(null));
        ps.setDouble(8, pedido.getTaxaEntrega());
    }

    private void atualizarNaConexao(Connection conexao, Pedido pedido) throws SQLException {
        String sql = "UPDATE pedido SET codigo = ?, cliente_id = ?, endereco_id = ?, estado = ?, "
                + "data = ?, data_conclusao = ?, cupom_codigo = ?, taxa_entrega = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencherCampos(ps, pedido);
            ps.setLong(9, pedido.getId());
            ps.executeUpdate();
        }
    }

    private void inserirItens(Connection conexao, Pedido pedido) throws SQLException {
        String sql = "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, valor_unitario) "
                + "VALUES (?, ?, ?, ?)";
        for (Item item : pedido.getItens()) {
            if (item.getProduto() == null) {
                throw new SQLException("Item sem produto vinculado: " + item.getNome());
            }
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, pedido.getId());
                ps.setLong(2, item.getProduto().getId());
                ps.setInt(3, item.getQuantidade());
                ps.setDouble(4, item.getValorUnitario());
                ps.executeUpdate();
            }
        }
    }

    private void inserirPagamento(Connection conexao, Pagamento pagamento) throws SQLException {
        String sql = "INSERT INTO pagamento (pedido_id, resultado, forma, id_transacao, "
                + "valor_pago, data_hora, prazo_entrega) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pagamento.getPedido().getId());
            ps.setString(2, pagamento.getResultado().getNome());
            ps.setString(3, pagamento.getForma().getNome());
            ps.setString(4, pagamento.getIdTransacao());
            ps.setDouble(5, pagamento.getValorPago());
            ps.setString(6, pagamento.getDataHora().format(FORMATO_DATA_HORA));
            ps.setString(7, pagamento.getPrazoEntrega() == null
                    ? null : pagamento.getPrazoEntrega().format(FORMATO_DATA_HORA));
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    pagamento.setId(chaves.getLong(1));
                }
            }
        }
    }

    private List<Pedido> buscarVarios(String sql, PreparadorSql preparador) {
        return db.executarEmTransacao(conexao -> {
            List<Pedido> pedidos = new ArrayList<>();
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                preparador.preparar(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pedidos.add(mapear(conexao, rs));
                    }
                }
            }
            return pedidos;
        });
    }

    private Pedido mapear(Connection conexao, ResultSet rs) throws SQLException {
        Cliente cliente = buscarCliente(conexao, rs.getLong("cliente_id"));

        Pedido pedido = new Pedido(
                LocalDateTime.parse(rs.getString("data"), FORMATO_DATA_HORA), cliente);
        pedido.setId(rs.getLong("id"));
        pedido.setCodigo(rs.getString("codigo"));

        long enderecoId = rs.getLong("endereco_id");
        cliente.getEnderecos().stream()
                .filter(e -> e.getId() != null && e.getId() == enderecoId)
                .findFirst()
                .ifPresent(pedido::definirEnderecoEntrega);

        String dataConclusao = rs.getString("data_conclusao");
        pedido.mudarEstado(EstadoPedido.porDescricao(rs.getString("estado")),
                dataConclusao == null ? null : LocalDate.parse(dataConclusao, FORMATO_DATA));

        carregarItens(conexao, pedido);
        carregarCupom(conexao, pedido, rs.getString("cupom_codigo"));
        return pedido;
    }

    private Cliente buscarCliente(Connection conexao, long clienteId) throws SQLException {
        Cliente cliente;
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT * FROM cliente WHERE id = ?")) {
            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Cliente do pedido não encontrado: " + clienteId);
                }
                cliente = new Cliente(rs.getLong("id"), rs.getString("nome"), rs.getString("cpf"));
            }
        }

        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT * FROM endereco WHERE cliente_id = ? ORDER BY id")) {
            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cliente.adicionarEndereco(new Endereco(
                            rs.getLong("id"),
                            rs.getString("logradouro"),
                            rs.getString("numero"),
                            rs.getString("complemento"),
                            rs.getString("bairro"),
                            rs.getString("cidade"),
                            rs.getString("uf"),
                            rs.getString("cep"),
                            rs.getInt("padrao") == 1));
                }
            }
        }
        return cliente;
    }

    private void carregarItens(Connection conexao, Pedido pedido) throws SQLException {
        String sql = "SELECT ip.quantidade, ip.valor_unitario AS valor_praticado, p.* "
                + "FROM item_pedido ip "
                + "JOIN produto p ON p.id = ip.produto_id WHERE ip.pedido_id = ? ORDER BY ip.id";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, pedido.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produto produto = ProdutoRepositorySqlite.mapear(rs);
                    pedido.adicionarItem(new Item(produto, rs.getInt("quantidade"),
                            rs.getDouble("valor_praticado")));
                }
            }
        }
    }

    private void carregarCupom(Connection conexao, Pedido pedido, String cupomCodigo)
            throws SQLException {
        if (cupomCodigo == null || cupomCodigo.isBlank()) {
            return;
        }
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT * FROM cupom WHERE codigo = ?")) {
            ps.setString(1, cupomCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pedido.setCupomAplicado(new CupomDescontoPedido(
                            rs.getString("codigo"),
                            rs.getDouble("percentual"),
                            LocalDateTime.parse(rs.getString("inicio"), FORMATO_DATA_HORA),
                            LocalDateTime.parse(rs.getString("fim"), FORMATO_DATA_HORA)));
                }
            }
        }
    }
}
