/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author fabia
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long>{
    
    @Query("SELECT MAX(c.id) FROM Cliente c")
    Long obtenerUltimoId();
    
    @Query("""
    SELECT c FROM Cliente c
    WHERE LOWER(c.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(CONCAT('cli_', LPAD(CAST(c.id AS string), 3, '0'))) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Cliente> buscarClientes(@Param("query") String query);
}
