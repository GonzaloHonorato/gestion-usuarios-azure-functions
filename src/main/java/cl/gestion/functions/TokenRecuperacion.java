package cl.gestion.functions;

import java.time.Instant;

public record TokenRecuperacion(String token, long usuarioId, Instant expiraEn, boolean usado) {

    public boolean vigente() {
        return !usado && Instant.now().isBefore(expiraEn);
    }
}
