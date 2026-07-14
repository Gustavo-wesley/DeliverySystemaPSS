package com.ufes.delivery.ui.view;

/**
 * View passiva da tela de login (US01, Figura 1). So expoe os campos
 * e o disparo dos eventos; nenhuma regra de negocio aqui.
 */
public interface ILoginView {

    String getNomeUsuario();

    char[] getSenha();

    void limparSenha();

    void mostrarErro(String mensagem);

    void abrir();

    void fechar();

    void setAoAcessar(Runnable acao);

    void setAoCancelar(Runnable acao);

    void setAoCadastrarUsuario(Runnable acao);
}
