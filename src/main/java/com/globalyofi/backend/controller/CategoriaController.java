package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.CategoriaRequestDTO;
import com.globalyofi.backend.dto.CategoriaResponseDTO;
import com.globalyofi.backend.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // Público: listar categorías activas
    @GetMapping
    public List<CategoriaResponseDTO> listar() {
        return categoriaService.obtenerTodas();
    }

    // ADMIN: listar TODAS las categorías (activas e inactivas)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public List<CategoriaResponseDTO> listarAdmin() {
        return categoriaService.obtenerTodasAdmin();
    }

    // Solo ADMIN puede crear categorías
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CategoriaResponseDTO crear(@Valid @RequestBody CategoriaRequestDTO dto) {
        return categoriaService.crear(dto);
    }

    // Solo ADMIN puede actualizar
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoriaResponseDTO actualizar(@PathVariable("id") Integer id,
            @Valid @RequestBody CategoriaRequestDTO dto) {
        return categoriaService.actualizar(id, dto);
    }

    // ADMIN: activar o desactivar categoría
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle")
    public CategoriaResponseDTO toggleStatus(@PathVariable("id") Integer id) {
        return categoriaService.toggleEstado(id);
    }

    // Solo ADMIN puede eliminar
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Integer id) {
        categoriaService.eliminar(id);
    }
}
