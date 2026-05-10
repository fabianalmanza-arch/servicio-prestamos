package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.Cronograma;
import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.model.Pago;
import com.inversionesanyelo.sistemaprestamos.model.Prestamo;
import com.inversionesanyelo.sistemaprestamos.repository.CronogramaRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PagoRepository;
import com.inversionesanyelo.sistemaprestamos.repository.PrestamoRepository;
import com.inversionesanyelo.sistemaprestamos.service.ClienteService;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    
    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Autowired
    private CronogramaService cronogramaService;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private CuentaBancariaService cuentaBancariaService;
    
    @Autowired
    private ClienteService clienteService;


    public PrestamoService(PrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    public Prestamo guardar(Prestamo prestamo) {

        boolean esNuevo = (prestamo.getId() == null || !prestamoRepository.existsById(prestamo.getId()));

        if (esNuevo) {
            // 🔹 Si no envían ID → generar consecutivo
            if (prestamo.getId() == null) {
                Long ultimo = prestamoRepository.obtenerUltimoId();
                prestamo.setId((ultimo != null ? ultimo : 0) + 1);
            }
        }
        Prestamo entidad;
        if (esNuevo) {
            entidad = prestamo;
        } else {
            entidad = prestamoRepository.findById(prestamo.getId())
                    .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

            // ✅ Solo actualiza campos editables, no toques las listas
            entidad.setMonto(prestamo.getMonto());
            entidad.setDias(prestamo.getDias());
            entidad.setFecha(prestamo.getFecha());
            entidad.setMetodoEntrega(prestamo.getMetodoEntrega());
            entidad.setMontoEfectivo(prestamo.getMontoEfectivo());
            entidad.setMontoTransferencia(prestamo.getMontoTransferencia());
            entidad.setBancoEntrega(prestamo.getBancoEntrega());
        }

        // Recalcular valores derivados
        LocalDate fechaSeleccionada = entidad.getFecha() != null
                ? entidad.getFecha()
                : LocalDate.now();
        entidad.setFecha(fechaSeleccionada);

        LocalDate fechaInicio = fechaSeleccionada.plusDays(1);
        if (fechaInicio.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fechaInicio = fechaInicio.plusDays(1);
        }
        entidad.setFechaInicio(fechaInicio);

        entidad.setInteres(0.20);
        double totalPagar = entidad.getMonto() + (entidad.getMonto() * entidad.getInteres());
        entidad.setTotalPagar(totalPagar);
        entidad.setCuotaDiaria(totalPagar / entidad.getDias());

        // Cálculo de fecha fin
        LocalDate fechaFin = fechaInicio;
        int diasAgregados = 0;
        while (diasAgregados < entidad.getDias()) {
            fechaFin = fechaFin.plusDays(1);
            if (fechaFin.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasAgregados++;
            }
        }
        entidad.setFechaFin(fechaFin);
        
        // ==========================================================
        //   AJUSTAR SALDO DE CUENTAS BANCARIAS (TRANSFERENCIAS)
        // ==========================================================
        if (esNuevo) {
            System.out.println("🧾 DEBUG PRESTAMO NUEVO → metodoEntrega=" + entidad.getMetodoEntrega()
                + ", montoEfectivo=" + entidad.getMontoEfectivo()
                + ", montoTransferencia=" + entidad.getMontoTransferencia());

            if (("transferencia".equalsIgnoreCase(entidad.getMetodoEntrega()) || 
                "mixto".equalsIgnoreCase(entidad.getMetodoEntrega())) && 
                entidad.getBancoEntrega() != null && entidad.getMontoTransferencia() != null) {

                // 🔹 Validar fondos antes de descontar
                if (!cuentaBancariaService.tieneSaldoDisponible(entidad.getBancoEntrega(), entidad.getMontoTransferencia())) {
                    throw new RuntimeException("Fondos insuficientes en la cuenta " + entidad.getBancoEntrega());
                }

                cuentaBancariaService.restarSaldoConValidacion(entidad.getBancoEntrega(), entidad.getMontoTransferencia());
            }
            // ==========================================================
            //   AJUSTAR SALDO DE EFECTIVO SI CORRESPONDE
            // ==========================================================
            // Si se entregó parte en efectivo
            if (("efectivo".equalsIgnoreCase(entidad.getMetodoEntrega()) || 
                "mixto".equalsIgnoreCase(entidad.getMetodoEntrega())) && 
                entidad.getMontoEfectivo() != null && entidad.getMontoEfectivo() > 0) {

                // 🔹 Validar que exista cuenta "Efectivo"
                if (!cuentaBancariaService.tieneSaldoDisponible("Efectivo", entidad.getMontoEfectivo())) {
                    throw new RuntimeException("Fondos insuficientes en caja (Efectivo)");
                }

                // 🔹 Restar del saldo de efectivo
                cuentaBancariaService.restarSaldoConValidacion("Efectivo", entidad.getMontoEfectivo());
            }
            
            System.out.println("🧾 DEBUG PRESTAMO NUEVO → metodoEntrega=" + entidad.getMetodoEntrega()
                + ", montoEfectivo=" + entidad.getMontoEfectivo()
                + ", montoTransferencia=" + entidad.getMontoTransferencia());

        } else {
            // 🧾 Edición: comparar banco o monto previo
            String bancoAnterior = entidad.getBancoEntrega();
            Double montoAnterior = entidad.getMontoTransferencia();
            String bancoNuevo = prestamo.getBancoEntrega();
            Double montoNuevo = prestamo.getMontoTransferencia();

            boolean bancoCambiado = bancoAnterior != null && bancoNuevo != null && !bancoAnterior.equals(bancoNuevo);
            boolean montoCambiado = montoAnterior != null && montoNuevo != null && !montoAnterior.equals(montoNuevo);

            if (("transferencia".equalsIgnoreCase(entidad.getMetodoEntrega()) ||
                "mixto".equalsIgnoreCase(entidad.getMetodoEntrega()))) {
                if (bancoCambiado) {
                    cuentaBancariaService.sumarSaldo(bancoAnterior, montoAnterior);
                    cuentaBancariaService.restarSaldoConValidacion(bancoNuevo, montoNuevo);
                } else if (montoCambiado) {
                    double diferencia = montoNuevo - montoAnterior;
                    if (diferencia > 0) {
                        cuentaBancariaService.restarSaldoConValidacion(bancoNuevo, diferencia);
                    } else if (diferencia < 0) {
                        cuentaBancariaService.sumarSaldo(bancoNuevo, Math.abs(diferencia));
                    }
                }
            }

            // 🔸 Ajuste de efectivo si cambió
            if (("efectivo".equalsIgnoreCase(entidad.getMetodoEntrega()) ||
                "mixto".equalsIgnoreCase(entidad.getMetodoEntrega()))) {
                Double montoEfectivoAnterior = entidad.getMontoEfectivo();
                Double montoEfectivoNuevo = prestamo.getMontoEfectivo();
                boolean montoEfectivoCambiado = montoEfectivoAnterior != null && montoEfectivoNuevo != null
                                                && !montoEfectivoAnterior.equals(montoEfectivoNuevo);

                if (montoEfectivoCambiado) {
                    double diferencia = montoEfectivoNuevo - montoEfectivoAnterior;
                    if (diferencia > 0) {
                        cuentaBancariaService.restarSaldoConValidacion("Efectivo", diferencia);
                    } else if (diferencia < 0) {
                        cuentaBancariaService.sumarSaldo("Efectivo", Math.abs(diferencia));
                    }
                }
            }
        }
        // Guardar sin perder la relación
        return prestamoRepository.save(entidad);
    }


    public Prestamo buscarPorId(Long id) {
        return prestamoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        prestamoRepository.deleteById(id);
    }
    
    public List<Prestamo> buscarPrestamos(String query) {
        return prestamoRepository.buscarPrestamos(query);
    }
    
    // ==========================================================
    //   REGENERAR CRONOGRAMA COMPLETO TRAS EDITAR UN PRÉSTAMO
    // ==========================================================
    public void recalcularCronogramaPorPrestamo(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // 🔹 Obtener todos los pagos asociados al préstamo y ordenarlos
        List<Pago> pagosOrdenados = pagoRepository.findAll().stream()
                .filter(p -> p.getPrestamo().getId().equals(prestamoId))
                .sorted((p1, p2) -> {
                    int compFecha = p1.getFechaPago().compareTo(p2.getFechaPago());
                    if (compFecha != 0) return compFecha;
                    return p1.getId().compareTo(p2.getId());
                })
                .toList();

        // 🔹 Borrar cronograma actual
        List<Cronograma> existentes = cronogramaRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId);
        if (!existentes.isEmpty()) {
            cronogramaRepository.deleteAll(existentes);
        }

        // 🔹 Regenerar cronograma base (y asegurar orden)
        List<Cronograma> cuotas = cronogramaService.generarCronograma(prestamoId).stream()
                .sorted((a, b) -> Integer.compare(a.getNumeroCuota(), b.getNumeroCuota()))
                .toList();

        // 🔹 Reaplicar todos los pagos existentes
        for (Pago pago : pagosOrdenados) {
            double restanteTransf = pago.getMontoTransferencia() != null ? pago.getMontoTransferencia() : 0.0;
            double restanteEfec = pago.getMontoEfectivo() != null ? pago.getMontoEfectivo() : 0.0;

            System.out.printf(
                "🔁 Reaplicando pago ID %d (%s): Efectivo=%.2f | Transf=%.2f%n",
                pago.getId(), pago.getFechaPago(), restanteEfec, restanteTransf
            );

            for (Cronograma c : cuotas) {
                if (restanteTransf <= 0 && restanteEfec <= 0) break;

                double saldo = c.getSaldoPendiente() != null ? c.getSaldoPendiente() : c.getMontoTotal();
                double abonoTransfer = 0.0;
                double abonoEfectivo = 0.0;

                // 🔸 Aplica primero transferencia
                if (restanteTransf > 0 && saldo > 0) {
                    abonoTransfer = Math.min(restanteTransf, saldo);
                    restanteTransf -= abonoTransfer;
                    saldo -= abonoTransfer;
                }

                // 🔸 Luego efectivo
                if (restanteEfec > 0 && saldo > 0) {
                    abonoEfectivo = Math.min(restanteEfec, saldo);
                    restanteEfec -= abonoEfectivo;
                    saldo -= abonoEfectivo;
                }

                double abonoTotal = abonoTransfer + abonoEfectivo;
                if (abonoTotal == 0) continue;

                // 🔸 Actualiza cuota
                c.setSaldoPendiente(saldo);
                c.setFechaPagoReal(pago.getFechaPago());
                if (saldo <= 0.01) {
                    c.setSaldoPendiente(0.0);
                    c.setEstado("pagada");
                } else {
                    c.setEstado("parcial");
                }

                // 🔸 Crear pago parcial solo con lo aplicado a esta cuota
                Pago pagoParcial = new Pago();
                pagoParcial.setMetodoPago(pago.getMetodoPago());
                pagoParcial.setFechaPago(pago.getFechaPago());
                pagoParcial.setMontoEfectivo(abonoEfectivo);
                pagoParcial.setMontoTransferencia(abonoTransfer);
                pagoParcial.setNumeroOperacion(pago.getNumeroOperacion());

                cronogramaService.agregarDetallePago(c, pagoParcial);
                cronogramaRepository.save(c);

                System.out.printf("   ➜ Cuota #%d: Efectivo %.2f | Transf %.2f | Saldo %.2f%n",
                        c.getNumeroCuota(), abonoEfectivo, abonoTransfer, saldo);
            }
        }

        // 🔹 Recalcular saldo general del préstamo
        double deudaPendiente = cuotas.stream()
                .mapToDouble(Cronograma::getSaldoPendiente)
                .sum();
        prestamo.setSaldoPendiente(deudaPendiente);
        prestamoRepository.save(prestamo);

        System.out.printf("✅ Recalculado cronograma para préstamo #%d | Nuevo saldo: %.2f%n",
                prestamoId, deudaPendiente);
    }
    
}
