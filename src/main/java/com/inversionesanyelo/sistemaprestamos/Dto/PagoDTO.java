/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author fabia
 */
public class PagoDTO {
    private BigDecimal monto;
    private String metodoPago; // EFECTIVO / TRANSFERENCIA
    private String numeroOperacion;
    private LocalDateTime fechaPago;
    // getters/setters
}
