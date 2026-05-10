package com.inversionesanyelo.sistemaprestamos.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    private Long id;
    
    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cronograma> cronogramas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Fecha seleccionada al crear el préstamo
    private LocalDate fecha;

    // Nueva fecha de inicio (día siguiente hábil)
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    // Nueva fecha de finalización calculada (sin domingos)
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    // Fecha de creación (puedes llenarla en el backend si deseas)
    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    private Double monto;
    private Integer dias;
    private Double cuotaDiaria;
    private Double totalPagar;

    // Interés fijo del 20%
    private Double interes = 0.20;

    // Estado del préstamo (opcional)
    private String estado = "activo";
    
    @Column(name = "saldo_pendiente")
    private Double saldoPendiente;  
    
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado; // quién otorgó el préstamo

    @Column(name = "monto_efectivo")
    private Double montoEfectivo = 0.0;
    
    // 🔹 Método de entrega del dinero (efectivo, transferencia, mixto)
    @Column(name = "metodo_entrega")
    private String metodoEntrega;

    // 🔹 Banco o entidad usada si aplica
    @Column(name = "banco_entrega")
    private String bancoEntrega;

    // 🔹 Monto entregado por transferencia
    @Column(name = "monto_transferencia")
    private Double montoTransferencia;



    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }

    public Double getCuotaDiaria() { return cuotaDiaria; }
    public void setCuotaDiaria(Double cuotaDiaria) { this.cuotaDiaria = cuotaDiaria; }

    public Double getTotalPagar() { return totalPagar; }
    public void setTotalPagar(Double totalPagar) { this.totalPagar = totalPagar; }

    public Double getInteres() { return interes; }
    public void setInteres(Double interes) { this.interes = interes; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Double getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(Double saldoPendiente) { this.saldoPendiente = saldoPendiente; }
    
    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();
    
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public Double getMontoEfectivo() { return montoEfectivo; }
    public void setMontoEfectivo(Double montoEfectivo) { this.montoEfectivo = montoEfectivo; }

    public String getMetodoEntrega() { return metodoEntrega; }
    public void setMetodoEntrega(String metodoEntrega) { this.metodoEntrega = metodoEntrega; }

    public String getBancoEntrega() { return bancoEntrega; }
    public void setBancoEntrega(String bancoEntrega) { this.bancoEntrega = bancoEntrega; }

    public Double getMontoTransferencia() { return montoTransferencia; }
    public void setMontoTransferencia(Double montoTransferencia) { this.montoTransferencia = montoTransferencia; }
    
    public List<Cronograma> getCronogramas() { return cronogramas; }
    public void setCronogramas(List<Cronograma> cronogramas) { this.cronogramas = cronogramas; }

}
