package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.ICadastroUsuarioView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Cadastro de usuario (US02). View passiva em dialogo modal.
 */
public class CadastroUsuarioDialog extends JDialog implements ICadastroUsuarioView {

    private final JTextField campoNome = new JTextField(22);
    private final JTextField campoUsuario = new JTextField(22);
    private final JPasswordField campoSenha = new JPasswordField(22);
    private final JPasswordField campoConfirmacao = new JPasswordField(22);
    private final JButton botaoSalvar = new JButton("Salvar");
    private final JButton botaoCancelar = new JButton("Cancelar");

    public CadastroUsuarioDialog() {
        setTitle("Cadastro de Usuário");
        setModal(true);
        montarTela();
    }

    private void montarTela() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        adicionarLinha(painel, gbc, 0, "Nome", campoNome);
        adicionarLinha(painel, gbc, 1, "Nome de usuário", campoUsuario);
        adicionarLinha(painel, gbc, 2, "Senha", campoSenha);
        adicionarLinha(painel, gbc, 3, "Confirmar senha", campoConfirmacao);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botoes.add(botaoSalvar);
        botoes.add(botaoCancelar);

        setLayout(new BorderLayout(8, 8));
        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(botaoSalvar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void adicionarLinha(JPanel painel, GridBagConstraints gbc, int linha,
            String rotulo, javax.swing.JComponent campo) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(new JLabel(rotulo), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painel.add(campo, gbc);
    }

    @Override
    public String getNome() {
        return campoNome.getText().trim();
    }

    @Override
    public String getNomeUsuario() {
        return campoUsuario.getText().trim();
    }

    @Override
    public char[] getSenha() {
        return campoSenha.getPassword();
    }

    @Override
    public char[] getConfirmacaoSenha() {
        return campoConfirmacao.getPassword();
    }

    @Override
    public void limparSenhas() {
        campoSenha.setText("");
        campoConfirmacao.setText("");
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Cadastro de Usuário",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Cadastro de Usuário",
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
