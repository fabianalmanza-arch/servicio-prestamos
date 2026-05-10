package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.model.Cronograma;
import com.inversionesanyelo.sistemaprestamos.service.CronogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/cronogramas")
public class CronogramaController {

    @Autowired
    private CronogramaService cronogramaService;

    // ==========================================================
    // 🔹 GENERAR CRONOGRAMA DE UN PRÉSTAMO
    // ==========================================================
    @PostMapping("/generar/{prestamoId}")
    public ResponseEntity<List<Cronograma>> generar(@PathVariable Long prestamoId) {
        return ResponseEntity.ok(cronogramaService.generarCronograma(prestamoId));
    }

    // ==========================================================
    // 🔹 LISTAR CUOTAS DE UN PRÉSTAMO
    // ==========================================================
    @GetMapping("/prestamo/{prestamoId}")
    public ResponseEntity<List<Map<String, Object>>> listarPorPrestamo(@PathVariable Long prestamoId) {
        List<Cronograma> cuotas = cronogramaService.listarPorPrestamo(prestamoId);
        List<Map<String, Object>> lista = new ArrayList<>();

        for (Cronograma c : cuotas) {
            Map<String, Object> map = new HashMap<>();
            map.put("codigoCliente", c.getPrestamo().getCliente().getCodigoCliente());
            map.put("numeroCuota", c.getNumeroCuota());
            map.put("fechaProgramada", c.getFechaProgramada());
            map.put("capital", c.getCapital());
            map.put("interes", c.getInteres());
            map.put("montoTotal", c.getMontoTotal());
            map.put("saldoPendiente", c.getSaldoPendiente());
            map.put("estado", c.getEstado());
            map.put("detalleEfectivo", c.getDetalleEfectivo() != null ? c.getDetalleEfectivo() : "-");
            map.put("detalleTransferencia", c.getDetalleTransferencia() != null ? c.getDetalleTransferencia() : "-");
            lista.add(map);
        }

        return ResponseEntity.ok(lista);
    }

    // ==========================================================
    // 🔹 OBTENER DEUDA TOTAL DE UN PRÉSTAMO
    // ==========================================================
    @GetMapping("/deuda-total/{prestamoId}")
    public Map<String, Object> obtenerDeudaTotal(@PathVariable Long prestamoId) {
        double deuda = cronogramaService.obtenerDeudaTotal(prestamoId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("deuda", deuda);
        return resp;
    }

    // ==========================================================
    // 🔹 LISTAR CUOTAS PENDIENTES O PARCIALES
    // ==========================================================
    @GetMapping("/cuotas/{prestamoId}")
    public List<Map<String, Object>> listarCuotasPendientes(@PathVariable Long prestamoId) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Cronograma c : cronogramaService.listarCuotasPendientes(prestamoId)) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("numero", c.getNumeroCuota());
            map.put("fechaTeorica", c.getFechaProgramada());
            map.put("valor", c.getMontoTotal());
            lista.add(map);
        }
        return lista;
    }
}
