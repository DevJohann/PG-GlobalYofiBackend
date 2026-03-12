package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Buscar por categoría
    List<Producto> findByCategoriaIdCategoria(Integer idCategoria);

    // Buscar por rango de precio
    List<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max);

    // Búsqueda combinada (categoría + rango de precio)
    @Query("SELECT p FROM Producto p WHERE (:categoriaId IS NULL OR p.categoria.idCategoria = :categoriaId) " +
            "AND (:minPrecio IS NULL OR p.precio >= :minPrecio) " +
            "AND (:maxPrecio IS NULL OR p.precio <= :maxPrecio)")
    List<Producto> buscarPorFiltros(@org.springframework.data.repository.query.Param("categoriaId") Integer categoriaId,
            @org.springframework.data.repository.query.Param("minPrecio") BigDecimal minPrecio,
            @org.springframework.data.repository.query.Param("maxPrecio") BigDecimal maxPrecio);
}
