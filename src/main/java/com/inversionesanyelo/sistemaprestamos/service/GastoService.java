/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.Gasto;
import com.inversionesanyelo.sistemaprestamos.repository.GastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Fabian
 */
@Service
public class GastoService {

    @Autowired
    private GastoRepository gastoRepository;

    // =============================
    // 🔹 Registrar nuevo gasto
    // =============================
    public Gasto registrar(Gasto gasto) {
        return gastoRepository.save(gasto);
    }

    // =============================
    // 🔹 Listar todos los gastos
    // =============================
    public List<Gasto> listarTodos() {
        return gastoRepository.findAllByOrderByFechaDesc();
    }

    // =============================
    // 🔹 Filtrar por tipo
    // =============================
    public List<Gasto> listarPorTipo(String tipo) {
        return gastoRepository.findByTipoOrderByFechaDesc(tipo);
    }

    // =============================
    // 🔹 Buscar por ID
    // =============================
    public Optional<Gasto> buscarPorId(Long id) {
        return gastoRepository.findById(id);
    }

    // =============================
    // 🔹 Eliminar un gasto
    // =============================
    public void eliminar(Long id) {
        gastoRepository.deleteById(id);
    }
}
