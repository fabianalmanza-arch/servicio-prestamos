/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.Dto;


import java.time.LocalDate;
/**
 *
 * @author fabia
 */
public class PrestamoEditarDto {

    private Long id;
    private Double monto;
    private Integer dias;
    private LocalDate fecha;
    private Double totalPagar;
    private Double cuotaDiaria;
    private Long clienteId;
    private String metodoEntrega;
    private Double montoEfectivo;
    private Double montoTransferencia;
    private String bancoEntrega;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getTotalPagar() { return totalPagar; }
    public void setTotalPagar(Double totalPagar) { this.totalPagar = totalPagar; }

    public Double getCuotaDiaria() { return cuotaDiaria; }
    public void setCuotaDiaria(Double cuotaDiaria) { this.cuotaDiaria = cuotaDiaria; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    
    public String getMetodoEntrega() { return metodoEntrega; }
    public void setMetodoEntrega(String metodoEntrega) { this.metodoEntrega = metodoEntrega; }
    
    public Double getMontoEfectivo() { return montoEfectivo; }
    public void setMontoEfectivo(Double montoEfectivo) { this.montoEfectivo = montoEfectivo; }
    
    public Double getMontoTransferencia() { return montoTransferencia; }
    public void setMontoTransferencia(Double montoTransferencia) { this.montoTransferencia = montoTransferencia; }
    
    public String getBancoEntrega() { return bancoEntrega; }
    public void setBancoEntrega(String bancoEntrega) { this.bancoEntrega = bancoEntrega; }
}
