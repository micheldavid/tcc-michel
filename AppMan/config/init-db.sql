CREATE SEQUENCE APPMAN_JOB_ID;
CREATE TABLE APPMAN_JOB(JOB_ID INTEGER NOT NULL PRIMARY KEY,USERNAME VARCHAR NOT NULL,FILE VARCHAR NOT NULL,DTSTART TIMESTAMP,DTEND TIMESTAMP,SUCCESS VARCHAR,EXEHDA_APP_ID VARCHAR,DELETED VARCHAR NOT NULL);
