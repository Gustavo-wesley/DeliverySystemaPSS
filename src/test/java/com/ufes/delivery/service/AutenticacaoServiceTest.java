package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.seguranca.GeradorHashSenha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacaoServiceTest {

    private UsuarioRepositoryEmMemoria repositorio;
    private CadastroUsuarioService cadastro;
    private SessaoService sessao;
    private AutenticacaoService service;

    @BeforeEach
    void preparar() {
        repositorio = new UsuarioRepositoryEmMemoria();
        GeradorHashSenha gerador = new GeradorHashSenha();
        cadastro = new CadastroUsuarioService(repositorio, gerador);
        sessao = new SessaoService();
        service = new AutenticacaoService(repositorio, gerador, sessao);
    }

    @Test
    void autenticaAdministradorAutorizadoEIniciaSessao() {
        cadastro.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());

        Usuario logado = service.autenticar("adminmaster", "senha12345".toCharArray());

        assertTrue(sessao.haSessaoAtiva());
        assertEquals("adminmaster", sessao.getNomeUsuario());
        assertEquals("Administrador", logado.getPerfil().getNome());
        assertNotNull(sessao.getDataHoraLogin());
    }

    @Test
    void mensagemDeCredencialInvalidaNaoRevelaQualDadoFalhou() {
        cadastro.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());

        ValidacaoException usuarioErrado = assertThrows(ValidacaoException.class,
                () -> service.autenticar("naoexiste1", "senha12345".toCharArray()));
        ValidacaoException senhaErrada = assertThrows(ValidacaoException.class,
                () -> service.autenticar("adminmaster", "senhaerrada".toCharArray()));

        assertEquals(AutenticacaoService.MSG_CREDENCIAIS_INVALIDAS, usuarioErrado.getMessage());
        assertEquals(AutenticacaoService.MSG_CREDENCIAIS_INVALIDAS, senhaErrada.getMessage());
        assertFalse(sessao.haSessaoAtiva());
    }

    @Test
    void bloqueiaUsuarioPendente() {
        cadastro.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());
        cadastro.cadastrar("Fulano de Tal", "fulano123", "senha12345".toCharArray());

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.autenticar("fulano123", "senha12345".toCharArray()));

        assertEquals(AutenticacaoService.MSG_ACESSO_PENDENTE, erro.getMessage());
        assertFalse(sessao.haSessaoAtiva());
    }

    @Test
    void bloqueiaUsuarioDesautorizado() {
        cadastro.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());
        Usuario fulano = cadastro.cadastrar("Fulano", "fulano123", "senha12345".toCharArray());
        fulano.desautorizar();
        repositorio.atualizar(fulano);

        assertThrows(ValidacaoException.class,
                () -> service.autenticar("fulano123", "senha12345".toCharArray()));
    }

    @Test
    void rejeitaUsernameComFormatoInvalidoSemConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.autenticar("Nome Errado", "senha12345".toCharArray()));
    }
}
