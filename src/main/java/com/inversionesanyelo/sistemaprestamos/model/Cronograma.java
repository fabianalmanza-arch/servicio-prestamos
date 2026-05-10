package com.inversionesanyelo.sistemaprestamos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cronogramas")
public class Cronograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prestamo_id", nullable = false)
    private Prestamo prestamo;

    private Integer numeroCuota;
    private Double capital;
    private Double interes;
    private Double montoTotal;

    private Double saldoPendiente;
    private String estado; // pendiente / parcial / pagada

    private LocalDate fechaProgramada;
    private LocalDate fechaPagoReal;

    // 🔹 NUEVOS CAMPOS PARA REGISTRO DE PAGOS
    @Column(columnDefinition = "TEXT")
    private String detalleEfectivo;        // "50000 / 2026-01-12 - 20000 / 2026-01-13"

    @Column(columnDefinition = "TEXT")
    private String detalleTransferencia;   // "40000 / OP12345 / 2026-01-12 - 30000 / OP56789 / 2026-01-14"

    // ==== CONSTRUCTOR ====
    public Cronograma() {}

    // ==== GETTERS Y SETTERS ====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }

    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer numeroCuota) { this.numeroCuota = numeroCuota; }

    public Double getCapital() { return capital; }
    public void setCapital(Double capital) { this.capital = capital; }

    public Double getInteres() { return interes; }
    public void setInteres(Double interes) { this.interes = interes; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public Double getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(Double saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDate fechaProgramada) { this.fechaProgramada = fechaProgramada; }

    public LocalDate getFechaPagoReal() { return fechaPagoReal; }
    public void setFechaPagoReal(LocalDate fechaPagoReal) { this.fechaPagoReal = fechaPagoReal; }

    public String getDetalleEfectivo() { return detalleEfectivo; }
    public void setDetalleEfectivo(String detalleEfectivo) { this.detalleEfectivo = detalleEfectivo; }

    public String getDetalleTransferencia() { return detalleTransferencia; }
    public void setDetalleTransferencia(String detalleTransferencia) { this.detalleTransferencia = detalleTransferencia; }
}
