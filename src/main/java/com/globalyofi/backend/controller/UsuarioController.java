package com.globalyofi.backend.controller;

import com.globalyofi.backend.entity.Usuario;
import com.globalyofi.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** Lista todos los usuarios — solo ADMIN */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listar() {
        return usuarioService.listarTodos();
    }

    /** Obtiene un usuario por ID — solo ADMIN */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario obtener(@PathVariable("id") Integer id) {
        return usuarioService.obtenerPorId(id);
    }

    /**
     * Asigna un rol a un usuario — solo ADMIN.
     * Body: { "rol": "ADMIN" } o { "rol": "CLIENTE" }
     */
    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario asignarRol(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> body) {
        String rol = body.get("rol");
        return usuarioService.asignarRol(id, rol);
    }

    /**
     * Activa o desactiva un usuario — solo ADMIN.
     * Body: { "activo": true } o { "activo": false }
     */
    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario cambiarActivo(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Boolean> body) {
        Boolean activo = body.get("activo");
        if (activo == null) throw new IllegalArgumentException("El campo 'activo' es obligatorio");
        return usuarioService.cambiarEstadoActivo(id, activo);
    }
}
