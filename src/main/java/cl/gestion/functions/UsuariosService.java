package cl.gestion.functions;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UsuariosService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int LARGO_MINIMO_PASSWORD = 8;

    private final UsuarioRepositorio repositorio;
    private final PublicadorEventos publicador;

    public UsuariosService(UsuarioRepositorio repositorio, PublicadorEventos publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public List<Usuario> listar() {
        return repositorio.listar();
    }

    public Optional<Usuario> obtener(long id) {
        return repositorio.porId(id);
    }

    public Usuario crear(CrearUsuario datos) {
        validarTexto(datos.nombre(), "nombre");
        validarTexto(datos.apellido(), "apellido");
        validarEmail(datos.email());
        validarPassword(datos.password());

        if (repositorio.existeEmail(datos.email())) {
            throw new EmailDuplicadoException(datos.email());
        }

        Usuario creado = repositorio.crear(datos, Passwords.cifrar(datos.password()));
        publicador.usuarioCreado(creado.id(), creado.email(), creado.nombre());
        return creado;
    }

    public boolean actualizar(long id, ActualizarUsuario datos) {
        validarTexto(datos.nombre(), "nombre");
        validarTexto(datos.apellido(), "apellido");
        validarEmail(datos.email());
        return repositorio.actualizar(id, datos);
    }

    public boolean desactivar(long id) {
        return repositorio.desactivar(id);
    }

    private static void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio");
        }
    }

    private static void validarEmail(String email) {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new DatosInvalidosException("El correo no tiene un formato valido");
        }
    }

    private static void validarPassword(String password) {
        if (password == null || password.length() < LARGO_MINIMO_PASSWORD) {
            throw new DatosInvalidosException(
                "La contrasena debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres");
        }
    }
}
