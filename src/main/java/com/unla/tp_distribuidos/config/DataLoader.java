package com.unla.tp_distribuidos.config;

import com.unla.tp_distribuidos.enums.Rol;
import com.unla.tp_distribuidos.model.Usuario;
import com.unla.tp_distribuidos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository) {
        return args -> {
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                admin.setRol(Rol.ROLE_ADMIN);
                admin.setActivo(true);
                admin.setCliente(null);

                usuarioRepository.save(admin);
                System.out.println(">>> [DataLoader] Usuario Administrador creado con éxito (admin / admin123)");
            }
        };
    }
}