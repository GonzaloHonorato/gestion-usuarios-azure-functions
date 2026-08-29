package cl.gestion.functions;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class Passwords {

    private static final int COSTO = 10;

    private Passwords() {
    }

    public static String cifrar(String password) {
        return BCrypt.withDefaults().hashToString(COSTO, password.toCharArray());
    }

    public static boolean coincide(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }
}
