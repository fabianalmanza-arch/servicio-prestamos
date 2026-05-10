package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.Prestamo;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.inversionesanyelo.sistemaprestamos.util.NumeroALetras;
import com.inversionesanyelo.sistemaprestamos.util.CronogramaGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class ContratoService {

    private final TemplateEngine templateEngine;

    public ContratoService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarContrato(Prestamo prestamo) throws Exception {
        // Seleccionar plantilla según el monto
        String plantilla = prestamo.getMonto() < 1500
                ? "contratos/contrato_menor"
                : "contratos/contrato_mayor";

        Context context = new Context();

        // Datos del cliente
        context.setVariable("nombre", prestamo.getCliente().getNombreCompleto());
        context.setVariable("dni", prestamo.getCliente().getNumeroDocumento());
        context.setVariable("direccion", prestamo.getCliente().getDireccion());
        context.setVariable("telefono", prestamo.getCliente().getTelefono());
        context.setVariable("firma", prestamo.getCliente().getNombreCompleto());

        // ✅ RUTA RELATIVA (desde /static)
        String firmaRuta = prestamo.getCliente().getFirmaRuta();
        if (firmaRuta != null && firmaRuta.startsWith("/")) {
            firmaRuta = firmaRuta.substring(1);
        }
        context.setVariable("firmaRuta", firmaRuta);
        context.setVariable("firmaGerente", "images/gerenteFirma.jpg");

        // Datos del préstamo
        context.setVariable("total", String.format("%.2f", prestamo.getTotalPagar()));
        context.setVariable("total_letras", NumeroALetras.convertir(prestamo.getTotalPagar()));
        context.setVariable("dias", prestamo.getDias());
        context.setVariable("fecha_inicio", prestamo.getFechaInicio().toString());
        context.setVariable("fecha", prestamo.getFecha().toString());
        context.setVariable("fecha_fin", prestamo.getFechaFin().toString());
        context.setVariable("cuota_diaria", prestamo.getCuotaDiaria());

        // Cronograma
        List<Map<String, String>> semanas = CronogramaGenerator.generarSemanas(
                prestamo.getFechaInicio(),
                prestamo.getFechaFin(),
                prestamo.getTotalPagar(),
                prestamo.getDias()
        );
        context.setVariable("semanas", semanas);

        // Renderizar HTML
        String html = templateEngine.process(plantilla, context);

        // ✅ Base URI (donde están /images y /firmas)
        String baseUri = new File("src/main/resources/static").toURI().toString();

        // Generar PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, baseUri);
        builder.toStream(out);
        
        System.out.println("Base URI: " + baseUri);
        System.out.println("Firma ruta: " + prestamo.getCliente().getFirmaRuta());
        System.out.println("Ruta combinada esperada: " + baseUri + prestamo.getCliente().getFirmaRuta());
        
        builder.run();

        return out.toByteArray();
    }
}
