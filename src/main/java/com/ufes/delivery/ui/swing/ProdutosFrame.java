package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IProdutosView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Busca de produtos (Figura 7): criterio de busca, resultados com
 * acao Visualizar por linha e botoes Novo, Visualizar e Fechar.
 */
public class ProdutosFrame extends JFrame implements IProdutosView {

    private static final int COLUNA_ACAO = 5;

    private final JComboBox<String> comboCriterio = new JComboBox<>(
            new String[]{CRITERIO_NOME, CRITERIO_CODIGO, CRITERIO_CATEGORIA});
    private final JTextField campoValor = new JTextField(22);
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoVisualizar = new JButton("Visualizar");
    private final JButton botaoFechar = new JButton("Fechar");

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final List<Long> idsDasLinhas = new ArrayList<>();

    private Consumer<Long> aoVisualizarLinha;

    public ProdutosFrame() {
        super("Produtos");

        modelo = new DefaultTableModel(new Object[]{
            "Código", "Nome", "Categoria", "Preço unitário", "Estoque atual", "Ação"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return coluna == COLUNA_ACAO;
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private void montarTela() {
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Produtos"));
        painelBusca.add(new JLabel("Buscar por"));
        painelBusca.add(comboCriterio);
        painelBusca.add(new JLabel("Valor"));
        painelBusca.add(campoValor);
        painelBusca.add(botaoBuscar);

        configurarColunaAcao();

        JPanel painelResultados = new JPanel(new BorderLayout());
        painelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));
        painelResultados.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        painelBotoes.add(botaoNovo);
        painelBotoes.add(botaoVisualizar);
        painelBotoes.add(botaoFechar);

        setLayout(new BorderLayout(8, 8));
        add(painelBusca, BorderLayout.NORTH);
        add(painelResultados, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(botaoBuscar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 440);
        setLocationRelativeTo(null);
    }

    private void configurarColunaAcao() {
        tabela.setRowHeight(24);
        tabela.getColumnModel().getColumn(COLUNA_ACAO).setCellRenderer(
                (JTable t, Object valor, boolean sel, boolean foco, int linha, int coluna)
                -> new JButton("Visualizar"));

        DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox()) {
            private final JButton botao = new JButton("Visualizar");

            {
                botao.addActionListener(e -> {
                    fireEditingStopped();
                    int linha = tabela.getSelectedRow();
                    // a linha acionada, sem usar dados de outra linha
                    if (linha >= 0 && aoVisualizarLinha != null) {
                        aoVisualizarLinha.accept(idsDasLinhas.get(linha));
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
    }

    @Override
    public String getCriterioBusca() {
        return (String) comboCriterio.getSelectedItem();
    }

    @Override
    public String getValorBusca() {
        return campoValor.getText().trim();
    }

    @Override
    public void mostrarResultados(List<LinhaProduto> linhas) {
        if (tabela.isEditing()) {
            tabela.getCellEditor().cancelCellEditing();
        }
        modelo.setRowCount(0);
        idsDasLinhas.clear();
        for (LinhaProduto linha : linhas) {
            idsDasLinhas.add(linha.id());
            modelo.addRow(new Object[]{
                linha.codigo(), linha.nome(), linha.categoria(),
                linha.precoUnitario(), linha.estoqueAtual(), "Visualizar"});
        }
    }

    @Override
    public Long getIdSelecionado() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : idsDasLinhas.get(linha);
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Produtos", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Produtos",
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
    public void setAoBuscar(Runnable acao) {
        botaoBuscar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoNovo(Runnable acao) {
        botaoNovo.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoVisualizar(Runnable acao) {
        botaoVisualizar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoFechar(Runnable acao) {
        botaoFechar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoVisualizarLinha(Consumer<Long> acao) {
        this.aoVisualizarLinha = acao;
    }
}
