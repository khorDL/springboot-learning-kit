-- Lock Database
UPDATE public.databasechangeloglock SET LOCKED = TRUE, LOCKEDBY = 'DESKTOP-A3IH1BA (172.21.224.1)', LOCKGRANTED = NOW() WHERE ID = 1 AND LOCKED = FALSE;

-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: src/main/resources/db/changelog/db.changelog-master.xml
-- Ran at: 10/07/2025, 1:36 pm
-- Against: user@jdbc:postgresql://localhost:5432/Order_Service
-- Liquibase version: 4.29.2
-- *********************************************************************

-- Changeset src/main/resources/db/changelog/shipment_table.xml::shipment_1::C3reless
CREATE TABLE public.shipments (shipment_number BIGINT NOT NULL, order_uuid BIGINT NOT NULL, warehouse_id INTEGER NOT NULL, shipping_address_id BIGINT NOT NULL, total_amount DECIMAL(19, 4) NOT NULL, shipping_cost DECIMAL(19, 4) NOT NULL, currency VARCHAR(3) NOT NULL, shipment_created TIMESTAMP WITHOUT TIME ZONE NOT NULL, CONSTRAINT shipments_pkey PRIMARY KEY (shipment_number));

ALTER TABLE public.shipments ADD CONSTRAINT fk_shipment_order_uuid FOREIGN KEY (order_uuid) REFERENCES public.orders (uuid);

ALTER TABLE public.shipments ADD CONSTRAINT fk_shipment_customer_address FOREIGN KEY (shipping_address_id) REFERENCES public.customer_address (id);

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('shipment_1', 'C3reless', 'src/main/resources/db/changelog/shipment_table.xml', NOW(), 8, '9:249915307cf6a0e52ccff190baae67ea', 'createTable tableName=shipments; addForeignKeyConstraint baseTableName=shipments, constraintName=fk_shipment_order_uuid, referencedTableName=orders; addForeignKeyConstraint baseTableName=shipments, constraintName=fk_shipment_customer_address, refe...', '', 'EXECUTED', NULL, NULL, '4.29.2', '2125784458');

-- Changeset src/main/resources/db/changelog/shipment_table.xml::2::C3reless
ALTER TABLE public.shipments ADD tracking_number VARCHAR(50) NOT NULL;

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2', 'C3reless', 'src/main/resources/db/changelog/shipment_table.xml', NOW(), 9, '9:78e41d77d1b9974dea6b17ed26e0ceda', 'addColumn tableName=shipments', '', 'EXECUTED', NULL, NULL, '4.29.2', '2125784458');

-- Release Database Lock
UPDATE public.databasechangeloglock SET LOCKED = FALSE, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1;

