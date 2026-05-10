package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {
    List<Cuota> findByPrestamoId(Long prestamoId);
    Cuota findByPrestamoIdAndNumero(Long prestamoId, Integer numero);
}
