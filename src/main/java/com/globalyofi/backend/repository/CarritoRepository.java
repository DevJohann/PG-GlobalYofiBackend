package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    @Query("SELECT c FROM Carrito c WHERE c.cliente.idCliente = :clienteId AND c.estado = :estado")
    Optional<Carrito> findByClienteIdAndEstado(@Param("clienteId") Integer clienteId, @Param("estado") String estado);
}
