package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.service.ClienteService;
import com.ufes.delivery.ui.view.IBuscaClientesView;
import com.ufes.delivery.validacao.ValidadorCpf;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Presenter da busca de clientes (US05): por nome (trecho) ou por
 * CPF exato, com o CPF validado antes da consulta.
 */
public class BuscaClientesPresenter {

    private final IBuscaClientesView view;
    private final ClienteService clienteService;
    private final INavegador navegador;

    public BuscaClientesPresenter(IBuscaClientesView view, ClienteService clienteService,
            INavegador navegador) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.clienteService = Objects.requireNonNull(clienteService,
                "Serviço de clientes deve ser informado");
        this.navegador = Objects.requireNonNull(navegador, "Navegador deve ser informado");

        view.setAoBuscar(this::buscar);
        view.setAoNovo(() -> navegador.abrirCadastroCliente(null));
        view.setAoVisualizar(this::visualizar);
        view.setAoFechar(view::fechar);
    }

    public void iniciar() {
        view.abrir();
    }

    private void buscar() {
        try {
            List<Cliente> encontrados = new ArrayList<>();

            if (IBuscaClientesView.CRITERIO_CPF.equals(view.getCriterioBusca())) {
                Optional<Cliente> cliente = clienteService.buscarPorCpf(view.getValorBusca());
                cliente.ifPresent(encontrados::add);
            } else {
                encontrados.addAll(clienteService.buscarPorNome(view.getValorBusca()));
            }

            if (encontrados.isEmpty()) {
                view.mostrarInformacao("Nenhum cliente encontrado para a busca informada");
            }
            mostrar(encontrados);
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void visualizar() {
        Long id = view.getIdSelecionado();
        if (id == null) {
            view.mostrarErro("Selecione um cliente na lista de resultados");
            return;
        }
        navegador.abrirCadastroCliente(id);
    }

    private void mostrar(List<Cliente> clientes) {
        List<IBuscaClientesView.LinhaCliente> linhas = clientes.stream()
                .map(c -> new IBuscaClientesView.LinhaCliente(
                        c.getId(), c.getNome(), ValidadorCpf.formatar(c.getCpf())))
                .toList();
        view.mostrarResultados(linhas);
    }
}
