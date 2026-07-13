package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.SituacaoUsuario;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.model.estoque.AjusteEstoque;
import com.ufes.delivery.model.estoque.Entrada;
import com.ufes.delivery.model.estoque.MovimentacaoEstoque;
import com.ufes.delivery.model.estoque.TiposMovimentacao;
import com.ufes.delivery.model.perfil.Administrador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MovimentacaoEstoqueServiceTest {

    private static final LocalDate DATA_OPERACIONAL = LocalDate.of(2026, 6, 20);

    private ProdutoRepositoryEmMemoria repositorio;
    private SessaoService sessao;
    private MovimentacaoEstoqueService service;
    private Produto caderno;

    @BeforeEach
    void preparar() {
        repositorio = new ProdutoRepositoryEmMemoria();
        sessao = new SessaoService();
        service = new MovimentacaoEstoqueService(repositorio,
                new GuardaAcessoAdministrativo(sessao), sessao);

        Usuario admin = new Usuario(1L, "Admin Master", "adminmaster", "hash", "salt",
                new Administrador(), SituacaoUsuario.AUTORIZADO);
        sessao.iniciarSessao(admin, LocalDateTime.now());

        caderno = repositorio.salvar(
                new Produto(null, 2001, "Caderno Universitário", "Papelaria", 18.50, 120));
    }

    @Test
    void previaCalculaSemPersistir() {
        int previa = service.preverEstoque(caderno, new AjusteEstoque(), -15);

        assertEquals(105, previa);
        assertEquals(120, caderno.getEstoqueAtual());
        assertTrue(repositorio.movimentacoes.isEmpty());
    }

    @Test
    void rejeitaAjusteSemMotivo() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.confirmar(caderno, new AjusteEstoque(), -15,
                        DATA_OPERACIONAL, "  ", null, DATA_OPERACIONAL));

        assertEquals(AjusteEstoque.MSG_MOTIVO_OBRIGATORIO, erro.getMessage());
        assertEquals(120, caderno.getEstoqueAtual());
    }

    @Test
    void exigeNotaFiscalNaEntrada() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.confirmar(caderno, new Entrada(), 30,
                        DATA_OPERACIONAL, null, "", DATA_OPERACIONAL));

        assertEquals(Entrada.MSG_NOTA_FISCAL_OBRIGATORIA, erro.getMessage());
    }

    @Test
    void rejeitaEstoqueResultanteNegativo() {
        assertThrows(ValidacaoException.class,
                () -> service.preverEstoque(caderno, new AjusteEstoque(), -130));
        assertThrows(ValidacaoException.class,
                () -> service.confirmar(caderno, new AjusteEstoque(), -130,
                        DATA_OPERACIONAL, "Correção", null, DATA_OPERACIONAL));
        assertEquals(120, caderno.getEstoqueAtual());
    }

    @Test
    void confirmaMovimentacaoValidaEAtualizaEstoque() {
        MovimentacaoEstoque mov = service.confirmar(caderno, new AjusteEstoque(), -15,
                DATA_OPERACIONAL, "Correção de contagem física", null, DATA_OPERACIONAL);

        assertEquals(105, caderno.getEstoqueAtual());
        assertEquals(1, repositorio.movimentacoes.size());
        assertEquals("Ajuste de estoque", mov.getTipo().getNome());
        assertEquals("adminmaster", mov.getResponsavel().getUsername());
    }

    @Test
    void confirmaEntradaComNotaFiscal() {
        service.confirmar(caderno, new Entrada(), 30,
                DATA_OPERACIONAL, null, "NF-123456", DATA_OPERACIONAL);

        assertEquals(150, caderno.getEstoqueAtual());
    }

    @Test
    void rejeitaDataPosteriorADataOperacional() {
        assertThrows(ValidacaoException.class,
                () -> service.confirmar(caderno, new Entrada(), 30,
                        DATA_OPERACIONAL.plusDays(1), null, "NF-1", DATA_OPERACIONAL));
    }

    @Test
    void tipoSaidaNaoExisteNoDominio() {
        assertThrows(IllegalArgumentException.class,
                () -> TiposMovimentacao.porNome("Saída"));
        assertEquals(2, TiposMovimentacao.disponiveis().size());
    }
}
