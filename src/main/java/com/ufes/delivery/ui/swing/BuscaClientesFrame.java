package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IBuscaClientesView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
 * Busca de clientes (Figura 4): criterio Nome ou CPF, resultados
 * em tabela com Nome e CPF.
 */
public class BuscaClientesFrame extends JFrame implements IBuscaClientesView {

    private final JComboBox<String> comboCriterio =
            new JComboBox<>(new String[]{CRITERIO_NOME, CRITERIO_CPF});
    private final JTextField campoValor = new JTextField(22);
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoVisualizar = new JButton("Visualizar");
    private final JButton botaoFechar = new JButton("Fechar");

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final List<Long> idsDasLinhas = new ArrayList<>();

    public BuscaClientesFrame() {
        super("Clientes");

        modelo = new DefaultTableModel(new Object[]{"Nome", "CPF"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private void montarTela() {
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Clientes"));
        painelBusca.add(new JLabel("Buscar por"));
        painelBusca.add(comboCriterio);
        painelBusca.add(new JLabel("Valor"));
        painelBusca.add(campoValor);
        painelBusca.add(botaoBuscar);

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
        setSize(620, 430);
        setLocationRelativeTo(null);
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
    public void mostrarResultados(List<LinhaCliente> linhas) {
        modelo.setRowCount(0);
        idsDasLinhas.clear();
        for (LinhaCliente linha : linhas) {
            idsDasLinhas.add(linha.id());
            modelo.addRow(new Object[]{linha.nome(), linha.cpf()});
        }
    }

    @Override
    public Long getIdSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return null;
        }
        return idsDasLinhas.get(linha);
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Clientes", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Clientes",
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
}
