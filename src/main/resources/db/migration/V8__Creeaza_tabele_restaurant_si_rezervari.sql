-- V8__Creeaza_tabele_restaurant_si_rezervari.sql

CREATE TABLE restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    status VARCHAR(255) NOT NULL
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    reservation_time TIMESTAMP NOT NULL,
    number_of_guests INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL,
    restaurant_table_id BIGINT NOT NULL,

    CONSTRAINT fk_reservations_on_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_reservations_on_restaurant_table
        FOREIGN KEY (restaurant_table_id) REFERENCES restaurant_tables(id)
);