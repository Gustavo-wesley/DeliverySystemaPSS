package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.perfil.Administrador;
import com.ufes.delivery.model.perfil.Atendente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoServiceTest {

    private ProdutoRepositoryEmMemoria repositorio;
    private SessaoService sessao;
    private ProdutoService service;

    @BeforeEach
    void preparar() {
        repositorio = new ProdutoRepositoryEmMemoria();
        sessao = new SessaoService();
        service = new ProdutoService(repositorio, new GuardaAcessoAdministrativo(sessao));
        logarComoAdmin();
    }

    private void logarComoAdmin() {
        Usuario admin = new Usuario(1L, "Admin Master", "adminmaster", "hash", "salt",
                new Administrador(), SituacaoUsuario.AUTORIZADO);
        sessao.iniciarSessao(admin, LocalDateTime.now());
    }

    private Produto produto(int codigo, String nome, String categoria) {
        return new Produto(null, codigo, nome, categoria, 18.50, 120);
    }

    @Test
    void salvaProdutoComDadosValidos() {
        Produto salvo = service.salvar(produto(2001, "Caderno Universitário", "Papelaria"));

        assertNotNull(salvo.getId());
        assertEquals(120, salvo.getEstoqueAtual());
    }

    @Test
    void rejeitaCodigoJaUtilizado() {
        service.salvar(produto(2001, "Caderno Universitário", "Papelaria"));

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.salvar(produto(2001, "Outro Produto", "Lazer")));

        assertEquals(ProdutoService.MSG_CODIGO_EM_USO, erro.getMessage());
    }

    @Test
    void rejeitaNomeDuplicadoNaMesmaCategoria() {
        service.salvar(produto(2001, "Caderno Universitário", "Papelaria"));

        assertThrows(ValidacaoException.class,
                () -> service.salvar(produto(2002, "caderno universitário", "Papelaria")));
    }

    @Test
    void rejeitaPrecoOuEstoqueInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new Produto(null, 2001, "Caderno", "Papelaria", 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new Produto(null, 2001, "Caderno", "Papelaria", 10.555, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new Produto(null, 2001, "Caderno", "Papelaria", 10.50, -1));
    }

    @Test
    void buscaPorNomeParcial() {
        service.salvar(produto(2001, "Caderno Universitário", "Papelaria"));
        service.salvar(produto(2002, "Caderno Pequeno", "Papelaria"));
        service.salvar(produto(2003, "Jogo de Xadrez", "Lazer"));

        List<Produto> resultado = service.buscarPorNome("Caderno");

        assertEquals(2, resultado.size());
    }

    @Test
    void buscaPorCodigo() {
        service.salvar(produto(2001, "Caderno Universitário", "Papelaria"));

        assertTrue(service.buscarPorCodigo("2001").isPresent());
        assertThrows(ValidacaoException.class, () -> service.buscarPorCodigo("abc"));
        assertThrows(ValidacaoException.class, () -> service.buscarPorCodigo("-1"));
    }

    @Test
    void cadastroExigePerfilAdministrador() {
        Usuario atendente = new Usuario(2L, "Carlos Atendente", "atendente01", "hash", "salt",
                new Atendente(), SituacaoUsuario.AUTORIZADO);
        sessao.iniciarSessao(atendente, LocalDateTime.now());

        assertThrows(ValidacaoException.class,
                () -> service.salvar(produto(2001, "Caderno", "Papelaria")));
    }
}
