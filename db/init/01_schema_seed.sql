CREATE TABLE IF NOT EXISTS items (
  id          SERIAL PRIMARY KEY,
  name        VARCHAR(120) NOT NULL,
  price       NUMERIC(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
  id          SERIAL PRIMARY KEY,
  item_id     INT NOT NULL,
  quantity    INT NOT NULL CHECK (quantity > 0),
  created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO items (id, name, price) VALUES
  (1, 'Laptop Gamer', 1299.99),
  (2, 'Mouse Pro', 49.90),
  (3, 'Teclado Mecánico', 89.50)
ON CONFLICT (id) DO NOTHING;
