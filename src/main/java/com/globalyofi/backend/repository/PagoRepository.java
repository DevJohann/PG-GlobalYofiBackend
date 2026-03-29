package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByPedidoIdPedido(Integer pedidoId);
}
