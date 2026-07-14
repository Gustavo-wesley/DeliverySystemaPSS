package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IMovimentacaoEstoqueView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Movimentacao de estoque (Figura 9): busca e selecao de produto,
 * campos de leitura, dados da movimentacao e previa do resultado.
 */
public class MovimentacaoEstoqueDialog extends JDialog implements IMovimentacaoEstoqueView {

    private final JTextField campoBusca = new JTextField(30);
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoSelecionar = new JButton("Selecionar");

    private final JTextField campoProduto = new JTextField(28);
    private final JTextField campoEstoqueAtual = new JTextField(10);

    private final JTextField campoData = new JTextField(10);
    private final JComboBox<String> comboTipo = new JComboBox<>();
    private final JTextField campoQuantidade = new JTextField(8);
    private final JTextField campoMotivo = new JTextField(22);
    private final JTextField campoPrevia = new JTextField(10);
    private final JTextField campoNotaFiscal = new JTextField(14);
    private final JButton botaoConfirmar = new JButton("Confirmar movimentação");
    private final JButton botaoCancelar = new JButton("Cancelar");

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final List<Long> idsDasLinhas = new ArrayList<>();

    private Runnable aoDadosAlterados;

    public MovimentacaoEstoqueDialog() {
        setTitle("Movimentação de Estoque");
        setModal(true);

        modelo = new DefaultTableModel(
                new Object[]{"Código", "Produto", "Categoria", "Estoque atual"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private void montarTela() {
        // ---- busca de produtos ----
        JPanel painelBusca = new JPanel(new BorderLayout(6, 6));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Produtos"));

        JPanel linhaBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaBusca.add(new JLabel("Buscar produto"));
        linhaBusca.add(campoBusca);
        linhaBusca.add(botaoBuscar);
        painelBusca.add(linhaBusca, BorderLayout.NORTH);

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setPreferredSize(new java.awt.Dimension(560, 110));
        painelBusca.add(rolagem, BorderLayout.CENTER);

        JPanel linhaSelecionar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        linhaSelecionar.add(botaoSelecionar);
        painelBusca.add(linhaSelecionar, BorderLayout.SOUTH);

        // ---- produto selecionado (somente leitura) ----
        JPanel painelSelecionado = new JPanel(new GridBagLayout());
        painelSelecionado.setBorder(BorderFactory.createTitledBorder("Produto Selecionado"));
        campoProduto.setEditable(false);
        campoEstoqueAtual.setEditable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        painelSelecionado.add(new JLabel("Produto"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelSelecionado.add(campoProduto, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelSelecionado.add(new JLabel("Quantidade atual em estoque"), gbc);
        gbc.gridx = 1;
        painelSelecionado.add(campoEstoqueAtual, gbc);

        // ---- movimentacao ----
        JPanel painelMovimentacao = new JPanel(new GridBagLayout());
        painelMovimentacao.setBorder(BorderFactory.createTitledBorder("Movimentação"));
        campoPrevia.setEditable(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 8, 3, 8);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        painelMovimentacao.add(new JLabel("Data da movimentação"), g);
        g.gridx = 1;
        painelMovimentacao.add(campoData, g);
        g.gridx = 2;
        painelMovimentacao.add(new JLabel("Tipo de movimentação"), g);
        g.gridx = 3;
        painelMovimentacao.add(comboTipo, g);

        g.gridx = 0;
        g.gridy = 1;
        painelMovimentacao.add(new JLabel("Quantidade a movimentar"), g);
        g.gridx = 1;
        painelMovimentacao.add(campoQuantidade, g);
        g.gridx = 2;
        painelMovimentacao.add(new JLabel("Motivo do ajuste"), g);
        g.gridx = 3;
        painelMovimentacao.add(campoMotivo, g);

        g.gridx = 0;
        g.gridy = 2;
        painelMovimentacao.add(new JLabel("Estoque após movimentação (prévia)"), g);
        g.gridx = 1;
        painelMovimentacao.add(campoPrevia, g);
        g.gridx = 2;
        painelMovimentacao.add(new JLabel("Nota fiscal de entrada"), g);
        g.gridx = 3;
        painelMovimentacao.add(campoNotaFiscal, g);

        JLabel aviso = new JLabel("<html>Pré-visualização; a atualização definitiva ocorrerá após a "
                + "confirmação da movimentação.<br>Ajustes de estoque requerem motivo. "
                + "Entradas requerem número da nota fiscal.</html>");
        aviso.setForeground(new Color(0, 90, 160));
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 4;
        painelMovimentacao.add(aviso, g);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botoes.add(botaoConfirmar);
        botoes.add(botaoCancelar);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(painelBusca);
        centro.add(painelSelecionado);
        centro.add(painelMovimentacao);

        setLayout(new BorderLayout(8, 8));
        add(centro, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        // quantidade e tipo atualizam a previa
        comboTipo.addActionListener(e -> dispararDadosAlterados());
        campoQuantidade.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                dispararDadosAlterados();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                dispararDadosAlterados();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                dispararDadosAlterados();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680, 560);
        setLocationRelativeTo(null);
    }

    private void dispararDadosAlterados() {
        if (aoDadosAlterados != null) {
            aoDadosAlterados.run();
        }
    }

    @Override
    public String getValorBuscaProduto() {
        return campoBusca.getText().trim();
    }

    @Override
    public void mostrarResultadosBusca(List<LinhaProdutoBusca> linhas) {
        modelo.setRowCount(0);
        idsDasLinhas.clear();
        for (LinhaProdutoBusca linha : linhas) {
            idsDasLinhas.add(linha.id());
            modelo.addRow(new Object[]{
                linha.codigo(), linha.nome(), linha.categoria(), linha.estoqueAtual()});
        }
    }

    @Override
    public Long getIdProdutoSelecionadoNaBusca() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : idsDasLinhas.get(linha);
    }

    @Override
    public void setProdutoSelecionado(String nome, String estoqueAtual) {
        campoProduto.setText(nome);
        campoEstoqueAtual.setText(estoqueAtual);
    }

    @Override
    public String getDataMovimentacao() {
        return campoData.getText().trim();
    }

    @Override
    public void setDataMovimentacao(String data) {
        campoData.setText(data);
    }

    @Override
    public String getTipoMovimentacao() {
        Object valor = comboTipo.getSelectedItem();
        return valor == null ? "" : valor.toString();
    }

    @Override
    public void setTiposDisponiveis(List<String> tipos) {
        comboTipo.removeAllItems();
        for (String tipo : tipos) {
            comboTipo.addItem(tipo);
        }
    }

    @Override
    public String getQuantidade() {
        return campoQuantidade.getText().trim();
    }

    @Override
    public String getMotivo() {
        return campoMotivo.getText().trim();
    }

    @Override
    public String getNotaFiscal() {
        return campoNotaFiscal.getText().trim();
    }

    @Override
    public void limparCamposMovimentacao() {
        campoQuantidade.setText("");
        campoMotivo.setText("");
        campoNotaFiscal.setText("");
    }

    @Override
    public void setPrevia(String valor) {
        campoPrevia.setText(valor);
    }

    @Override
    public void setMotivoHabilitado(boolean habilitado) {
        campoMotivo.setEnabled(habilitado);
    }

    @Override
    public void setNotaFiscalHabilitada(boolean habilitada) {
        campoNotaFiscal.setEnabled(habilitada);
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Movimentação de Estoque",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Movimentação de Estoque",
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
    public void setAoSelecionarProduto(Runnable acao) {
        botaoSelecionar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoDadosAlterados(Runnable acao) {
        this.aoDadosAlterados = acao;
    }

    @Override
    public void setAoConfirmar(Runnable acao) {
        botaoConfirmar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoCancelar(Runnable acao) {
        botaoCancelar.addActionListener(e -> acao.run());
    }
}
