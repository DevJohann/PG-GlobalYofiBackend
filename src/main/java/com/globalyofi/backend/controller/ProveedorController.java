package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.ProveedorRequestDTO;
import com.globalyofi.backend.dto.ProveedorResponseDTO;
import com.globalyofi.backend.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // Público: listar proveedores activos
    @GetMapping
    public List<ProveedorResponseDTO> listar() {
        return proveedorService.obtenerTodos();
    }

    // ADMIN: listar TODOS los proveedores (activos e inactivos)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public List<ProveedorResponseDTO> listarAdmin() {
        return proveedorService.obtenerTodosAdmin();
    }

    // Solo ADMIN puede crear proveedores
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProveedorResponseDTO crear(@Valid @RequestBody ProveedorRequestDTO dto) {
        return proveedorService.crear(dto);
    }

    // Solo ADMIN puede actualizar
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProveedorResponseDTO actualizar(@PathVariable("id") Integer id,
            @Valid @RequestBody ProveedorRequestDTO dto) {
        return proveedorService.actualizar(id, dto);
    }

    // Solo ADMIN puede eliminar (desactivar lógicamente)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Integer id) {
        proveedorService.eliminar(id);
    }

    // Solo ADMIN puede alternar el estado activo/inactivo
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle")
    public ProveedorResponseDTO toggleEstado(@PathVariable("id") Integer id) {
        return proveedorService.toggleEstado(id);
    }
}
