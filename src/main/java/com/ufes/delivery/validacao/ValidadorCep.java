package com.ufes.delivery.validacao;

/**
 * Validacao de CEP: oito digitos, aceitos com ou sem mascara.
 */
public final class ValidadorCep {

    private ValidadorCep() {
    }

    public static String normalizar(String cep) {
        if (cep == null) {
            return "";
        }
        return cep.replaceAll("\\D", "");
    }

    public static boolean ehValido(String cep) {
        return normalizar(cep).length() == 8;
    }

    public static String formatar(String cep) {
        String digitos = normalizar(cep);
        if (digitos.length() != 8) {
            return cep;
        }
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }
}
