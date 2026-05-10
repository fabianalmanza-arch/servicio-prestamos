package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Pago;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("""
        SELECT p FROM Pago p
        WHERE LOWER(p.prestamo.cliente.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%'))
           OR CAST(p.prestamo.id AS string) LIKE CONCAT('%', :query, '%')
    """)
    List<Pago> buscarPorClienteONumero(String query);

    boolean existsByNumeroOperacion(String numeroOperacion);

    // 🔹 Total cobrado por empleado y rango de fechas
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.prestamo.cliente.empleado.id = :empleadoId
          AND p.fechaPago BETWEEN :desde AND :hasta
    """)
    Double obtenerTotalCobradoPorEmpleadoYRango(
            @Param("empleadoId") Long empleadoId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}

