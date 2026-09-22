package com.unla.tp_distribuidos.repository;

import com.unla.tp_distribuidos.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Busca el historial de reservas de un cliente específico
    List<Reserva> findByClienteId(Long clienteId);

    // Consulta JPQL para verificar solapamiento de fechas en el mismo vehículo
    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
           "WHERE r.vehiculo.id = :vehiculoId " +
           "AND r.estado != 'CANCELADA' " +
           "AND (:fechaInicio < r.fechaHoraFin AND :fechaFin > r.fechaHoraInicio)")
    boolean existeSolapamiento(@Param("vehiculoId") Long vehiculoId,
                               @Param("fechaInicio") LocalDateTime fechaInicio,
                               @Param("fechaFin") LocalDateTime fechaFin);

    // Retorna los IDs de vehículos con reservas activas (no canceladas) solapadas en el rango
    @Query("SELECT DISTINCT r.vehiculo.id FROM Reserva r " +
           "WHERE r.estado != 'CANCELADA' " +
           "AND (:fechaInicio < r.fechaHoraFin AND :fechaFin > r.fechaHoraInicio)")
    List<Long> findVehiculosOcupadosEnRango(@Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin);
}