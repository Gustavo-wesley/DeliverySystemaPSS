package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.AuditoriaPublisher;
import com.ufes.delivery.auditoria.EventoAuditoria;
import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.repository.IUsuarioRepository;
import com.ufes.delivery.seguranca.GeradorHashSenha;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Autenticacao (US01). A mensagem de credencial invalida nao informa
 * se a falha ocorreu no nome de usuario ou na senha. Usuario pendente
 * ou nao autorizado recebe mensagem propria, conforme cenario 5.
 */
public class AutenticacaoService {

    public static final String MSG_CREDENCIAIS_INVALIDAS = "Credenciais inválidas";
    public static final String MSG_ACESSO_PENDENTE =
            "O acesso depende de autorização administrativa";

    private final IUsuarioRepository usuarioRepository;
    private final GeradorHashSenha geradorHashSenha;
    private final SessaoService sessaoService;
    private final AuditoriaPublisher auditoria;

    public AutenticacaoService(IUsuarioRepository usuarioRepository,
            GeradorHashSenha geradorHashSenha, SessaoService sessaoService) {
        this(usuarioRepository, geradorHashSenha, sessaoService, new AuditoriaPublisher());
    }

    public AutenticacaoService(IUsuarioRepository usuarioRepository,
            GeradorHashSenha geradorHashSenha, SessaoService sessaoService,
            AuditoriaPublisher auditoria) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository,
                "Repositório de usuários deve ser informado");
        this.geradorHashSenha = Objects.requireNonNull(geradorHashSenha,
                "Gerador de hash deve ser informado");
        this.sessaoService = Objects.requireNonNull(sessaoService,
                "Serviço de sessão deve ser informado");
        this.auditoria = Objects.requireNonNull(auditoria,
                "Publicador de auditoria deve ser informado");
    }

    public Usuario autenticar(String username, char[] senha) {
        Usuario.validarUsername(username);

        if (senha == null || senha.length < 8 || senha.length > 64) {
            throw new ValidacaoException("Senha deve conter de 8 a 64 caracteres");
        }

        Optional<Usuario> encontrado = usuarioRepository.buscarPorUsername(username);

        // a senha nunca aparece nos eventos de auditoria
        if (encontrado.isEmpty()) {
            auditoria.publicar(EventoAuditoria.semPedido("Autenticação",
                    "Usuário " + username, "Rejeitado", "Credenciais inválidas"));
            throw new ValidacaoException(MSG_CREDENCIAIS_INVALIDAS);
        }

        Usuario usuario = encontrado.get();

        if (!geradorHashSenha.verificar(senha, usuario.getSalt(), usuario.getSenhaHash())) {
            auditoria.publicar(EventoAuditoria.semPedido("Autenticação",
                    "Usuário " + username, "Rejeitado", "Credenciais inválidas"));
            throw new ValidacaoException(MSG_CREDENCIAIS_INVALIDAS);
        }

        if (!usuario.podeIniciarSessao()) {
            auditoria.publicar(EventoAuditoria.semPedido("Autenticação",
                    "Usuário " + username, "Bloqueado",
                    "Situação " + usuario.getSituacao().getDescricao()));
            throw new ValidacaoException(MSG_ACESSO_PENDENTE);
        }

        sessaoService.iniciarSessao(usuario, LocalDateTime.now());
        auditoria.publicar(EventoAuditoria.semPedido("Autenticação",
                "Usuário " + username, "Sucesso",
                "Sessão iniciada com perfil " + usuario.getPerfil().getNome()));
        return usuario;
    }
}
