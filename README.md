

# FabLab Mérida Web App

<div align="center">
   <img src="src/main/resources/static/img/logo.png" alt="Logo del FabLab" />
   <br/>
   <img src="Readme/Preview.png" alt="Vista previa de la app" width="600"/>
   <br/>
   <em>Vista previa de la aplicación FabLab Mérida</em>
</div>
---

![Java](https://img.shields.io/badge/Java-21-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

Aplicación web para la gestión de un laboratorio de fabricación digital (FabLab) en Mérida. Permite a usuarios y administradores gestionar reservas de máquinas, cursos, productos y pagos de forma sencilla y centralizada.

---

## 🚀 Características principales

- Registro e inicio de sesión de usuarios
- Catálogo de máquinas y productos
- Reserva de máquinas con calendario de disponibilidad
- Inscripción y gestión de cursos/talleres
- Carrito de compra y pagos online
- Panel de administración para gestión de recursos y usuarios
- Interfaz responsive y fácil de usar

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **Thymeleaf**
- **Bootstrap 5.3**
- **MySQL 8**
- **Docker**
- **Maven**

---

## 📦 Estructura del proyecto

```
├── docker/
│   └── mysql/
│       ├── backups/
│       └── init/
│           ├── 01_CREATE.sql
│           └── 02_POPULATE.sql
├── src/
│   ├── main/java/com/uex/fablab/
│   │   ├── controller/
│   │   ├── data/model/
│   │   ├── data/repository/
│   │   └── data/services/
│   ├── resources/
│   │   ├── application.properties
│   │   ├── static/
│   │   └── templates/
│   └── test/java/com/uex/fablab/
├── pom.xml
├── start-docker.bat
├── clean-docker.bat
├── start-app.bat
└── README.md
```

---

## ⚡ Instalación rápida

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/iviivanoff24/FabLab.git
   cd FabLab
   ```
2. **Arranca la base de datos y la aplicación:**
   ```powershell
   ./start-docker.bat   # Inicia MySQL en Docker
   ./start-app.bat      # Arranca la aplicación Spring Boot
   ```
3. **Accede a la web:**
   Abre [http://localhost:8081](http://localhost:8081) en tu navegador.

---

## ⚙️ Configuración

Asegúrate de que el archivo `src/main/resources/application.properties` contiene:

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3307/fablabdb
spring.datasource.username=admin
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
app.storage.location=src/main/resources/static/img/upload
```

Puedes modificar estos valores según tus necesidades.

---

## 🧪 Datos de prueba y scripts

El script [`docker/mysql/init/02_POPULATE.sql`](docker/mysql/init/02_POPULATE.sql) carga datos de ejemplo: usuarios, máquinas, cursos, productos y reservas para que puedas probar la app desde el primer momento.

---

## 🖥️ Scripts útiles

- `start-docker.bat`: Inicia el contenedor de MySQL en Docker
- `clean-docker.bat`: Elimina contenedores y volúmenes de Docker
- `start-app.bat`: Arranca la aplicación Spring Boot

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Puedes abrir issues o pull requests para sugerir mejoras, reportar bugs o añadir nuevas funcionalidades.

1. Haz un fork del repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Realiza tus cambios y haz commit (`git commit -am 'Añade nueva funcionalidad'`)
4. Haz push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

## 📫 Contacto

¿Dudas o sugerencias? Abre un issue o contacta con los autores en el repositorio.


