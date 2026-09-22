package com.unla.tp_distribuidos.repository;

import com.unla.tp_distribuidos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Validaciones para asegurar documento y email únicos
    boolean existsByDocumento(Long documento);

    boolean existsByEmail(String email);

    // Busca un cliente activo por ID
    Optional<Cliente> findByIdAndActivoTrue(Long id);
}