package com.globalyofi.backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ReporteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Ventas totales por mes
    public List<Object[]> ventasTotalesPorMes() {
        return entityManager.createNativeQuery("""
                    SELECT
                        MONTH(p.fecha_pedido) AS mes,
                        YEAR(p.fecha_pedido) AS anio,
                        SUM(p.total) AS total_ventas
                    FROM pedido p
                    GROUP BY YEAR(p.fecha_pedido), MONTH(p.fecha_pedido)
                    ORDER BY anio, mes
                """).getResultList();
    }

    // Clientes con más compras
    public List<Object[]> clientesConMasCompras() {
        return entityManager.createNativeQuery("""
                    SELECT
                        u.nombre AS cliente,
                        COUNT(pe.id_pedido) AS total_pedidos
                    FROM pedido pe
                    JOIN cliente c ON pe.cliente_id = c.id_cliente
                    JOIN usuario u ON c.usuario_id = u.id_usuario
                    GROUP BY u.nombre
                    ORDER BY total_pedidos DESC
                    LIMIT 5
                """).getResultList();
    }

    // Pedidos por estado
    public List<Object[]> pedidosPorEstado() {
        return entityManager.createNativeQuery("""
                    SELECT
                        estado, COUNT(*) AS cantidad
                    FROM pedido
                    GROUP BY estado
                """).getResultList();
    }

    // Ventas por ciudad
    public List<Object[]> ventasPorCiudad() {
        return entityManager.createNativeQuery("""
                    SELECT
                        ciudad_envio, SUM(total) AS total
                    FROM pedido
                    GROUP BY ciudad_envio
                """).getResultList();
    }

    // Rentabilidad por proveedor
    public List<Object[]> rentabilidadPorProveedor() {
        return entityManager.createNativeQuery("""
                    SELECT
                        pr.nombre AS proveedor,
                        SUM(dp.subtotal) AS ingresos_totales
                    FROM detalle_pedido dp
                    JOIN producto p ON dp.producto_id = p.id_producto
                    JOIN proveedor pr ON p.proveedor_id = pr.id_proveedor
                    GROUP BY pr.nombre
                    ORDER BY ingresos_totales DESC
                """).getResultList();
    }

    // Rotación de inventario
    public List<Object[]> rotacionInventario() {
        return entityManager.createNativeQuery("""
                    SELECT
                        p.nombre AS producto,
                        SUM(CASE WHEN i.tipo_movimiento = 'ENTRADA' THEN i.cantidad ELSE 0 END) AS total_entradas,
                        SUM(CASE WHEN i.tipo_movimiento = 'SALIDA' THEN i.cantidad ELSE 0 END) AS total_salidas
                    FROM inventario i
                    JOIN producto p ON i.producto_id = p.id_producto
                    GROUP BY p.nombre
                """).getResultList();
    }

    // Historial de movimientos de inventario
    public List<Object[]> historialInventario() {
        return entityManager.createNativeQuery("""
                    SELECT
                        p.nombre AS producto,
                        i.tipo_movimiento,
                        i.cantidad,
                        i.fecha_movimiento,
                        u.nombre AS usuario
                    FROM inventario i
                    JOIN producto p ON i.producto_id = p.id_producto
                    JOIN usuario u ON i.usuario_id = u.id_usuario
                    ORDER BY i.fecha_movimiento DESC
                    LIMIT 50
                """).getResultList();
    }
}
