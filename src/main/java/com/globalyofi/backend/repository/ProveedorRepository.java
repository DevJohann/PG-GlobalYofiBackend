package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    boolean existsByNit(String nit);
    
    // Método para filtrar usando tanto la columna activo (booleana) o la columna estado (texto) independientemente de minúsculas/mayúsculas
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true OR LOWER(p.estado) = 'activo'")
    List<Proveedor> findActiveProveedores();
}
