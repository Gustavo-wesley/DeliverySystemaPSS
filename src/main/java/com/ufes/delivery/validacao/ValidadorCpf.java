package com.ufes.delivery.validacao;

/**
 * Validacao de CPF pelos digitos verificadores. Aceita valor com ou
 * sem mascara e normaliza para 11 digitos.
 *
 * Observacao: a validacao segue estritamente o calculo dos digitos
 * verificadores, conforme os cenarios da especificacao (que usam
 * 000.000.000-00 e 111.111.111-11 como CPFs validos de exemplo).
 */
public final class ValidadorCpf {

    private ValidadorCpf() {
    }

    /**
     * Remove a mascara, mantendo somente os digitos.
     */
    public static String normalizar(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    public static boolean ehValido(String cpf) {
        String digitos = normalizar(cpf);

        if (digitos.length() != 11) {
            return false;
        }

        int dv1 = calcularDigito(digitos, 9, 10);
        int dv2 = calcularDigito(digitos, 10, 11);

        return dv1 == Character.getNumericValue(digitos.charAt(9))
                && dv2 == Character.getNumericValue(digitos.charAt(10));
    }

    public static String formatar(String cpf) {
        String digitos = normalizar(cpf);
        if (digitos.length() != 11) {
            return cpf;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    private static int calcularDigito(String digitos, int quantidade, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (int i = 0; i < quantidade; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * peso;
            peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
