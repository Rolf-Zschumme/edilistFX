--<ScriptOptions statementTerminator=";"/>

ALTER TABLE `EDIDOKULINK_EDIANBINDUNG` DROP PRIMARY KEY;

ALTER TABLE `EDIDOKULINK_EDIANBINDUNG` DROP INDEX `FK_EDIDOKULINK_EDIANBINDUNG_ediSzenario_id`;

DROP TABLE `EDIDOKULINK_EDIANBINDUNG`;

