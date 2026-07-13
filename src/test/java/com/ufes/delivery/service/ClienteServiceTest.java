package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    private ClienteRepositoryEmMemoria repositorio;
    private ClienteService service;

    @BeforeEach
    void preparar() {
        repositorio = new ClienteRepositoryEmMemoria();
        service = new ClienteService(repositorio);
    }

    private Cliente clienteValido(String nome, String cpf) {
        Cliente cliente = new Cliente(null, nome, cpf);
        cliente.adicionarEndereco(new Endereco(null, "Rua Fulano", "123", "Apto 101",
                "Centro", "Cidade Exemplo", "ES", "29000-000", true));
        return cliente;
    }

    @Test
    void salvaClienteComEnderecoPadrao() {
        Cliente salvo = service.salvar(clienteValido("Fulano de Tal", "000.000.000-00"));

        assertNotNull(salvo.getId());
        assertTrue(salvo.getEnderecoPadrao().isPresent());
    }

    @Test
    void rejeitaCpfDuplicado() {
        service.salvar(clienteValido("Fulano de Tal", "000.000.000-00"));

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.salvar(clienteValido("Outro Cliente", "000.000.000-00")));

        assertEquals(ClienteService.MSG_CPF_DUPLICADO, erro.getMessage());
    }

    @Test
    void permiteAtualizarMantendoOMesmoCpf() {
        Cliente salvo = service.salvar(clienteValido("Fulano de Tal", "000.000.000-00"));
        salvo.setNome("Fulano de Tal Segundo");

        assertDoesNotThrow(() -> service.salvar(salvo));
        assertEquals("Fulano de Tal Segundo",
                repositorio.buscarPorId(salvo.getId()).orElseThrow().getNome());
    }

    @Test
    void buscaPorNomeParcial() {
        service.salvar(clienteValido("Fulano de Tal", "000.000.000-00"));
        service.salvar(clienteValido("Fulana da Silva", "111.111.111-11"));
        service.salvar(clienteValido("Beltrano", "529.982.247-25"));

        List<Cliente> resultado = service.buscarPorNome("Ful");

        assertEquals(2, resultado.size());
    }

    @Test
    void buscaPorCpfComMascaraConsideraSomenteOsDigitos() {
        service.salvar(clienteValido("Fulano de Tal", "000.000.000-00"));

        Optional<Cliente> resultado = service.buscarPorCpf("000.000.000-00");

        assertTrue(resultado.isPresent());
        assertEquals("Fulano de Tal", resultado.get().getNome());
    }

    @Test
    void rejeitaBuscaComCpfInvalido() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> service.buscarPorCpf("123.456.789-00"));

        assertEquals(ClienteService.MSG_CPF_INVALIDO, erro.getMessage());
    }

    @Test
    void rejeitaBuscaComValorAusente() {
        assertThrows(ValidacaoException.class, () -> service.buscarPorNome("  "));
        assertThrows(ValidacaoException.class, () -> service.buscarPorCpf(null));
    }

    @Test
    void rejeitaSalvarSemEnderecoPadrao() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");
        cliente.adicionarEndereco(new Endereco(null, "Rua A", "1", "",
                "Centro", "Cidade", "ES", "29000-000", false));

        assertThrows(IllegalStateException.class, () -> service.salvar(cliente));
    }
}
