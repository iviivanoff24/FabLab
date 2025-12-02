USE fablabdb;

-- ***************************************************************
-- Inserts abundantes para poblar la base de datos de prueba
-- ***************************************************************

-- ----------------------------------------------------------------
-- 1) Tablas de estado / enumeraciones
-- ----------------------------------------------------------------
INSERT IGNORE INTO Estado_u (nombre) VALUES ('Activo'), ('Finalizado'), ('Cancelado');
INSERT IGNORE INTO Estado_m (nombre) VALUES ('Disponible'), ('En_mantenimiento');
INSERT IGNORE INTO Estado_turno (nombre) VALUES ('Disponible'), ('Reservado'), ('Cancelado');
INSERT IGNORE INTO Estado_reserva (nombre) VALUES ('Confirmada'), ('Pendiente'), ('Cancelada');
INSERT IGNORE INTO Estado_recibo (nombre) VALUES ('Pagado'), ('Pendiente'), ('Anulado');
INSERT IGNORE INTO Metodo_pago (nombre) VALUES ('Tarjeta'), ('Efectivo'), ('Online');

-- ----------------------------------------------------------------
-- 2) Usuarios (20 usuarios - ids 1..20)
-- ----------------------------------------------------------------
INSERT INTO Usuario (id_u, nombre_u, email_u, contrasena_u, rol, telefono, fecha_registro) VALUES
(1, 'Administrador', 'admin@fablab.local', 'Admin123', 'ADMIN', '600000000', '2025-01-01'),
(2, 'Pablo Martín', 'pablo.martin@example.com', 'PabloPass1', 'USER', '600001001', '2025-02-01'),
(3, 'Lucía Pérez', 'lucia.perez@example.com', 'LuciaPass2', 'USER', '600002002', '2025-02-02'),
(4, 'Isabel Gómez', 'isabel.gomez@example.com', 'IsabelPass3', 'USER', '600003003', '2025-02-03'),
(5, 'Jorge Ruiz', 'jorge.ruiz@example.com', 'JorgePass4', 'USER', '600004004', '2025-02-04'),
(6, 'Sergio Fernández', 'sergio.fernandez@example.com', 'SergioPass5', 'USER', '600005005', '2025-02-05'),
(7, 'Ana Castillo', 'ana.castillo@example.com', 'AnaPass6', 'USER', '600006006', '2025-02-06'),
(8, 'María López', 'maria.lopez@example.com', 'MariaPass7', 'USER', '600007007', '2025-02-07'),
(9, 'Carlos Díaz', 'carlos.diaz@example.com', 'CarlosPass8', 'USER', '600008008', '2025-02-08'),
(10, 'Laura Sánchez', 'laura.sanchez@example.com', 'LauraPass9', 'USER', '600009009', '2025-02-09'),
(11, 'Andrés Romero', 'andres.romero@example.com', 'Andres10', 'USER', '600010010', '2025-02-10'),
(12, 'Felipe Ortega', 'felipe.ortega@example.com', 'Felipe11', 'USER', '600011011', '2025-02-11'),
(13, 'Patricia Varela', 'patricia.varela@example.com', 'Patri12', 'USER', '600012012', '2025-02-12'),
(14, 'Diego Marín', 'diego.marin@example.com', 'Diego13', 'USER', '600013013', '2025-02-13'),
(15, 'Rosa Navarro', 'rosa.navarro@example.com', 'Rosa14', 'USER', '600014014', '2025-02-14'),
(16, 'Victor Salas', 'victor.salas@example.com', 'Victor15', 'USER', '600015015', '2025-02-15'),
(17, 'Marta Gil', 'marta.gil@example.com', 'Marta16', 'USER', '600016016', '2025-02-16'),
(18, 'Óscar Ramos', 'oscar.ramos@example.com', 'Oscar17', 'USER', '600017017', '2025-02-17'),
(19, 'Nuria Molina', 'nuria.molina@example.com', 'Nuria18', 'USER', '600018018', '2025-02-18'),
(20, 'Paco Herrera', 'paco.herrera@example.com', 'Paco19', 'USER', '600019019', '2025-02-19');

-- ----------------------------------------------------------------
-- 3) Cursos (10 cursos)
-- ----------------------------------------------------------------
INSERT INTO Curso (id_c, nombre_c, descripcion_c, capacidad_c, fecha_ini, fecha_fin, precio, estado_c) VALUES
(1, 'Introducción a Impresión 3D', 'Curso básico de impresión 3D', 12, '2025-11-01', '2025-11-30', 45.00, 'Activo'),
(2, 'Corte Láser para Principiantes', 'Uso de cortadora láser y seguridad', 10, '2025-12-05', '2025-12-20', 55.00, 'Activo'),
(3, 'Electrónica Básica', 'Taller práctico de electrónica', 15, '2026-01-10', '2026-01-20', 60.00, 'Activo'),
(4, 'Modelado CAD', 'Modelado 3D con herramientas CAD', 10, '2025-12-01', '2025-12-10', 70.00, 'Activo'),
(5, 'Robótica Arduino', 'Proyectos con Arduino', 8, '2026-02-01', '2026-02-28', 120.00, 'Activo'),
(6, 'Taller de Prototipos', 'Prototipado rápido y testing', 20, '2025-10-10', '2025-10-20', 30.00, 'Finalizado'),
(7, 'Diseño de Producto', 'Conceptos de diseño y fabricación', 12, '2026-03-01', '2026-03-15', 90.00, 'Activo'),
(8, 'Impresión Textil', 'Serigrafía y transferencia térmica', 10, '2025-12-15', '2025-12-16', 40.00, 'Activo'),
(9, 'Mecanizado CNC', 'Fresado y control numérico', 6, '2026-04-01', '2026-04-10', 150.00, 'Activo'),
(10, 'Introducción a Láser CNC', 'Curso intensivo de láser CNC', 10, NULL, NULL, 80.00, 'Activo');

-- ----------------------------------------------------------------
-- 4) Inscripciones (simulamos mucha actividad: 30 inscripciones)
-- ----------------------------------------------------------------
INSERT INTO Inscripcion (id_insc, id_c, id_u, fecha_insc, estado_insc) VALUES
(1, 1, 2, '2025-10-20', 'Activo'),
(2, 1, 3, '2025-10-21', 'Activo'),
(3, 1, 4, '2025-10-22', 'Activo'),
(4, 2, 5, '2025-11-10', 'Activo'),
(5, 2, 6, '2025-11-11', 'Activo'),
(6, 2, 7, '2025-11-12', 'Activo'),
(7, 3, 8, '2025-11-20', 'Activo'),
(8, 3, 9, '2025-11-21', 'Activo'),
(9, 4, 10, '2025-11-22', 'Activo'),
(10, 4, 11, '2025-11-23', 'Activo'),
(11, 5, 12, '2025-11-24', 'Activo'),
(12, 6, 13, '2025-09-15', 'Finalizado'),
(13, 6, 14, '2025-09-16', 'Finalizado'),
(14, 7, 15, '2025-11-25', 'Activo'),
(15, 8, 16, '2025-11-26', 'Activo'),
(16, 8, 17, '2025-11-27', 'Activo'),
(17, 9, 18, '2025-11-28', 'Activo'),
(18, 10, 19, '2025-11-29', 'Activo'),
(19, 2, 20, '2025-11-29', 'Activo'),
(20, 4, 5, '2025-11-30', 'Activo'),
(21, 1, 6, '2025-11-30', 'Activo'),
(22, 1, 7, '2025-11-30', 'Activo'),
(23, 3, 2, '2025-11-28', 'Activo'),
(24, 3, 3, '2025-11-29', 'Activo'),
(25, 5, 4, '2025-11-30', 'Activo'),
(26, 7, 8, '2025-11-30', 'Activo'),
(27, 7, 9, '2025-11-30', 'Activo'),
(28, 8, 10, '2025-11-30', 'Activo'),
(29, 9, 11, '2025-11-30', 'Activo'),
(30, 10, 12, '2025-11-30', 'Activo');

-- ----------------------------------------------------------------
-- 5) Máquinas (8 máquinas)
-- ----------------------------------------------------------------
INSERT INTO Maquina (id_m, nombre_m, descripcion_m, ubicacion, precio_hora, estado_m) VALUES
(1, 'Impresora 3D Prusa MK3', 'Impresora FDM de alta precisión', 'Taller 1', 2.50, 'Disponible'),
(2, 'Cortadora Láser Epilog', 'Corte y grabado láser CO2', 'Taller 2', 5.00, 'Disponible'),
(3, 'Fresadora CNC 6040', 'Fresado de madera y plásticos', 'Taller 3', 8.00, 'Disponible'),
(4, 'Impresora 3D SLA', 'Resina para piezas finas', 'Taller 1', 4.00, 'En_mantenimiento'),
(5, 'Plotter Vinilo', 'Corte de vinilos y adhesivos', 'Taller 2', 3.00, 'Disponible'),
(6, 'CNC Router', 'Router para madera', 'Taller 3', 7.50, 'Disponible'),
(7, 'Impresora Textil', 'Prensa y serigrafía', 'Taller 4', 6.00, 'Disponible'),
(8, 'CNC Laser S/R', 'Láser de gran formato', 'Taller 2', 10.00, 'Disponible');

-- ----------------------------------------------------------------
-- 6) Turnos (Reservado) - Generamos muchos turnos para 2 semanas laborales
--    Fechas: 2025-12-01 .. 2025-12-14 (se omiten sábados y domingos en la lógica de ciclo)
--    Horarios por día: 09:00..13:00 y 16:00..19:00  (9 franjas)
--    Día aleatorio: todos libres / todos reservados / mixto 50%
-- ----------------------------------------------------------------

-- Helper: We'll insert turnos con ids explícitos empezando en 1
SET @turno_id = 1;

DELIMITER $$
DROP PROCEDURE IF EXISTS generate_turnos$$
CREATE PROCEDURE generate_turnos()
BEGIN
  DECLARE d DATE;
  DECLARE m INT;
  DECLARE h_idx INT;
  DECLARE hour_time TIME;
  DECLARE day_mode INT; -- 0 = all free, 1 = all reserved, 2 = mixed
  SET d = '2025-12-01';
  WHILE d <= '2025-12-14' DO
    -- omitir sábados y domingos
    IF DAYOFWEEK(d) NOT IN (1,7) THEN
      -- decidir modo del día aleatoriamente: 0 (libre), 1 (todo reservado), 2 (mixto)
      SET day_mode = FLOOR(RAND()*3);
      SET m = 1;
      WHILE m <= 8 DO
        SET h_idx = 1;
        WHILE h_idx <= 9 DO
          CASE h_idx
            WHEN 1 THEN SET hour_time = '09:00:00';
            WHEN 2 THEN SET hour_time = '10:00:00';
            WHEN 3 THEN SET hour_time = '11:00:00';
            WHEN 4 THEN SET hour_time = '12:00:00';
            WHEN 5 THEN SET hour_time = '13:00:00';
            WHEN 6 THEN SET hour_time = '16:00:00';
            WHEN 7 THEN SET hour_time = '17:00:00';
            WHEN 8 THEN SET hour_time = '18:00:00';
            WHEN 9 THEN SET hour_time = '19:00:00';
          END CASE;
          IF day_mode = 1 THEN
            INSERT INTO Turno (id_turno, id_m, fecha_turno, hora_ini, hora_fin, estado_turno)
              VALUES (@turno_id, m, d, hour_time, ADDTIME(hour_time, '01:00:00'), 'Reservado');
          ELSEIF day_mode = 0 THEN
            INSERT INTO Turno (id_turno, id_m, fecha_turno, hora_ini, hora_fin, estado_turno)
              VALUES (@turno_id, m, d, hour_time, ADDTIME(hour_time, '01:00:00'), 'Disponible');
          ELSE
            -- modo mixto: reservar con probabilidad 50%
            IF RAND() < 0.5 THEN
              INSERT INTO Turno (id_turno, id_m, fecha_turno, hora_ini, hora_fin, estado_turno)
                VALUES (@turno_id, m, d, hour_time, ADDTIME(hour_time, '01:00:00'), 'Reservado');
            ELSE
              INSERT INTO Turno (id_turno, id_m, fecha_turno, hora_ini, hora_fin, estado_turno)
                VALUES (@turno_id, m, d, hour_time, ADDTIME(hour_time, '01:00:00'), 'Disponible');
            END IF;
          END IF;
          SET @turno_id = @turno_id + 1;
          SET h_idx = h_idx + 1;
        END WHILE;
        SET m = m + 1;
      END WHILE;
    END IF;
    SET d = DATE_ADD(d, INTERVAL 1 DAY);
  END WHILE;
END$$
CALL generate_turnos()$$
DROP PROCEDURE IF EXISTS generate_turnos$$
DELIMITER ;

-- Al final de este procedimiento se habrán insertado muchos turnos (todos Reservado)

-- ----------------------------------------------------------------
-- 7) Reservas: crear una reserva por cada turno asignada ciclicamente a usuarios
-- ----------------------------------------------------------------

SET @res_id = 1;
SET @t_min = (SELECT MIN(id_turno) FROM Turno);
SET @t_max = (SELECT MAX(id_turno) FROM Turno);

DELIMITER $$
DROP PROCEDURE IF EXISTS generate_reservas$$
CREATE PROCEDURE generate_reservas()
BEGIN
  DECLARE t INT;
  -- Empezar por el primer turno reservado
  SELECT MIN(id_turno) INTO t FROM Turno WHERE estado_turno = 'Reservado';
  WHILE t IS NOT NULL DO
    INSERT INTO Reserva (id_reserva, id_u, id_turno, fecha_reserva, estado_reserva)
      VALUES (@res_id, (2 + (t % 11)), t, '2025-11-30', 'Confirmada');
    SET @res_id = @res_id + 1;
    -- siguiente turno reservado
    SELECT MIN(id_turno) INTO t FROM Turno WHERE id_turno > t AND estado_turno = 'Reservado';
  END WHILE;
END$$
CALL generate_reservas()$$
DROP PROCEDURE IF EXISTS generate_reservas$$
DELIMITER ;

-- ----------------------------------------------------------------
-- 8) Recibos: generar algunos recibos y asociarlos a turnos aleatoriamente (en bloque)
-- ----------------------------------------------------------------
INSERT INTO Recibo (id_recibo, id_u, fecha_emision, importe_total, metodo_pago, concepto, id_c, id_m, estado_recibo) VALUES
(1, 2, '2025-11-30', 25.00, 'Tarjeta', 'Pago sesión impresión 3D', NULL, 1, 'Pagado'),
(2, 3, '2025-11-30', 50.00, 'Online', 'Pago corte láser', NULL, 2, 'Pagado'),
(3, 4, '2025-11-30', 75.00, 'Efectivo', 'Pago fresado', NULL, 3, 'Pendiente');

-- Asociar algunos turnos a recibos (ejemplo: primeros 20 turnos a recibo 1, siguiente 20 a recibo 2, etc.)
INSERT INTO Recibo_Turno (id_recibo, id_turno)
  SELECT 1, id_turno FROM Turno WHERE id_turno BETWEEN 1 AND 20;
INSERT INTO Recibo_Turno (id_recibo, id_turno)
  SELECT 2, id_turno FROM Turno WHERE id_turno BETWEEN 21 AND 40;
INSERT INTO Recibo_Turno (id_recibo, id_turno)
  SELECT 3, id_turno FROM Turno WHERE id_turno BETWEEN 41 AND 60;

-- ----------------------------------------------------------------
-- 9) Ajustes finales: asegurarse que las secuencias AUTO_INCREMENT no colisionen
-- ----------------------------------------------------------------
-- Ajustar auto_increment para cada tabla al siguiente valor viable
ALTER TABLE Usuario AUTO_INCREMENT = 21;
ALTER TABLE Curso AUTO_INCREMENT = 11;
ALTER TABLE Inscripcion AUTO_INCREMENT = 31;
ALTER TABLE Maquina AUTO_INCREMENT = 9;
-- Para Turno y Reserva usar MAX(id)+1 con SQL dinámico (evita variables en DDL)
SET @next_turno_ai = (SELECT COALESCE(MAX(id_turno)+1, 1) FROM Turno);
SET @sql_turno_ai = CONCAT('ALTER TABLE Turno AUTO_INCREMENT = ', @next_turno_ai);
PREPARE stmt_turno_ai FROM @sql_turno_ai; EXECUTE stmt_turno_ai; DEALLOCATE PREPARE stmt_turno_ai;

SET @next_reserva_ai = (SELECT COALESCE(MAX(id_reserva)+1, 1) FROM Reserva);
SET @sql_reserva_ai = CONCAT('ALTER TABLE Reserva AUTO_INCREMENT = ', @next_reserva_ai);
PREPARE stmt_reserva_ai FROM @sql_reserva_ai; EXECUTE stmt_reserva_ai; DEALLOCATE PREPARE stmt_reserva_ai;
ALTER TABLE Recibo AUTO_INCREMENT = 4;

-- Fin del script
SELECT 'POPULATE_COMPLETED' as status;
