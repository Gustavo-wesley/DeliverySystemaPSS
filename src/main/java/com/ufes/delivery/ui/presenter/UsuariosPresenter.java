package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.auditoria.AuditoriaIndisponivelException;
import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.perfil.PerfilUsuario;
import com.ufes.delivery.model.perfil.Perfis;
import com.ufes.delivery.service.GestaoUsuariosService;
import com.ufes.delivery.ui.view.IUsuariosView;
import java.util.List;
import java.util.Objects;

/**
 * Presenter da gestao de usuarios e autorizacoes (US03). As regras
 * (guard de administrador, lote em transacao unica) ficam no service.
 */
public class UsuariosPresenter {

    private final IUsuariosView view;
    private final GestaoUsuariosService gestaoService;
    private final INavegador navegador;

    public UsuariosPresenter(IUsuariosView view, GestaoUsuariosService gestaoService,
            INavegador navegador) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.gestaoService = Objects.requireNonNull(gestaoService,
                "Serviço de gestão deve ser informado");
        this.navegador = Objects.requireNonNull(navegador, "Navegador deve ser informado");

        view.setAoBuscar(this::buscar);
        view.setAoAutorizar(this::autorizar);
        view.setAoDesautorizar(this::desautorizar);
        view.setAoExcluir(this::excluir);
        view.setAoNovo(navegador::abrirCadastroUsuario);
        view.setAoFechar(view::fechar);
        view.setAoAlterarPerfil(this::alterarPerfil);
    }

    public void iniciar() {
        List<String> nomes = Perfis.disponiveis().stream()
                .map(PerfilUsuario::getNome)
                .toList();
        view.setPerfisDisponiveis(nomes);

        try {
            // abre listando todos os usuarios cadastrados
            mostrar(gestaoService.buscarPorNome(""));
            view.abrir();
        } catch (ValidacaoException e) {
            // sem perfil administrativo a tela nem abre
            view.mostrarErro(e.getMessage());
        }
    }

    private void buscar() {
        try {
            List<Usuario> encontrados = gestaoService.buscarPorNome(view.getNomeBusca());
            if (encontrados.isEmpty()) {
                view.mostrarInformacao("Nenhum usuário encontrado para a busca informada");
            }
            mostrar(encontrados);
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void autorizar() {
        executarLote(() -> gestaoService.autorizar(view.getIdsSelecionados()));
    }

    private void desautorizar() {
        executarLote(() -> gestaoService.desautorizar(view.getIdsSelecionados()));
    }

    private void excluir() {
        List<Long> ids = view.getIdsSelecionados();
        if (ids.isEmpty()) {
            view.mostrarErro(GestaoUsuariosService.MSG_SELECAO_OBRIGATORIA);
            return;
        }
        if (!view.confirmarExclusao(ids.size())) {
            return;
        }
        try {
            gestaoService.excluir(ids);
            recarregar();
        } catch (ValidacaoException | AuditoriaIndisponivelException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void alterarPerfil(Long usuarioId, String nomePerfil) {
        try {
            gestaoService.definirPerfil(usuarioId, nomePerfil);
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
        }
        recarregar();
    }

    private void executarLote(Runnable operacao) {
        try {
            operacao.run();
            recarregar();
        } catch (ValidacaoException | AuditoriaIndisponivelException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void recarregar() {
        try {
            mostrar(gestaoService.buscarPorNome(view.getNomeBusca()));
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void mostrar(List<Usuario> usuarios) {
        List<IUsuariosView.LinhaUsuario> linhas = usuarios.stream()
                .map(u -> new IUsuariosView.LinhaUsuario(
                        u.getId(),
                        u.getUsername(),
                        u.getNome(),
                        u.getSituacao() == SituacaoUsuario.AUTORIZADO,
                        u.getPerfil().getNome(),
                        u.getSituacao().getDescricao()))
                .toList();
        view.mostrarUsuarios(linhas);
    }
}
