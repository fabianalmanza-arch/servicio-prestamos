package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.Cronograma;
import com.inversionesanyelo.sistemaprestamos.model.Pago;
import com.inversionesanyelo.sistemaprestamos.model.Prestamo;
import com.inversionesanyelo.sistemaprestamos.repository.CronogramaRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PagoRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.OutputStream;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;
    
    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Autowired
    private CronogramaService cronogramaService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private CuentaBancariaService cuentaBancariaService;

    // ==========================================================
    //   LISTAR TODOS
    // ==========================================================
    public List<Pago> listarPagos() {
        return pagoRepository.findAll();
    }

    // ==========================================================
    //   BUSCAR PAGOS
    // ==========================================================
    public List<Pago> buscarPagos(String query) {
        if (query == null || query.trim().isEmpty()) {
            return pagoRepository.findAll();
        }
        return pagoRepository.buscarPorClienteONumero(query.trim());
    }

    // ==========================================================
    //   OBTENER UNO
    // ==========================================================
    public Pago obtenerPago(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    // ==========================================================
    //   ELIMINAR (recalcula cronograma al borrar pago)
    // ==========================================================
    public void eliminar(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        Prestamo prestamo = pago.getPrestamo();

        // 1️⃣ Eliminar el pago
        pagoRepository.delete(pago);

        // 2️⃣ Recalcular cronograma completo según pagos restantes
        recalcularCronogramaPorPrestamo(prestamo.getId());
    }

    // ==========================================================
    //   GUARDAR PAGO (ÚNICO POR PRÉSTAMO)
    // ==========================================================
    public void guardarDesdeFormulario(
            Long id,
            Long prestamoId,
            String fechaPago,
            String metodoPago,
            Double montoEfectivo,
            Double montoTransferencia,
            String numeroOperacion,
            String banco,
            String baucherBase64
    ) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        LocalDate fecha = LocalDate.parse(fechaPago);
        double efectivo = (montoEfectivo != null ? montoEfectivo : 0.0);
        double transferencia = (montoTransferencia != null ? montoTransferencia : 0.0);
        double totalPagado = efectivo + transferencia;

        Pago pago;

        // =========================================================
        // 🔹 SI EXISTE ID → EDITAR
        // =========================================================
        if (id != null) {
            pago = pagoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado para edición"));

            // Solo validar duplicado si el número fue cambiado
            if (numeroOperacion != null && !numeroOperacion.isBlank()) {
                if (!numeroOperacion.equals(pago.getNumeroOperacion()) &&
                    pagoRepository.existsByNumeroOperacion(numeroOperacion)) {
                    throw new RuntimeException("El número de operación ya existe: " + numeroOperacion);
                }
            }

            // Actualizar campos
            pago.setFechaPago(fecha);
            pago.setMetodoPago(metodoPago);
            pago.setMontoEfectivo(efectivo);
            pago.setMontoTransferencia(transferencia);
            pago.setMonto(totalPagado);
            pago.setNumeroOperacion(numeroOperacion);
            pago.setBanco(banco);
            if (baucherBase64 != null && !baucherBase64.isBlank()) {
                pago.setBaucher(baucherBase64);
            }
            
            // ==========================================================
            // 🔹 AJUSTE DE SALDO EN EDICIÓN
            // ==========================================================
            if ("transferencia".equalsIgnoreCase(pago.getMetodoPago()) || "mixto".equalsIgnoreCase(pago.getMetodoPago())) {
                String bancoAnterior = pago.getBanco();
                Double montoAnterior = pago.getMontoTransferencia();
                String bancoNuevo = banco;
                Double montoNuevo = transferencia;

                boolean bancoCambiado = bancoAnterior != null && bancoNuevo != null && !bancoAnterior.equals(bancoNuevo);
                boolean montoCambiado = montoAnterior != null && montoNuevo != null && !montoAnterior.equals(montoNuevo);

                if (bancoCambiado) {
                    cuentaBancariaService.restarSaldo(bancoAnterior, montoAnterior);
                    cuentaBancariaService.sumarSaldo(bancoNuevo, montoNuevo);
                } else if (montoCambiado) {
                    double diferencia = montoNuevo - montoAnterior;
                    if (diferencia > 0) {
                        cuentaBancariaService.sumarSaldo(bancoNuevo, diferencia);
                    } else if (diferencia < 0) {
                        cuentaBancariaService.restarSaldo(bancoNuevo, Math.abs(diferencia));
                    }
                }
            }

            pagoRepository.save(pago);

            // 🔁 Siempre recalcular cronograma tras editar
            recalcularCronogramaPorPrestamo(prestamoId);

        } else {
            // =========================================================
            // 🔹 SI NO EXISTE ID → CREAR NUEVO
            // =========================================================

            // Validar número único
            if (numeroOperacion != null && !numeroOperacion.isBlank()) {
                if (pagoRepository.existsByNumeroOperacion(numeroOperacion)) {
                    throw new RuntimeException("El número de operación ya existe: " + numeroOperacion);
                }
            }

            // 🔸 Buscar cuotas pendientes
            List<Cronograma> cuotasPendientes = cronogramaRepository
                    .findByPrestamoIdAndEstadoInOrderByNumeroCuotaAsc(prestamoId, List.of("pendiente", "parcial"));

            double restanteTransf = transferencia;
            double restanteEfec = efectivo;

            for (Cronograma c : cuotasPendientes) {
                if (restanteTransf <= 0 && restanteEfec <= 0) break;

                double saldo = c.getSaldoPendiente() != null ? c.getSaldoPendiente() : c.getMontoTotal();
                double abonoTransfer = 0.0;
                double abonoEfectivo = 0.0;

                // 🔸 Primero aplicar transferencia
                if (restanteTransf > 0) {
                    abonoTransfer = Math.min(restanteTransf, saldo);
                    restanteTransf -= abonoTransfer;
                    saldo -= abonoTransfer;
                }

                // 🔸 Luego aplicar efectivo
                if (saldo > 0 && restanteEfec > 0) {
                    abonoEfectivo = Math.min(restanteEfec, saldo);
                    restanteEfec -= abonoEfectivo;
                    saldo -= abonoEfectivo;
                }

                double abonoTotal = abonoTransfer + abonoEfectivo;
                if (abonoTotal == 0) continue;

                // 🔸 Actualizar saldo y estado
                c.setSaldoPendiente(saldo);
                c.setFechaPagoReal(fecha);
                if (c.getSaldoPendiente() <= 0.01) {
                    c.setEstado("pagada");
                    c.setSaldoPendiente(0.0);
                } else {
                    c.setEstado("parcial");
                }

                // 🔸 Agregar detalle con número de operación
                Pago pagoTemporal = new Pago();
                pagoTemporal.setFechaPago(fecha);
                pagoTemporal.setMonto(abonoTotal);
                pagoTemporal.setMontoEfectivo(abonoEfectivo);
                pagoTemporal.setMontoTransferencia(abonoTransfer);
                pagoTemporal.setMetodoPago(metodoPago);
                pagoTemporal.setNumeroOperacion(numeroOperacion);

                cronogramaService.agregarDetallePago(c, pagoTemporal);
                cronogramaRepository.save(c);
            }

            // 🔹 Crear y guardar pago global
            pago = new Pago();
            pago.setPrestamo(prestamo);
            pago.setFechaPago(fecha);
            pago.setMetodoPago(metodoPago);
            pago.setNumeroOperacion(numeroOperacion);
            pago.setMonto(totalPagado);
            pago.setMontoEfectivo(efectivo);
            pago.setMontoTransferencia(transferencia);
            pago.setBanco(banco);
            pago.setBaucher(baucherBase64);

            pagoRepository.save(pago);
            
            // =========================================================
            // 🔹 Actualizar cuenta bancaria según método de pago
            // =========================================================
            if (transferencia > 0 && banco != null && !banco.isBlank()) {
                cuentaBancariaService.sumarSaldo(banco, transferencia);
            }
            if (efectivo > 0) {
                cuentaBancariaService.sumarSaldo("Efectivo", efectivo);
            }

            // =========================================================
            // 🔹 Si el nuevo pago tiene una fecha anterior a otros
            //    ya existentes, recalcular cronograma completo.
            // =========================================================
            List<Pago> pagosPrevios = pagoRepository.findAll().stream()
                    .filter(p -> p.getPrestamo().getId().equals(prestamoId))
                    .toList();

            boolean hayPagosPosteriores = pagosPrevios.stream()
                    .anyMatch(p -> p.getFechaPago().isAfter(fecha));

            if (hayPagosPosteriores) {
                System.out.println("⚙️ Se detectó un pago con fecha anterior, recalculando cronograma completo desde PrestamoService...");
                prestamoService.recalcularCronogramaPorPrestamo(prestamoId);
            }
        }

        // =========================================================
        // 🔹 Actualizar saldo general del préstamo
        // =========================================================
        double saldoActual = prestamo.getSaldoPendiente() != null
                ? prestamo.getSaldoPendiente()
                : prestamo.getTotalPagar();
        prestamo.setSaldoPendiente(Math.max(saldoActual - totalPagado, 0.0));
        prestamoRepository.save(prestamo);

        // =========================================================
        // 🔹 Actualizar montos en efectivo / banco
        // =========================================================
        try {
            if (efectivo > 0 && prestamo.getEmpleado() != null) {
                empleadoService.sumarEfectivo(prestamo.getEmpleado().getId(), efectivo);
            }
            if (transferencia > 0 && prestamo.getBancoEntrega() != null) {
                cuentaBancariaService.sumarSaldo(prestamo.getBancoEntrega(), transferencia);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando montos de pago: " + e.getMessage());
        }
    }
    
    // VALIDAR NUMERO DE OPERACION EXISTENTE
    public boolean existeNumeroOperacion(String numeroOperacion) {
        if (numeroOperacion == null || numeroOperacion.isBlank()) return false;
        return pagoRepository.existsByNumeroOperacion(numeroOperacion);
    }

    // ==========================================================
    //   RECALCULAR CRONOGRAMA COMPLETO POR PRÉSTAMO
    // ==========================================================
    private void recalcularCronogramaPorPrestamo(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // 🔹 Obtener todos los pagos del préstamo y ordenarlos:
        // Primero por fecha ascendente y, si coinciden, priorizando transferencias.
        List<Pago> pagosOrdenados = pagoRepository.findAll().stream()
                .filter(p -> p.getPrestamo().getId().equals(prestamoId))
                .sorted((p1, p2) -> {
                    int compFecha = p1.getFechaPago().compareTo(p2.getFechaPago());
                    if (compFecha != 0) return compFecha;
                    boolean t1 = p1.getMontoTransferencia() != null && p1.getMontoTransferencia() > 0;
                    boolean t2 = p2.getMontoTransferencia() != null && p2.getMontoTransferencia() > 0;
                    return Boolean.compare(!t1, !t2); // true → efectivo, false → transferencia
                })
                .toList();

        // 🔹 Borrar cronograma actual y regenerar desde cero
        List<Cronograma> cuotasActuales = cronogramaRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId);
        if (!cuotasActuales.isEmpty()) {
            cronogramaRepository.deleteAll(cuotasActuales);
        }
        List<Cronograma> cuotas = cronogramaService.generarCronograma(prestamoId);

        // 🔹 Reaplicar todos los pagos en orden
        for (Pago pago : pagosOrdenados) {
            double restanteTransf = pago.getMontoTransferencia() != null ? pago.getMontoTransferencia() : 0.0;
            double restanteEfec = pago.getMontoEfectivo() != null ? pago.getMontoEfectivo() : 0.0;

            for (Cronograma c : cuotas) {
                if (restanteTransf <= 0 && restanteEfec <= 0) break;

                double saldo = c.getSaldoPendiente() != null ? c.getSaldoPendiente() : c.getMontoTotal();
                double abonoTransfer = 0.0;
                double abonoEfectivo = 0.0;

                // 🔸 Aplicar primero transferencia
                if (restanteTransf > 0) {
                    abonoTransfer = Math.min(restanteTransf, saldo);
                    restanteTransf -= abonoTransfer;
                    saldo -= abonoTransfer;
                }

                // 🔸 Luego efectivo
                if (saldo > 0 && restanteEfec > 0) {
                    abonoEfectivo = Math.min(restanteEfec, saldo);
                    restanteEfec -= abonoEfectivo;
                    saldo -= abonoEfectivo;
                }

                double abonoTotal = abonoTransfer + abonoEfectivo;
                if (abonoTotal == 0) continue;

                // 🔸 Actualizar saldo, estado y fecha
                c.setSaldoPendiente(saldo);
                c.setFechaPagoReal(pago.getFechaPago());
                if (c.getSaldoPendiente() <= 0.01) {
                    c.setEstado("pagada");
                    c.setSaldoPendiente(0.0);
                } else if (abonoTotal > 0) {
                    c.setEstado("parcial");
                }

                // 🔸 Registrar detalles del pago
                cronogramaService.agregarDetallePago(c, pago);
                cronogramaRepository.save(c);
            }
        }

        // 🔹 Guardar cronograma actualizado
        cronogramaRepository.saveAll(cuotas);

        // 🔹 Recalcular saldo pendiente del préstamo
        double deudaPendiente = cuotas.stream()
                .mapToDouble(Cronograma::getSaldoPendiente)
                .sum();
        prestamo.setSaldoPendiente(deudaPendiente);
        prestamoRepository.save(prestamo);
    }


    // ==========================================================
    //   FACTURA PDF
    // ==========================================================
    public void generarFacturaPdf(Long pagoId, OutputStream out) {
        try {
            Pago pago = pagoRepository.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
            Prestamo prestamo = pago.getPrestamo();

            long cuotasAtrasadas = cronogramaRepository.contarCuotasAtrasadas(
                    prestamo.getId(), LocalDate.now());

            double totalPagado = (pago.getMontoEfectivo() != null ? pago.getMontoEfectivo() : 0)
                               + (pago.getMontoTransferencia() != null ? pago.getMontoTransferencia() : 0);
            double deuda = prestamo.getSaldoPendiente() != null ? prestamo.getSaldoPendiente() : 0.0;

            Rectangle ticket = new Rectangle(226, 600);
            Document doc = new Document(ticket, 20, 20, 20, 20);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 9);
            Font fontNegrita = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

            doc.add(new Paragraph("INVERSIONES ANYELO S.A.C", fontTitulo));
            doc.add(new Paragraph("FACTURA DE PAGO", fontNegrita));
            doc.add(new Paragraph("Fecha emisión: " + LocalDate.now(), fontNormal));
            doc.add(new LineSeparator());

            doc.add(new Paragraph("Cliente: " + prestamo.getCliente().getNombreCompleto(), fontNormal));
            doc.add(new Paragraph("Código Cliente: " + prestamo.getCliente().getCodigoCliente(), fontNormal));
            doc.add(new Paragraph("Préstamo ID: #" + prestamo.getId(), fontNormal));
            doc.add(new Paragraph("Fecha de Pago: " + pago.getFechaPago(), fontNormal));
            doc.add(new Paragraph("Método: " + pago.getMetodoPago(), fontNormal));

            if (pago.getNumeroOperacion() != null && !pago.getNumeroOperacion().isBlank()) {
                doc.add(new Paragraph("Operación: " + pago.getNumeroOperacion(), fontNormal));
            }

            doc.add(new Paragraph("Monto Pagado: $" + String.format("%.2f", totalPagado), fontNegrita));
            doc.add(new Paragraph("Saldo Pendiente: $" + String.format("%.2f", deuda), fontNormal));
            doc.add(new Paragraph("Cuotas Atrasadas: " + cuotasAtrasadas, fontNormal));

            doc.add(new LineSeparator());
            doc.add(new Paragraph("Gracias por su pago.", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC)));

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar factura: " + e.getMessage(), e);
        }
    }

    // 🔹 TOTAL COBRADO POR EMPLEADO Y RANGO
    public double obtenerTotalCobradoPorEmpleadoYRango(Long empleadoId, LocalDate desde, LocalDate hasta) {
        Double total = pagoRepository.obtenerTotalCobradoPorEmpleadoYRango(empleadoId, desde, hasta);
        return total != null ? total : 0.0;
    }
}
