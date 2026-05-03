package com.globalyofi.backend.controller;

import com.globalyofi.backend.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // Productos por categoría (para gráfico de torta)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/productos-por-categoria")
    public Map<String, Long> productosPorCategoria() {
        return reporteService.obtenerProductosPorCategoria();
    }

    // Stock total por proveedor (para barras)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stock-por-proveedor")
    public Map<String, Integer> stockPorProveedor() {
        return reporteService.obtenerStockPorProveedor();
    }

    // Top 5 productos más caros (ranking)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/productos-top-precio")
    public Map<String, Double> productosMasCaros() {
        return reporteService.obtenerTopProductosPorPrecio();
    }

    // Productos con bajo stock
    @GetMapping("/productos-bajo-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> productosConBajoStock() {
        return reporteService.obtenerProductosConBajoStock();
    }

    // Ventas por mes
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ventas-por-mes")
    public List<Map<String, Object>> ventasPorMes() {
        return reporteService.obtenerVentasPorMes();
    }

    // Clientes frecuentes
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/clientes-frecuentes")
    public List<Map<String, Object>> clientesFrecuentes() {
        return reporteService.obtenerClientesFrecuentes();
    }

    // Pedidos por estado
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pedidos-por-estado")
    public Map<String, Long> pedidosPorEstado() {
        return reporteService.obtenerPedidosPorEstado();
    }

    // Ventas por ciudad
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ventas-por-ciudad")
    public Map<String, Double> ventasPorCiudad() {
        return reporteService.obtenerVentasPorCiudad();
    }

    // Rentabilidad por proveedor
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rentabilidad-por-proveedor")
    public Map<String, Double> rentabilidadPorProveedor() {
        return reporteService.obtenerRentabilidadPorProveedor();
    }

    // Rotación de inventario
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rotacion-inventario")
    public List<Map<String, Object>> rotacionInventario() {
        return reporteService.obtenerRotacionInventario();
    }

    // Historial de inventario
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/historial-inventario")
    public List<Map<String, Object>> historialInventario() {
        return reporteService.obtenerHistorialInventario();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/clientes-total")
    public Long obtenerTotalClientes() {
        return reporteService.obtenerTotalClientes();
    }

}
