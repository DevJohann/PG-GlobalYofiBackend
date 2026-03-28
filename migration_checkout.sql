-- SQL Migration Script for GlobalYofi
-- Execute these commands in your MySQL console/workbench to fix the "Unknown column" errors.

USE global_yofi;

-- 1. Add missing columns to 'pedido' table
ALTER TABLE pedido ADD COLUMN tipo_documento VARCHAR(50);
ALTER TABLE pedido ADD COLUMN numero_documento VARCHAR(50);
ALTER TABLE pedido ADD COLUMN observaciones TEXT;
ALTER TABLE pedido ADD COLUMN metodo_pago VARCHAR(50);

-- 2. Add missing columns to 'detalle_pedido' table
ALTER TABLE detalle_pedido ADD COLUMN descuento DECIMAL(10,2) DEFAULT 0.00;
