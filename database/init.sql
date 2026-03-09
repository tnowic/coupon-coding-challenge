DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'couponuser') THEN
    CREATE ROLE couponuser LOGIN PASSWORD 'password';
    END IF;
END $$;

CREATE SCHEMA IF NOT EXISTS coupons AUTHORIZATION couponuser;

CREATE TABLE IF NOT EXISTS coupons.coupons
(
    "id" bigserial PRIMARY KEY,
    "code" varchar(20) UNIQUE NOT NULL ,
    "create_timestamp" timestamp with time zone NOT NULL,
    "max_counter" integer NOT NULL,
    "counter" integer NOT NULL DEFAULT 0,
    "country_code" char(2),
    "is_for_reg_users" boolean,
    "version" bigint
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS coupons.coupons OWNER to couponuser;

CREATE TABLE IF NOT EXISTS coupons.customers
(
    "id" bigserial PRIMARY KEY,
    "name" varchar(100) NOT NULL ,
    "surname" varchar(100) NOT NULL ,
    "email" varchar(100) NOT NULL ,
    "create_timestamp" timestamp with time zone NOT NULL
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS coupons.customers OWNER to couponuser;

CREATE TABLE IF NOT EXISTS coupons.coupon_usages
(
    "id" bigserial PRIMARY KEY,
    "create_timestamp" timestamp with time zone NOT NULL,
    "coupon_id" bigint REFERENCES coupons.coupons(id),
    "customer_id" bigint REFERENCES coupons.customers(id)
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS coupons.coupon_usages OWNER to couponuser;

insert into coupons.customers (name, surname, email, create_timestamp) values ('Janusz', 'Biznesu', 'prezes@januszex.pl', CURRENT_TIMESTAMP);
insert into coupons.customers (name, surname, email, create_timestamp) values ('Abdullah', 'Lachamudynda', 'ablmd@farfaraway.pk', CURRENT_TIMESTAMP);
