package com.ufes.delivery.service;

import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.repository.IClienteRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Fake em memoria para testes do ClienteService.
 */
class ClienteRepositoryEmMemoria implements IClienteRepository {

    private final Map<Long, Cliente> dados = new HashMap<>();
    private long proximoId = 1;

    @Override
    public Cliente salvar(Cliente cliente) {
        cliente.setId(proximoId++);
        dados.put(cliente.getId(), cliente);
        return cliente;
    }

    @Override
    public void atualizar(Cliente cliente) {
        dados.put(cliente.getId(), cliente);
    }

    @Override
    public void excluir(Long id) {
        dados.remove(id);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return dados.values().stream()
                .filter(c -> c.getCpf().equals(cpf))
                .findFirst();
    }

    @Override
    public List<Cliente> buscarPorNome(String trecho) {
        String filtro = trecho.toLowerCase(Locale.ROOT);
        return dados.values().stream()
                .filter(c -> c.getNome().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }

    @Override
    public List<Cliente> buscarTodos() {
        return new ArrayList<>(dados.values());
    }
}
