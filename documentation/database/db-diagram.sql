CREATE TABLE "products" (
  "id" integer PRIMARY KEY,
  "name" varchar(255) UNIQUE NOT NULL,
  "description" varchar(255),
  "quantity" integer NOT NULL DEFAULT 0,
  "price" float NOT NULL,
  "last_update" timestamp NOT NULL DEFAULT (now())
);

CREATE TABLE "order_items" (
  "product_id" integer UNIQUE,
  "order_id" integer UNIQUE,
  PRIMARY KEY ("product_id", "order_id")
);

CREATE TABLE "orders" (
  "id" integer PRIMARY KEY,
  "description" varchar(255),
  "total_amount" float NOT NULL,
  "last_update" timestamp NOT NULL DEFAULT (now())
);

ALTER TABLE "products" ADD FOREIGN KEY ("id") REFERENCES "order_items" ("product_id");

ALTER TABLE "orders" ADD FOREIGN KEY ("id") REFERENCES "order_items" ("order_id");
