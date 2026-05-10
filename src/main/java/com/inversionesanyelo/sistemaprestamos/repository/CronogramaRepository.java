/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Cronograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

@Repository
public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {
    List<Cronograma> findByPrestamoId(Long prestamoId);
    // Todas las cuotas del préstamo (ordenadas)
    List<Cronograma> findByPrestamoIdOrderByNumeroCuotaAsc(Long prestamoId);

    // Cuotas filtradas por estado (pendiente, parcial, etc.)
    List<Cronograma> findByPrestamoIdAndEstadoInOrderByNumeroCuotaAsc(Long prestamoId, List<String> estados);
    
    @Query("""
        SELECT COUNT(c) FROM Cronograma c
        WHERE c.prestamo.id = :prestamoId
          AND c.estado <> 'pagada'
          AND c.fechaProgramada < :fechaActual
    """)
    long contarCuotasAtrasadas(@Param("prestamoId") Long prestamoId, 
                               @Param("fechaActual") LocalDate fechaActual);
    
    @Query("""
        SELECT c FROM Cronograma c
        JOIN c.prestamo p
        JOIN p.empleado e
        WHERE c.estado <> 'pagada'
          AND c.fechaProgramada < :fechaLimite
        ORDER BY e.id, p.id
    """)
    List<Cronograma> findCuotasVencidasMasDeDosDias(@Param("fechaLimite") LocalDate fechaLimite);

    // 🔹 Clientes atrasados (+2 días)
    @Query("""
        SELECT DISTINCT CONCAT(
            c.prestamo.cliente.nombreCompleto,
            ' (Préstamo #', c.prestamo.id, ')'
        )
        FROM Cronograma c
        WHERE c.prestamo.cliente.empleado.id = :empleadoId
          AND (c.estado = 'pendiente' OR c.estado = 'parcial')
          AND c.fechaProgramada < :fechaLimite
    """)
    List<String> obtenerClientesAtrasadosPorEmpleado(
            @Param("empleadoId") Long empleadoId,
            @Param("fechaLimite") LocalDate fechaLimite);


    // 🔹 Clientes sin pago hoy (no han pagado ninguna cuota hoy)
    @Query("""
        SELECT DISTINCT CONCAT(
            c.prestamo.cliente.nombreCompleto,
            ' (Préstamo #', c.prestamo.id, ')'
        )
        FROM Cronograma c
        WHERE c.prestamo.cliente.empleado.id = :empleadoId
          AND (c.estado = 'pendiente' OR c.estado = 'parcial')
          AND c.fechaProgramada <= :hoy
          AND (c.fechaPagoReal IS NULL OR c.fechaPagoReal <> :hoy)
    """)
    List<String> obtenerClientesSinPagoHoyPorEmpleado(
            @Param("empleadoId") Long empleadoId,
            @Param("hoy") LocalDate hoy);

}

