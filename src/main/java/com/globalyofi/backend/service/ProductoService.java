package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.ProductoRequestDTO;
import com.globalyofi.backend.dto.ProductoResponseDTO;
import com.globalyofi.backend.entity.Categoria;
import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.entity.Proveedor;
import com.globalyofi.backend.repository.CategoriaRepository;
import com.globalyofi.backend.repository.ProductoRepository;
import com.globalyofi.backend.repository.ProveedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

        // Filtro general
        public List<ProductoResponseDTO> filtrar(Integer categoriaId, BigDecimal minPrecio, BigDecimal maxPrecio) {
                return productoRepository.buscarPorFiltros(categoriaId, minPrecio, maxPrecio)
                                .stream()
                                .map(this::convertirAResponseDTO)
                                .collect(Collectors.toList());
        }

        // Filtro por categoría
        public List<ProductoResponseDTO> obtenerPorCategoria(Integer categoriaId) {
                return productoRepository.findByCategoriaIdCategoria(categoriaId)
                                .stream()
                                .map(this::convertirAResponseDTO)
                                .collect(Collectors.toList());
        }

        // Filtro por rango de precios
        public List<ProductoResponseDTO> obtenerPorRango(BigDecimal min, BigDecimal max) {
                return productoRepository.findByPrecioBetween(min, max)
                                .stream()
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

                productoRepository.save(producto);
                return convertirAResponseDTO(producto);
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

        public ProductoResponseDTO actualizarConImagen(Integer id, ProductoRequestDTO dto, MultipartFile nuevaImagen) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

                Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

                Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));

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

                productoRepository.save(producto);
                return convertirAResponseDTO(producto);
        }

        // Eliminar producto
        public void eliminar(Integer id) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

                producto.setEstado("INACTIVO");
                productoRepository.save(producto);
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
                                .build();
        }
}
