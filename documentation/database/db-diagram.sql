CREATE TABLE IF NOT EXISTS "orders" (
  "id" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "description" varchar(255),
  "total_amount" float NOT NULL,
  "last_update" timestamp NOT NULL DEFAULT (now())
);

CREATE TABLE "products" (
  "id" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "name" varchar(255) UNIQUE NOT NULL,
  "description" varchar(255),
  "quantity" integer NOT NULL DEFAULT 0,
  "price" float NOT NULL,
  "last_update" timestamp NOT NULL DEFAULT (now())
);

CREATE TABLE IF NOT EXISTS "order_products" (
  "order_id" integer,
  "product_id" integer,
  PRIMARY KEY ("order_id", "product_id")
);

ALTER TABLE IF EXISTS "order_products" ADD FOREIGN KEY ("order_id") REFERENCES "orders" ("id");
ALTER TABLE IF EXISTS "order_products" ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id");