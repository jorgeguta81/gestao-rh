package rh.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Cifra e confere palavras-passe com PBKDF2-HMAC-SHA256 e sal aleatório (só JDK, sem bibliotecas). */
public final class Senha {
    private static final int ITERACOES = 120_000;
    private static final int BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    public static final int TAMANHO_MINIMO = 8;

    private Senha() {}

    public static String novoSal() {
        byte[] b = new byte[16];
        ALEATORIO.nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    public static String hash(String password, String sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(sal), ITERACOES, BITS);
            byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(h);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 indisponível neste JDK", e);
        }
    }

    /** Comparação em tempo constante. */
    public static boolean confere(String password, String sal, String hashGuardado) {
        return MessageDigest.isEqual(hash(password, sal).getBytes(), hashGuardado.getBytes());
    }

    /** Política mínima: 8+ caracteres, com letras e números. */
    public static void validarForca(String password) {
        if (password == null || password.length() < TAMANHO_MINIMO)
            throw new IllegalArgumentException("A palavra-passe deve ter pelo menos " + TAMANHO_MINIMO + " caracteres.");
        boolean letra = password.chars().anyMatch(Character::isLetter);
        boolean digito = password.chars().anyMatch(Character::isDigit);
        if (!letra || !digito)
            throw new IllegalArgumentException("A palavra-passe deve conter letras e números.");
    }

    /** Palavra-passe temporária legível (sem caracteres ambíguos). */
    public static String temporaria() {
        String letras = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz", nums = "23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(letras.charAt(ALEATORIO.nextInt(letras.length())));
        for (int i = 0; i < 3; i++) sb.append(nums.charAt(ALEATORIO.nextInt(nums.length())));
        return sb.toString();
    }
}
