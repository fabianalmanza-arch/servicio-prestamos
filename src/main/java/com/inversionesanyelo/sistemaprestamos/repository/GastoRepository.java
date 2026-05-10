/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 *
 * @author Fabian
 */
@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {
    // 🔹 Filtros útiles
    List<Gasto> findByTipoOrderByFechaDesc(String tipo);
    List<Gasto> findAllByOrderByFechaDesc();
}
