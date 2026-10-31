package com.unla.tp_distribuidos.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.unla.tp_distribuidos.dto.LoginResponseDTO;
import com.unla.tp_distribuidos.model.Usuario;
import com.unla.tp_distribuidos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // Inyección explícita por constructor sin Lombok
    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO autenticar(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Usuario y contraseña no pueden estar vacíos");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        Usuario usuario = usuarioOpt.get();

       // Verificación híbrida: BCrypt o texto plano (para compatibilidad con seeds/admin)
        boolean passwordValida = false;
        if (usuario.getPassword().startsWith("$2a$") || usuario.getPassword().startsWith("$2b$")) {
            passwordValida = passwordEncoder.matches(password, usuario.getPassword());
        } else {
            passwordValida = usuario.getPassword().equals(password);
        }

        if (!passwordValida) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        // Validación de usuario activo
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException("El usuario se encuentra inhabilitado");
        }

        // Si el usuario está asociado a un Cliente, extraemos su ID de negocio
        Long clienteId = (usuario.getCliente() != null) ? usuario.getCliente().getId() : null;

        String rolNombre = (usuario.getRol() != null) ? usuario.getRol().name() : "CLIENTE";
        String dummyToken = "token-auth-" + usuario.getUsername();

        return new LoginResponseDTO(
                usuario.getUsername(),
                rolNombre,
                clienteId,
                dummyToken
        );
    }
}