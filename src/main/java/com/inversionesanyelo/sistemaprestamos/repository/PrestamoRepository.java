package com.inversionesanyelo.sistemaprestamos.repository;

import com.inversionesanyelo.sistemaprestamos.model.Prestamo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    @Query("SELECT MAX(p.id) FROM Prestamo p")
    Long obtenerUltimoId();

    @Query("""
    SELECT p FROM Prestamo p 
    WHERE LOWER(p.cliente.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(CONCAT('cli_', LPAD(CAST(p.cliente.id AS string), 3, '0'))) LIKE LOWER(CONCAT('%', :query, '%'))
       OR CAST(p.id AS string) LIKE CONCAT('%', :query, '%')
    """)
    List<Prestamo> buscarPrestamos(@Param("query") String query);

}
