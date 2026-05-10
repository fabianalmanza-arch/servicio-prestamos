package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.model.Cronograma;
import com.inversionesanyelo.sistemaprestamos.model.CuentaBancaria;
import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.service.EmpleadoService;
import com.inversionesanyelo.sistemaprestamos.service.CronogramaService;
import com.inversionesanyelo.sistemaprestamos.service.CuentaBancariaService;
import com.inversionesanyelo.sistemaprestamos.service.PagoService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador principal del panel de inicio
 * 
 * Muestra el dashboard con empleados, cuentas bancarias
 * y provee endpoint /api/resumen para las tarjetas dinámicas.
 * 
 * @author fabia
 */
@Controller
@RequestMapping("/")
public class InicioController {

    private final EmpleadoService empleadoService;
    private final CronogramaService cronogramaService;
    private final PagoService pagoService;
    private final CuentaBancariaService cuentaBancariaService;

    public InicioController(EmpleadoService empleadoService, 
                                CronogramaService cronogramaService, 
                                PagoService pagoService, 
                                CuentaBancariaService cuentaBancariaService) {
            this.empleadoService = empleadoService;
            this.cronogramaService = cronogramaService;
            this.pagoService = pagoService;
            this.cuentaBancariaService = cuentaBancariaService;
        }

    // ==========================================================
    // 🔹 CARGAR PANEL DE INICIO
    // ==========================================================
    @GetMapping
    public String inicio(Model model) {

        // 🧍‍♂️ Empleados activos y totales
        List<Empleado> empleados = empleadoService.listarTodos();
        long totalEmpleadosActivos = empleados.stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .count();

        // 💰 Cuentas bancarias actuales
        List<CuentaBancaria> cuentas = cuentaBancariaService.listarTodas();

        // 📅 Totales generales base
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate sabado = lunes.plusDays(5);

        // 🔹 Total cobrado por empleado (semana actual)
        Map<String, Double> totalCobrado = empleados.stream()
                .collect(Collectors.toMap(
                        Empleado::getNombreCompleto,
                        e -> pagoService.obtenerTotalCobradoPorEmpleadoYRango(
                                e.getId(), lunes, sabado)
                ));

        // 🔹 Total general de la semana
        double totalSemana = totalCobrado.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // 🔹 Clientes atrasados (+2 días)
        Map<Long, List<String>> resumenAtrasos = empleados.stream()
                .collect(Collectors.toMap(
                        Empleado::getId,
                        e -> cronogramaService.obtenerClientesAtrasadosPorEmpleado(e.getId())
                ));

        // 🔹 Enviar datos al modelo para la vista
        model.addAttribute("empleados", empleados);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("totalSemana", totalSemana);
        model.addAttribute("totalEmpleadosActivos", totalEmpleadosActivos);
        model.addAttribute("resumenAtrasos", resumenAtrasos);

        return "inicio";
    }
    
    // ==========================================================
    // 🔹 API: RESUMEN DINÁMICO PARA LAS TARJETAS
    // ==========================================================
    @GetMapping("/api/resumen")
    @ResponseBody
    public Map<String, Object> obtenerResumen(Long empleadoId, String desde, String hasta) {
        
        // 🪶 DEBUG: verificar qué parámetros están llegando
        System.out.println("📡 [DEBUG] empleadoId=" + empleadoId + 
                           ", desde=" + desde + ", hasta=" + hasta);
        
        Map<String, Object> respuesta = new java.util.HashMap<>();

        if (empleadoId == null) {
            respuesta.put("totalCobrado", 0.0);
            respuesta.put("clientesAtrasados", java.util.Collections.emptyList());
            respuesta.put("clientesSinPagoHoy", java.util.Collections.emptyList());
            return respuesta;
        }

        LocalDate fechaDesde = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : LocalDate.now();
        LocalDate fechaHasta = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : LocalDate.now();

        // 💰 Total cobrado por empleado en rango
        double totalCobrado = pagoService.obtenerTotalCobradoPorEmpleadoYRango(empleadoId, fechaDesde, fechaHasta);

        // ⏰ Clientes atrasados (+2 días)
        List<String> clientesAtrasados = cronogramaService.obtenerClientesAtrasadosPorEmpleado(empleadoId);

        // 🚫 Clientes sin pago hoy
        List<String> clientesSinPagoHoy = cronogramaService.obtenerClientesSinPagoHoyPorEmpleado(empleadoId, LocalDate.now());

        respuesta.put("totalCobrado", totalCobrado);
        respuesta.put("clientesAtrasados", clientesAtrasados);
        respuesta.put("clientesSinPagoHoy", clientesSinPagoHoy);

        return respuesta;
    }

}
