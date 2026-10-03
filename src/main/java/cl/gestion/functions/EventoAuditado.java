package cl.gestion.functions;

public record EventoAuditado(
    long id,
    String idEvento,
    String tipo,
    String subject,
    String instante,
    String datos
) {}
