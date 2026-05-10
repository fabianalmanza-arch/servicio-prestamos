/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.model;

import jakarta.persistence.*;

/**
 *
 * @author fabia
 */
@Entity
@Table(name = "cuentas_bancarias")
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;  // Ejemplo: BCP, Interbank, Plin

    @Column(nullable = false)
    private Double saldo = 0.0;

    // ==========================
    //   CONSTRUCTORES
    // ==========================
    public CuentaBancaria() {
    }

    public CuentaBancaria(String nombre, Double saldo) {
        this.nombre = nombre;
        this.saldo = saldo;
    }

    // ==========================
    //   GETTERS Y SETTERS
    // ==========================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    // ==========================
    //   toString (opcional)
    // ==========================
    @Override
    public String toString() {
        return "CuentaBancaria{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", saldo=" + saldo +
                '}';
    }
}
