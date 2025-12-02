-- ============================================================
-- Creación de la base de datos
-- ============================================================
DROP DATABASE IF EXISTS fablabdb;
CREATE DATABASE IF NOT EXISTS fablabdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fablabdb;
SET NAMES utf8mb4;

-- ============================================================
-- Enumeraciones
-- ============================================================
-- Estados generales para cursos e inscripciones
CREATE TABLE Estado_u (
    nombre ENUM('Activo', 'Finalizado', 'Cancelado') PRIMARY KEY
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Estado de máquinas
CREATE TABLE Estado_m (
    nombre ENUM('Disponible', 'En_mantenimiento') PRIMARY KEY
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Estado de turnos
CREATE TABLE Estado_turno (
    nombre ENUM('Disponible', 'Reservado', 'Cancelado') PRIMARY KEY
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Estado de reservas
CREATE TABLE Estado_reserva (
    nombre ENUM('Confirmada', 'Pendiente', 'Cancelada') PRIMARY KEY
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Estado de recibos
CREATE TABLE Estado_recibo (
    nombre ENUM('Pagado', 'Pendiente', 'Anulado') PRIMARY KEY
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Métodos de pago
CREATE TABLE Metodo_pago (
    nombre ENUM('Tarjeta', 'Efectivo', 'Online') PRIMARY KEY
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Usuario
-- ============================================================
CREATE TABLE Usuario (
    id_u INT AUTO_INCREMENT PRIMARY KEY,
    nombre_u VARCHAR(100) NOT NULL,
    email_u VARCHAR(100) UNIQUE NOT NULL,
    contrasena_u VARCHAR(100) NOT NULL,
    rol VARCHAR(50),
    telefono VARCHAR(20),
    fecha_registro DATE NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Curso
-- ============================================================
CREATE TABLE Curso (
    id_c INT AUTO_INCREMENT PRIMARY KEY,
    nombre_c VARCHAR(100) NOT NULL,
    descripcion_c TEXT,
    capacidad_c INT NOT NULL,
    fecha_ini DATE,
    fecha_fin DATE,
    precio FLOAT NOT NULL,
    estado_c ENUM('Activo', 'Finalizado', 'Cancelado') NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Inscripción
-- ============================================================
CREATE TABLE Inscripcion (
    id_insc INT AUTO_INCREMENT PRIMARY KEY,
    id_c INT NOT NULL,
    id_u INT NOT NULL,
    fecha_insc DATE NOT NULL,
    estado_insc ENUM('Activo', 'Finalizado', 'Cancelado') NOT NULL,
    FOREIGN KEY (id_c) REFERENCES Curso(id_c) ON DELETE CASCADE,
    FOREIGN KEY (id_u) REFERENCES Usuario(id_u) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Máquina
-- ============================================================
CREATE TABLE Maquina (
    id_m INT AUTO_INCREMENT PRIMARY KEY,
    nombre_m VARCHAR(100) NOT NULL,
    descripcion_m TEXT,
    ubicacion VARCHAR(100),
    precio_hora DECIMAL(10,2) DEFAULT 0.00,
    estado_m ENUM('Disponible', 'En_mantenimiento') NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Turno
-- ============================================================
CREATE TABLE Turno (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    id_m INT NOT NULL,
    fecha_turno DATE NOT NULL,
    hora_ini TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado_turno ENUM('Disponible', 'Reservado', 'Cancelado') NOT NULL,
    FOREIGN KEY (id_m) REFERENCES Maquina(id_m) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Reserva
-- ============================================================
CREATE TABLE Reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_u INT NOT NULL,
    id_turno INT NOT NULL,
    fecha_reserva DATE NOT NULL,
    estado_reserva ENUM('Confirmada', 'Pendiente', 'Cancelada') NOT NULL,
    FOREIGN KEY (id_u) REFERENCES Usuario(id_u) ON DELETE CASCADE,
    FOREIGN KEY (id_turno) REFERENCES Turno(id_turno) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Tabla: Recibo
-- ============================================================
CREATE TABLE Recibo (
    id_recibo INT AUTO_INCREMENT PRIMARY KEY,
    id_u INT NOT NULL,
    fecha_emision DATE NOT NULL,
    importe_total FLOAT NOT NULL,
    metodo_pago ENUM('Tarjeta', 'Efectivo', 'Online') NOT NULL,
    concepto VARCHAR(255),
    -- Asociaciones opcionales para más control
    id_c INT DEFAULT NULL,
    id_m INT DEFAULT NULL,
    estado_recibo ENUM('Pagado', 'Pendiente', 'Anulado') NOT NULL,
    FOREIGN KEY (id_u) REFERENCES Usuario(id_u) ON DELETE CASCADE,
    FOREIGN KEY (id_c) REFERENCES Curso(id_c) ON DELETE SET NULL,
    FOREIGN KEY (id_m) REFERENCES Maquina(id_m) ON DELETE SET NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de asociación entre recibos y turnos (un recibo de máquina puede cubrir varios turnos)
CREATE TABLE Recibo_Turno (
    id_recibo INT NOT NULL,
    id_turno INT NOT NULL,
    PRIMARY KEY (id_recibo, id_turno),
    FOREIGN KEY (id_recibo) REFERENCES Recibo(id_recibo) ON DELETE CASCADE,
    FOREIGN KEY (id_turno) REFERENCES Turno(id_turno) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Comprobación final
-- ============================================================
SHOW TABLES;