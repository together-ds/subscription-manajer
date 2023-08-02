CREATE TABLE if not exists demo (
	id BIGINT NOT NULL AUTO_INCREMENT,
	create_time TIMESTAMP DEFAULT now() NOT NULL
);
CREATE UNIQUE INDEX if not exists "PRIMARY_KEY_demo__id" ON demo (id);


CREATE TABLE if not exists access_log (
	id BIGINT NOT NULL AUTO_INCREMENT,
	"start" TIMESTAMP NOT NULL,
	"end" TIMESTAMP NOT NULL,
	ip CHARACTER VARYING(255) NOT NULL,
	status_code INTEGER NOT NULL,
	"path" CHARACTER LARGE OBJECT NOT NULL
);
CREATE UNIQUE INDEX if not exists "PRIMARY_KEY_access_log__id" ON access_log (id);
