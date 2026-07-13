package com.ufes.delivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Endereco novoEndereco(String logradouro, boolean padrao) {
        return new Endereco(null, logradouro, "123", "Apto 101",
                "Centro", "Cidade Exemplo", "ES", "29000-000", padrao);
    }

    @Test
    void clienteValidoComUmEnderecoPadrao() {
        Cliente cliente = new Cliente(null, "Fulano de Tal", "000.000.000-00");
        cliente.adicionarEndereco(novoEndereco("Rua Fulano", true));

        assertDoesNotThrow(cliente::validarParaSalvar);
        assertEquals("Rua Fulano", cliente.getEnderecoPadrao().orElseThrow().getLogradouro());
        assertEquals("00000000000", cliente.getCpf());
    }

    @Test
    void rejeitaCpfInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente(null, "Fulano", "123.456.789-00"));
    }

    @Test
    void rejeitaSalvarSemEnderecoPadrao() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");
        cliente.adicionarEndereco(novoEndereco("Rua A", false));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                cliente::validarParaSalvar);
        assertEquals(Cliente.MSG_ENDERECO_PADRAO_OBRIGATORIO, erro.getMessage());
    }

    @Test
    void rejeitaSalvarSemEndereco() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");

        assertThrows(IllegalStateException.class, cliente::validarParaSalvar);
    }

    @Test
    void rejeitaQuartoEndereco() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");
        cliente.adicionarEndereco(novoEndereco("Rua A", true));
        cliente.adicionarEndereco(novoEndereco("Rua B", false));
        cliente.adicionarEndereco(novoEndereco("Rua C", false));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> cliente.adicionarEndereco(novoEndereco("Rua D", false)));
        assertEquals(Cliente.MSG_LIMITE_ENDERECOS, erro.getMessage());
    }

    @Test
    void definirPadraoDesmarcaOsDemais() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");
        Endereco a = novoEndereco("Rua A", true);
        Endereco b = novoEndereco("Rua B", false);
        cliente.adicionarEndereco(a);
        cliente.adicionarEndereco(b);

        cliente.definirEnderecoPadrao(b);

        assertFalse(a.isPadrao());
        assertTrue(b.isPadrao());
        assertDoesNotThrow(cliente::validarParaSalvar);
    }

    @Test
    void adicionarEnderecoPadraoDesmarcaAnterior() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");
        Endereco a = novoEndereco("Rua A", true);
        cliente.adicionarEndereco(a);
        cliente.adicionarEndereco(novoEndereco("Rua B", true));

        assertFalse(a.isPadrao());
        assertDoesNotThrow(cliente::validarParaSalvar);
    }

    @Test
    void bairroVemDoEnderecoPadraoParaAsStrategiesDoCr1() {
        Cliente cliente = new Cliente(null, "Fulano", "000.000.000-00");
        cliente.adicionarEndereco(novoEndereco("Rua A", true));

        assertEquals("Centro", cliente.getBairro());
    }

    @Test
    void rejeitaEnderecoComUfOuCepInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Endereco(null,
                "Rua A", "1", "", "Centro", "Cidade", "XX", "29000-000", false));
        assertThrows(IllegalArgumentException.class, () -> new Endereco(null,
                "Rua A", "1", "", "Centro", "Cidade", "ES", "2900", false));
    }
}
