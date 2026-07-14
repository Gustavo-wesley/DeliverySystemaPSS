package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.ILoginView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Tela de login (Figura 1). View passiva: so campos, botoes e repasse
 * dos eventos para o Presenter.
 */
public class LoginFrame extends JFrame implements ILoginView {

    private final JTextField campoUsuario = new JTextField(18);
    private final JPasswordField campoSenha = new JPasswordField(18);
    private final JButton botaoAcessar = new JButton("Acessar");
    private final JButton botaoCancelar = new JButton("Cancelar");
    private final JButton botaoCadastrar = new JButton("Cadastrar usuário");

    public LoginFrame() {
        super("Login");
        montarTela();
    }

    private void montarTela() {
        JPanel painelDados = new JPanel(new GridBagLayout());
        painelDados.setBorder(BorderFactory.createTitledBorder("Dados de Acesso"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painelDados.add(new JLabel("Nome de usuário"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelDados.add(campoUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painelDados.add(new JLabel("Senha"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        painelDados.add(campoSenha, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        painelBotoes.add(botaoAcessar);
        painelBotoes.add(botaoCancelar);
        painelBotoes.add(botaoCadastrar);

        setLayout(new BorderLayout(8, 8));
        add(painelDados, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(botaoAcessar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 210);
        setLocationRelativeTo(null);
        setResizable(false);
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
    public void limparSenha() {
        campoSenha.setText("");
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Login", JOptionPane.ERROR_MESSAGE);
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
    public void setAoAcessar(Runnable acao) {
        botaoAcessar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoCancelar(Runnable acao) {
        botaoCancelar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoCadastrarUsuario(Runnable acao) {
        botaoCadastrar.addActionListener(e -> acao.run());
    }
}
