package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.repository.IClienteRepository;
import com.ufes.delivery.validacao.ValidadorCpf;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Busca (US05) e manutencao (US06) de clientes.
 */
public class ClienteService {

    public static final String MSG_CPF_INVALIDO = "CPF inválido";
    public static final String MSG_CPF_DUPLICADO = "CPF já está vinculado a cliente existente";
    public static final String MSG_VALOR_BUSCA_OBRIGATORIO = "O valor da busca é obrigatório";

    private final IClienteRepository clienteRepository;

    public ClienteService(IClienteRepository clienteRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository,
                "Repositório de clientes deve ser informado");
    }

    public List<Cliente> buscarPorNome(String valor) {
        String trecho = exigirValor(valor);

        if (trecho.length() < 2 || trecho.length() > 120) {
            throw new ValidacaoException("Nome para busca deve conter de 2 a 120 caracteres");
        }
        if (!trecho.matches("^[\\p{L}' \\-]+$")) {
            throw new ValidacaoException("Nome aceita apenas letras, espaços, apóstrofos e hífens");
        }

        return clienteRepository.buscarPorNome(trecho);
    }

    public Optional<Cliente> buscarPorCpf(String valor) {
        String informado = exigirValor(valor);

        if (!ValidadorCpf.ehValido(informado)) {
            throw new ValidacaoException(MSG_CPF_INVALIDO);
        }

        return clienteRepository.buscarPorCpf(ValidadorCpf.normalizar(informado));
    }

    public Cliente salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente deve ser informado");
        cliente.validarParaSalvar();
        exigirCpfUnico(cliente);

        if (cliente.getId() == null) {
            return clienteRepository.salvar(cliente);
        }

        clienteRepository.atualizar(cliente);
        return cliente;
    }

    public void excluir(Long id) {
        if (id == null || clienteRepository.buscarPorId(id).isEmpty()) {
            throw new ValidacaoException("Cliente não encontrado");
        }
        clienteRepository.excluir(id);
    }

    private void exigirCpfUnico(Cliente cliente) {
        Optional<Cliente> existente = clienteRepository.buscarPorCpf(cliente.getCpf());

        if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
            throw new ValidacaoException(MSG_CPF_DUPLICADO);
        }
    }

    private String exigirValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacaoException(MSG_VALOR_BUSCA_OBRIGATORIO);
        }
        return valor.trim();
    }
}
