package com.ufes.delivery.validacao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorCepTest {

    @Test
    void aceitaCepComOuSemMascara() {
        assertTrue(ValidadorCep.ehValido("29000-000"));
        assertTrue(ValidadorCep.ehValido("29000000"));
    }

    @Test
    void rejeitaTamanhoInvalido() {
        assertFalse(ValidadorCep.ehValido("2900-000"));
        assertFalse(ValidadorCep.ehValido(""));
        assertFalse(ValidadorCep.ehValido(null));
    }

    @Test
    void formataParaMascaraPadrao() {
        assertEquals("29000-000", ValidadorCep.formatar("29000000"));
    }
}
