package com.inversionesanyelo.sistemaprestamos.service;

import com.inversionesanyelo.sistemaprestamos.model.CuentaBancaria;
import com.inversionesanyelo.sistemaprestamos.repository.CuentaBancariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CuentaBancariaService {

    @Autowired
    private CuentaBancariaRepository cuentaBancariaRepository;

    // 🔹 Listar todas las cuentas
    public List<CuentaBancaria> listarTodas() {
        return cuentaBancariaRepository.findAll();
    }
    
    //Validad saldos en las cuentas(BCP, Plin Interbanck, Efectivo)
    public boolean tieneSaldoDisponible(String nombreBanco, Double monto) {
        return cuentaBancariaRepository.findByNombre(nombreBanco)
                .map(cuenta -> cuenta.getSaldo() >= monto)
                .orElse(false);
    }

    public void restarSaldoConValidacion(String nombreBanco, Double monto) {
        cuentaBancariaRepository.findByNombre(nombreBanco).ifPresentOrElse(cuenta -> {
            if (monto == null || monto <= 0) return;
            if (cuenta.getSaldo() < monto) {
                throw new RuntimeException("Fondos insuficientes en " + nombreBanco 
                    + ". Disponible: " + cuenta.getSaldo() + " | Requerido: " + monto);
            }
            cuenta.setSaldo(cuenta.getSaldo() - monto);
            cuentaBancariaRepository.save(cuenta);
        }, () -> {
            throw new RuntimeException("Cuenta no encontrada: " + nombreBanco);
        });
    }


    // 🔹 Actualizar saldo de una cuenta específica
    public void actualizarSaldo(Long id, Double nuevoSaldo) {
        CuentaBancaria cuenta = cuentaBancariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        cuenta.setSaldo(nuevoSaldo);
        cuentaBancariaRepository.save(cuenta);
    }

    // 🔹 Sumar al saldo actual
    public void sumarSaldo(String nombreBanco, Double monto) {
        cuentaBancariaRepository.findByNombre(nombreBanco).ifPresent(cuenta -> {
            if (monto != null) {
                cuenta.setSaldo(cuenta.getSaldo() + monto);
                cuentaBancariaRepository.save(cuenta);
            }
        });
    }

    // 🔹 Restar del saldo actual
    public void restarSaldo(String nombreBanco, Double monto) {
        cuentaBancariaRepository.findByNombre(nombreBanco).ifPresent(cuenta -> {
            if (monto != null) {
                cuenta.setSaldo(cuenta.getSaldo() - monto);
                cuentaBancariaRepository.save(cuenta);
            }
        });
    }


    // 🔹 Reiniciar todos los saldos a cero
    public void reiniciarSaldos() {
        List<CuentaBancaria> cuentas = cuentaBancariaRepository.findAll();
        for (CuentaBancaria c : cuentas) {
            c.setSaldo(0.0);
        }
        cuentaBancariaRepository.saveAll(cuentas);
    }
    
    public void sumarSaldoPorNombre(String nombre, Double monto) {
        CuentaBancaria cuenta = cuentaBancariaRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Banco no encontrado: " + nombre));
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        cuentaBancariaRepository.save(cuenta);
    }
    
    // ==========================================================
    // 🔹 Buscar cuenta por nombre (sin ignore case)
    // ==========================================================
    public Optional<CuentaBancaria> buscarPorNombre(String nombre) {
        return cuentaBancariaRepository.findByNombre(nombre);
    }
    
    // ==========================================================
    // 🔹 Restar saldo a una cuenta por su nombre (para registrar gastos)
    // ==========================================================
    public void restarSaldoPorNombre(String nombreCuenta, Double monto) {
        CuentaBancaria cuenta = cuentaBancariaRepository.findByNombre(nombreCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + nombreCuenta));

        double nuevoSaldo = cuenta.getSaldo() - monto;
        if (nuevoSaldo < 0) {
            throw new RuntimeException("Saldo insuficiente en la cuenta: " + nombreCuenta);
        }

        cuenta.setSaldo(nuevoSaldo);
        cuentaBancariaRepository.save(cuenta);
    }

}
