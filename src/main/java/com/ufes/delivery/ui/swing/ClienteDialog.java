package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IClienteView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Cadastro de cliente (Figura 5): dados do cliente e tabela com ate
 * tres enderecos de entrega, com a coluna "Padrao" em botao de radio
 * (apenas um endereco padrao por vez).
 */
public class ClienteDialog extends JDialog implements IClienteView {

    private static final int LINHAS_ENDERECO = 3;
    private static final int COLUNA_PADRAO = 0;

    private final JTextField campoNome = new JTextField(30);
    private final JTextField campoCpf = new JTextField(30);
    private final JButton botaoSalvar = new JButton("Salvar");
    private final JButton botaoCancelar = new JButton("Cancelar");

    private final DefaultTableModel modelo;
    private final JTable tabela;
    // guarda pra nao entrar em loop quando o proprio listener desmarca as outras linhas
    private boolean ajustandoPadrao = false;

    public ClienteDialog() {
        setTitle("Cliente");
        setModal(true);

        modelo = new DefaultTableModel(new Object[]{
            "Padrão", "Logradouro", "Número", "Complemento", "Bairro",
            "Cidade", "UF", "CEP"}, LINHAS_ENDERECO) {
            @Override
            public Class<?> getColumnClass(int coluna) {
                return coluna == COLUNA_PADRAO ? Boolean.class : String.class;
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private void montarTela() {
        JPanel painelDados = new JPanel(new GridBagLayout());
        painelDados.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painelDados.add(new JLabel("Nome"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelDados.add(campoNome, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelDados.add(new JLabel("CPF"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelDados.add(campoCpf, gbc);

        configurarColunaPadrao();
        tabela.setRowHeight(24);
        tabela.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JPanel painelEnderecos = new JPanel(new BorderLayout());
        painelEnderecos.setBorder(BorderFactory.createTitledBorder("Endereços de Entrega"));
        painelEnderecos.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoCancelar);

        setLayout(new BorderLayout(8, 8));
        add(painelDados, BorderLayout.NORTH);
        add(painelEnderecos, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 420);
        setLocationRelativeTo(null);
    }

    private void configurarColunaPadrao() {
        tabela.getColumnModel().getColumn(COLUNA_PADRAO).setMaxWidth(60);

        // radio na celula: renderer proprio e editor baseado em checkbox
        tabela.getColumnModel().getColumn(COLUNA_PADRAO).setCellRenderer(
                (JTable t, Object valor, boolean sel, boolean foco, int linha, int coluna) -> {
                    JRadioButton radio = new JRadioButton();
                    radio.setSelected(Boolean.TRUE.equals(valor));
                    radio.setHorizontalAlignment(SwingConstants.CENTER);
                    return radio;
                });
        tabela.getColumnModel().getColumn(COLUNA_PADRAO)
                .setCellEditor(new DefaultCellEditor(new JCheckBox()) {
                    private final JRadioButton radio = new JRadioButton();

                    {
                        radio.setHorizontalAlignment(SwingConstants.CENTER);
                        radio.addActionListener(e -> stopCellEditing());
                    }

                    @Override
                    public Object getCellEditorValue() {
                        return radio.isSelected();
                    }

                    @Override
                    public Component getTableCellEditorComponent(JTable t, Object valor,
                            boolean selecionado, int linha, int coluna) {
                        radio.setSelected(Boolean.TRUE.equals(valor));
                        return radio;
                    }
                });

        // marcar uma linha como padrao desmarca as demais
        modelo.addTableModelListener(e -> {
            if (ajustandoPadrao || e.getColumn() != COLUNA_PADRAO || e.getFirstRow() < 0) {
                return;
            }
            if (Boolean.TRUE.equals(modelo.getValueAt(e.getFirstRow(), COLUNA_PADRAO))) {
                ajustandoPadrao = true;
                try {
                    for (int i = 0; i < modelo.getRowCount(); i++) {
                        if (i != e.getFirstRow()) {
                            modelo.setValueAt(Boolean.FALSE, i, COLUNA_PADRAO);
                        }
                    }
                } finally {
                    ajustandoPadrao = false;
                }
            }
        });
    }

    @Override
    public String getNome() {
        return campoNome.getText().trim();
    }

    @Override
    public String getCpf() {
        return campoCpf.getText().trim();
    }

    @Override
    public List<LinhaEndereco> getEnderecos() {
        if (tabela.isEditing()) {
            tabela.getCellEditor().stopCellEditing();
        }
        List<LinhaEndereco> linhas = new ArrayList<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            linhas.add(new LinhaEndereco(
                    Boolean.TRUE.equals(modelo.getValueAt(i, COLUNA_PADRAO)),
                    texto(i, 1), texto(i, 2), texto(i, 3), texto(i, 4),
                    texto(i, 5), texto(i, 6), texto(i, 7)));
        }
        return linhas;
    }

    private String texto(int linha, int coluna) {
        Object valor = modelo.getValueAt(linha, coluna);
        return valor == null ? "" : valor.toString().trim();
    }

    @Override
    public void setNome(String nome) {
        campoNome.setText(nome);
    }

    @Override
    public void setCpf(String cpf) {
        campoCpf.setText(cpf);
    }

    @Override
    public void setEnderecos(List<LinhaEndereco> linhas) {
        ajustandoPadrao = true;
        try {
            for (int i = 0; i < LINHAS_ENDERECO; i++) {
                if (i < linhas.size()) {
                    LinhaEndereco l = linhas.get(i);
                    modelo.setValueAt(l.padrao(), i, COLUNA_PADRAO);
                    modelo.setValueAt(l.logradouro(), i, 1);
                    modelo.setValueAt(l.numero(), i, 2);
                    modelo.setValueAt(l.complemento(), i, 3);
                    modelo.setValueAt(l.bairro(), i, 4);
                    modelo.setValueAt(l.cidade(), i, 5);
                    modelo.setValueAt(l.uf(), i, 6);
                    modelo.setValueAt(l.cep(), i, 7);
                } else {
                    for (int c = 0; c < modelo.getColumnCount(); c++) {
                        modelo.setValueAt(c == COLUNA_PADRAO ? Boolean.FALSE : "", i, c);
                    }
                }
            }
        } finally {
            ajustandoPadrao = false;
        }
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Cliente", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Cliente",
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
    public void setAoSalvar(Runnable acao) {
        botaoSalvar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoCancelar(Runnable acao) {
        botaoCancelar.addActionListener(e -> acao.run());
    }
}
