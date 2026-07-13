package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.seguranca.GeradorHashSenha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GestaoUsuariosServiceTest {

    private UsuarioRepositoryEmMemoria repositorio;
    private CadastroUsuarioService cadastro;
    private SessaoService sessao;
    private GestaoUsuariosService service;

    private Usuario admin;
    private Usuario atendente1;
    private Usuario atendente2;

    @BeforeEach
    void preparar() {
        repositorio = new UsuarioRepositoryEmMemoria();
        cadastro = new CadastroUsuarioService(repositorio, new GeradorHashSenha());
        sessao = new SessaoService();
        service = new GestaoUsuariosService(repositorio, new GuardaAcessoAdministrativo(sessao));

        admin = cadastro.cadastrar("Admin Master", "adminmaster", "senha12345".toCharArray());
        atendente1 = cadastro.cadastrar("Fulano de Tal", "fulano123", "senha12345".toCharArray());
        atendente2 = cadastro.cadastrar("Beltrano Silva", "beltrano9", "senha12345".toCharArray());
    }

    private void logarComoAdmin() {
        sessao.iniciarSessao(admin, LocalDateTime.now());
    }

    @Test
    void bloqueiaAcessoDeAtendente() {
        atendente1.autorizar();
        sessao.iniciarSessao(atendente1, LocalDateTime.now());

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.buscarPorNome("ful"));

        assertEquals(GuardaAcessoAdministrativo.MSG_ACESSO_RESTRITO, erro.getMessage());
    }

    @Test
    void buscaPorNomeOuUsernameInsensivelAMaiusculas() {
        logarComoAdmin();

        List<Usuario> resultado = service.buscarPorNome("FULANO");

        assertEquals(1, resultado.size());
        assertEquals("fulano123", resultado.get(0).getUsername());
    }

    @Test
    void autorizaVariosUsuariosSelecionados() {
        logarComoAdmin();

        service.autorizar(List.of(atendente1.getId(), atendente2.getId()));

        assertEquals(SituacaoUsuario.AUTORIZADO,
                repositorio.buscarPorId(atendente1.getId()).orElseThrow().getSituacao());
        assertEquals(SituacaoUsuario.AUTORIZADO,
                repositorio.buscarPorId(atendente2.getId()).orElseThrow().getSituacao());
    }

    @Test
    void desautorizaUsuarioSelecionado() {
        logarComoAdmin();
        service.autorizar(List.of(atendente1.getId()));

        service.desautorizar(List.of(atendente1.getId()));

        Usuario atualizado = repositorio.buscarPorId(atendente1.getId()).orElseThrow();
        assertEquals(SituacaoUsuario.NAO_AUTORIZADO, atualizado.getSituacao());
        assertFalse(atualizado.podeIniciarSessao());
    }

    @Test
    void rejeitaOperacaoSemSelecao() {
        logarComoAdmin();

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.autorizar(List.of()));

        assertEquals(GestaoUsuariosService.MSG_SELECAO_OBRIGATORIA, erro.getMessage());
    }

    @Test
    void excluiUsuariosSelecionados() {
        logarComoAdmin();

        service.excluir(List.of(atendente2.getId()));

        assertTrue(repositorio.buscarPorId(atendente2.getId()).isEmpty());
        assertEquals(2, repositorio.contar());
    }

    @Test
    void definePerfilValidandoValor() {
        logarComoAdmin();

        service.definirPerfil(atendente1.getId(), "Administrador");

        assertEquals("Administrador",
                repositorio.buscarPorId(atendente1.getId()).orElseThrow().getPerfil().getNome());
        assertThrows(IllegalArgumentException.class,
                () -> service.definirPerfil(atendente1.getId(), "Gerente"));
    }
}
