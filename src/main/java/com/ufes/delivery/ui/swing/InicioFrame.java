package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IInicioView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Painel operacional (Figura 3): menu Operacao, data de operacao,
 * cartoes de metricas por estado, lista de pedidos com botao
 * Visualizar e barra de status do usuario logado.
 */
public class InicioFrame extends JFrame implements IInicioView {

    private static final int COLUNA_ACAO = 6;

    private final JMenuItem itemNovoPedido = new JMenuItem("Novo pedido");
    private final JMenuItem itemBuscarProdutos = new JMenuItem("Buscar produtos");
    private final JMenuItem itemNovoProduto = new JMenuItem("Novo produto");
    private final JMenuItem itemMovimentacao = new JMenuItem("Movimentação de estoque");
    private final JMenuItem itemNovoCliente = new JMenuItem("Novo cliente");
    private final JMenuItem itemBuscarClientes = new JMenuItem("Buscar clientes");
    private final JMenu menuAdministracao = new JMenu("Administração");
    private final JMenuItem itemGestaoUsuarios = new JMenuItem("Gestão de usuários");

    private final JLabel rotuloDataOperacao = new JLabel("", SwingConstants.CENTER);

    private final JLabel valorPedidosDoDia = novoValorMetrica();
    private final JLabel valorNovos = novoValorMetrica();
    private final JLabel valorAguardandoPagamento = novoValorMetrica();
    private final JLabel valorEmPreparo = novoValorMetrica();
    private final JLabel valorAguardandoEntrega = novoValorMetrica();
    private final JLabel valorEmTransito = novoValorMetrica();
    private final JLabel valorEntreguesHoje = novoValorMetrica();

    private final JLabel rotuloUsuario = new JLabel(" ");
    private final JLabel rotuloLogin = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel rotuloTipo = new JLabel(" ", SwingConstants.RIGHT);

    private final DefaultTableModel modelo;
    private final JTable tabela;

    private Consumer<String> aoVisualizarPedido;
    private Runnable aoAtualizar;
    // evita atualizar duas vezes na primeira exibicao da janela
    private boolean jaExibida = false;

    public InicioFrame() {
        super("Início");

        modelo = new DefaultTableModel(new Object[]{
            "Pedido", "Cliente", "Data do pedido", "Data de conclusão",
            "Estado do pedido", "Valor total", "Ação"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return coluna == COLUNA_ACAO; // so o botao Visualizar
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private static JLabel novoValorMetrica() {
        JLabel rotulo = new JLabel("0", SwingConstants.CENTER);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 22f));
        return rotulo;
    }

    private void montarTela() {
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuOperacao = new JMenu("Operação");
        menuOperacao.add(itemNovoPedido);
        menuOperacao.add(itemBuscarProdutos);
        menuOperacao.add(itemNovoProduto);
        menuOperacao.add(itemMovimentacao);
        menuOperacao.add(itemNovoCliente);
        menuOperacao.add(itemBuscarClientes);
        barraMenu.add(menuOperacao);
        menuAdministracao.add(itemGestaoUsuarios);
        barraMenu.add(menuAdministracao);
        setJMenuBar(barraMenu);

        rotuloDataOperacao.setFont(rotuloDataOperacao.getFont().deriveFont(Font.BOLD, 16f));
        rotuloDataOperacao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 120, 4, 120),
                BorderFactory.createLineBorder(java.awt.Color.GRAY)));

        JPanel painelMetricas = new JPanel(new GridLayout(2, 4, 10, 10));
        painelMetricas.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        painelMetricas.add(cartao("Pedidos do dia", valorPedidosDoDia));
        painelMetricas.add(cartao("Novos", valorNovos));
        painelMetricas.add(cartao("Aguardando pagamento", valorAguardandoPagamento));
        painelMetricas.add(cartao("Em preparo", valorEmPreparo));
        painelMetricas.add(cartao("Aguardando entrega", valorAguardandoEntrega));
        painelMetricas.add(cartao("Em trânsito", valorEmTransito));
        painelMetricas.add(cartao("Entregues hoje", valorEntreguesHoje));
        painelMetricas.add(new JPanel()); // celula vazia pra fechar a grade

        configurarColunaAcao();

        JPanel painelPedidos = new JPanel(new BorderLayout());
        painelPedidos.setBorder(BorderFactory.createTitledBorder("Lista de Pedidos"));
        painelPedidos.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelCentro = new JPanel(new BorderLayout());
        painelCentro.add(painelMetricas, BorderLayout.NORTH);
        painelCentro.add(painelPedidos, BorderLayout.CENTER);

        JPanel barraStatus = new JPanel(new GridLayout(1, 3));
        barraStatus.setBorder(BorderFactory.createEtchedBorder());
        barraStatus.add(rotuloUsuario);
        barraStatus.add(rotuloLogin);
        barraStatus.add(rotuloTipo);

        JPanel painelTopo = new JPanel();
        painelTopo.setLayout(new BoxLayout(painelTopo, BoxLayout.Y_AXIS));
        rotuloDataOperacao.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelTopo.add(Box.createVerticalStrut(4));
        painelTopo.add(rotuloDataOperacao);

        setLayout(new BorderLayout());
        add(painelTopo, BorderLayout.NORTH);
        add(painelCentro, BorderLayout.CENTER);
        add(barraStatus, BorderLayout.SOUTH);

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (!jaExibida) {
                    jaExibida = true; // a primeira carga o Presenter ja fez
                    return;
                }
                if (aoAtualizar != null) {
                    aoAtualizar.run();
                }
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
    }

    private JPanel cartao(String titulo, JLabel valor) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
        JLabel rotulo = new JLabel(titulo, SwingConstants.CENTER);
        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(valor, BorderLayout.CENTER);
        return painel;
    }

    private void configurarColunaAcao() {
        // renderer e editor de botao na coluna Acao (adaptado dos tutoriais de JTable)
        tabela.getColumnModel().getColumn(COLUNA_ACAO).setCellRenderer(
                (JTable t, Object valor, boolean sel, boolean foco, int linha, int coluna)
                -> new JButton("Visualizar"));

        DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox()) {
            private final JButton botao = new JButton("Visualizar");

            {
                botao.addActionListener(e -> {
                    fireEditingStopped();
                    int linha = tabela.getEditingRow() >= 0
                            ? tabela.getEditingRow() : tabela.getSelectedRow();
                    // fireEditingStopped ja encerrou a edicao, pega a linha selecionada
                    if (linha < 0) {
                        linha = tabela.getSelectedRow();
                    }
                    if (linha >= 0 && aoVisualizarPedido != null) {
                        aoVisualizarPedido.accept(
                                String.valueOf(modelo.getValueAt(linha, 0)));
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable t, Object valor,
                    boolean selecionado, int linha, int coluna) {
                return botao;
            }
        };
        tabela.getColumnModel().getColumn(COLUNA_ACAO).setCellEditor(editor);
        tabela.setRowHeight(24);
    }

    @Override
    public void setDataOperacao(String texto) {
        rotuloDataOperacao.setText(texto);
    }

    @Override
    public void setMetricaPedidosDoDia(String valor) {
        valorPedidosDoDia.setText(valor);
    }

    @Override
    public void setMetricaNovos(String valor) {
        valorNovos.setText(valor);
    }

    @Override
    public void setMetricaAguardandoPagamento(String valor) {
        valorAguardandoPagamento.setText(valor);
    }

    @Override
    public void setMetricaEmPreparo(String valor) {
        valorEmPreparo.setText(valor);
    }

    @Override
    public void setMetricaAguardandoEntrega(String valor) {
        valorAguardandoEntrega.setText(valor);
    }

    @Override
    public void setMetricaEmTransito(String valor) {
        valorEmTransito.setText(valor);
    }

    @Override
    public void setMetricaEntreguesHoje(String valor) {
        valorEntreguesHoje.setText(valor);
    }

    @Override
    public void mostrarPedidos(List<LinhaPedidoPainel> linhas) {
        if (tabela.isEditing()) {
            tabela.getCellEditor().cancelCellEditing();
        }
        modelo.setRowCount(0);
        for (LinhaPedidoPainel linha : linhas) {
            modelo.addRow(new Object[]{
                linha.codigo(), linha.nomeCliente(), linha.dataPedido(),
                linha.dataConclusao(), linha.estado(), linha.valorTotal(),
                "Visualizar"});
        }
    }

    @Override
    public void setUsuarioLogado(String texto) {
        rotuloUsuario.setText(texto);
    }

    @Override
    public void setInformacaoLogin(String texto) {
        rotuloLogin.setText(texto);
    }

    @Override
    public void setTipoPerfil(String texto) {
        rotuloTipo.setText(texto);
    }

    @Override
    public void setMenuAdministracaoVisivel(boolean visivel) {
        menuAdministracao.setVisible(visivel);
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Início", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Início",
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
    public void setAoNovoPedido(Runnable acao) {
        itemNovoPedido.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoBuscarProdutos(Runnable acao) {
        itemBuscarProdutos.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoNovoProduto(Runnable acao) {
        itemNovoProduto.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoMovimentacaoEstoque(Runnable acao) {
        itemMovimentacao.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoNovoCliente(Runnable acao) {
        itemNovoCliente.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoBuscarClientes(Runnable acao) {
        itemBuscarClientes.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoGestaoUsuarios(Runnable acao) {
        itemGestaoUsuarios.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoVisualizarPedido(Consumer<String> acao) {
        this.aoVisualizarPedido = acao;
    }

    @Override
    public void setAoAtualizar(Runnable acao) {
        this.aoAtualizar = acao;
    }
}
