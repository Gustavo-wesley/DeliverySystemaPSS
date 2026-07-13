package com.ufes.delivery.service;

import com.ufes.delivery.auditoria.AuditoriaPublisher;
import com.ufes.delivery.auditoria.EventoAuditoria;
import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.perfil.Administrador;
import com.ufes.delivery.model.perfil.Atendente;
import com.ufes.delivery.model.perfil.PerfilUsuario;
import com.ufes.delivery.repository.IUsuarioRepository;
import com.ufes.delivery.seguranca.GeradorHashSenha;
import java.util.Objects;

/**
 * Regras de cadastro de usuario (US02):
 * - primeiro usuario persistido recebe perfil Administrador e situacao Autorizado;
 * - cadastros posteriores recebem perfil Atendente e situacao Pendente;
 * - nome de usuario unico entre todos os cadastros, inclusive pendentes.
 *
 * Service de dominio testavel sem tela.
 */
public class CadastroUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final GeradorHashSenha geradorHashSenha;
    private final AuditoriaPublisher auditoria;

    public CadastroUsuarioService(IUsuarioRepository usuarioRepository,
            GeradorHashSenha geradorHashSenha) {
        this(usuarioRepository, geradorHashSenha, new AuditoriaPublisher());
    }

    public CadastroUsuarioService(IUsuarioRepository usuarioRepository,
            GeradorHashSenha geradorHashSenha, AuditoriaPublisher auditoria) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository,
                "Repositório de usuários deve ser informado");
        this.geradorHashSenha = Objects.requireNonNull(geradorHashSenha,
                "Gerador de hash deve ser informado");
        this.auditoria = Objects.requireNonNull(auditoria,
                "Publicador de auditoria deve ser informado");
    }

    public Usuario cadastrar(String nome, String username, char[] senha) {
        validarNome(nome);
        Usuario.validarUsername(username);
        validarSenha(senha);

        if (usuarioRepository.buscarPorUsername(username).isPresent()) {
            auditoria.publicar(EventoAuditoria.semPedido("Cadastro de usuário",
                    "Usuário " + username, "Rejeitado", "Nome de usuário já está em uso"));
            throw new ValidacaoException("Nome de usuário já está em uso");
        }

        boolean primeiroUsuario = usuarioRepository.contar() == 0;
        PerfilUsuario perfil = primeiroUsuario ? new Administrador() : new Atendente();
        SituacaoUsuario situacao = primeiroUsuario
                ? SituacaoUsuario.AUTORIZADO
                : SituacaoUsuario.PENDENTE;

        String salt = geradorHashSenha.gerarSalt();
        String hash = geradorHashSenha.gerarHash(senha, salt);

        Usuario usuario = new Usuario(null, nome, username, hash, salt, perfil, situacao);
        Usuario salvo = usuarioRepository.salvar(usuario);

        auditoria.publicar(EventoAuditoria.semPedido("Cadastro de usuário",
                "Usuário " + username, "Sucesso",
                "Perfil " + perfil.getNome() + ", situação " + situacao.getDescricao()));
        return salvo;
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoException("Nome é obrigatório");
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.length() < 2 || nomeLimpo.length() > 120) {
            throw new ValidacaoException("Nome deve conter de 2 a 120 caracteres");
        }
        if (!nomeLimpo.matches("^[\\p{L}' \\-]+$")) {
            throw new ValidacaoException("Nome aceita apenas letras, espaços, apóstrofos e hífens");
        }
    }

    private void validarSenha(char[] senha) {
        if (senha == null || senha.length == 0) {
            throw new ValidacaoException("Senha é obrigatória");
        }
        if (senha.length < 8 || senha.length > 64) {
            throw new ValidacaoException("Senha deve conter de 8 a 64 caracteres");
        }
    }
}
