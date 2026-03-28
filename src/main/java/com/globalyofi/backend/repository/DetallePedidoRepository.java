package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    @Transactional
    void deleteByProductoIdProducto(Integer idProducto);
}
