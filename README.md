# Aplicación web del Fablab (Mérida)

**Metodología y Desarrollo de Aplicaciones para Internet – Curso 2025/26**

---
<!-- Badges -->
<div align="center">

[![Project Status](https://img.shields.io/badge/status-acad%C3%A9mico-blue)](https://github.com/calvarezju/ProyectoMDAI)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-enabled-orange)](https://www.thymeleaf.org/)
[![Maven](https://img.shields.io/badge/Maven-wrapper-yellow)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-pink)](https://www.docker.com/)

</div>

---

## 1. LOGO
<div align="center">
<img src="src/main/resources/static/img/logo.png" width="200">
</div>

---

## 2. Integrantes   
| Nombre | DNI | Foto |
| :--- | :---: | :---: |
| **Iván Herculano García** | 80100837W | <img src="src/main/resources/static/img/ivan.png" alt="Foto Iván" width="150"> |
| **Carmen Álvarez Murillo** | 80230317S | <img src="src/main/resources/static/img/carmen.png" alt="Foto Carmen" width="150"> |

---

## 3. Eslogan
> "El futuro se fabrica aquí."

---

## 4. Resumen
La aplicación web del FabLab de Mérida busca ser una plataforma para la gestión y difusión de los servicios del laboratorio de fabricación digital de la Universidad de Mérida.

---

## 5. Descripción
La aplicación web del FabLab Mérida permitirá a los usuarios gestionar de forma sencilla el acceso a los servicios del laboratorio de fabricación digital.

**Permitirá a los usuarios:**
* Registrarse e iniciar sesión.
* Ver los cursos disponibles.
* Consultar el catálogo de máquinas, cursos y productos.
* Ver la disponibilidad mediante un calendario y realizar reservas.
* Realizar pagos

La web ofrecerá también información práctica como los precios.

---

## 6. Funcionalidades, Requisitos y Pliego de condiciones

### 6.1 Funcionalidades principales
* **Gestión de usuarios:** Registro e inicio de sesión.
* **Reservas:** Calendario y tabla para reservar máquinas y cursos.
* **Cursos y talleres:** Listado, inscripción y gestión de plazas.
* **Información general:** Presentación del FabLab, normas, contacto y ubicación.
* **Productos:** Catálogo online.
* **Pagos:** Se prodece a realizar el pago en línea.
* Historial de uso de máquinas y cursos para cada usuario.
* Integración con redes sociales para difundir cursos y proyectos.

### 6.2 Requisitos
* **Funcionales (RF):** Registro/Login, Consulta de máquinas, Reservas, Productos, Inscripción a cursos, Gestión por administradores.
* **No funcionales (RNF):** Interfaz clara y responsive, Seguridad de datos, Rapidez en la navegación.

### 6.3 Pliego de condiciones
* Desarrollo en HTML, CSS y JavaScript (con opción a frameworks).
* Base de datos para usuarios, máquinas, reservas y cursos.
* Uso orientado a estudiantes y administradores.
* Compatible con navegadores comunes (Chrome, Firefox, Edge).
*Entrega con código fuente, documentación y manual de usuario.

### 6.4 Funcionalidades opcionales, recomendables o futuribles 
* Notificaciones por correo sobre reservas, recordatorios de cursos o incidencias.
* Sección de proyectos de usuarios donde compartir trabajos realizados en el FabLab.

---

## 7. Tecnologías Utilizadas

| Tecnología | Versión | Uso Principal |
| :--- | :---: | :--- |
| **Java** | 21 | Lenguaje base |
| **Spring Boot** | 3.5.6 | Framework principal |
| **Spring Data JPA** | 3.5.6 | Persistencia de datos |
| **Thymeleaf** | 3.1.3 | Motor de plantillas |
| **Bootstrap** | 5.3 | Diseño responsive |
| **MySQL** | 8.0 | Base de datos |
| **Docker** | - | Contenerización |
| **Maven** | - | Gestión del proyecto |

---

## 8. Estructura del Proyecto

```text
📦 Documentacion
 ┣ 📂 mysql
 ┃ ┣ 01_CREATE.sql           # Script de creación de la base de datos
 ┃ ┣ 02_POPULATE.sql         # Script de inserción de datos
 ┃ ┗ Acceso a mysql.txt      # Instrucciones de acceso
 ┃
📦 ProyectoMDAI
 ┣ 📂 docker
 ┃ ┗ 📂 mysql
 ┃   ┣ 📂 backups            # Backups de la base de datos
 ┃   ┗ 📂 init
 ┃      ┣ 01_CREATE.sql      # Script de creación (copia)
 ┃      ┗ 02_POPULATE.sql    # Script de inserción (copia)
 ┣ 📂 src
 ┃ ┣ 📂 main
 ┃ ┃ ┣ 📂 java
 ┃ ┃ ┃ ┗ 📂 com
 ┃ ┃ ┃   ┗ 📂 uex
 ┃ ┃ ┃     ┗ 📂 fablab
 ┃ ┃ ┃       ┣ 📂 controller    # Controladores (peticiones HTTP)
 ┃ ┃ ┃       ┣ 📂 data
 ┃ ┃ ┃       ┃ ┣ 📂 model       # Entidades JPA
 ┃ ┃ ┃       ┃ ┣ 📂 repository  # Repositorios de datos
 ┃ ┃ ┃       ┃ ┗ 📂 services    # Lógica de negocio
 ┃ ┃ ┣ 📂 resources
 ┃ ┃ ┃ ┣ application.properties # Configuración
 ┃ ┃ ┃ ┣ 📂 static
 ┃ ┃ ┃ ┃ ┣ 📂 css              # Hojas de estilo
 ┃ ┃ ┃ ┃ ┣ 📂 img              # Imágenes
 ┃ ┃ ┃ ┃ ┣ 📂 js               # Scripts JS
 ┃ ┃ ┃ ┃ ┗ 📂 uploads          # Subidas de archivos
 ┃ ┃ ┃ ┣ 📂 templates
 ┃ ┃ ┃ ┃ ┣ cart.html, login.html, ... # Vistas Thymeleaf
 ┃ ┃ ┃ ┃ ┣ 📂 admin            # Vistas de administración
 ┃ ┃ ┃ ┃ ┣ 📂 fragments        # Fragmentos reutilizables
 ┃ ┃ ┃ ┃ ┗ 📂 user             # Vistas de usuario
 ┃ ┗ 📂 test
 ┃   ┗ 📂 java
 ┃     ┗ 📂 com
 ┃       ┗ 📂 uex
 ┃         ┗ 📂 fablab
 ┃           ┣ 📂 repository   # Tests de repositorios
 ┃           ┗ 📂 usecases     # Tests de casos de uso
 ┣ 📂 target                   # Archivos generados (compilación)
 ┣ clean-docker.bat            # Limpieza de contenedores Docker
 ┣ start-docker.bat            # Inicialización de Docker
 ┣ start-app.bat               # Arranque de la aplicación Spring Boot
 ┗ README.md                   # Documentación del proyecto
```
## 9. Docker: Creación de la Base de Datos

Abre la carpeta general ("Proyecto MDAI") y ejecuta los siguientes archivos en orden:

```powershell
 📜start-docker.bat  # Inicializa el Docker con la configuración necesaria
 📜clean-docker.bat  # Limpia la configuración del Docker
 📜start-app.bat     # Inicializa springboots
 ```

---

## 10. Batería de tests: Casos de uso

El sistema ha sido diseñado para cubrir los principales flujos de trabajo del FabLab. A continuación se detallan las interacciones principales validadas en los tests de uso (`FablabUseCasesTest`).

### 10.1 Actor: Usuario (Estudiante/Miembro)
Estos casos de uso describen las acciones que puede realizar un usuario estándar:

* **Gestión de Cuenta:**
    * **Registrarse:** Crear una cuenta con nombre, email único y contraseña.
    * **Iniciar Sesión:** Acceso seguro mediante credenciales.
    * **Modificar Perfil:** Actualización de datos personales desde el panel de usuario.

* **Reserva de Máquinas:**
    * **Consultar Catálogo:** Visualización de máquinas disponibles (impresoras 3D, láser, CNC, etc.) con sus características y precios por hora.
    * **Consultar Disponibilidad:** Verificación de turnos (*shifts*) libres para una máquina y fecha concreta.
    * **Realizar Reserva:** Bloqueo de un turno de máquina, quedando vinculado al usuario.
    * **Cancelar Reserva:** Posibilidad de anular reservas existentes.

* **Cursos y Formación:**
    * **Explorar Cursos:** Visualización de la oferta de talleres, descripciones, aforo y fechas.
    * **Inscribirse en Curso:** Registro formal en una actividad formativa (*inscription*).
    * **Pagar Inscripción:** Generación del recibo correspondiente para formalizar la plaza.

* **Tienda y Productos:**
    * **Catálogo de Productos:** Consulta de materiales y componentes disponibles.
    * **Selección de Subproductos:** Elección de variantes específicas de un producto (ej. color de filamento o modelo de sensor).
    * **Gestión del Carrito:** Añadir ítems al carrito de compra, ajustar cantidades y persistir la selección por usuario.
    * **Recibos de Compra:** Generación de recibos detallados con productos, precios unitarios y total de la transacción.

### 10.2 Actor: Administrador
El administrador tiene control total sobre los recursos y la supervisión del centro:

* **Gestión de Recursos (CRUD):**
    * **Alta de Máquinas:** Registrar nuevo equipamiento definiendo ubicación, coste y estado.
    * **Gestión de Cursos:** Crear, modificar o eliminar la oferta formativa (fechas, aforo, precios).
    * **Gestión de Productos:** Administrar el catálogo de materiales y sus existencias (stock).

* **Gestión de Usuarios:**
    * **Listar Usuarios:** Supervisión de todos los miembros registrados en la plataforma.
    * **Modificar Roles:** Asignación o revocación de permisos de administración.
    * **Eliminar Usuarios:** Baja de cuentas con borrado automático de sus reservas e inscripciones asociadas.

* **Supervisión y Control:**
    * **Gestión de Turnos:** Configurar y actualizar los horarios disponibles para cada máquina.
    * **Control de Pagos:** Seguimiento y actualización del estado de los recibos (Pendiente/Pagado).

### 10.3 Cobertura de Tests e Integridad (`FablabUseCasesTest`)
La robustez del sistema se garantiza mediante la verificación de la persistencia y las relaciones en:
* `UserRepository`: Gestión de miembros y seguridad.
* `MachineRepository` & `ShiftRepository`: Disponibilidad y catálogo de maquinaria.
* `BookingRepository`: Lógica de reservas de turnos.
* `CourseRepository` & `InscriptionRepository`: Control académico y de plazas.
* `ProductRepository` & `SubProductRepository`: Inventario detallado de materiales.
* `CartRepository` & `CartItemRepository`: Flujo de compra y carrito.
* `ReceiptRepository`: Trazabilidad de transacciones y estados de pago.

---

## 11. Guía de uso

1.  **Base de datos:** Consultar punto 9.
2.  **Arrancar la aplicación:** Consultar punto 9.
3.  **Acceder:** Abrir el navegador en la siguiente URL:
    [http://localhost:8081](http://localhost:8081)

---


## 12. Configuración del Entorno

Para ejecutar el proyecto localmente, asegúrate de que el archivo `src/main/resources/application.properties` tiene configurada la conexión a la base de datos que crearemos con Docker.

**Configuración requerida:**
```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3307/fablabdb
spring.datasource.username=admin
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
# Ruta para subida de imágenes (ajustar según entorno)
app.storage.location=src/main/resources/static/img/upload
```

## 13. Enlace a Github
**Repositorio:**
    [https://github.com/calvarezju/ProyectoMDAI.git](https://github.com/calvarezju/ProyectoMDAI.git)

## 14. Script de población

Para garantizar que la aplicación sea funcional y demostrable desde el primer momento, el proyecto incluye un mecanismo de **población de datos**.

> Un **"script de población"** se interpreta en este contexto como el conjunto de instrucciones que llena o administra el contenido de la página web de manera dinámica o automatizada. Su objetivo es cargar contenido inicial desde la base de datos para que la interfaz muestre elementos reales (catálogos, agendas, perfiles) sin necesidad de entrada manual previa.

### Implementación en el Proyecto

Hemos automatizado este proceso mediante el archivo `docker/mysql/init/02_POPULATE.sql`. Al ejecutar este script en la base de datos dockerizada, la aplicación web "cobra vida" instantáneamente con el siguiente contenido dinámico:

1. **Usuarios y Roles:**
    * Se generan usuarios predefinidos (`admin` y `user`) con contraseñas encriptadas para probar los sistemas de login, perfiles y niveles de acceso.
2. **Catálogo de Máquinas:**
    * Puebla la vista `/machines` con equipamiento real (Impresoras 3D, Láser, CNC) incluyendo descripciones, precios, ubicaciones e imágenes.
3. **Agenda y Disponibilidad:**
    * Genera turnos (*shifts*) dinámicos para los días siguientes a la fecha actual (usando funciones `CURDATE()`), permitiendo probar el **Calendario de Reservas** y la tabla de disponibilidad de forma inmediata.
4. **Oferta Académica:**
    * Carga cursos de ejemplo en la vista `/courses` para validar el flujo completo de exploración, inscripción y pago.
5. **Inventario de Productos:**
    * Carga el catálogo de materiales y componentes con sus respectivos subproductos (variantes de color, tamaño o tipo) para probar el sistema de **Carrito de Compra** y gestión de stock.


