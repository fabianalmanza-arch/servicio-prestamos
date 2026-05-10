/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.controller;

import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.service.EmpleadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author fabia
 */
@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public String listarEmpleados(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("empleados", empleadoService.buscarEmpleados(search));
        } else {
            model.addAttribute("empleados", empleadoService.listarTodos());
        }
        model.addAttribute("empleado", new Empleado());
        model.addAttribute("search", search);
        return "empleados";
    }


    @PostMapping("/guardar")
    public String guardarEmpleado(@ModelAttribute Empleado empleado) {
        empleadoService.guardar(empleado);
        return "redirect:/empleados";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return "redirect:/empleados";
    }

    @GetMapping("/editar/{id}")
    public String editarEmpleado(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.buscarPorId(id));
        model.addAttribute("empleados", empleadoService.listarTodos());
        return "empleados";
    }
    
    // ==============================================================
    //  ACTIVAR / DESACTIVAR EMPLEADOS
    // ==============================================================

    @PostMapping("/activar")
    @ResponseBody
    public String activarEmpleado(@RequestParam Long id, @RequestParam Double montoInicial) {
        try {
            empleadoService.activarEmpleado(id, montoInicial);
            return "Empleado activado correctamente";
        } catch (Exception e) {
            return "Error al activar empleado: " + e.getMessage();
        }
    }

    @PostMapping("/desactivar/{id}")
    @ResponseBody
    public String desactivarEmpleado(@PathVariable Long id) {
        try {
            empleadoService.desactivarEmpleado(id);
            return "Empleado desactivado correctamente";
        } catch (Exception e) {
            return "Error al desactivar empleado: " + e.getMessage();
        }
    }

}

