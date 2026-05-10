/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author fabia
 */
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    @Query("""
    SELECT e FROM Empleado e
    WHERE LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(CONCAT('emp_', LPAD(CAST(e.id AS string), 3, '0'))) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Empleado> buscarEmpleados(@Param("query") String query);
    
    Empleado findByCorreo(String correo);
}
