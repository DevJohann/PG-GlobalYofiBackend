#  Global Yofi - Backend

**Global Yofi** es una plataforma de administración y venta de productos de belleza.  
Este repositorio contiene el backend desarrollado en **Spring Boot 3**, utilizando **Java 17**, **Spring Security con JWT**, **JPA/Hibernate**, y **MySQL** como base de datos relacional.

---

##  Objetivo del Proyecto

El propósito de este sistema es centralizar y optimizar la gestión de productos, inventarios, proveedores, reportes y usuarios dentro de una tienda de belleza.  
Incluye autenticación segura mediante **JSON Web Tokens (JWT)** y soporte para **roles (ADMIN y CLIENTE)**.

---

##  Características Principales

-  **Gestión de Catálogo**: CRUD completo de Productos, Proveedores y Categorías.
-  **Seguridad**: Sistema de autenticación y registro basado en JWT (Stateless).
-  **Control de Acceso**: Gestión de usuarios con roles diferenciados (`ROLE_ADMIN`, `ROLE_CLIENTE`).
-  **Inventario**: Registro de movimientos de entrada y salida de stock.
-  **Ventas**: Gestión de pedidos, carritos de compras y validación de pagos.
-  **Reportes**: Generación de reportes técnicos de productos e inventarios.
-  **Arquitectura**: Desarrollado bajo principios RESTful y Clean Architecture.

---

##  Tecnologías Utilizadas

| Tipo | Tecnología / Herramienta |
|------|---------------------------|
| Lenguaje | Java 17 |
| Framework principal | Spring Boot 3.5.6 |
| Seguridad | Spring Security 6 + JWT |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8 |
| Construcción | Maven |

---

##  Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

-  **Java 17** (JDK)
-  **Maven 3.8+**
-  **MySQL Server 8.0+**
-  Un IDE (IntelliJ IDEA, VS Code con Java Extension Pack)

---

##  Guía de Inicio y Configuración

### 1. Preparar la Base de Datos

Se ha proporcionado un script completo para la creación de la estructura necesaria.

1.  Abre tu cliente de MySQL (Workbench, Terminal, etc.).
2.  Ejecuta el script ubicado en: `.Scripts/schema_completo.sql`.
    -  Este script creará la base de datos `global_yofi` y todas las tablas necesarias.
    -  También inicializará la configuración de pagos y el usuario administrador.

### 2. Configurar Propiedades
Edita el archivo `src/main/resources/application.properties` con tus credenciales locales:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/global_yofi?useSSL=false&serverTimezone=America/Bogota
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
```

### 3. Ejecutar la Aplicación
Desde la raíz del proyecto, utiliza Maven para compilar y ejecutar:

```bash
mvn clean install
mvn spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

---

##  Credenciales de Administrador (Inicial)

Para la entrega y pruebas iniciales, se ha precargado un usuario con privilegios de administrador:

-  **Usuario:** `admin@globalyofi.com`
-  **Contraseña:** `Admin123.,`

> [!IMPORTANT]
> Se recomienda cambiar esta contraseña después del primer inicio de sesión mediante los endpoints de gestión de usuarios por motivos de seguridad.

---

##  Resumen de Endpoints Principales

### Autenticación (`/api/auth`)
- `POST /api/auth/login`: Iniciar sesión y obtener token JWT.
- `POST /api/auth/register`: Registro de nuevos clientes.

### Productos (`/api/productos`)
- `GET /api/productos`: Listar todos los productos (público).
- `POST /api/productos`: Crear nuevo producto (solo ADMIN).

### Pedidos (`/api/pedidos`)
- `POST /api/pedidos/realizar`: Crear un nuevo pedido (clientes autenticados).
- `GET /api/pedidos/mis-pedidos`: Ver histórico personal.

---

##  Estructura del Proyecto

```text
src/
├── main/
│   ├── java/com/globalyofi/backend/
│   │   ├── config/               # Configuración de Seguridad y Beans
│   │   ├── controller/           # Controladores REST (Endpoints)
│   │   ├── dto/                  # Objetos de Transferencia de Datos
│   │   ├── entity/               # Entidades JPA (Modelos de BD)
│   │   ├── repository/           # Interfaces de Acceso a Datos
│   │   ├── security/             # Lógica de JWT y Filtros
│   │   └── service/              # Lógica de Negocio
│   └── resources/
│       ├── application.properties
│       └── .Scripts/             # Scripts SQL de inicialización
└── test/                         # Pruebas Unitarias e Integración
```

---

##  Autores
- **Edison Mauricio Beltrán** - [embeltrang@unbosque.edu.co]
- **Johann Toncon** - [jtoncon@unbosque.edu.co]
