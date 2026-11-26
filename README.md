# Demo

API REST de gestion de clients, produits, commandes et lignes de commande (Spring Boot, Java 21, JPA).

## Lancement

```bash
./mvnw spring-boot:run
./mvnw test
```

L'application écoute sur `http://localhost:8080`.

## Configuration

`src/main/resources/application.properties` : MySQL `localhost:3306/demo` (utilisateur `root`, mot de passe vide).
Les tests utilisent H2 en mémoire (`src/test/resources/application.properties`).

## Endpoints

- `/api/clients`
- `/api/products`
- `/api/orders` et `/api/orders/client/{clientId}`
- `/api/order-details` et `/api/order-details/order/{orderId}`

CRUD complet : `GET`, `POST`, `PUT`, `DELETE /{id}`.
