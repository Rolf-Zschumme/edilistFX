--<ScriptOptions statementTerminator=";"/>

ALTER TABLE EDI_KOMPONENTE DROP INDEX FK_EDI_KOMPONENTE_EDI_SYSTEM_ID;

ALTER TABLE EDI_PARTNER DROP INDEX PARTNER;

ALTER TABLE SZENARIO DROP INDEX FK_SZENARIO_ANBINDUNG_ID;

ALTER TABLE SYSTEM DROP INDEX FK_SYSTEM_PARTNER_ID;

ALTER TABLE EMPFAENGER DROP INDEX FK_EMPFAENGER_EDIEINTRAG_ID;

ALTER TABLE EDIEINTRAG DROP INDEX FK_EDIEINTRAG_SZENARIO_ID;

ALTER TABLE EDI_SZENARIO DROP INDEX SZENARIO;

ALTER TABLE EDI_SZENARIO DROP INDEX FK_EDI_SZENARIO_EDI_ANBINDUNG_ID;

ALTER TABLE EDIEINTRAG_DOKUMENT DROP INDEX FK_EDIEINTRAG_DOKUMENT_ediEintrag_ID;

ALTER TABLE EDI_KONTEXT DROP INDEX KONTEXT;

ALTER TABLE EDI_EMPFAENGER DROP INDEX FK_EDI_EMPFAENGER_EDI_EINTRAG_ID;

ALTER TABLE EDI_EMPFAENGER DROP INDEX FK_EDI_EMPFAENGER_EDI_KOMPONENTE_ID;

ALTER TABLE EDIEINTRAGDOKUMENT DROP INDEX FK_EDIEINTRAGDOKUMENT_EDIEINTRAG_ID;

ALTER TABLE EDI_EINTRAG DROP INDEX FK_EDI_EINTRAG_EDI_KOMPONENTE_ID;

ALTER TABLE EDI_SYSTEM DROP INDEX FK_EDI_SYSTEM_EDI_PARTNER_ID;

ALTER TABLE EDIEINTRAGDOKUMENT DROP INDEX FK_EDIEINTRAGDOKUMENT_DOKUMENT_ID;

ALTER TABLE KOMPONENTE DROP INDEX FK_KOMPONENTE_SYSTEM_ID;

ALTER TABLE EDI_EINTRAG DROP INDEX FK_EDI_EINTRAG_EDI_SZENARIO_ID;

DROP TABLE EDIEINTRAG;

DROP TABLE SZENARIO;

DROP TABLE EDI_EMPFAENGER;

DROP TABLE KOMPONENTE;

DROP TABLE EDI_PARTNER;

DROP TABLE EDI_KONTEXT;

DROP TABLE PARTNER;

DROP TABLE EDIEINTRAG_DOKUMENT;

DROP TABLE EDI_SZENARIO;

DROP TABLE EDI_EINTRAG;

DROP TABLE DOKUMENT;

DROP TABLE EDI_SYSTEM;

DROP TABLE ANBINDUNG;

DROP TABLE EMPFAENGER;

DROP TABLE EDI_KOMPONENTE;

DROP TABLE SYSTEM;

DROP TABLE EDIEINTRAGDOKUMENT;

