/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.repository.EmpleadoRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.inversionesanyelo.sistemaprestamos.repository.PagoRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PrestamoRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author fabia
 */
@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    public Empleado guardar(Empleado empleado) {
        if (empleado.getId() == null) { // nuevo empleado
            empleado.setPassword(passwordEncoder.encode(empleado.getPassword()));
            empleado.setRol("Empleado");
            empleado.setFirmaId(null);
        } else {
            // si se edita y cambia la contraseña, vuelve a codificarla
            Empleado existente = empleadoRepository.findById(empleado.getId()).orElse(null);
            if (existente != null && !empleado.getPassword().equals(existente.getPassword())) {
                empleado.setPassword(passwordEncoder.encode(empleado.getPassword()));
            }
        }
        if (empleado.getTipoEmpleado() == null || empleado.getTipoEmpleado().isBlank()) {
            empleado.setTipoEmpleado("user");
        }
        return empleadoRepository.save(empleado);
    }

    public Empleado buscarPorId(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }

    public void eliminar(Long id) {
        empleadoRepository.deleteById(id);
    }
    
    public List<Empleado> buscarEmpleados(String query) {
        return empleadoRepository.buscarEmpleados(query);
    }
    
    // ==============================================================
    //  MÉTODOS NUEVOS PARA ACTIVAR/DESACTIVAR Y CALCULAR MONTO
    // ==============================================================

    @Transactional
    public void activarEmpleado(Long id, Double montoInicial) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        empleado.setActivo(true);
        empleado.setMontoInicialEfectivo(montoInicial != null ? BigDecimal.valueOf(montoInicial) : BigDecimal.ZERO);
        empleadoRepository.save(empleado);
    }

    @Transactional
    public void desactivarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
    }

    /**
     * Calcula el monto disponible actual de un empleado:
     * montoInicial + total cobros en efectivo - total préstamos entregados en efectivo
     */
    public Double calcularMontoDisponible(Long idEmpleado) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // 🔹 Total de cobros (pagos) en efectivo realizados por este empleado
        BigDecimal totalCobrosEfectivo = pagoRepository.findAll().stream()
                .filter(p -> p.getPrestamo() != null
                        && p.getPrestamo().getEmpleado() != null
                        && p.getPrestamo().getEmpleado().getId().equals(idEmpleado))
                .map(p -> p.getMontoEfectivo() != null ? BigDecimal.valueOf(p.getMontoEfectivo()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🔹 Total de préstamos otorgados en efectivo por este empleado
        BigDecimal totalPrestamosEfectivo = prestamoRepository.findAll().stream()
                .filter(p -> p.getEmpleado() != null
                        && p.getEmpleado().getId().equals(idEmpleado))
                .map(p -> p.getMontoEfectivo() != null ? BigDecimal.valueOf(p.getMontoEfectivo()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🔹 Monto inicial (BigDecimal)
        BigDecimal montoInicial = empleado.getMontoInicialEfectivo() != null
                ? empleado.getMontoInicialEfectivo()
                : BigDecimal.ZERO;

        // 🔹 Fórmula: inicial + cobros - préstamos
        BigDecimal disponible = montoInicial.add(totalCobrosEfectivo).subtract(totalPrestamosEfectivo);

        // 🔹 Convertimos a Double solo al final para retornarlo
        return disponible.doubleValue();
    }
    
    public void sumarEfectivo(Long idEmpleado, Double monto) {
        if (monto == null || monto <= 0) return;

        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        BigDecimal actual = empleado.getMontoInicialEfectivo() != null 
                ? empleado.getMontoInicialEfectivo() 
                : BigDecimal.ZERO;

        empleado.setMontoInicialEfectivo(actual.add(BigDecimal.valueOf(monto)));
        empleadoRepository.save(empleado);
    }

    
}
