CREATE TABLE IF NOT EXISTS items (
  id       SERIAL PRIMARY KEY,
  name     VARCHAR(120) NOT NULL UNIQUE,
  price    NUMERIC(10,2) NOT NULL
);

ALTER TABLE items
  ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS orders (
  id          SERIAL PRIMARY KEY,
  item_id     INT NOT NULL,
  quantity    INT NOT NULL CHECK (quantity > 0),
  created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO items (id, name, price, quantity) VALUES
  (1, 'Laptop Gamer', 1299.99, 10),
  (2, 'Mouse Pro', 49.90, 50),
  (3, 'Teclado Mecánico', 89.50, 25)
ON CONFLICT (id) DO NOTHING;
