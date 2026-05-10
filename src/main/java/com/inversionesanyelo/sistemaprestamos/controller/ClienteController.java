package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.model.Cliente;
import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.repository.ClienteRepository;
import com.inversionesanyelo.sistemaprestamos.repository.EmpleadoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    // Directorio donde se guardarán las firmas
    private final Path uploadDir = Paths.get("uploads/signatures");

    public ClienteController(ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;

        // Crear la carpeta si no existe
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Listar clientes y empleados
    @GetMapping
    public String listarClientes(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("clientes", clienteRepository.buscarClientes(search));
        } else {
            model.addAttribute("clientes", clienteRepository.findAll());
        }
        model.addAttribute("empleados", empleadoRepository.findAll());
        model.addAttribute("nuevoCliente", new Cliente());
        model.addAttribute("search", search);
        return "clientes";
    }


    // Crear o editar cliente (acepta firma)
    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente,
                                 @RequestParam(value = "firma", required = false) MultipartFile firma,
                                 @RequestParam(value = "infoPdf", required = false) MultipartFile infoPdf) {
        try {
            // 📁 Directorios para firma y PDF
            Path firmasDir = Paths.get("src/main/resources/static/firmas");
            Path pdfDir = Paths.get("src/main/resources/static/pdf_clientes");
            if (!Files.exists(firmasDir)) Files.createDirectories(firmasDir);
            if (!Files.exists(pdfDir)) Files.createDirectories(pdfDir);

            // 📷 Guardar firma
            if (firma != null && !firma.isEmpty()) {
                String fileName = "firma_" + System.currentTimeMillis() + "_" + firma.getOriginalFilename();
                Path destino = firmasDir.resolve(fileName);
                Files.copy(firma.getInputStream(), destino);
                cliente.setFirmaRuta("firmas/" + fileName);
            } else if (cliente.getId() != null) {
                clienteRepository.findById(cliente.getId()).ifPresent(existente -> {
                    if (existente.getFirmaRuta() != null) {
                        cliente.setFirmaRuta(existente.getFirmaRuta());
                    }
                });
            }

            // 📄 Guardar PDF adicional
            if (infoPdf != null && !infoPdf.isEmpty()) {
                String pdfName = "info_" + System.currentTimeMillis() + "_" + infoPdf.getOriginalFilename();
                Path destinoPdf = pdfDir.resolve(pdfName);
                Files.copy(infoPdf.getInputStream(), destinoPdf);
                cliente.setInfoPdfRuta("pdf_clientes/" + pdfName);
            } else if (cliente.getId() != null) {
                clienteRepository.findById(cliente.getId()).ifPresent(existente -> {
                    if (existente.getInfoPdfRuta() != null) {
                        cliente.setInfoPdfRuta(existente.getInfoPdfRuta());
                    }
                });
            }

            clienteRepository.save(cliente);
            return "redirect:/clientes";

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/clientes?error";
        }
    }

    // Editar (cargar datos al modal)
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
            model.addAttribute("empleados", empleadoRepository.findAll());
            return "clientes";
        }
        return "redirect:/clientes";
    }

    // Eliminar cliente
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
        return "redirect:/clientes";
    }

    // Subir firma individual (por API, si deseas usar aparte)
    @PostMapping("/firma/{id}")
    public ResponseEntity<String> subirFirma(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // Crear carpeta si no existe
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            // Generar nombre único
            String fileName = "firma_" + id + "_" + System.currentTimeMillis() + ".png";
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // Guardar ruta en la BD
            Cliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            cliente.setFirmaRuta("/uploads/signatures/" + fileName);
            clienteRepository.save(cliente);

            return ResponseEntity.ok("Firma subida correctamente");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir la firma: " + e.getMessage());
        }
    }
}
