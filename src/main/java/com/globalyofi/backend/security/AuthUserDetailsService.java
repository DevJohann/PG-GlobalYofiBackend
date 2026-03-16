package com.globalyofi.backend.security;

import com.globalyofi.backend.entity.Usuario;
import com.globalyofi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        //Retornamos un objeto User de Spring Security
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasena())
                .roles(usuario.getRol() != null ? usuario.getRol().toUpperCase() : "CLIENTE")
                // Nota: .roles("ADMIN") en Spring Security genera automáticamente "ROLE_ADMIN"
                // Pero lo dejamos así que es la forma estándar de User.builder()
                .disabled(!usuario.isActivo())
                .build();
    }
}
