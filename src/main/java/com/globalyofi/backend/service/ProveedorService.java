package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.ProveedorRequestDTO;
import com.globalyofi.backend.dto.ProveedorResponseDTO;
import com.globalyofi.backend.entity.Proveedor;
import com.globalyofi.backend.repository.ProveedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /** Lista solo proveedores activos — para el frontend general */
    public List<ProveedorResponseDTO> obtenerTodos() {
        return proveedorRepository.findAll().stream()
                .filter(Proveedor::isActivo)
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /** Lista TODOS los proveedores — solo para ADMIN */
    public List<ProveedorResponseDTO> obtenerTodosAdmin() {
        return proveedorRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        if (proveedorRepository.existsByNit(dto.getNit())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese NIT");
        }

        Proveedor proveedor = Proveedor.builder()
                .nombre(dto.getNombre())
                .contactoPrincipal(dto.getContactoPrincipal())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .direccion(dto.getDireccion())
                .ciudad(dto.getCiudad())
                .nit(dto.getNit())
                .estado(dto.getEstado())
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();

        proveedorRepository.save(proveedor);
        return convertirAResponse(proveedor);
    }

    public ProveedorResponseDTO actualizar(Integer id, ProveedorRequestDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));

        proveedor.setNombre(dto.getNombre());
        proveedor.setContactoPrincipal(dto.getContactoPrincipal());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setCiudad(dto.getCiudad());
        proveedor.setNit(dto.getNit());
        proveedor.setEstado(dto.getEstado());

        proveedorRepository.save(proveedor);
        return convertirAResponse(proveedor);
    }

    /**
     * Eliminación LÓGICA: marca el proveedor como inactivo.
     * Los datos del proveedor se conservan para el historial de productos.
     */
    public void eliminar(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    private ProveedorResponseDTO convertirAResponse(Proveedor proveedor) {
        return ProveedorResponseDTO.builder()
                .id(proveedor.getIdProveedor())
                .nombre(proveedor.getNombre())
                .contactoPrincipal(proveedor.getContactoPrincipal())
                .telefono(proveedor.getTelefono())
                .email(proveedor.getEmail())
                .direccion(proveedor.getDireccion())
                .ciudad(proveedor.getCiudad())
                .nit(proveedor.getNit())
                .estado(proveedor.getEstado())
                .fechaRegistro(proveedor.getFechaRegistro())
                .build();
    }
}
