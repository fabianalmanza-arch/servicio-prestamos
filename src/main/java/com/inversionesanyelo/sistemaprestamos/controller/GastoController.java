/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.model.Gasto;
import com.inversionesanyelo.sistemaprestamos.service.GastoService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 *
 * @author Fabian
 */
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    // ==========================================================
    // 🔹 Registrar nuevo gasto
    // ==========================================================
    @PostMapping
    public ResponseEntity<Gasto> registrar(@RequestBody Gasto gasto) {
        return ResponseEntity.ok(gastoService.registrar(gasto));
    }

    // ==========================================================
    // 🔹 Listar todos los gastos
    // ==========================================================
    @GetMapping
    public List<Gasto> listar() {
        return gastoService.listarTodos();
    }

    // ==========================================================
    // 🔹 Listar por tipo (trabajador / operativo)
    // ==========================================================
    @GetMapping("/tipo/{tipo}")
    public List<Gasto> listarPorTipo(@PathVariable String tipo) {
        return gastoService.listarPorTipo(tipo);
    }

    // ==========================================================
    // 🔹 Obtener gasto por ID
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<Gasto> obtenerPorId(@PathVariable Long id) {
        return gastoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 Eliminar gasto
    // ==========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        gastoService.eliminar(id);
        return ResponseEntity.ok("Gasto eliminado correctamente");
    }
    
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        Gasto gasto = gastoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, out);
            document.open();

            // 🏢 Encabezado general
            Paragraph empresa = new Paragraph("INVERSIONES ANYELO SAC", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            empresa.setAlignment(Element.ALIGN_CENTER);
            document.add(empresa);

            Paragraph ruc = new Paragraph("RUC: 20611854359", new Font(Font.FontFamily.HELVETICA, 11));
            ruc.setAlignment(Element.ALIGN_CENTER);
            document.add(ruc);

            document.add(Chunk.NEWLINE);

            // 🔹 Si el gasto es de tipo trabajador → comprobante de pago
            if ("trabajador".equalsIgnoreCase(gasto.getTipo())) {
                Paragraph titulo = new Paragraph("COMPROBANTE DE PAGO", new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD));
                titulo.setAlignment(Element.ALIGN_CENTER);
                document.add(titulo);
                document.add(Chunk.NEWLINE);

                // 🔸 Buscar empleado asociado (si tu modelo de Gasto tiene un campo empleado)
                if (gasto.getEmpleado() != null) {
                    Empleado emp = gasto.getEmpleado();

                    PdfPTable infoEmpleado = new PdfPTable(2);
                    infoEmpleado.setWidthPercentage(90);
                    infoEmpleado.setSpacingBefore(5f);
                    infoEmpleado.setSpacingAfter(15f);

                    Font bold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
                    Font normal = new Font(Font.FontFamily.HELVETICA, 11);

                    infoEmpleado.addCell(new Phrase("Nombre completo:", bold));
                    infoEmpleado.addCell(new Phrase(emp.getNombreCompleto(), normal));
                    infoEmpleado.addCell(new Phrase("No documento:", bold));
                    infoEmpleado.addCell(new Phrase(emp.getTipoDocumento() + " - " + emp.getNumeroDocumento(), normal));
                    infoEmpleado.addCell(new Phrase("Teléfono:", bold));
                    infoEmpleado.addCell(new Phrase(emp.getTelefono() != null ? emp.getTelefono() : "-", normal));
                    infoEmpleado.addCell(new Phrase("Correo:", bold));
                    infoEmpleado.addCell(new Phrase(emp.getCorreo(), normal));

                    document.add(infoEmpleado);
                } else {
                    Paragraph sinEmpleado = new Paragraph("(Sin información del empleado asociado)", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY));
                    sinEmpleado.setAlignment(Element.ALIGN_CENTER);
                    document.add(sinEmpleado);
                    document.add(Chunk.NEWLINE);
                }
            }

            // 🧾 Datos del gasto
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font bold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font normal = new Font(Font.FontFamily.HELVETICA, 11);

            table.addCell(new Phrase("Fecha:", bold));
            table.addCell(new Phrase(String.valueOf(gasto.getFecha()), normal));
            table.addCell(new Phrase("Motivo:", bold));
            table.addCell(new Phrase(gasto.getMotivo(), normal));
            table.addCell(new Phrase("Cuenta:", bold));
            table.addCell(new Phrase(gasto.getCuenta(), normal));
            table.addCell(new Phrase("Tipo de Gasto:", bold));
            table.addCell(new Phrase(gasto.getTipo(), normal));
            table.addCell(new Phrase("Cantidad:", bold));
            table.addCell(new Phrase("$ " + gasto.getCantidad(), normal));
            table.addCell(new Phrase("Observaciones:", bold));
            table.addCell(new Phrase(gasto.getObservaciones() != null ? gasto.getObservaciones() : "-", normal));

            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Firma del Gerente:", bold));
            document.add(Chunk.NEWLINE);

            // 🖋️ Firma del gerente
            try {
                String firmaPath = "src/main/resources/static/images/gerenteFirma.jpg";
                Image firma = Image.getInstance(firmaPath);
                firma.scaleToFit(150, 80);
                firma.setAlignment(Element.ALIGN_LEFT);
                document.add(firma);
            } catch (Exception ex) {
                document.add(new Paragraph("[Firma no disponible]", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY)));
            }

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=gasto_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
