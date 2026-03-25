package com.globalyofi.backend.service;

import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.repository.ClienteRepository;
import com.globalyofi.backend.repository.ProductoRepository;
import com.globalyofi.backend.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    //Productos agrupados por categoría
    public Map<String, Long> obtenerProductosPorCategoria() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getNombre(),
                        Collectors.counting()
                ));
    }

    //Stock total por proveedor
    public Map<String, Integer> obtenerStockPorProveedor() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getProveedor() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getProveedor().getNombre(),
                        Collectors.summingInt(Producto::getStockActual)
                ));
    }

    //Top 5 productos más caros
    public Map<String, Double> obtenerTopProductosPorPrecio() {
        return productoRepository.findAll().stream()
                .sorted(Comparator.comparing(Producto::getPrecio).reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Producto::getNombre,
                        p -> p.getPrecio().doubleValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    //Productos con bajo stock
    public List<Map<String, Object>> obtenerProductosConBajoStock() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStockActual() < p.getStockMinimo())
                .map(p -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("nombre", p.getNombre());
                    data.put("stockActual", p.getStockActual());
                    data.put("stockMinimo", p.getStockMinimo());
                    data.put("categoria", p.getCategoria() != null ? p.getCategoria().getNombre() : null);
                    return data;
                })
                .collect(Collectors.toList());
    }

    //Ventas totales por mes
    public List<Map<String, Object>> obtenerVentasPorMes() {
        return reporteRepository.ventasTotalesPorMes().stream()
                .map(r -> Map.of(
                        "anio", r[1],
                        "mes", r[0],
                        "totalVentas", r[2]
                ))
                .collect(Collectors.toList());
    }

    //Clientes con más compras
    public List<Map<String, Object>> obtenerClientesFrecuentes() {
        return reporteRepository.clientesConMasCompras().stream()
                .map(r -> Map.of(
                        "cliente", r[0],
                        "totalPedidos", r[1]
                ))
                .collect(Collectors.toList());
    }

    //Pedidos por estado
    public Map<String, Long> obtenerPedidosPorEstado() {
        return reporteRepository.pedidosPorEstado().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> ((Number) r[1]).longValue()
                ));
    }

    //Ventas por ciudad
    public Map<String, Double> obtenerVentasPorCiudad() {
        return reporteRepository.ventasPorCiudad().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0
                ));
    }

    //Rentabilidad por proveedor
    public Map<String, Double> obtenerRentabilidadPorProveedor() {
        return reporteRepository.rentabilidadPorProveedor().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0
                ));
    }

    //Rotación de inventario
    public List<Map<String, Object>> obtenerRotacionInventario() {
        return reporteRepository.rotacionInventario().stream()
                .map(r -> Map.of(
                        "producto", r[0],
                        "entradas", r[1],
                        "salidas", r[2]
                ))
                .collect(Collectors.toList());
    }

    //Historial de inventario
    public List<Map<String, Object>> obtenerHistorialInventario() {
        return reporteRepository.historialInventario().stream()
                .map(r -> Map.of(
                        "producto", r[0],
                        "tipoMovimiento", r[1],
                        "cantidad", r[2],
                        "fecha", r[3],
                        "usuario", r[4]
                ))
                .collect(Collectors.toList());
    }

    public Long obtenerTotalClientes() {
        return clienteRepository.count();
    }
}
