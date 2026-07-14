package com.ufes.delivery.ui.presenter;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.service.ClienteService;
import com.ufes.delivery.ui.view.IClienteView;
import com.ufes.delivery.validacao.ValidadorCpf;
import java.util.List;
import java.util.Objects;

/**
 * Presenter do cadastro de cliente (US06). Monta o agregado a partir
 * das linhas digitadas e delega as invariantes (1 a 3 enderecos,
 * exatamente um padrao, CPF unico) ao dominio e ao service.
 */
public class ClientePresenter {

    private final IClienteView view;
    private final ClienteService clienteService;
    private final Cliente clienteEmEdicao; // null quando e cadastro novo

    public ClientePresenter(IClienteView view, ClienteService clienteService,
            Cliente clienteEmEdicao) {
        this.view = Objects.requireNonNull(view, "View deve ser informada");
        this.clienteService = Objects.requireNonNull(clienteService,
                "Serviço de clientes deve ser informado");
        this.clienteEmEdicao = clienteEmEdicao;

        view.setAoSalvar(this::salvar);
        view.setAoCancelar(view::fechar);
    }

    public void iniciar() {
        if (clienteEmEdicao != null) {
            view.setNome(clienteEmEdicao.getNome());
            view.setCpf(ValidadorCpf.formatar(clienteEmEdicao.getCpf()));

            List<IClienteView.LinhaEndereco> linhas = clienteEmEdicao.getEnderecos().stream()
                    .map(e -> new IClienteView.LinhaEndereco(
                            e.isPadrao(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                            e.getBairro(), e.getCidade(), e.getUf(), e.getCep()))
                    .toList();
            view.setEnderecos(linhas);
        }
        view.abrir();
    }

    private void salvar() {
        try {
            Long id = clienteEmEdicao == null ? null : clienteEmEdicao.getId();
            Cliente cliente = new Cliente(id, view.getNome(), view.getCpf());

            // aqui percorre as linhas da tabela montando os enderecos
            for (IClienteView.LinhaEndereco linha : view.getEnderecos()) {
                if (linha.estaVazia()) {
                    continue;
                }
                cliente.adicionarEndereco(new Endereco(null,
                        linha.logradouro(), linha.numero(), linha.complemento(),
                        linha.bairro(), linha.cidade(), linha.uf(), linha.cep(),
                        linha.padrao()));
            }

            clienteService.salvar(cliente);
            view.mostrarInformacao("Cliente salvo com sucesso");
            view.fechar();
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            // os campos validos digitados permanecem na tela
            view.mostrarErro(e.getMessage());
        }
    }
}
