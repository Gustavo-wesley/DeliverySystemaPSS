package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.estoque.AjusteEstoque;
import com.ufes.delivery.model.estoque.Entrada;
import com.ufes.delivery.model.estoque.ITipoMovimentacao;
import com.ufes.delivery.model.estoque.TiposMovimentacao;
import com.ufes.delivery.service.GuardaAcessoAdministrativo;
import com.ufes.delivery.service.MovimentacaoEstoqueService;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.ui.view.IMovimentacaoEstoqueView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Presenter da movimentacao de estoque (US08). A previa e calculo
 * puro no service; somente Confirmar movimentacao persiste, e em
 * transacao unica.
 */
public class MovimentacaoEstoquePresenter {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IMovimentacaoEstoqueView view;
    private final ProdutoService produtoService;
    private final MovimentacaoEstoqueService movimentacaoService;
    private final GuardaAcessoAdministrativo guarda;

    private List<Produto> resultadosBusca = new ArrayList<>();
    private Produto produtoSelecionado;

    public MovimentacaoEstoquePresenter(IMovimentacaoEstoqueView view,
            ProdutoService produtoService, MovimentacaoEstoqueService movimentacaoService,
            GuardaAcessoAdministrativo guarda) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.produtoService = Objects.requireNonNull(produtoService,
                "Serviço de produtos deve ser informado");
        this.movimentacaoService = Objects.requireNonNull(movimentacaoService,
                "Serviço de movimentação deve ser informado");
        this.guarda = Objects.requireNonNull(guarda, "Guarda de acesso deve ser informado");

        view.setAoBuscar(this::buscar);
        view.setAoSelecionarProduto(this::selecionarProduto);
        view.setAoDadosAlterados(this::atualizarPrevia);
        view.setAoConfirmar(this::confirmar);
        view.setAoCancelar(view::fechar);
    }

    public void iniciar() {
        try {
            // acesso restrito ao Administrador (regra da US08)
            guarda.exigirAdministrador();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
            return;
        }

        view.setTiposDisponiveis(TiposMovimentacao.disponiveis().stream()
                .map(ITipoMovimentacao::getNome)
                .toList());
        view.setDataMovimentacao(FORMATO_DATA.format(LocalDate.now()));
        atualizarCamposCondicionais();
        view.abrir();
    }

    private void buscar() {
        try {
            resultadosBusca = produtoService.buscarPorNome(view.getValorBuscaProduto());
            if (resultadosBusca.isEmpty()) {
                view.mostrarInformacao("Nenhum produto encontrado para a busca informada");
            }

            List<IMovimentacaoEstoqueView.LinhaProdutoBusca> linhas = resultadosBusca.stream()
                    .map(p -> new IMovimentacaoEstoqueView.LinhaProdutoBusca(
                            p.getId(),
                            String.valueOf(p.getCodigo()),
                            p.getNome(),
                            p.getCategoria(),
                            String.valueOf(p.getEstoqueAtual())))
                    .toList();
            view.mostrarResultadosBusca(linhas);
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void selecionarProduto() {
        Long id = view.getIdProdutoSelecionadoNaBusca();
        if (id == null) {
            view.mostrarErro("Selecione um produto no resultado da busca");
            return;
        }

        produtoSelecionado = resultadosBusca.stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElse(null);

        if (produtoSelecionado != null) {
            view.setProdutoSelecionado(produtoSelecionado.getNome(),
                    String.valueOf(produtoSelecionado.getEstoqueAtual()));
            atualizarPrevia();
        }
    }

    /**
     * Previa informativa: nada persiste aqui (cenario 1 da US08).
     */
    private void atualizarPrevia() {
        atualizarCamposCondicionais();

        if (produtoSelecionado == null) {
            view.setPrevia("");
            return;
        }

        try {
            ITipoMovimentacao tipo = TiposMovimentacao.porNome(view.getTipoMovimentacao());
            int quantidade = Integer.parseInt(view.getQuantidade().trim());
            int previa = movimentacaoService.preverEstoque(produtoSelecionado, tipo, quantidade);
            view.setPrevia(String.valueOf(previa));
        } catch (ValidacaoException | IllegalArgumentException e) {
            // quantidade incompleta ou invalida: a previa fica em branco
            view.setPrevia("");
        }
    }

    private void confirmar() {
        if (produtoSelecionado == null) {
            view.mostrarErro("Produto deve ser selecionado a partir do resultado de busca");
            return;
        }

        try {
            ITipoMovimentacao tipo = TiposMovimentacao.porNome(view.getTipoMovimentacao());
            int quantidade = parseQuantidade();
            LocalDate data = parseData();

            movimentacaoService.confirmar(produtoSelecionado, tipo, quantidade, data,
                    view.getMotivo(), view.getNotaFiscal(), LocalDate.now());

            view.mostrarInformacao("Movimentação confirmada. Estoque atual: "
                    + produtoSelecionado.getEstoqueAtual());
            view.setProdutoSelecionado(produtoSelecionado.getNome(),
                    String.valueOf(produtoSelecionado.getEstoqueAtual()));
            view.limparCamposMovimentacao();
            view.setPrevia("");
            buscar(); // atualiza o estoque exibido na lista
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void atualizarCamposCondicionais() {
        // Ajuste exige motivo; Entrada exige nota fiscal
        boolean ehAjuste = AjusteEstoque.NOME.equals(view.getTipoMovimentacao());
        boolean ehEntrada = Entrada.NOME.equals(view.getTipoMovimentacao());
        view.setMotivoHabilitado(ehAjuste);
        view.setNotaFiscalHabilitada(ehEntrada);
    }

    private int parseQuantidade() {
        try {
            return Integer.parseInt(view.getQuantidade().trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException(
                    "Quantidade a movimentar deve ser um número inteiro diferente de zero");
        }
    }

    private LocalDate parseData() {
        try {
            return LocalDate.parse(view.getDataMovimentacao().trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data da movimentação inválida (use dd/mm/aaaa)");
        }
    }
}
