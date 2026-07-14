package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.auditoria.AuditoriaIndisponivelException;
import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.service.CadastroUsuarioService;
import com.ufes.delivery.ui.view.ICadastroUsuarioView;
import java.util.Arrays;
import java.util.Objects;

/**
 * Presenter do cadastro de usuario (US02).
 */
public class CadastroUsuarioPresenter {

    private final ICadastroUsuarioView view;
    private final CadastroUsuarioService cadastroService;

    public CadastroUsuarioPresenter(ICadastroUsuarioView view,
            CadastroUsuarioService cadastroService) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.cadastroService = Objects.requireNonNull(cadastroService,
                "Serviço de cadastro deve ser informado");

        view.setAoSalvar(this::salvar);
        view.setAoCancelar(view::fechar);
    }

    public void iniciar() {
        view.abrir();
    }

    private void salvar() {
        char[] senha = view.getSenha();
        char[] confirmacao = view.getConfirmacaoSenha();
        try {
            if (!Arrays.equals(senha, confirmacao)) {
                view.mostrarErro("A confirmação não confere com a senha informada");
                view.limparSenhas();
                return;
            }

            Usuario salvo = cadastroService.cadastrar(view.getNome(), view.getNomeUsuario(), senha);

            // o primeiro usuario ja sai autorizado; os demais aguardam o admin
            if (salvo.podeIniciarSessao()) {
                view.mostrarInformacao("Usuário cadastrado com perfil "
                        + salvo.getPerfil().getNome() + " e acesso autorizado");
            } else {
                view.mostrarInformacao("Usuário cadastrado com situação Pendente. "
                        + "O acesso depende de autorização administrativa");
            }
            view.fechar();
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
            view.limparSenhas();
        } catch (AuditoriaIndisponivelException e) {
            view.mostrarErro(e.getMessage());
        } finally {
            if (senha != null) {
                Arrays.fill(senha, '\0');
            }
            if (confirmacao != null) {
                Arrays.fill(confirmacao, '\0');
            }
        }
    }
}
