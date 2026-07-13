package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.perfil.PerfilUsuario;
import com.ufes.delivery.model.perfil.Perfis;
import com.ufes.delivery.repository.IUsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Operacoes administrativas sobre usuarios (US03): buscar, definir perfil,
 * autorizar, desautorizar e excluir. As operacoes em lote sao aplicadas
 * em transacao unica pelo repositorio.
 */
public class GestaoUsuariosService {

    public static final String MSG_SELECAO_OBRIGATORIA =
            "Selecione pelo menos um usuário";

    private final IUsuarioRepository usuarioRepository;
    private final GuardaAcessoAdministrativo guarda;

    public GestaoUsuariosService(IUsuarioRepository usuarioRepository,
            GuardaAcessoAdministrativo guarda) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository,
                "Repositório de usuários deve ser informado");
        this.guarda = Objects.requireNonNull(guarda, "Guarda de acesso deve ser informado");
    }

    public List<Usuario> buscarPorNome(String trecho) {
        guarda.exigirAdministrador();

        if (trecho == null || trecho.trim().isEmpty()) {
            return usuarioRepository.buscarTodos();
        }

        String valor = trecho.trim();
        if (valor.length() < 2 || valor.length() > 120) {
            throw new ValidacaoException("Nome para busca deve conter de 2 a 120 caracteres");
        }

        return usuarioRepository.buscarPorNome(valor);
    }

    public void definirPerfil(Long usuarioId, String nomePerfil) {
        guarda.exigirAdministrador();

        Usuario usuario = buscarObrigatorio(usuarioId);
        PerfilUsuario perfil = Perfis.porNome(nomePerfil);
        usuario.setPerfil(perfil);
        usuarioRepository.atualizar(usuario);
    }

    public List<Usuario> autorizar(List<Long> ids) {
        return aplicarEmLote(ids, Usuario::autorizar);
    }

    public List<Usuario> desautorizar(List<Long> ids) {
        return aplicarEmLote(ids, Usuario::desautorizar);
    }

    public void excluir(List<Long> ids) {
        guarda.exigirAdministrador();
        exigirSelecao(ids);

        for (Long id : ids) {
            buscarObrigatorio(id);
            usuarioRepository.excluir(id);
        }
    }

    private List<Usuario> aplicarEmLote(List<Long> ids, java.util.function.Consumer<Usuario> acao) {
        guarda.exigirAdministrador();
        exigirSelecao(ids);

        List<Usuario> alterados = new ArrayList<>();
        for (Long id : ids) {
            Usuario usuario = buscarObrigatorio(id);
            acao.accept(usuario);
            alterados.add(usuario);
        }

        usuarioRepository.atualizarTodos(alterados);
        return alterados;
    }

    private void exigirSelecao(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidacaoException(MSG_SELECAO_OBRIGATORIA);
        }
    }

    private Usuario buscarObrigatorio(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new ValidacaoException("Usuário não encontrado: " + id));
    }
}
