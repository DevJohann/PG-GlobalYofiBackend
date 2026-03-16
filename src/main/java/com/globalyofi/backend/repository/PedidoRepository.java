package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByClienteIdClienteOrderByFechaPedidoDesc(Integer idCliente);
}
