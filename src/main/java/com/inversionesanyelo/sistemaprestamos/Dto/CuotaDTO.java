/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author fabia
 */

public class CuotaDTO {

    private Long id;
    private Integer numero;
    private LocalDate fechaTeorica;
    private BigDecimal valor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public LocalDate getFechaTeorica() { return fechaTeorica; }
    public void setFechaTeorica(LocalDate fechaTeorica) { this.fechaTeorica = fechaTeorica; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}

