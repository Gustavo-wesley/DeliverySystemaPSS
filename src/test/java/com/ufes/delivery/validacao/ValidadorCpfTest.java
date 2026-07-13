package com.ufes.delivery.validacao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorCpfTest {

    @Test
    void aceitaCpfValidoComMascara() {
        assertTrue(ValidadorCpf.ehValido("111.111.111-11"));
        assertTrue(ValidadorCpf.ehValido("000.000.000-00"));
    }

    @Test
    void aceitaCpfValidoSemMascara() {
        assertTrue(ValidadorCpf.ehValido("11111111111"));
        assertTrue(ValidadorCpf.ehValido("52998224725"));
    }

    @Test
    void rejeitaDigitoVerificadorErrado() {
        assertFalse(ValidadorCpf.ehValido("111.111.111-12"));
        assertFalse(ValidadorCpf.ehValido("52998224724"));
    }

    @Test
    void rejeitaTamanhoDiferenteDeOnzeDigitos() {
        assertFalse(ValidadorCpf.ehValido("123"));
        assertFalse(ValidadorCpf.ehValido(""));
        assertFalse(ValidadorCpf.ehValido(null));
        assertFalse(ValidadorCpf.ehValido("123456789012"));
    }

    @Test
    void normalizaMantendoSomenteDigitos() {
        assertEquals("11111111111", ValidadorCpf.normalizar("111.111.111-11"));
    }

    @Test
    void formataParaMascaraPadrao() {
        assertEquals("529.982.247-25", ValidadorCpf.formatar("52998224725"));
    }
}
