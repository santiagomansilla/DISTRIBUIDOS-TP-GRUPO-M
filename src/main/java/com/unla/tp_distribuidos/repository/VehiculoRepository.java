package com.unla.tp_distribuidos.repository;

import com.unla.tp_distribuidos.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    // Comprueba si existe la patente para validaciones al crear
    boolean existsByPatente(String patente);

    // Busca un vehículo activo por su ID
    Optional<Vehiculo> findByIdAndActivoTrue(Long id);

    // Lista únicamente los vehículos activos (que no tienen baja lógica)
    List<Vehiculo> findByActivoTrue();
}
