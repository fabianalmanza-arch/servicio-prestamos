package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.Dto.PrestamoEditarDto;
import com.inversionesanyelo.sistemaprestamos.model.Prestamo;
import com.inversionesanyelo.sistemaprestamos.service.ClienteService;
import com.inversionesanyelo.sistemaprestamos.service.ContratoService;
import com.inversionesanyelo.sistemaprestamos.service.PrestamoService;
import com.inversionesanyelo.sistemaprestamos.service.CronogramaService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final CronogramaService cronogramaService;

    // ✅ Constructor único que inyecta los tres servicios
    public PrestamoController(PrestamoService prestamoService,
                              ClienteService clienteService,
                              ContratoService contratoService,
                                CronogramaService cronogramaService) {
        this.prestamoService = prestamoService;
        this.clienteService = clienteService;
        this.contratoService = contratoService;
        this.cronogramaService = cronogramaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Prestamo> prestamos;
        if (search != null && !search.isBlank()) {
            prestamos = prestamoService.buscarPrestamos(search);
        } else {
            prestamos = prestamoService.listar();
        }

        model.addAttribute("prestamos", prestamos);
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("prestamo", new Prestamo());
        model.addAttribute("search", search);
        return "prestamos";
    }


    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<?> guardar(@ModelAttribute Prestamo prestamo) {
        try {
            Prestamo nuevo = prestamoService.guardar(prestamo);
            cronogramaService.generarCronograma(nuevo.getId());
            return ResponseEntity.ok("Préstamo registrado correctamente");
        } catch (RuntimeException e) {
            // 👇 Aquí capturamos "Fondos insuficientes" y cualquier otro error controlado
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al registrar el préstamo");
        }
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Prestamo prestamo) {
        // 🔹 Guardar cambios básicos del préstamo
        Prestamo actualizado = prestamoService.guardar(prestamo);

        // 🔹 Recalcular el cronograma completo en el servicio
        prestamoService.recalcularCronogramaPorPrestamo(actualizado.getId());

        return "redirect:/prestamos";
    }



    @GetMapping("/editar/{id}")
    @ResponseBody
    public PrestamoEditarDto editar(@PathVariable Long id) {
        Prestamo p = prestamoService.buscarPorId(id); // tu método existente
        PrestamoEditarDto dto = new PrestamoEditarDto();
        dto.setId(p.getId());
        dto.setMonto(p.getMonto());
        dto.setDias(p.getDias());
        dto.setFecha(p.getFecha());
        dto.setTotalPagar(p.getTotalPagar());
        dto.setCuotaDiaria(p.getCuotaDiaria());
        dto.setClienteId(p.getCliente().getId());
        dto.setMetodoEntrega(p.getMetodoEntrega());
        dto.setMontoEfectivo(p.getMontoEfectivo());
        dto.setMontoTransferencia(p.getMontoTransferencia());
        dto.setBancoEntrega(p.getBancoEntrega());
        
        return dto;
    }


    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        prestamoService.eliminar(id);
        return "redirect:/prestamos";
    }

    // ✅ Generar PDF del contrato
    @GetMapping("/pdf/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> generarContrato(@PathVariable Long id) {
        try {
            Prestamo prestamo = prestamoService.buscarPorId(id);
            if (prestamo == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = contratoService.generarContrato(prestamo);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=contrato_prestamo_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    @GetMapping("/buscar")
    @ResponseBody
    public List<Map<String, Object>> buscarPrestamos(@RequestParam("query") String query) {
        List<Prestamo> prestamos = prestamoService.buscarPrestamos(query);

        return prestamos.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("clienteNombre", p.getCliente().getNombreCompleto());
            map.put("fecha", p.getFecha().toString());
            return map;
        }).collect(Collectors.toList());
    }
    
}
