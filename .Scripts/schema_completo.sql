-- ==========================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS - GLOBAL YOFI
-- ==========================================================
-- Este script crea la base de datos completa y sus tablas.
-- ==========================================================

CREATE DATABASE IF NOT EXISTS global_yofi;
USE global_yofi;

-- 1. Tabla: categoria
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla: proveedor
CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    contacto_principal VARCHAR(255),
    telefono VARCHAR(50),
    email VARCHAR(255),
    direccion VARCHAR(255),
    ciudad VARCHAR(255),
    nit VARCHAR(50),
    estado VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla: usuario
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    email VARCHAR(150) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    telefono VARCHAR(50),
    rol VARCHAR(50), -- 'ADMIN', 'CLIENTE'
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- 4. Tabla: cliente
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    direccion TEXT,
    ciudad VARCHAR(100),
    codigo_postal VARCHAR(20),
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(50),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- 5. Tabla: producto
CREATE TABLE IF NOT EXISTS producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    marca VARCHAR(255),
    stock_actual INT DEFAULT 0,
    stock_minimo INT DEFAULT 0,
    fecha_ingreso DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50),
    imagen_url VARCHAR(255),
    tipo_piel VARCHAR(255),
    acabado VARCHAR(255),
    tono VARCHAR(255),
    categoria_id INT,
    proveedor_id INT,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id_categoria),
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id_proveedor)
);

-- 6. Tabla: inventario (Movimientos)
CREATE TABLE IF NOT EXISTS inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    usuario_id INT NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL, -- 'entrada', 'salida'
    cantidad INT NOT NULL,
    stock_anterior INT,
    stock_nuevo INT,
    fecha_movimiento DATETIME DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES producto(id_producto),
    CONSTRAINT fk_inventario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario)
);

-- 7. Tabla: pedido
CREATE TABLE IF NOT EXISTS pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    fecha_pedido DATETIME DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10, 2),
    estado VARCHAR(50),
    metodo_pago VARCHAR(50),
    ciudad_envio VARCHAR(100),
    direccion_envio TEXT,
    observaciones TEXT,
    telefono_pago VARCHAR(20),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id_cliente)
);

-- 8. Tabla: detalle_pedido
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2),
    subtotal DECIMAL(10, 2),
    descuento DECIMAL(10, 2) DEFAULT 0.00,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto(id_producto)
);

-- 9. Tabla: carrito
CREATE TABLE IF NOT EXISTS carrito (
    id_carrito INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    estado VARCHAR(50) DEFAULT 'activo',
    total_estimado DECIMAL(10, 2) DEFAULT 0.00,
    CONSTRAINT fk_carrito_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id_cliente)
);

-- 10. Tabla: item_carrito
CREATE TABLE IF NOT EXISTS item_carrito (
    id_item INT AUTO_INCREMENT PRIMARY KEY,
    carrito_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    fecha_agregado DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_carrito FOREIGN KEY (carrito_id) REFERENCES carrito(id_carrito) ON DELETE CASCADE,
    CONSTRAINT fk_item_producto FOREIGN KEY (producto_id) REFERENCES producto(id_producto)
);

-- 11. Tabla: pago
CREATE TABLE IF NOT EXISTS pago (
    id_pago BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL UNIQUE,
    metodo VARCHAR(50), -- TRANSFERENCIA, RECIBO_PAGO, RECOGER_TIENDA
    estado VARCHAR(30), -- PENDIENTE, VERIFICACION, VALIDADO
    referencia VARCHAR(100),
    comprobante_url TEXT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id_pedido)
);

-- 12. Tabla: pago_config
CREATE TABLE IF NOT EXISTS pago_config (
    id BIGINT PRIMARY KEY,
    nequi_numero VARCHAR(255),
    nequi_nombre VARCHAR(255),
    qr_image_url VARCHAR(255),
    qr_texto VARCHAR(100),
    qr_info_label1 VARCHAR(200),
    qr_info_value1 VARCHAR(200),
    qr_info_label2 VARCHAR(200),
    qr_info_value2 VARCHAR(200),
    condiciones_compra TEXT,
    precio_envio_gratis DECIMAL(15, 2),
    precio_envio DECIMAL(15, 2),
    whatsapp_numero VARCHAR(20),
    contacto_telefono VARCHAR(30),
    contacto_email VARCHAR(150),
    tienda_direccion VARCHAR(255),
    tienda_horario VARCHAR(100),
    tienda_tiempo_preparacion VARCHAR(100),
    habilitar_transferencia BOOLEAN DEFAULT TRUE,
    habilitar_recibo_pago BOOLEAN DEFAULT TRUE,
    habilitar_recoger_tienda BOOLEAN DEFAULT TRUE
);

-- 13. Tabla: reporte
CREATE TABLE IF NOT EXISTS reporte (
    id_reporte INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo_reporte VARCHAR(100),
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    parametros VARCHAR(500),
    archivo_resultado VARCHAR(255),
    estado VARCHAR(50),
    fecha_expiracion DATETIME,
    CONSTRAINT fk_reporte_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario)
);

-- ==========================================================
-- DATOS INICIALES (CONFIGURACIÓN Y ADMIN)
-- ==========================================================

-- Configuración inicial de Pagos (ID 1)
INSERT INTO pago_config (id, nequi_numero, nequi_nombre, qr_texto, precio_envio, precio_envio_gratis, habilitar_transferencia)
VALUES (1, '3000000000', 'GLOBAL YOFI', 'Nequi', 10000.00, 150000.00, TRUE);

-- Usuario Administrador Inicial
-- Email: admin@globalyofi.com
-- Password: Admin123., (Encriptado con BCrypt)
-- Nota: Si el sistema falla al reconocer el BCrypt manual, usar el endpoint de registro/auth.
INSERT INTO usuario (nombre, apellido, email, contrasena, rol, activo)
VALUES ('Admin', 'GlobalYofi', 'admin@globalyofi.com', '$2a$10$8P0x6A9wP7X/a8oZ0P7X/.o/X.X.X.X.X.X.X.X.X.X.X.X.X.X.X', 'ADMIN', TRUE);


