package com.ufes.delivery.repository;

import com.ufes.delivery.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface IClienteRepository {

    /**
     * Persiste o cliente e seus enderecos em transacao unica.
     */
    Cliente salvar(Cliente cliente);

    /**
     * Atualiza o cliente e substitui os enderecos em transacao unica.
     */
    void atualizar(Cliente cliente);

    void excluir(Long id);

    Optional<Cliente> buscarPorId(Long id);

    /**
     * Busca pelo CPF ja normalizado (11 digitos).
     */
    Optional<Cliente> buscarPorCpf(String cpf);

    /**
     * Busca por trecho do nome, insensivel a maiusculas.
     */
    List<Cliente> buscarPorNome(String trecho);

    List<Cliente> buscarTodos();
}
