package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.seguranca.GeradorHashSenha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CadastroUsuarioServiceTest {

    private UsuarioRepositoryEmMemoria repositorio;
    private CadastroUsuarioService service;

    @BeforeEach
    void preparar() {
        repositorio = new UsuarioRepositoryEmMemoria();
        service = new CadastroUsuarioService(repositorio, new GeradorHashSenha());
    }

    @Test
    void primeiroUsuarioRecebePerfilAdministradorESituacaoAutorizado() {
        Usuario usuario = service.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());

        assertEquals("Administrador", usuario.getPerfil().getNome());
        assertEquals(SituacaoUsuario.AUTORIZADO, usuario.getSituacao());
        assertNotNull(usuario.getId());
    }

    @Test
    void usuarioPosteriorRecebePerfilAtendenteESituacaoPendente() {
        service.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());
        Usuario segundo = service.cadastrar("Fulano de Tal", "fulano123", "senha12345".toCharArray());

        assertEquals("Atendente", segundo.getPerfil().getNome());
        assertEquals(SituacaoUsuario.PENDENTE, segundo.getSituacao());
    }

    @Test
    void rejeitaNomeDeUsuarioDuplicado() {
        service.cadastrar("Fulano", "fulano123", "senha12345".toCharArray());

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.cadastrar("Outro Fulano", "fulano123", "senha12345".toCharArray()));

        assertEquals("Nome de usuário já está em uso", erro.getMessage());
    }

    @Test
    void rejeitaUsernameComMaiusculaOuEspaco() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar("Fulano", "Fulano123", "senha12345".toCharArray()));
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar("Fulano", "fulano 123", "senha12345".toCharArray()));
    }

    @Test
    void rejeitaCamposObrigatoriosAusentes() {
        assertThrows(ValidacaoException.class,
                () -> service.cadastrar("  ", "fulano123", "senha12345".toCharArray()));
        assertThrows(ValidacaoException.class,
                () -> service.cadastrar("Fulano", "fulano123", new char[0]));
    }

    @Test
    void rejeitaSenhaCurta() {
        assertThrows(ValidacaoException.class,
                () -> service.cadastrar("Fulano", "fulano123", "curta".toCharArray()));
    }

    @Test
    void senhaNaoFicaArmazenadaEmTextoAberto() {
        Usuario usuario = service.cadastrar("Fulano", "fulano123", "senha12345".toCharArray());

        assertNotEquals("senha12345", usuario.getSenhaHash());
        assertFalse(usuario.getSenhaHash().contains("senha12345"));
    }
}
