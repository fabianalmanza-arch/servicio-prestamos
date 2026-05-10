/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 *
 * @author fabia
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    private Long id;

    @Column(name = "nombre_completo", length = 100, nullable = false)
    private String nombreCompleto;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(length = 255)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    
    @Column(name = "firma_ruta")
    private String firmaRuta;
    
    @Column(name = "info_pdf_ruta")
    private String infoPdfRuta;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    
    public String getFirmaRuta() { return firmaRuta; }
    public void setFirmaRuta(String firmaRuta) { this.firmaRuta = firmaRuta; }
    
    public String getInfoPdfRuta() { return infoPdfRuta; }
    public void setInfoPdfRuta(String infoPdfRuta) { this.infoPdfRuta = infoPdfRuta; }
    
    @Transient
    public String getCodigoCliente() {
        return String.format("CLI_%03d", this.id);
    }

}
