package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.Cronograma;
import com.inversionesanyelo.sistemaprestamos.model.Pago;
import com.inversionesanyelo.sistemaprestamos.model.Prestamo;
import com.inversionesanyelo.sistemaprestamos.Dto.CuotaDTO;
import com.inversionesanyelo.sistemaprestamos.repository.CronogramaRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PagoRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CronogramaService {

    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    // ==========================================================
    // 🔹 GENERAR CRONOGRAMA BASE
    // ==========================================================
    public List<Cronograma> generarCronograma(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        List<Cronograma> cronogramas = new ArrayList<>();

        // ✅ Tomamos directamente el total a pagar desde la tabla de préstamos
        double totalPagar = prestamo.getTotalPagar();
        int dias = prestamo.getDias();

        // Cuotas diarias
        double montoDiario = totalPagar / dias;
        double capitalDiario = prestamo.getMonto() / dias;
        double interesTotal = totalPagar - prestamo.getMonto();
        double interesDiario = interesTotal / dias;

        // El saldo pendiente inicia en el total a pagar (monto + interés)
        double saldoRestante = totalPagar;

        LocalDate fecha = Optional.ofNullable(prestamo.getFechaInicio())
                .orElse(prestamo.getFecha());

        // Si la fecha inicial cae en domingo, empezar el lunes
        while (fecha.getDayOfWeek().getValue() == 7) {
            fecha = fecha.plusDays(1);
        }

        for (int i = 1; i <= dias; i++) {
            Cronograma c = new Cronograma();
            c.setPrestamo(prestamo);
            c.setNumeroCuota(i);
            c.setCapital(capitalDiario);
            c.setInteres(interesDiario);
            c.setMontoTotal(montoDiario);

            // 🔹 Restar esta cuota al saldo restante
            saldoRestante -= montoDiario;
            if (saldoRestante < 0) saldoRestante = 0;

            c.setSaldoPendiente(saldoRestante);
            c.setEstado("pendiente");
            c.setFechaProgramada(fecha);

            cronogramas.add(c);

            // 🔹 Calcular la siguiente fecha y saltar domingos
            fecha = fecha.plusDays(1);
            while (fecha.getDayOfWeek().getValue() == 7) {
                fecha = fecha.plusDays(1);
            }
        }

        return cronogramaRepository.saveAll(cronogramas);
    }

    // ==========================================================
    // 🔹 LISTAR CRONOGRAMAS POR PRÉSTAMO
    // ==========================================================
    public List<Cronograma> listarPorPrestamo(Long prestamoId) {
        return cronogramaRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId);
    }

    // ==========================================================
    // 🔹 ACTUALIZAR ESTADO DE UNA CUOTA
    // ==========================================================
    public void actualizarEstadoCuota(Long idCuota, String estado, LocalDate fechaPago) {
        Cronograma c = cronogramaRepository.findById(idCuota)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        c.setEstado(estado);
        c.setFechaPagoReal(fechaPago);

        cronogramaRepository.save(c);
    }

    // ==========================================================
    // 🔹 LISTAR CUOTAS PENDIENTES O PARCIALES
    // ==========================================================
    public List<Cronograma> listarCuotasPendientes(Long prestamoId) {
        return cronogramaRepository.findByPrestamoIdAndEstadoInOrderByNumeroCuotaAsc(
                prestamoId, List.of("pendiente", "parcial"));
    }

    // ==========================================================
    // 🔹 OBTENER DEUDA TOTAL DE UN PRÉSTAMO
    // ==========================================================
    public double obtenerDeudaTotal(Long prestamoId) {
        List<Cronograma> cuotas = cronogramaRepository.findByPrestamoId(prestamoId);
        return cuotas.stream()
                .filter(c -> !"pagada".equalsIgnoreCase(c.getEstado()))
                .mapToDouble(Cronograma::getSaldoPendiente)
                .sum();
    }

    // ==========================================================
    // 🔹 AGREGAR DETALLE DE PAGO EN CRONOGRAMA
    // ==========================================================
    public void agregarDetallePago(Cronograma cuota, Pago pago) {
        boolean tieneEfectivo = pago.getMontoEfectivo() != null && pago.getMontoEfectivo() > 0;
        boolean tieneTransferencia = pago.getMontoTransferencia() != null && pago.getMontoTransferencia() > 0;
        LocalDate fecha = pago.getFechaPago();

        if (tieneEfectivo) {
            String nuevo = String.format("%.2f / %s", pago.getMontoEfectivo(), fecha);
            String anterior = (cuota.getDetalleEfectivo() != null && !cuota.getDetalleEfectivo().isBlank())
                    ? cuota.getDetalleEfectivo() + " - " + nuevo
                    : nuevo;
            cuota.setDetalleEfectivo(anterior);
        }

        if (tieneTransferencia) {
            String numOp = (pago.getNumeroOperacion() != null && !pago.getNumeroOperacion().isBlank())
                    ? " / " + pago.getNumeroOperacion()
                    : "";
            String nuevo = String.format("%.2f%s / %s", pago.getMontoTransferencia(), numOp, fecha);
            String anterior = (cuota.getDetalleTransferencia() != null && !cuota.getDetalleTransferencia().isBlank())
                    ? cuota.getDetalleTransferencia() + " - " + nuevo
                    : nuevo;
            cuota.setDetalleTransferencia(anterior);
        }

        cronogramaRepository.save(cuota);
    }

    // 🔹 LISTAR CLIENTES ATRASADOS POR EMPLEADO
    @Transactional(readOnly = true)
    public List<String> obtenerClientesAtrasadosPorEmpleado(Long empleadoId) {
        LocalDate fechaLimite = LocalDate.now().minusDays(2);
        return cronogramaRepository.obtenerClientesAtrasadosPorEmpleado(empleadoId, fechaLimite);
    }
    
    // ==========================================================
    // 🔹 LISTAR CUOTAS (DTO) POR PRÉSTAMO
    // ==========================================================
    public List<com.inversionesanyelo.sistemaprestamos.Dto.CuotaDTO> listarCuotasPorPrestamo(Long prestamoId) {
        List<Cronograma> cuotas = cronogramaRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId);
        List<com.inversionesanyelo.sistemaprestamos.Dto.CuotaDTO> lista = new ArrayList<>();

        for (Cronograma c : cuotas) {
            com.inversionesanyelo.sistemaprestamos.Dto.CuotaDTO dto = new com.inversionesanyelo.sistemaprestamos.Dto.CuotaDTO();
            dto.setId(c.getId());
            dto.setNumero(c.getNumeroCuota());
            dto.setFechaTeorica(c.getFechaProgramada());
            dto.setValor(java.math.BigDecimal.valueOf(c.getMontoTotal()));
            lista.add(dto);
        }
        return lista;
    }
    
    // 🔹 LISTAR CLIENTES SIN PAGO HOY POR EMPLEADO
    @Transactional(readOnly = true)
    public List<String> obtenerClientesSinPagoHoyPorEmpleado(Long empleadoId, LocalDate hoy) {
        return cronogramaRepository.obtenerClientesSinPagoHoyPorEmpleado(empleadoId, hoy);
    }
    
}
