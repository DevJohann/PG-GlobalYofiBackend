package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Buscar por categoría
    List<Producto> findByCategoriaIdCategoria(Integer idCategoria);

    // Buscar por rango de precio
    List<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max);

    // Búsqueda combinada avanzada profesional (categorías + rango + búsqueda global)
    @Query("SELECT p FROM Producto p WHERE " +
            "(COALESCE(:categoriaIds, NULL) IS NULL OR p.categoria.idCategoria IN :categoriaIds) AND " +
            "(:minPrecio IS NULL OR p.precio >= :minPrecio) AND " +
            "(:maxPrecio IS NULL OR p.precio <= :maxPrecio) AND " +
            "(:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "p.estado = 'ACTIVO'")
    List<Producto> buscarPorFiltros(
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio,
            @Param("search") String search,
            Pageable pageable);
}
