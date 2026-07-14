package com.ufes.delivery.ui.view;

/**
 * View passiva do cadastro de usuario (US02).
 */
public interface ICadastroUsuarioView {

    String getNome();

    String getNomeUsuario();

    char[] getSenha();

    char[] getConfirmacaoSenha();

    void limparSenhas();

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoSalvar(Runnable acao);

    void setAoCancelar(Runnable acao);
}
