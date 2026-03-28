package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.ProductoRequestDTO;
import com.globalyofi.backend.dto.ProductoResponseDTO;
import com.globalyofi.backend.entity.Categoria;
import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.entity.Proveedor;
import com.globalyofi.backend.entity.Inventario;
import com.globalyofi.backend.entity.Usuario;
import com.globalyofi.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

        @Autowired
        private ProductoRepository productoRepository;

        @Autowired
        private CategoriaRepository categoriaRepository;

        @Autowired
        private ProveedorRepository proveedorRepository;
        
        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private ItemCarritoRepository itemCarritoRepository;

        @Autowired
        private InventarioRepository inventarioRepository;

        @Autowired
        private DetallePedidoRepository detallePedidoRepository;

        @Autowired
        private FileStorageService fileStorageService;

        public List<ProductoResponseDTO> obtenerTodos() {
                return productoRepository.findAll()
                                .stream()
                                .map(this::convertirAResponseDTO)
                                .collect(Collectors.toList());
        }

        public ProductoResponseDTO obtenerPorId(Integer id) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Producto no encontrado con el id: " + id));
                return convertirAResponseDTO(producto);
        }

        // Filtro general avanzado profesional
        public List<ProductoResponseDTO> filtrar(List<Integer> categoriaIds, BigDecimal minPrecio, BigDecimal maxPrecio,
                        String search, String sortBy, String estado) {

                Sort sort = Sort.by("nombre").ascending(); // Default sort

                if (sortBy != null) {
                        switch (sortBy) {
                                case "price-asc":
                                        sort = Sort.by("precio").ascending();
                                        break;
                                case "price-desc":
                                        sort = Sort.by("precio").descending();
                                        break;
                                case "name-asc":
                                        sort = Sort.by("nombre").ascending();
                                        break;
                        }
                }

                Pageable pageable = PageRequest.of(0, 100, sort);

                return productoRepository.buscarPorFiltros(categoriaIds, minPrecio, maxPrecio, search, estado, pageable)
                                .stream()
                                .map(this::convertirAResponseDTO)
                                .collect(Collectors.toList());
        }

        // Filtro por categoría
        public List<ProductoResponseDTO> obtenerPorCategoria(Integer categoriaId) {
                return productoRepository.findByCategoriaIdCategoria(categoriaId)
                                .stream()
                                .filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstado()))
                                .map(this::convertirAResponseDTO)
                                .collect(Collectors.toList());
        }

        // Filtro por rango de precios
        public List<ProductoResponseDTO> obtenerPorRango(BigDecimal min, BigDecimal max) {
                return productoRepository.findByPrecioBetween(min, max)
                                .stream()
                                .filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstado()))
                                .map(this::convertirAResponseDTO)
                                .collect(Collectors.toList());
        }

        // Crear producto
        // *public ProductoResponseDTO guardar(ProductoRequestDTO dto) {
        // Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
        // .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        //
        // Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
        // .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        //
        // Producto producto = Producto.builder()
        // .nombre(dto.getNombre())
        // .descripcion(dto.getDescripcion())
        // .precio(dto.getPrecio())
        // .marca(dto.getMarca())
        // .stockActual(dto.getStockActual())
        // .stockMinimo(dto.getStockMinimo())
        // .fechaIngreso(LocalDateTime.now())
        // .estado("ACTIVO")
        // .imagenUrl(dto.getImagenUrl())
        // .categoria(categoria)
        // .proveedor(proveedor)
        // .build();
        //
        // productoRepository.save(producto);
        // return convertirAResponseDTO(producto);
        // }

        // Metodo para guardar imagen en el backend
        @Transactional
        public ProductoResponseDTO guardarConImagen(ProductoRequestDTO dto, MultipartFile imagen) {
                Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

                Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));

                // Guardar imagen si viene en la petición
                String imagenUrl = null;
                if (imagen != null && !imagen.isEmpty()) {
                        imagenUrl = fileStorageService.guardarArchivo(imagen);
                }

                Producto producto = Producto.builder()
                                .nombre(dto.getNombre())
                                .descripcion(dto.getDescripcion())
                                .precio(dto.getPrecio())
                                .marca(dto.getMarca())
                                .stockActual(dto.getStockActual())
                                .stockMinimo(dto.getStockMinimo())
                                .fechaIngreso(LocalDateTime.now())
                                .estado("ACTIVO")
                                .imagenUrl(imagenUrl)
                                .categoria(categoria)
                                .proveedor(proveedor)
                                .build();

                Producto guardado = productoRepository.save(producto);
                
                // Registrar movimiento inicial en inventario
                registrarMovimiento(guardado, "entrada", dto.getStockActual(), 0, dto.getStockActual(), "Carga inicial de producto");
                
                return convertirAResponseDTO(guardado);
        }

        // Actualizar producto
        // public ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO dto) {
        // Producto producto = productoRepository.findById(id)
        // .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        //
        // Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
        // .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        // Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
        // .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        //
        // producto.setNombre(dto.getNombre());
        // producto.setDescripcion(dto.getDescripcion());
        // producto.setPrecio(dto.getPrecio());
        // producto.setMarca(dto.getMarca());
        // producto.setStockActual(dto.getStockActual());
        // producto.setStockMinimo(dto.getStockMinimo());
        // producto.setEstado(dto.getEstado());
        // producto.setImagenUrl(dto.getImagenUrl());
        // producto.setCategoria(categoria);
        // producto.setProveedor(proveedor);
        //
        // productoRepository.save(producto);
        // return convertirAResponseDTO(producto);
        // }

        @Transactional
        public ProductoResponseDTO actualizarConImagen(Integer id, ProductoRequestDTO dto, MultipartFile nuevaImagen) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

                Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

                Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));

                Integer stockAnterior = producto.getStockActual();
                
                // Si se envía una nueva imagen, la guardamos y reemplazamos la anterior
                if (nuevaImagen != null && !nuevaImagen.isEmpty()) {
                        String nuevaUrl = fileStorageService.guardarArchivo(nuevaImagen);
                        producto.setImagenUrl(nuevaUrl);
                }

                // Actualizamos el resto de los campos
                producto.setNombre(dto.getNombre());
                producto.setDescripcion(dto.getDescripcion());
                producto.setPrecio(dto.getPrecio());
                producto.setMarca(dto.getMarca());
                producto.setStockActual(dto.getStockActual());
                producto.setStockMinimo(dto.getStockMinimo());
                producto.setEstado(dto.getEstado() != null ? dto.getEstado() : producto.getEstado());
                producto.setCategoria(categoria);
                producto.setProveedor(proveedor);

                Producto guardado = productoRepository.save(producto);
                
                // Si hubo cambio de stock, registrar movimiento
                if (!stockAnterior.equals(dto.getStockActual())) {
                    String tipo = dto.getStockActual() > stockAnterior ? "entrada" : "salida";
                    int diferencia = Math.abs(dto.getStockActual() - stockAnterior);
                    registrarMovimiento(guardado, tipo, diferencia, stockAnterior, dto.getStockActual(), "Actualización de stock vía panel admin");
                }
                
                return convertirAResponseDTO(guardado);
        }

        private void registrarMovimiento(Producto producto, String tipo, Integer cantidad, Integer anterior, Integer nuevo, String obs) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null && auth.isAuthenticated()) ? auth.getName() : "sistema@globalyofi.com";

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseGet(() -> {
                        // Si no hay usuario en sesión, buscar un usuario admin por defecto o el primero que exista
                        return usuarioRepository.findAll().stream()
                                .filter(u -> "ADMIN".equals(u.getRol()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No se encontró un usuario válido para registrar el movimiento"));
                    });

            Inventario movimiento = Inventario.builder()
                    .producto(producto)
                    .usuario(usuario)
                    .tipoMovimiento(tipo)
                    .cantidad(cantidad)
                    .stockAnterior(anterior)
                    .stockNuevo(nuevo)
                    .fechaMovimiento(LocalDateTime.now())
                    .observaciones(obs)
                    .build();

            inventarioRepository.save(movimiento);
        }

        @Transactional
        public void eliminar(Integer id) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con el id: " + id));

                // 1. Eliminar de carritos
                itemCarritoRepository.deleteByProductoIdProducto(id);

                // 2. Eliminar movimientos de inventario
                inventarioRepository.deleteByProductoIdProducto(id);

                // 3. Eliminar detalles de pedidos
                detallePedidoRepository.deleteByProductoIdProducto(id);

                // 4. Eliminar el producto físicamente
                productoRepository.delete(producto);
        }

        private ProductoResponseDTO convertirAResponseDTO(Producto producto) {
                return ProductoResponseDTO.builder()
                                .id(producto.getIdProducto())
                                .nombre(producto.getNombre())
                                .descripcion(producto.getDescripcion())
                                .precio(producto.getPrecio())
                                .marca(producto.getMarca())
                                .imagenUrl(producto.getImagenUrl())
                                .categoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                                .proveedor(producto.getProveedor() != null ? producto.getProveedor().getNombre() : null)
                                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null)
                                .proveedorId(producto.getProveedor() != null ? producto.getProveedor().getIdProveedor() : null)
                                .stockActual(producto.getStockActual())
                                .stockMinimo(producto.getStockMinimo())
                                .estado(producto.getEstado())
                                .build();
        }
}
