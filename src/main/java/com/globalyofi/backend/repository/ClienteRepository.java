package com.globalyofi.backend.repository;

import com.globalyofi.backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import com.globalyofi.backend.entity.Usuario;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario(Usuario usuario);
}
