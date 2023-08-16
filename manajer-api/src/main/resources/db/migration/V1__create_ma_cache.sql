CREATE TABLE manajer.ma_cache (
    id INTEGER NOT NULL AUTO_INCREMENT,
	"key" CHARACTER VARYING NOT NULL,
	content CHARACTER VARYING,
	created TIMESTAMP,
    CONSTRAINT ma_cache_pk PRIMARY KEY (id),
	CONSTRAINT ma_cache_un_key UNIQUE ("key")
);
