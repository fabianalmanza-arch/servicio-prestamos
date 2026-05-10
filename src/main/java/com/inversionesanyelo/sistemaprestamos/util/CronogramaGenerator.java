package com.inversionesanyelo.sistemaprestamos.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generador del cronograma semanal para el contrato.
 * Calcula las semanas (lunes-sábado o hasta el día final) y los montos por semana.
 */
public class CronogramaGenerator {

    /**
     * Genera una lista de semanas con sus fechas de vencimiento y montos de cuota.
     *
     * @param fechaInicio Fecha de inicio del préstamo
     * @param fechaFin    Fecha final del préstamo
     * @param total       Total a pagar (S/)
     * @param dias        Número de días del préstamo
     * @return Lista de semanas con fecha y monto formateado
     */
    public static List<Map<String, String>> generarSemanas(LocalDate fechaInicio, LocalDate fechaFin, double total, int dias) {
        List<Map<String, String>> semanas = new ArrayList<>();

        // Valor de la cuota diaria
        double cuotaDiaria = total / dias;

        // Formato de fecha en español
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM yyyy", new Locale("es", "ES"));

        LocalDate fechaActual = fechaInicio;
        int semana = 1;

        while (!fechaActual.isAfter(fechaFin)) {
            LocalDate finSemana;

            // 🔹 Si es la primera semana y el inicio no es lunes,
            // el fin será el sábado de esa semana o la fecha final.
            if (semana == 1 && fechaActual.getDayOfWeek() != DayOfWeek.MONDAY) {
                int diasHastaSabado = DayOfWeek.SATURDAY.getValue() - fechaActual.getDayOfWeek().getValue();
                if (diasHastaSabado < 0) diasHastaSabado = 0;
                finSemana = fechaActual.plusDays(diasHastaSabado);
            } else {
                // 🔹 En las siguientes semanas, siempre lunes - sábado
                finSemana = fechaActual.plusDays(5);
            }

            // 🔹 No pasar la fecha final
            if (finSemana.isAfter(fechaFin)) {
                finSemana = fechaFin;
            }

            // Calcular cantidad de días y monto semanal
            int diasSemana = (int) (finSemana.toEpochDay() - fechaActual.toEpochDay()) + 1;
            double montoSemana = cuotaDiaria * diasSemana;

            // Texto descriptivo de rango
            String inicioTexto = capitalizar(fechaActual.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES")));
            String finTexto = capitalizar(finSemana.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES")));
            String rango = "SEMANA " + semana + " (" + inicioTexto + " - " + finTexto + ")";

            // Crear mapa con datos de la semana
            Map<String, String> semanaData = new HashMap<>();
            semanaData.put("nombre", rango);
            semanaData.put("fecha_vencimiento", finSemana.format(formatoFecha));
            semanaData.put("monto", String.format("S/ %.2f", montoSemana));

            semanas.add(semanaData);

            // Avanzar al siguiente lunes
            fechaActual = finSemana.plusDays(1);
            // Si la nueva fecha cae domingo, saltar al lunes siguiente
            if (fechaActual.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fechaActual = fechaActual.plusDays(1);
            }

            semana++;
        }

        return semanas;
    }

    // 🔤 Convierte la primera letra en mayúscula
    private static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
