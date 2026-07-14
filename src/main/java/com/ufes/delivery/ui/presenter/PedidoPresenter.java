package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.desconto.pedido.AplicadorCupomPedidoService;
import com.ufes.delivery.desconto.taxa.entrega.CalculadoraTaxaDescontoPedidoService;
import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.repository.IClienteRepository;
import com.ufes.delivery.repository.IPedidoRepository;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.service.ProcessarPagamentoService;
import com.ufes.delivery.ui.view.IPedidoView;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Presenter da tela de pedido (US09/US10). O pedido em elaboracao
 * vive aqui; os calculos financeiros ficam no agregado Pedido e nas
 * strategies de desconto; a tentativa de pagamento e delegada ao
 * ProcessarPagamentoService (tudo-ou-nada).
 */
public class PedidoPresenter {

    private final IPedidoView view;
    private final IClienteRepository clienteRepository;
    private final IPedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final AplicadorCupomPedidoService aplicadorCupom;
    private final CalculadoraTaxaDescontoPedidoService calculadoraTaxa;
    private final ProcessarPagamentoService processarPagamentoService;
    private final INavegador navegador;
    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private Cliente clienteSelecionado;
    private Pedido pedidoAtual;

    public PedidoPresenter(IPedidoView view, IClienteRepository clienteRepository,
            IPedidoRepository pedidoRepository, ProdutoService produtoService,
            AplicadorCupomPedidoService aplicadorCupom,
            CalculadoraTaxaDescontoPedidoService calculadoraTaxa,
            ProcessarPagamentoService processarPagamentoService, INavegador navegador) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.clienteRepository = Objects.requireNonNull(clienteRepository,
                "Repositório de clientes deve ser informado");
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository,
                "Repositório de pedidos deve ser informado");
        this.produtoService = Objects.requireNonNull(produtoService,
                "Serviço de produtos deve ser informado");
        this.aplicadorCupom = Objects.requireNonNull(aplicadorCupom,
                "Aplicador de cupom deve ser informado");
        this.calculadoraTaxa = Objects.requireNonNull(calculadoraTaxa,
                "Calculadora de descontos deve ser informada");
        this.processarPagamentoService = Objects.requireNonNull(processarPagamentoService,
                "Serviço de pagamento deve ser informado");
        this.navegador = Objects.requireNonNull(navegador, "Navegador deve ser informado");

        view.setAoSelecionarCliente(this::selecionarCliente);
        view.setAoNovoCliente(this::novoCliente);
        view.setAoSelecionarEndereco(this::selecionarEndereco);
        view.setAoAdicionarItem(this::adicionarItem);
        view.setAoExcluirItem(this::excluirItem);
        view.setAoAlterarQuantidade(this::alterarQuantidade);
        view.setAoAplicarCupom(this::aplicarCupom);
        view.setAoPagar(this::pagar);
        view.setAoCancelar(this::cancelar);
    }

    public void iniciar() {
        carregarClientes();
        recalcular();
        view.abrir();
    }

    private void carregarClientes() {
        List<IPedidoView.OpcaoCliente> opcoes = clienteRepository.buscarTodos().stream()
                .map(c -> new IPedidoView.OpcaoCliente(c.getId(), c.getNome()))
                .toList();
        view.setClientesDisponiveis(opcoes);
    }

    private void selecionarCliente() {
        Long id = view.getIdClienteSelecionado();
        if (id == null) {
            return;
        }

        clienteSelecionado = clienteRepository.buscarPorId(id).orElse(null);
        if (clienteSelecionado == null) {
            view.mostrarErro("Cliente não encontrado");
            return;
        }

        // recria o pedido para o novo cliente preservando itens e cupom
        Pedido anterior = pedidoAtual;
        pedidoAtual = new Pedido(LocalDateTime.now(), clienteSelecionado);
        if (anterior != null) {
            for (Item item : anterior.getItens()) {
                pedidoAtual.adicionarItem(item);
            }
            Optional<CupomDescontoPedido> cupom = anterior.getCupomAplicado();
            cupom.ifPresent(pedidoAtual::setCupomAplicado);
        }

        // a caixa lista somente os enderecos do cliente selecionado
        List<Endereco> enderecos = clienteSelecionado.getEnderecos();
        view.setEnderecosDisponiveis(enderecos.stream()
                .map(Endereco::descricaoCompleta)
                .toList());

        int indicePadrao = 0;
        for (int i = 0; i < enderecos.size(); i++) {
            if (enderecos.get(i).isPadrao()) {
                indicePadrao = i;
            }
        }
        view.setIndiceEnderecoSelecionado(indicePadrao);
        if (!enderecos.isEmpty()) {
            pedidoAtual.definirEnderecoEntrega(enderecos.get(indicePadrao));
        }

        recalcular();
    }

    private void selecionarEndereco() {
        int indice = view.getIndiceEnderecoSelecionado();
        if (pedidoAtual == null || clienteSelecionado == null || indice < 0) {
            return;
        }
        List<Endereco> enderecos = clienteSelecionado.getEnderecos();
        if (indice < enderecos.size()) {
            pedidoAtual.definirEnderecoEntrega(enderecos.get(indice));
        }
    }

    private void novoCliente() {
        // o cadastro e modal: quando retornar, a lista e recarregada
        navegador.abrirCadastroCliente(null);
        carregarClientes();
    }

    private void adicionarItem() {
        if (pedidoAtual == null) {
            view.mostrarErro("Selecione o cliente antes de adicionar itens");
            return;
        }

        try {
            Produto produto = resolverProduto(view.getProdutoParaAdicionar());
            int quantidade = parseQuantidade(view.getQuantidadeParaAdicionar());

            // se o produto ja esta no pedido, soma na quantidade
            Item existente = null;
            for (Item item : pedidoAtual.getItens()) {
                if (item.getProduto() != null
                        && item.getProduto().getCodigo() == produto.getCodigo()) {
                    existente = item;
                }
            }

            if (existente != null) {
                existente.setQuantidade(existente.getQuantidade() + quantidade);
            } else {
                pedidoAtual.adicionarItem(new Item(produto, quantidade));
            }

            view.limparCamposDeItem();
            recalcular();
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private Produto resolverProduto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacaoException("Informe o código ou o nome do produto");
        }

        String informado = valor.trim();
        if (informado.matches("\\d+")) {
            return produtoService.buscarPorCodigo(informado)
                    .orElseThrow(() -> new ValidacaoException(
                            "Produto não encontrado: " + informado));
        }

        List<Produto> encontrados = produtoService.buscarPorNome(informado);
        if (encontrados.isEmpty()) {
            throw new ValidacaoException("Produto não encontrado: " + informado);
        }
        if (encontrados.size() > 1) {
            throw new ValidacaoException(
                    "Mais de um produto encontrado; informe o código do produto");
        }
        return encontrados.get(0);
    }

    private void excluirItem(int indice) {
        if (pedidoAtual == null || indice < 0 || indice >= pedidoAtual.getItens().size()) {
            return;
        }
        pedidoAtual.removerItem(pedidoAtual.getItens().get(indice));
        recalcular();
    }

    private void alterarQuantidade(Integer indice, String valor) {
        if (pedidoAtual == null || indice < 0 || indice >= pedidoAtual.getItens().size()) {
            return;
        }
        try {
            pedidoAtual.getItens().get(indice).setQuantidade(parseQuantidade(valor));
        } catch (ValidacaoException | IllegalArgumentException e) {
            view.mostrarErro(e.getMessage());
        }
        recalcular();
    }

    private void aplicarCupom(){
        if (pedidoAtual == null) {
            view.mostrarErro("Selecione o cliente antes de aplicar cupom");
            return;
        }
        try {
            aplicadorCupom.aplicarCupom(pedidoAtual, view.getCupom(), LocalDateTime.now());
            recalcular();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // cenario 5 da US09: cupom inexistente, expirado ou indisponivel
            view.mostrarErro("O cupom não é válido para o pedido: " + e.getMessage());
        }
    }

    private void pagar() {
        if (pedidoAtual == null) {
            view.mostrarErro("Cliente é obrigatório");
            return;
        }

        try {
            selecionarEndereco();
            try {
                pedidoAtual.validarParaPagamento();
            } catch (IllegalStateException e) {
                throw new ValidacaoException(e.getMessage());
            }

            // persiste o pedido antes da tentativa (o pagamento referencia o pedido)
            if (pedidoAtual.getId() == null) {
                pedidoRepository.salvar(pedidoAtual);
            } else {
                pedidoRepository.atualizar(pedidoAtual);
            }

            Pagamento pagamento = processarPagamentoService.processar(
                    pedidoAtual, LocalDateTime.now());
            navegador.mostrarResultadoPagamento(pagamento);

            if (pagamento.getResultado().isAprovado()) {
                view.fechar();
            }
            // reprovado: a tela permanece com os dados preservados
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void cancelar() {
        boolean haDados = pedidoAtual != null && !pedidoAtual.getItens().isEmpty();
        if (haDados && !view.confirmarCancelamento()) {
            return;
        }
        view.fechar();
    }

    private void recalcular() {
        if (pedidoAtual == null) {
            view.mostrarItens(List.of());
            view.setTotalDescontos(formatoMoeda.format(0));
            view.setDescontoTaxaEntrega(formatoMoeda.format(0));
            view.setTaxaEntregaFinal(formatoMoeda.format(0));
            view.setTotalPedido(formatoMoeda.format(0));
            return;
        }

        calculadoraTaxa.calcularDesconto(pedidoAtual);

        List<IPedidoView.LinhaItem> linhas = new ArrayList<>();
        for (Item item : pedidoAtual.getItens()) {
            linhas.add(new IPedidoView.LinhaItem(
                    item.getTipo(),
                    item.getNome(),
                    formatoMoeda.format(item.getValorUnitario()),
                    String.valueOf(item.getQuantidade()),
                    formatoMoeda.format(item.valorTotal())));
        }
        view.mostrarItens(linhas);

        double subtotal = pedidoAtual.getValorPedido();
        double taxaFinal = pedidoAtual.getTaxaEntregaComDesconto();
        double total = pedidoAtual.calcularValorTotal();
        // total = subtotal - descontos + taxa final, entao descontos derivam daqui
        double totalDescontos = subtotal + taxaFinal - total;
        if (totalDescontos < 0) {
            totalDescontos = 0;
        }

        view.setTotalDescontos(formatoMoeda.format(totalDescontos));
        view.setDescontoTaxaEntrega(
                formatoMoeda.format(pedidoAtual.getTotalDescontosTaxaEntrega()));
        view.setTaxaEntregaFinal(formatoMoeda.format(taxaFinal));
        view.setTotalPedido(formatoMoeda.format(total));
    }

    private int parseQuantidade(String valor) {
        try {
            int quantidade = Integer.parseInt(valor.trim());
            if (quantidade <= 0) {
                throw new NumberFormatException();
            }
            return quantidade;
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Quantidade deve ser um número inteiro maior que zero");
        }
    }
}
