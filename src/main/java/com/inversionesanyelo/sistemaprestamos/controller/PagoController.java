package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.Dto.CuotaDTO;
import com.inversionesanyelo.sistemaprestamos.model.Pago;
import com.inversionesanyelo.sistemaprestamos.service.PagoService;
import com.inversionesanyelo.sistemaprestamos.service.CronogramaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private CronogramaService cronogramaService;

    // ==========================================================
    //  LISTAR VISTA PRINCIPAL
    // ==========================================================
    @GetMapping
    public String vistaPagos(@RequestParam(required = false) String search,
                             Model model,
                             @ModelAttribute("mensajeExito") String mensajeExito,
                             @ModelAttribute("mensajeError") String mensajeError) {

        List<Pago> pagos = (search == null || search.isBlank())
                ? pagoService.listarPagos()
                : pagoService.buscarPagos(search);

        model.addAttribute("pagos", pagos);
        model.addAttribute("search", search);

        // Para mensajes flash
        if (mensajeExito != null && !mensajeExito.isBlank())
            model.addAttribute("mensajeExito", mensajeExito);
        if (mensajeError != null && !mensajeError.isBlank())
            model.addAttribute("mensajeError", mensajeError);

        return "pagos";
    }

    // ==========================================================
    //  GUARDAR PAGO (FORMULARIO)
    // ==========================================================
    @PostMapping("/guardar")
    public String guardarPago(
            @RequestParam(required = false) Long id,
            @RequestParam Long prestamoId,
            @RequestParam String fechaPago,
            @RequestParam String metodoPago,
            @RequestParam(required = false) Double montoEfectivo,
            @RequestParam(required = false) Double montoTransferencia,
            @RequestParam(required = false) String numeroOperacion,
            @RequestParam(required = false) String banco,
            @RequestParam(required = false, name = "baucherBase64") String baucherBase64,
            RedirectAttributes redirectAttributes
    ) {
        try {
            pagoService.guardarDesdeFormulario(
                    id, prestamoId, fechaPago, metodoPago,
                    montoEfectivo, montoTransferencia, numeroOperacion,
                    banco, baucherBase64
            );
            redirectAttributes.addFlashAttribute("mensajeExito", "✅ Pago registrado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "⚠️ " + e.getMessage());
        }
        return "redirect:/pagos";
    }

    // ==========================================================
    //  OBTENER PAGO POR ID (AJAX)
    // ==========================================================
    @GetMapping("/{id}")
    @ResponseBody
    public Pago obtenerPago(@PathVariable Long id) {
        return pagoService.obtenerPago(id);
    }

    // ==========================================================
    //  VER BAUCHER EN EL NAVEGADOR
    // ==========================================================
    @GetMapping("/baucher/{id}")
    public void verBaucher(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Pago pago = pagoService.obtenerPago(id);
        if (pago.getBaucher() != null && !pago.getBaucher().isBlank()) {
            byte[] imagenBytes = Base64.getDecoder().decode(pago.getBaucher());
            response.setContentType("image/png");
            response.getOutputStream().write(imagenBytes);
            response.getOutputStream().flush();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No hay baucher para este pago");
        }
    }

    // ==========================================================
    //  ELIMINAR PAGO
    // ==========================================================
    @GetMapping("/eliminar/{id}")
    public String eliminarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pagoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "🗑️ Pago eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar pago: " + e.getMessage());
        }
        return "redirect:/pagos";
    }

    // ==========================================================
    //  GENERAR FACTURA PDF
    // ==========================================================
    @GetMapping("/factura/{id}")
    public void generarFactura(@PathVariable Long id, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=factura_pago_" + id + ".pdf");
        pagoService.generarFacturaPdf(id, response.getOutputStream());
    }
    
    @GetMapping("/validar-operacion")
    @ResponseBody
    public boolean validarNumeroOperacion(@RequestParam String numero) {
        return pagoService.existeNumeroOperacion(numero);
    }
}
