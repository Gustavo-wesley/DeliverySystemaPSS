package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IPedidoView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Tela de pedido (Figuras 10 e 11): cliente, endereco de entrega,
 * itens com quantidade editavel e exclusao por menu de contexto,
 * cupom de desconto e resumo financeiro somente leitura.
 */
public class PedidoFrame extends JFrame implements IPedidoView {

    private static final int COLUNA_QUANTIDADE = 3;

    private final JComboBox<String> comboCliente = new JComboBox<>();
    private final JButton botaoNovoCliente = new JButton("Novo Cliente");
    private final JComboBox<String> comboEndereco = new JComboBox<>();

    private final JTextField campoProduto = new JTextField(18);
    private final JTextField campoQuantidadeItem = new JTextField(4);
    private final JButton botaoAdicionarItem = new JButton("Adicionar item");

    private final JTextField campoCupom = new JTextField(12);
    private final JButton botaoAplicarCupom = new JButton("Aplicar");
    private final JLabel valorTotalDescontos = new JLabel("R$ 0,00", SwingConstants.RIGHT);
    private final JLabel valorDescontoTaxa = new JLabel("R$ 0,00", SwingConstants.RIGHT);
    private final JLabel valorTaxaFinal = new JLabel("R$ 0,00", SwingConstants.RIGHT);
    private final JLabel valorTotalPedido = new JLabel("R$ 0,00", SwingConstants.RIGHT);

    private final JButton botaoPagar = new JButton("Pagar");
    private final JButton botaoCancelar = new JButton("Cancelar");

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final List<Long> idsClientes = new ArrayList<>();

    private Runnable aoSelecionarCliente;
    private Runnable aoSelecionarEndereco;
    private IntConsumer aoExcluirItem;
    private BiConsumer<Integer, String> aoAlterarQuantidade;
    // evita disparar eventos enquanto o Presenter recarrega os dados
    private boolean carregando = false;

    public PedidoFrame() {
        super("Pedido");

        modelo = new DefaultTableModel(new Object[]{
            "Categoria", "Item", "Preço unitário", "Quantidade", "Preço total"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                // so a quantidade pode ser editada; valores sao calculados
                return coluna == COLUNA_QUANTIDADE;
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private void montarTela() {
        JPanel painelDados = new JPanel(new GridBagLayout());
        painelDados.setBorder(BorderFactory.createTitledBorder("Dados do Pedido"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painelDados.add(new JLabel("Cliente"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelDados.add(comboCliente, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelDados.add(botaoNovoCliente, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painelDados.add(new JLabel("Endereço de entrega"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelDados.add(comboEndereco, gbc);
        gbc.gridwidth = 1;

        JPanel painelItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        painelItem.add(new JLabel("Produto (código ou nome)"));
        painelItem.add(campoProduto);
        painelItem.add(new JLabel("Qtde"));
        painelItem.add(campoQuantidadeItem);
        painelItem.add(botaoAdicionarItem);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelDados.add(painelItem, gbc);

        montarMenuDeContexto();
        montarEventosTabela();

        // ---- resumo financeiro (somente leitura) ----
        JPanel painelResumo = new JPanel(new GridBagLayout());
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(2, 8, 2, 8);
        r.anchor = GridBagConstraints.WEST;

        r.gridx = 0;
        r.gridy = 0;
        painelResumo.add(new JLabel("Cupom de desconto"), r);
        r.gridx = 1;
        painelResumo.add(campoCupom, r);
        r.gridx = 2;
        painelResumo.add(botaoAplicarCupom, r);

        adicionarLinhaResumo(painelResumo, r, 1, "Total de descontos", valorTotalDescontos);
        adicionarLinhaResumo(painelResumo, r, 2, "Desconto na taxa de entrega", valorDescontoTaxa);
        adicionarLinhaResumo(painelResumo, r, 3, "Taxa de entrega final", valorTaxaFinal);

        JLabel rotuloTotal = new JLabel("Total do pedido");
        rotuloTotal.setFont(rotuloTotal.getFont().deriveFont(Font.BOLD));
        valorTotalPedido.setFont(valorTotalPedido.getFont().deriveFont(Font.BOLD));
        adicionarLinhaResumo(painelResumo, r, 4, null, null);
        r.gridx = 0;
        r.gridy = 5;
        painelResumo.add(rotuloTotal, r);
        r.gridx = 1;
        r.gridwidth = 2;
        r.fill = GridBagConstraints.HORIZONTAL;
        painelResumo.add(valorTotalPedido, r);
        r.gridwidth = 1;
        r.fill = GridBagConstraints.NONE;

        JPanel linhaResumo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        linhaResumo.add(painelResumo);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botoes.add(botaoPagar);
        botoes.add(botaoCancelar);

        JPanel painelSul = new JPanel(new BorderLayout());
        painelSul.add(linhaResumo, BorderLayout.NORTH);
        painelSul.add(botoes, BorderLayout.SOUTH);

        setLayout(new BorderLayout(8, 8));
        add(painelDados, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelSul, BorderLayout.SOUTH);

        comboCliente.addActionListener(e -> {
            if (!carregando && aoSelecionarCliente != null) {
                aoSelecionarCliente.run();
            }
        });
        comboEndereco.addActionListener(e -> {
            if (!carregando && aoSelecionarEndereco != null) {
                aoSelecionarEndereco.run();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);
    }

    private void adicionarLinhaResumo(JPanel painel, GridBagConstraints r, int linha,
            String rotulo, JLabel valor) {
        if (rotulo == null) {
            return; // linha separadora simbolica
        }
        r.gridx = 0;
        r.gridy = linha;
        painel.add(new JLabel(rotulo), r);
        r.gridx = 1;
        r.gridwidth = 2;
        r.fill = GridBagConstraints.HORIZONTAL;
        painel.add(valor, r);
        r.gridwidth = 1;
        r.fill = GridBagConstraints.NONE;
    }

    private void montarMenuDeContexto() {
        // exclusao pela acao do menu apos clique com o botao direito (US09)
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemExcluir = new JMenuItem("Excluir");
        itemExcluir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha >= 0 && aoExcluirItem != null) {
                aoExcluirItem.accept(linha);
            }
        });
        menu.add(itemExcluir);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                exibirMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                exibirMenu(e);
            }

            private void exibirMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int linha = tabela.rowAtPoint(e.getPoint());
                if (linha >= 0) {
                    tabela.setRowSelectionInterval(linha, linha);
                    menu.show(tabela, e.getX(), e.getY());
                }
            }
        });
    }

    private void montarEventosTabela() {
        tabela.setRowHeight(24);
        tabela.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        modelo.addTableModelListener(e -> {
            if (carregando || e.getColumn() != COLUNA_QUANTIDADE || e.getFirstRow() < 0) {
                return;
            }
            if (aoAlterarQuantidade != null) {
                Object valor = modelo.getValueAt(e.getFirstRow(), COLUNA_QUANTIDADE);
                aoAlterarQuantidade.accept(e.getFirstRow(),
                        valor == null ? "" : valor.toString());
            }
        });
    }

    @Override
    public void setClientesDisponiveis(List<OpcaoCliente> clientes) {
        carregando = true;
        try {
            Long selecionadoAntes = getIdClienteSelecionado();
            comboCliente.removeAllItems();
            idsClientes.clear();
            for (OpcaoCliente cliente : clientes) {
                idsClientes.add(cliente.id());
                comboCliente.addItem(cliente.nome());
            }
            if (selecionadoAntes != null && idsClientes.contains(selecionadoAntes)) {
                comboCliente.setSelectedIndex(idsClientes.indexOf(selecionadoAntes));
            } else {
                comboCliente.setSelectedIndex(-1);
            }
        } finally {
            carregando = false;
        }
    }

    @Override
    public Long getIdClienteSelecionado() {
        int indice = comboCliente.getSelectedIndex();
        return indice < 0 ? null : idsClientes.get(indice);
    }

    @Override
    public void setEnderecosDisponiveis(List<String> enderecos) {
        carregando = true;
        try {
            comboEndereco.removeAllItems();
            for (String endereco : enderecos) {
                comboEndereco.addItem(endereco);
            }
        } finally {
            carregando = false;
        }
    }

    @Override
    public int getIndiceEnderecoSelecionado() {
        return comboEndereco.getSelectedIndex();
    }

    @Override
    public void setIndiceEnderecoSelecionado(int indice) {
        carregando = true;
        try {
            comboEndereco.setSelectedIndex(indice);
        } finally {
            carregando = false;
        }
    }

    @Override
    public void mostrarItens(List<LinhaItem> linhas) {
        carregando = true;
        try {
            if (tabela.isEditing()) {
                tabela.getCellEditor().cancelCellEditing();
            }
            modelo.setRowCount(0);
            for (LinhaItem linha : linhas) {
                modelo.addRow(new Object[]{
                    linha.categoria(), linha.nome(), linha.precoUnitario(),
                    linha.quantidade(), linha.precoTotal()});
            }
        } finally {
            carregando = false;
        }
    }

    @Override
    public String getProdutoParaAdicionar() {
        return campoProduto.getText().trim();
    }

    @Override
    public String getQuantidadeParaAdicionar() {
        return campoQuantidadeItem.getText().trim();
    }

    @Override
    public void limparCamposDeItem() {
        campoProduto.setText("");
        campoQuantidadeItem.setText("");
    }

    @Override
    public String getCupom() {
        return campoCupom.getText().trim();
    }

    @Override
    public void setTotalDescontos(String valor) {
        valorTotalDescontos.setText(valor);
    }

    @Override
    public void setDescontoTaxaEntrega(String valor) {
        valorDescontoTaxa.setText(valor);
    }

    @Override
    public void setTaxaEntregaFinal(String valor) {
        valorTaxaFinal.setText(valor);
    }

    @Override
    public void setTotalPedido(String valor) {
        valorTotalPedido.setText(valor);
    }

    @Override
    public boolean confirmarCancelamento() {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Há itens ou valores informados. Descartar as alterações do pedido?",
                "Pedido", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return resposta == JOptionPane.YES_OPTION;
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Pedido", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Pedido",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void abrir() {
        setVisible(true);
    }

    @Override
    public void fechar() {
        dispose();
    }

    @Override
    public void setAoSelecionarCliente(Runnable acao) {
        this.aoSelecionarCliente = acao;
    }

    @Override
    public void setAoNovoCliente(Runnable acao) {
        botaoNovoCliente.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoSelecionarEndereco(Runnable acao) {
        this.aoSelecionarEndereco = acao;
    }

    @Override
    public void setAoAdicionarItem(Runnable acao) {
        botaoAdicionarItem.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoExcluirItem(IntConsumer acao) {
        this.aoExcluirItem = acao;
    }

    @Override
    public void setAoAlterarQuantidade(BiConsumer<Integer, String> acao) {
        this.aoAlterarQuantidade = acao;
    }

    @Override
    public void setAoAplicarCupom(Runnable acao) {
        botaoAplicarCupom.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoPagar(Runnable acao) {
        botaoPagar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoCancelar(Runnable acao) {
        botaoCancelar.addActionListener(e -> acao.run());
    }
}
