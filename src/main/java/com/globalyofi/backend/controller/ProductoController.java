package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.ProductoRequestDTO;
import com.globalyofi.backend.dto.ProductoResponseDTO;
import com.globalyofi.backend.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Endpoint general (filtros combinados)
    @GetMapping
    public List<ProductoResponseDTO> listarProductos(
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "minPrecio", required = false) BigDecimal minPrecio,
            @RequestParam(value = "maxPrecio", required = false) BigDecimal maxPrecio) {
        return productoService.filtrar(categoriaId, minPrecio, maxPrecio);
    }

    @GetMapping("/{id}")
    public ProductoResponseDTO obtenerPorId(@PathVariable("id") Integer id) {
        return productoService.obtenerPorId(id);
    }

    // Buscar por categoría
    @GetMapping("/categoria/{id}")
    public List<ProductoResponseDTO> obtenerPorCategoria(@PathVariable("id") Integer id) {
        return productoService.obtenerPorCategoria(id);
    }

    // Buscar por rango de precio
    @GetMapping("/precio")
    public List<ProductoResponseDTO> obtenerPorPrecio(
            @RequestParam("min") BigDecimal min,
            @RequestParam("max") BigDecimal max) {
        return productoService.obtenerPorRango(min, max);
    }

    // Solo ADMIN puede crear productos
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = { "multipart/form-data" })
    public ProductoResponseDTO crearProducto(
            @RequestPart("producto") ProductoRequestDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return productoService.guardarConImagen(dto, imagen);
    }

    // Actualizar producto (solo ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ProductoResponseDTO actualizarProducto(
            @PathVariable("id") Integer id,
            @RequestPart("producto") ProductoRequestDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return productoService.actualizarConImagen(id, dto, imagen);
    }

    // Eliminar producto (solo ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable("id") Integer id) {
        productoService.eliminar(id);
    }
}
