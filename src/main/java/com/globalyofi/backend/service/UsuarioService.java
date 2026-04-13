package com.globalyofi.backend.service;

import com.globalyofi.backend.entity.Usuario;
import com.globalyofi.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /** Lista todos los usuarios del sistema */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /** Obtiene un usuario por ID */
    public Usuario obtenerPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Asigna un nuevo rol al usuario.
     * Solo puede ser invocado por un ADMIN.
     *
     * @param id  ID del usuario a modificar
     * @param rol Nuevo rol (ej: "ADMIN", "CLIENTE")
     */
    @Transactional
    public Usuario asignarRol(Integer id, String rol) {
        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }
        String rolNormalizado = rol.trim().toUpperCase();
        if (!rolNormalizado.equals("ADMIN") && !rolNormalizado.equals("CLIENTE")) {
            throw new IllegalArgumentException("Rol inválido: " + rol + ". Los roles permitidos son ADMIN y CLIENTE.");
        }
        Usuario usuario = obtenerPorId(id);
        usuario.setRol(rolNormalizado);
        return usuarioRepository.save(usuario);
    }

    /**
     * Activa o desactiva un usuario (eliminación lógica desde gestión de usuarios).
     */
    @Transactional
    public Usuario cambiarEstadoActivo(Integer id, boolean activo) {
        Usuario usuario = obtenerPorId(id);
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }
}
