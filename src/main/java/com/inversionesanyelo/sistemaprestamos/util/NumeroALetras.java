/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.util;


import java.text.DecimalFormat;

/**
 *
 * @author fabia
 */
public class NumeroALetras {

    private static final String[] UNIDADES = {
            "", "uno", "dos", "tres", "cuatro", "cinco", "seis",
            "siete", "ocho", "nueve", "diez", "once", "doce",
            "trece", "catorce", "quince", "dieciséis", "diecisiete",
            "dieciocho", "diecinueve", "veinte"
    };

    private static final String[] DECENAS = {
            "", "", "veinte", "treinta", "cuarenta", "cincuenta",
            "sesenta", "setenta", "ochenta", "noventa"
    };

    private static final String[] CENTENAS = {
            "", "ciento", "doscientos", "trescientos", "cuatrocientos",
            "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    public static String convertir(double numero) {
        long parteEntera = (long) numero;
        int parteDecimal = (int) Math.round((numero - parteEntera) * 100);

        if (parteEntera == 0) return "cero con " + parteDecimal + "/100 soles";

        return convertirNumero(parteEntera) + " con " + parteDecimal + "/100 soles";
    }

    private static String convertirNumero(long numero) {
        if (numero < 21) {
            return UNIDADES[(int) numero];
        } else if (numero < 100) {
            return DECENAS[(int) numero / 10] + ((numero % 10 != 0) ? " y " + UNIDADES[(int) numero % 10] : "");
        } else if (numero < 1000) {
            if (numero == 100) return "cien";
            return CENTENAS[(int) numero / 100] + " " + convertirNumero(numero % 100);
        } else if (numero < 1000000) {
            if (numero == 1000) return "mil";
            return convertirNumero(numero / 1000) + " mil " + convertirNumero(numero % 1000);
        } else if (numero < 1000000000) {
            if (numero == 1000000) return "un millón";
            return convertirNumero(numero / 1000000) + " millones " + convertirNumero(numero % 1000000);
        } else {
            return String.valueOf(numero); // fallback
        }
    }
}
