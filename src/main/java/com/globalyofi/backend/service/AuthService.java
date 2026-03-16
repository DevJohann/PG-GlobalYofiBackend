package com.globalyofi.backend.service;

import com.globalyofi.backend.entity.Cliente;
import com.globalyofi.backend.entity.Usuario;
import com.globalyofi.backend.repository.ClienteRepository;
import com.globalyofi.backend.repository.UsuarioRepository;
import com.globalyofi.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    //Registrar un nuevo usuario en el sistema
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    //Iniciar sesión y generar un token JWT
    public Map<String, Object> login(String email, String contrasena) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, contrasena));
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
            
            System.out.println("DEBUG [Auth]: Login successful for " + email);
            System.out.println("DEBUG [Auth]: Server Time: " + java.time.LocalDateTime.now());
            System.out.println("DEBUG [Auth]: Token IAT (approx): " + System.currentTimeMillis() / 1000);

            Map<String, Object> response = new HashMap<>();
            
            // Lógica para devolver el ID correcto (idCliente si es CLIENTE, idUsuario si es ADMIN)
            Integer idParaFrontend;
            if ("CLIENTE".equals(usuario.getRol())) {
                idParaFrontend = clienteRepository.findByUsuario(usuario)
                        .map(Cliente::getIdCliente)
                        .orElseGet(() -> {
                            // Si es un cliente antiguo que no tiene perfil, lo creamos on-the-fly
                            Cliente nuevoCliente = Cliente.builder()
                                    .usuario(usuario)
                                    .fechaRegistro(java.time.LocalDateTime.now())
                                    .build();
                            return clienteRepository.save(nuevoCliente).getIdCliente();
                        });
            } else {
                idParaFrontend = usuario.getIdUsuario();
            }

            response.put("id", idParaFrontend);
            response.put("token", token);
            response.put("rol", usuario.getRol());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            return response;

        } catch (AuthenticationException e) {
            throw new RuntimeException("Credenciales inválidas");
        }
    }
}
