package com.inversionesanyelo.sistemaprestamos.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prestamo_id", nullable = false)
    @JsonIgnoreProperties({"cronogramas", "pagos"})
    private Prestamo prestamo;

    @ManyToOne(optional = true)
    @JoinColumn(name = "cuota_id", nullable = true)
    private Cronograma cuota;

    private Double monto;
    private String metodoPago;

    @Column(unique = true)
    private String numeroOperacion;

    private Double montoEfectivo;
    private Double montoTransferencia;
    private LocalDate fechaPago;

    // 🔹 Banco de transferencia
    private String banco;

    // 🔹 Imagen del baucher en Base64
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String baucher;

    public Pago() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }

    public Cronograma getCuota() { return cuota; }
    public void setCuota(Cronograma cuota) { this.cuota = cuota; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }

    public Double getMontoEfectivo() { return montoEfectivo; }
    public void setMontoEfectivo(Double montoEfectivo) { this.montoEfectivo = montoEfectivo; }

    public Double getMontoTransferencia() { return montoTransferencia; }
    public void setMontoTransferencia(Double montoTransferencia) { this.montoTransferencia = montoTransferencia; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getBaucher() { return baucher; }
    public void setBaucher(String baucher) { this.baucher = baucher; }
}
