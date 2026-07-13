package com.ufes.delivery.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gera e verifica hash de senha com SHA-256 + salt aleatorio.
 * A senha em texto aberto nunca e armazenada nem registrada em log.
 */
public class GeradorHashSenha {

    private static final int TAMANHO_SALT = 16;

    private final SecureRandom random = new SecureRandom();

    public String gerarSalt() {
        byte[] salt = new byte[TAMANHO_SALT];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String gerarHash(char[] senha, String salt) {
        if (senha == null || senha.length == 0) {
            throw new IllegalArgumentException("Senha deve ser informada");
        }
        if (salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("Salt deve ser informado");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            digest.update(new String(senha).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash indisponível", e);
        }
    }

    public boolean verificar(char[] senha, String salt, String hashEsperado) {
        String hashCalculado = gerarHash(senha, salt);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(StandardCharsets.UTF_8),
                hashEsperado.getBytes(StandardCharsets.UTF_8));
    }
}
