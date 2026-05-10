/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.model.CuentaBancaria;
import com.inversionesanyelo.sistemaprestamos.service.CuentaBancariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cuentas")
public class CuentaBancariaController {

    @Autowired
    private CuentaBancariaService cuentaBancariaService;

    // 🔹 Ver lista de cuentas (para mostrar en Thymeleaf o JSON)
    @GetMapping
    public String listarCuentas(Model model) {
        List<CuentaBancaria> cuentas = cuentaBancariaService.listarTodas();
        model.addAttribute("cuentas", cuentas);
        return "cuentas"; // nombre del template Thymeleaf (cuentas.html)
    }

    // 🔹 API REST: devolver todas las cuentas como JSON (para AJAX, por ejemplo)
    @GetMapping("/api")
    @ResponseBody
    public List<CuentaBancaria> obtenerTodasJson() {
        return cuentaBancariaService.listarTodas();
    }

    // 🔹 Endpoint para actualizar saldo de una cuenta
    @PostMapping("/actualizar-saldo")
    @ResponseBody
    public ResponseEntity<String> actualizarSaldo(
            @RequestParam Long id,
            @RequestParam Double nuevoSaldo) {

        try {
            cuentaBancariaService.actualizarSaldo(id, nuevoSaldo);
            return ResponseEntity.ok("Saldo actualizado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
        // 🔹 Obtener saldo actual de una cuenta por nombre (para validar antes de gasto)
    @GetMapping("/saldo")
    @ResponseBody
    public ResponseEntity<Double> obtenerSaldoPorNombre(@RequestParam String nombre) {
        try {
            CuentaBancaria cuenta = cuentaBancariaService
                    .buscarPorNombre(nombre)
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + nombre));
            return ResponseEntity.ok(cuenta.getSaldo());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0.0);
        }
    }
    
    // 🔹 Endpoint para restar saldo según nombre de cuenta (para registrar gastos)
    @PostMapping("/restar-saldo")
    @ResponseBody
    public ResponseEntity<String> restarSaldo(
            @RequestParam String nombre,
            @RequestParam Double monto) {

        try {
            cuentaBancariaService.restarSaldoPorNombre(nombre, monto);
            return ResponseEntity.ok("Saldo actualizado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al restar saldo: " + e.getMessage());
        }
    }

    // 🔹 Endpoint para reiniciar todos los saldos a 0.0
    @PostMapping("/reiniciar")
    @ResponseBody
    public ResponseEntity<String> reiniciarSaldos() {
        cuentaBancariaService.reiniciarSaldos();
        return ResponseEntity.ok("Todos los saldos fueron reiniciados a 0.00");
    }
}
