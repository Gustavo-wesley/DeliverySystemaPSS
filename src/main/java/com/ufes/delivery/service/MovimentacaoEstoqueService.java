package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.estoque.ITipoMovimentacao;
import com.ufes.delivery.model.estoque.MovimentacaoEstoque;
import com.ufes.delivery.repository.IProdutoRepository;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Movimentacao de estoque (US08). A previa e um calculo puro,
 * sem persistencia; a confirmacao valida tudo novamente e grava
 * movimentacao + novo estoque em transacao unica.
 */
public class MovimentacaoEstoqueService {

    private final IProdutoRepository produtoRepository;
    private final GuardaAcessoAdministrativo guarda;
    private final SessaoService sessaoService;

    public MovimentacaoEstoqueService(IProdutoRepository produtoRepository,
            GuardaAcessoAdministrativo guarda, SessaoService sessaoService) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository,
                "Repositório de produtos deve ser informado");
        this.guarda = Objects.requireNonNull(guarda, "Guarda de acesso deve ser informado");
        this.sessaoService = Objects.requireNonNull(sessaoService,
                "Serviço de sessão deve ser informado");
    }

    /**
     * Previa do estoque apos a movimentacao: calculo puro, nada persiste.
     */
    public int preverEstoque(Produto produto, ITipoMovimentacao tipo, int quantidade) {
        Objects.requireNonNull(produto, "Produto deve ser informado");
        Objects.requireNonNull(tipo, "Tipo de movimentação deve ser informado");

        int resultado = produto.getEstoqueAtual() + tipo.variacaoDeEstoque(quantidade);
        if (resultado < 0) {
            throw new ValidacaoException("Estoque resultante não pode ser negativo. "
                    + "Quantidade disponível: " + produto.getEstoqueAtual());
        }
        return resultado;
    }

    public MovimentacaoEstoque confirmar(Produto produto, ITipoMovimentacao tipo,
            int quantidade, LocalDate data, String motivo, String notaFiscal,
            LocalDate dataOperacional) {

        guarda.exigirAdministrador();
        Objects.requireNonNull(produto, "Produto deve ser informado");
        Objects.requireNonNull(tipo, "Tipo de movimentação deve ser informado");

        if (data == null) {
            throw new ValidacaoException("Data da movimentação é obrigatória");
        }
        if (dataOperacional != null && data.isAfter(dataOperacional)) {
            throw new ValidacaoException(
                    "Data da movimentação não pode ser posterior à data operacional vigente");
        }

        tipo.validar(quantidade, motivo, notaFiscal);

        // valida o estoque resultante e ja aplica no agregado
        int variacao = tipo.variacaoDeEstoque(quantidade);
        if (produto.getEstoqueAtual() + variacao < 0) {
            throw new ValidacaoException("Estoque resultante não pode ser negativo. "
                    + "Quantidade disponível: " + produto.getEstoqueAtual());
        }

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(null, produto, tipo,
                quantidade, data, motivo, notaFiscal, sessaoService.getUsuarioLogado());

        produto.aplicarVariacaoEstoque(variacao);
        produtoRepository.registrarMovimentacao(movimentacao);

        return movimentacao;
    }
}
