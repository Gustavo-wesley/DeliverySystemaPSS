package com.ufes.delivery.ui;

import com.ufes.delivery.auditoria.AuditoriaAdapter;
import com.ufes.delivery.auditoria.AuditoriaLoggerFactory;
import com.ufes.delivery.auditoria.AuditoriaObserver;
import com.ufes.delivery.auditoria.AuditoriaPublisher;
import com.ufes.delivery.desconto.pedido.AplicadorCupomPedidoService;
import com.ufes.delivery.desconto.taxa.entrega.CalculadoraTaxaDescontoPedidoService;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Pagamento;
import com.ufes.delivery.model.Produto;
import com.ufes.delivery.repository.IClienteRepository;
import com.ufes.delivery.repository.IPedidoRepository;
import com.ufes.delivery.repository.IProdutoRepository;
import com.ufes.delivery.repository.IUsuarioRepository;
import com.ufes.delivery.repository.sqlite.ClienteRepositorySqlite;
import com.ufes.delivery.repository.sqlite.CupomRepositorySqlite;
import com.ufes.delivery.repository.sqlite.DatabaseManager;
import com.ufes.delivery.repository.sqlite.PedidoRepositorySqlite;
import com.ufes.delivery.repository.sqlite.ProdutoRepositorySqlite;
import com.ufes.delivery.repository.sqlite.UsuarioRepositorySqlite;
import com.ufes.delivery.seguranca.GeradorHashSenha;
import com.ufes.delivery.service.AutenticacaoService;
import com.ufes.delivery.service.CadastroUsuarioService;
import com.ufes.delivery.service.ClienteService;
import com.ufes.delivery.service.GestaoUsuariosService;
import com.ufes.delivery.service.GuardaAcessoAdministrativo;
import com.ufes.delivery.service.MovimentacaoEstoqueService;
import com.ufes.delivery.service.PainelService;
import com.ufes.delivery.service.ProcessarPagamentoService;
import com.ufes.delivery.service.ProdutoService;
import com.ufes.delivery.service.SessaoService;
import com.ufes.delivery.service.pagamento.GeradorAleatorioPadrao;
import com.ufes.delivery.service.pagamento.GeradorIdTransacao;
import com.ufes.delivery.service.pagamento.GeradorPrazoEntregaSimulado;
import com.ufes.delivery.service.pagamento.IGeradorAleatorio;
import com.ufes.delivery.service.pagamento.SimuladorPagamento;
import com.ufes.delivery.ui.presenter.BuscaClientesPresenter;
import com.ufes.delivery.ui.presenter.CadastroUsuarioPresenter;
import com.ufes.delivery.ui.presenter.ClientePresenter;
import com.ufes.delivery.ui.presenter.INavegador;
import com.ufes.delivery.ui.presenter.InicioPresenter;
import com.ufes.delivery.ui.presenter.LoginPresenter;
import com.ufes.delivery.ui.presenter.MovimentacaoEstoquePresenter;
import com.ufes.delivery.ui.presenter.PagamentoPresenter;
import com.ufes.delivery.ui.presenter.PedidoPresenter;
import com.ufes.delivery.ui.presenter.ProdutoPresenter;
import com.ufes.delivery.ui.presenter.ProdutosPresenter;
import com.ufes.delivery.ui.presenter.UsuariosPresenter;
import com.ufes.delivery.ui.swing.BuscaClientesFrame;
import com.ufes.delivery.ui.swing.CadastroUsuarioDialog;
import com.ufes.delivery.ui.swing.ClienteDialog;
import com.ufes.delivery.ui.swing.InicioFrame;
import com.ufes.delivery.ui.swing.LoginFrame;
import com.ufes.delivery.ui.swing.MovimentacaoEstoqueDialog;
import com.ufes.delivery.ui.swing.PagamentoDialog;
import com.ufes.delivery.ui.swing.PedidoFrame;
import com.ufes.delivery.ui.swing.ProdutoDialog;
import com.ufes.delivery.ui.swing.ProdutosFrame;
import com.ufes.delivery.ui.swing.UsuariosFrame;
import com.ufes.logger.ILogger;
import java.time.LocalDateTime;

/**
 * Composicao da aplicacao: injecao de dependencia manual por
 * construtor, sem framework. Cria banco, repositorios, auditoria e
 * services uma unica vez e implementa a navegacao entre as telas.
 */
public class AplicacaoDelivery implements INavegador {

    public static final String ARQUIVO_BANCO = "delivery.db";

    private final SessaoService sessaoService;
    private final GuardaAcessoAdministrativo guarda;
    private final AutenticacaoService autenticacaoService;
    private final CadastroUsuarioService cadastroUsuarioService;
    private final GestaoUsuariosService gestaoUsuariosService;
    private final ClienteService clienteService;
    private final PainelService painelService;
    private final ProdutoService produtoService;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final AplicadorCupomPedidoService aplicadorCupomService;
    private final CalculadoraTaxaDescontoPedidoService calculadoraTaxaService;
    private final ProcessarPagamentoService processarPagamentoService;
    private final IClienteRepository clienteRepository;
    private final IProdutoRepository produtoRepository;
    private final IPedidoRepository pedidoRepository;

    public AplicacaoDelivery() {
        DatabaseManager db = new DatabaseManager(ARQUIVO_BANCO);
        IUsuarioRepository usuarioRepository = new UsuarioRepositorySqlite(db);
        clienteRepository = new ClienteRepositorySqlite(db);
        produtoRepository = new ProdutoRepositorySqlite(db);
        pedidoRepository = new PedidoRepositorySqlite(db);
        CupomRepositorySqlite cupomRepository = new CupomRepositorySqlite(db);

        sessaoService = new SessaoService();
        GeradorHashSenha geradorHashSenha = new GeradorHashSenha();

        // auditoria (US12): factory decide a modalidade, Observer assina os eventos
        ILogger logger = AuditoriaLoggerFactory.criarLoggerConfigurado();
        AuditoriaAdapter adapter = new AuditoriaAdapter(logger, sessaoService);
        AuditoriaPublisher auditoria = new AuditoriaPublisher();
        auditoria.registrar(new AuditoriaObserver(adapter));

        autenticacaoService = new AutenticacaoService(
                usuarioRepository, geradorHashSenha, sessaoService, auditoria);
        cadastroUsuarioService = new CadastroUsuarioService(
                usuarioRepository, geradorHashSenha, auditoria);
        guarda = new GuardaAcessoAdministrativo(sessaoService);
        gestaoUsuariosService = new GestaoUsuariosService(usuarioRepository, guarda);
        clienteService = new ClienteService(clienteRepository);
        painelService = new PainelService(pedidoRepository);
        produtoService = new ProdutoService(produtoRepository, guarda);
        movimentacaoEstoqueService = new MovimentacaoEstoqueService(
                produtoRepository, guarda, sessaoService);

        aplicadorCupomService = new AplicadorCupomPedidoService(cupomRepository);
        calculadoraTaxaService = new CalculadoraTaxaDescontoPedidoService();

        IGeradorAleatorio geradorAleatorio = new GeradorAleatorioPadrao();
        processarPagamentoService = new ProcessarPagamentoService(
                pedidoRepository, produtoRepository,
                new SimuladorPagamento(geradorAleatorio),
                new GeradorPrazoEntregaSimulado(geradorAleatorio),
                new GeradorIdTransacao(geradorAleatorio));

        // carga inicial: cupom EDUCAR10 dos cenarios de aceite (INSERT OR IGNORE)
        cupomRepository.adicionarCupom(new CupomDescontoPedido("EDUCAR10", 10.0,
                LocalDateTime.now().minusYears(1), LocalDateTime.now().plusYears(1)));
    }

    public void iniciar() {
        new LoginPresenter(new LoginFrame(), autenticacaoService, this).iniciar();
    }

    @Override
    public void abrirTelaInicio() {
        new InicioPresenter(new InicioFrame(), painelService, sessaoService, this).iniciar();
    }

    @Override
    public void abrirCadastroUsuario() {
        new CadastroUsuarioPresenter(new CadastroUsuarioDialog(), cadastroUsuarioService)
                .iniciar();
    }

    @Override
    public void abrirGestaoUsuarios() {
        new UsuariosPresenter(new UsuariosFrame(), gestaoUsuariosService, this).iniciar();
    }

    @Override
    public void abrirBuscaClientes() {
        new BuscaClientesPresenter(new BuscaClientesFrame(), clienteService, this).iniciar();
    }

    @Override
    public void abrirCadastroCliente(Long clienteId) {
        Cliente cliente = clienteId == null
                ? null
                : clienteRepository.buscarPorId(clienteId).orElse(null);
        new ClientePresenter(new ClienteDialog(), clienteService, cliente).iniciar();
    }

    @Override
    public void abrirBuscaProdutos() {
        new ProdutosPresenter(new ProdutosFrame(), produtoService, this).iniciar();
    }

    @Override
    public void abrirCadastroProduto(Long produtoId) {
        Produto produto = produtoId == null
                ? null
                : produtoRepository.buscarPorId(produtoId).orElse(null);
        new ProdutoPresenter(new ProdutoDialog(), produtoService, guarda, produto).iniciar();
    }

    @Override
    public void abrirMovimentacaoEstoque() {
        new MovimentacaoEstoquePresenter(new MovimentacaoEstoqueDialog(), produtoService,
                movimentacaoEstoqueService, guarda).iniciar();
    }

    @Override
    public void abrirNovoPedido() {
        new PedidoPresenter(new PedidoFrame(), clienteRepository, pedidoRepository,
                produtoService, aplicadorCupomService, calculadoraTaxaService,
                processarPagamentoService, this).iniciar();
    }

    @Override
    public void mostrarResultadoPagamento(Pagamento pagamento) {
        new PagamentoPresenter(new PagamentoDialog(), pagamento).iniciar();
    }
}
