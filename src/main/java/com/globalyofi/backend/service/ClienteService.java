package com.globalyofi.backend.service;

import com.globalyofi.backend.entity.Cliente;
import com.globalyofi.backend.entity.Usuario;
import com.globalyofi.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final com.globalyofi.backend.repository.UsuarioRepository usuarioRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente obtenerPorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    @Transactional
    public Cliente crear(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Integer id, Cliente detalles) {
        Cliente cliente = obtenerPorId(id);

        // Actualizar campos
        cliente.setDireccion(detalles.getDireccion());
        cliente.setCiudad(detalles.getCiudad());
        cliente.setCodigoPostal(detalles.getCodigoPostal());
        cliente.setTipoDocumento(detalles.getTipoDocumento());
        cliente.setNumeroDocumento(detalles.getNumeroDocumento());

        // ACTUALIZAR CAMPOS DEL USUARIO
        if (detalles.getUsuario() != null && cliente.getUsuario() != null) {
            Usuario userRepo = cliente.getUsuario();
            Usuario detallesUsuario = detalles.getUsuario();

            userRepo.setNombre(detallesUsuario.getNombre());
            userRepo.setApellido(detallesUsuario.getApellido());
            userRepo.setEmail(detallesUsuario.getEmail());
            userRepo.setTelefono(detallesUsuario.getTelefono());
            userRepo.setActivo(detallesUsuario.isActivo());
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = obtenerPorId(id);
        Usuario usuario = cliente.getUsuario();
        
        if (usuario != null) {
            // Un usuario puede tener varios movimientos en el inventario o reportes
            // pero para un CLIENTE usualmente no hay estos registros o queremos borrarlos
            usuarioRepository.delete(usuario);
        } else {
            clienteRepository.delete(cliente);
        }
    }
}
