package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.model.EstadoPedido;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.service.PainelService;
import com.ufes.delivery.service.PainelViewModel;
import com.ufes.delivery.service.SessaoService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.ufes.delivery.ui.view.IInicioView;

/**
 * Presenter do painel operacional (US04). Monta os textos prontos
 * a partir do PainelViewModel; a formatacao brasileira (data e moeda)
 * acontece aqui, na borda da UI.
 */
public class InicioPresenter {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IInicioView view;
    private final PainelService painelService;
    private final SessaoService sessaoService;
    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    public InicioPresenter(IInicioView view, PainelService painelService,
            SessaoService sessaoService, INavegador navegador) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.painelService = Objects.requireNonNull(painelService,
                "Serviço do painel deve ser informado");
        this.sessaoService = Objects.requireNonNull(sessaoService,
                "Serviço de sessão deve ser informado");
        Objects.requireNonNull(navegador, "Navegador deve ser informado");

        view.setAoNovoCliente(() -> navegador.abrirCadastroCliente(null));
        view.setAoBuscarClientes(navegador::abrirBuscaClientes);
        view.setAoGestaoUsuarios(navegador::abrirGestaoUsuarios);
        view.setAoNovoPedido(navegador::abrirNovoPedido);
        view.setAoBuscarProdutos(navegador::abrirBuscaProdutos);
        view.setAoNovoProduto(() -> navegador.abrirCadastroProduto(null));
        view.setAoMovimentacaoEstoque(navegador::abrirMovimentacaoEstoque);
        view.setAoVisualizarPedido(codigo ->
                emConstrucao("Visualização do pedido " + codigo));
        view.setAoAtualizar(this::atualizarPainel);
    }

    public void iniciar() {
        Usuario usuario = sessaoService.getUsuarioLogado();
        view.setUsuarioLogado("Usuário logado: " + usuario.getUsername());
        view.setInformacaoLogin("Login: "
                + FORMATO_DATA_HORA.format(sessaoService.getDataHoraLogin()));
        view.setTipoPerfil("Tipo: " + usuario.getPerfil().getNome());

        // o menu administrativo so aparece para o Administrador
        view.setMenuAdministracaoVisivel(
                usuario.getPerfil().podeExecutarOperacoesAdministrativas());

        atualizarPainel();
        view.abrir();
    }

    public void atualizarPainel() {
        LocalDate hoje = LocalDate.now();
        PainelViewModel painel = painelService.montarPainel(hoje);

        view.setDataOperacao("Data de operação: " + FORMATO_DATA.format(hoje));
        view.setMetricaPedidosDoDia(String.valueOf(painel.getPedidosDoDia()));
        view.setMetricaNovos(String.valueOf(painel.contagem(EstadoPedido.NOVO)));
        view.setMetricaAguardandoPagamento(
                String.valueOf(painel.contagem(EstadoPedido.AGUARDANDO_PAGAMENTO)));
        view.setMetricaEmPreparo(String.valueOf(painel.contagem(EstadoPedido.EM_PREPARO)));
        view.setMetricaAguardandoEntrega(
                String.valueOf(painel.contagem(EstadoPedido.AGUARDANDO_ENTREGA)));
        view.setMetricaEmTransito(String.valueOf(painel.contagem(EstadoPedido.EM_TRANSITO)));
        view.setMetricaEntreguesHoje(String.valueOf(painel.contagem(EstadoPedido.ENTREGUE)));

        List<IInicioView.LinhaPedidoPainel> linhas = painel.getLinhas().stream()
                .map(l -> new IInicioView.LinhaPedidoPainel(
                        l.codigo(),
                        l.nomeCliente(),
                        FORMATO_DATA.format(l.dataPedido()),
                        l.dataConclusao() == null ? "-" : FORMATO_DATA.format(l.dataConclusao()),
                        l.estado(),
                        formatoMoeda.format(l.valorTotal())))
                .toList();
        view.mostrarPedidos(linhas);
    }

    private void emConstrucao(String funcionalidade) {
        // TODO: telas das proximas US serao plugadas aqui
        view.mostrarInformacao(funcionalidade + ": tela em construção");
    }
}
