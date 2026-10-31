package com.unla.tp_distribuidos.service;
import java.time.LocalDate;
import java.util.Optional;
import com.unla.tp_distribuidos.model.Usuario;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.service.ClienteService;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.unla.tp_distribuidos.dto.ClienteDTO;
import com.unla.tp_distribuidos.repository.ClienteRepository;
import com.unla.tp_distribuidos.repository.UsuarioRepository;
import com.unla.tp_distribuidos.dto.RegistroRequestDto;
import com.unla.tp_distribuidos.enums.Rol;

@Service
public class RegistroService {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public RegistroService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, ClienteService clienteService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
    }
    @Transactional 
      public Usuario crearUsuario(RegistroRequestDto dto) {
        if (clienteRepository.existsByDocumento(Long.parseLong(dto.getDocumento().trim()))) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento: " + dto.getDocumento());
        }

        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el email: " + dto.getEmail());
        }
       
         if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el username: " + dto.getUsername());
        }

        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setDocumento(Long.parseLong(dto.getDocumento().trim()));
        clienteDTO.setNombre(dto.getNombre());
        clienteDTO.setApellido(dto.getApellido());
        clienteDTO.setEmail(dto.getEmail());
        clienteDTO.setTelefono(dto.getTelefono());
        clienteDTO.setFechaNacimiento(LocalDate.parse(dto.getFechaNacimiento().trim()));

       Cliente cliente = clienteService.crearCliente(clienteDTO); 
        
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(Rol.ROLE_CLIENTE);
        usuario.setActivo(true);
        usuario.setCliente(cliente);
        return usuarioRepository.save(usuario);
    }




}
