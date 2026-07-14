package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IProdutoView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Cadastro de produto (Figura 8): codigo, nome, categoria em lista
 * controlada, preco unitario e quantidade inicial em estoque.
 */
public class ProdutoDialog extends JDialog implements IProdutoView {

    private final JTextField campoCodigo = new JTextField(10);
    private final JTextField campoNome = new JTextField(30);
    private final JComboBox<String> comboCategoria = new JComboBox<>();
    private final JTextField campoPreco = new JTextField(10);
    private final JTextField campoQuantidade = new JTextField(10);
    private final JButton botaoSalvar = new JButton("Salvar");
    private final JButton botaoCancelar = new JButton("Cancelar");

    public ProdutoDialog() {
        setTitle("Produto");
        setModal(true);
        montarTela();
    }

    private void montarTela() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do Produto"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        adicionarLinha(painel, gbc, 0, "Código", campoCodigo, false);
        adicionarLinha(painel, gbc, 1, "Nome", campoNome, true);
        adicionarLinha(painel, gbc, 2, "Categoria", comboCategoria, false);
        adicionarLinha(painel, gbc, 3, "Preço unitário", campoPreco, false);
        adicionarLinha(painel, gbc, 4, "Quantidade inicial em estoque", campoQuantidade, false);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botoes.add(botaoSalvar);
        botoes.add(botaoCancelar);

        setLayout(new BorderLayout(8, 8));
        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(botaoSalvar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 300);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void adicionarLinha(JPanel painel, GridBagConstraints gbc, int linha,
            String rotulo, JComponent campo, boolean expandir) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(new JLabel(rotulo), gbc);
        gbc.gridx = 1;
        gbc.fill = expandir ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        gbc.weightx = expandir ? 1 : 0;
        painel.add(campo, gbc);
    }

    @Override
    public String getCodigo() {
        return campoCodigo.getText().trim();
    }

    @Override
    public String getNome() {
        return campoNome.getText().trim();
    }

    @Override
    public String getCategoria() {
        Object valor = comboCategoria.getSelectedItem();
        return valor == null ? "" : valor.toString();
    }

    @Override
    public String getPrecoUnitario() {
        return campoPreco.getText().trim();
    }

    @Override
    public String getQuantidadeInicial() {
        return campoQuantidade.getText().trim();
    }

    @Override
    public void setCodigo(String codigo) {
        campoCodigo.setText(codigo);
    }

    @Override
    public void setNome(String nome) {
        campoNome.setText(nome);
    }

    @Override
    public void setCategoria(String categoria) {
        comboCategoria.setSelectedItem(categoria);
    }

    @Override
    public void setPrecoUnitario(String preco) {
        campoPreco.setText(preco);
    }

    @Override
    public void setQuantidadeInicial(String quantidade) {
        campoQuantidade.setText(quantidade);
    }

    @Override
    public void setCategoriasDisponiveis(List<String> categorias) {
        comboCategoria.removeAllItems();
        for (String categoria : categorias) {
            comboCategoria.addItem(categoria);
        }
        comboCategoria.setSelectedIndex(-1);
    }

    @Override
    public void setModoEdicao(boolean edicao) {
        campoCodigo.setEnabled(!edicao);
        campoQuantidade.setEnabled(!edicao);
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Produto", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Produto",
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
