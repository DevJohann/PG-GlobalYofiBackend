package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.transaction.annotation.Transactional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Integer> {
    @Transactional
    void deleteByProductoIdProducto(Integer idProducto);
}
