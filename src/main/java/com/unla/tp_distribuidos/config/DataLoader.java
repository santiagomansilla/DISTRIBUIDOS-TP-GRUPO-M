package com.unla.tp_distribuidos.config;

import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.model.EstadoVehiculo;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.ClienteRepository;
import com.unla.tp_distribuidos.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(VehiculoRepository vehiculoRepo, ClienteRepository clienteRepo) {
        return args -> {
            if (vehiculoRepo.count() == 0) {
                // Vehículo 1
                Vehiculo v1 = new Vehiculo();
                v1.setPatente("AE123CD");
                v1.setMarca("Toyota");
                v1.setModelo("Corolla");
                v1.setAnio(2023);
                v1.setColor("Blanco");
                v1.setTipo(TipoVehiculo.SEDAN);
                v1.setPrecioDiario(new BigDecimal("45000.00"));
                v1.setEstado(EstadoVehiculo.DISPONIBLE);
                v1.setActivo(true);
                vehiculoRepo.save(v1);

                // Vehículo 2
                Vehiculo v2 = new Vehiculo();
                v2.setPatente("AF456GH");
                v2.setMarca("Ford");
                v2.setModelo("Ranger");
                v2.setAnio(2024);
                v2.setColor("Gris");
                v2.setTipo(TipoVehiculo.PICKUP);
                v2.setPrecioDiario(new BigDecimal("75000.00"));
                v2.setEstado(EstadoVehiculo.DISPONIBLE);
                v2.setActivo(true);
                vehiculoRepo.save(v2);

                // Vehículo 3
                Vehiculo v3 = new Vehiculo();
                v3.setPatente("AD789JK");
                v3.setMarca("Volkswagen");
                v3.setModelo("Taos");
                v3.setAnio(2022);
                v3.setColor("Azul");
                v3.setTipo(TipoVehiculo.SUV);
                v3.setPrecioDiario(new BigDecimal("60000.00"));
                v3.setEstado(EstadoVehiculo.DISPONIBLE);
                v3.setActivo(true);
                vehiculoRepo.save(v3);

                // Vehículo 4
                Vehiculo v4 = new Vehiculo();
                v4.setPatente("AC321MN");
                v4.setMarca("Peugeot");
                v4.setModelo("208");
                v4.setAnio(2021);
                v4.setColor("Rojo");
                v4.setTipo(TipoVehiculo.HATCHBACK);
                v4.setPrecioDiario(new BigDecimal("38000.00"));
                v4.setEstado(EstadoVehiculo.DISPONIBLE);
                v4.setActivo(true);
                vehiculoRepo.save(v4);
            }

            if (clienteRepo.count() == 0) {
                // Cliente 1
                Cliente c1 = new Cliente();
                c1.setDocumento(38450123L);
                c1.setNombre("Agustin");
                c1.setApellido("Gonzalez");
                c1.setEmail("agustin@unla.edu.ar");
                c1.setTelefono("1155443322");
                c1.setFechaNacimiento(LocalDate.of(1995, 5, 12));
                c1.setActivo(true);
                clienteRepo.save(c1);

                // Cliente 2
                Cliente c2 = new Cliente();
                c2.setDocumento(40123987L);
                c2.setNombre("Maria");
                c2.setApellido("Fernandez");
                c2.setEmail("maria.fernandez@gmail.com");
                c2.setTelefono("1166778899");
                c2.setFechaNacimiento(LocalDate.of(1998, 8, 24));
                c2.setActivo(true);
                clienteRepo.save(c2);
            }
        };
    }
}
