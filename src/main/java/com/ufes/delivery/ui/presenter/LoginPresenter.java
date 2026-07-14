package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.auditoria.AuditoriaIndisponivelException;
import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.service.AutenticacaoService;
import com.ufes.delivery.ui.view.ILoginView;
import java.util.Arrays;
import java.util.Objects;

/**
 * Presenter da tela de login (US01). Orquestra a autenticacao e a
 * navegacao; nao conhece Swing.
 */
public class LoginPresenter {

    private final ILoginView view;
    private final AutenticacaoService autenticacaoService;
    private final INavegador navegador;

    public LoginPresenter(ILoginView view, AutenticacaoService autenticacaoService,
            INavegador navegador) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.autenticacaoService = Objects.requireNonNull(autenticacaoService,
                "Serviço de autenticação deve ser informado");
        this.navegador = Objects.requireNonNull(navegador, "Navegador deve ser informado");

        view.setAoAcessar(this::acessar);
        view.setAoCancelar(view::fechar);
        view.setAoCadastrarUsuario(navegador::abrirCadastroUsuario);
    }

    public void iniciar() {
        view.abrir();
    }

    private void acessar() {
        char[] senha = view.getSenha();
        try {
            autenticacaoService.autenticar(view.getNomeUsuario(), senha);
            view.fechar();
            navegador.abrirTelaInicio();
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
            view.limparSenha();
        } catch (AuditoriaIndisponivelException e) {
            view.mostrarErro(e.getMessage());
            view.limparSenha();
        } finally {
            // limpa a copia da senha da memoria por seguranca
            if (senha != null) {
                Arrays.fill(senha, '\0');
            }
        }
    }
}
