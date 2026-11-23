# Demo — API REST de gestion de commandes

Ce projet est une **API REST** développée en **Java / Spring Boot** qui permet de gérer des **clients**, des **produits**, des **commandes** et leurs **lignes de détail**.

L'objectif est de fournir un backend complet (CRUD) avec une architecture en couches, une validation des données, une base de données relationnelle MySQL et une suite de tests automatisés.

---

## 🛠️ Technologies utilisées

| Technologie | Version | Rôle |
|---|---|---|
| **Java** | 21 | Langage de programmation |
| **Spring Boot** | 4.2.0 | Framework principal (application, configuration auto) |
| **Spring Web (MVC)** | — | Création des endpoints REST (`@RestController`) |
| **Spring Data JPA** | — | Accès aux données (repositories) |
| **Hibernate** | — | ORM : mapping objet ↔ base de données |
| **Spring Validation** | — | Validation des entrées (`@NotBlank`, `@Email`, `@Positive`, …) |
| **MySQL Connector/J** | — | Pilote JDBC pour MySQL/MariaDB |
| **H2 Database** | — | Base en mémoire utilisée pour les tests |
| **Lombok** | — | Réduction du code boilerplate (`@Getter`, `@Builder`, …) |
| **Maven** | wrapper `mvnw` | Gestion des dépendances et du build |
| **JUnit 5** | — | Écriture des tests unitaires et d'intégration |
| **Mockito** | — | Simulation des dépendances dans les tests unitaires |
| **AssertJ** | — | Assertions lisibles dans les tests |

Toutes les dépendances sont déclarées dans le [`pom.xml`](pom.xml).

---

## 🧱 Architecture

Le code suit une **architecture en couches** (séparation des responsabilités) :

```
HTTP (JSON)
    ↓
┌─────────────────────────────────────────┐
│ Controller  → reçoit/valide les requêtes│  (demo.controller)
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Service     → logique métier + transactions│ (demo.services)
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Repository  → accès à la base (JPA)     │  (demo.repository)
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Entity      → modèle de données         │  (demo.entity)
└─────────────────────────────────────────┘
    ↓
Base de données (MySQL en prod / H2 en tests)
```

- **Controller** : expose les routes HTTP, valide le corps avec `@Valid`, renvoie les codes HTTP adaptés (`200`, `201`, `204`, `404`, `400`).
- **Service** : contient les règles métier, annotated `@Transactional` (lecture seule par défaut, écriture quand nécessaire).
- **Repository** : interfaces `JpaRepository` avec quelques méthodes de recherche personnalisées (`findByEmail`, `findByClientId`, `findByOrderId`).
- **Entity** : classes JPA mapées sur les tables, avec validation et relations.

---

## 📁 Structure du projet

```
src/
├── main/
│   ├── java/demo/
│   │   ├── DemoApplication.java        ← point d'entrée Spring Boot
│   │   ├── controller/
│   │   │   ├── ClientController.java
│   │   │   ├── ProductController.java
│   │   │   ├── OrderController.java
│   │   │   └── OrderDetailsController.java
│   │   ├── services/
│   │   │   ├── ClientService.java
│   │   │   ├── ProductService.java
│   │   │   ├── OrderService.java
│   │   │   └── OrderDetailsService.java
│   │   ├── repository/
│   │   │   ├── ClientRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── OrderDetailsRepository.java
│   │   └── entity/
│   │       ├── Client.java
│   │       ├── Product.java
│   │       ├── Order.java
│   │       └── OrderDetails.java
│   └── resources/application.properties ← config MySQL
└── test/
    ├── java/demo/
    │   ├── DemoApplicationTests.java        ← test de chargement du contexte
    │   ├── services/OrderDetailsServiceTest.java ← tests unitaires (Mockito)
    │   └── repository/ClientRepositoryTest.java  ← test d'intégration JPA
    └── resources/application.properties    ← config H2 pour les tests
```

---

## 🗄️ Modèle de données

```
Client (clients)
  │ 1
  │
  │ N
Order (orders) ──────── N ──────── Product (products)
  │ 1                              │ 1
  │                                │
  │ N                              │ N
OrderDetails (order_details) ──────┘
```

| Entité | Table | Champs principaux | Relations |
|---|---|---|---|
| **Client** | `clients` | `id`, `firstname`, `lastname`, `email` (unique) | `1..N` Order |
| **Product** | `products` | `id`, `name`, `price` (BigDecimal) | `1..N` OrderDetails |
| **Order** | `orders` | `id`, `date`, `quantity`, `totalPrice`, `client` | `N..1` Client, `1..N` OrderDetails |
| **OrderDetails** | `order_details` | `id`, `prix`, `quantite`, `order`, `product` | `N..1` Order, `N..1` Product |

Détails importants :
- Un `OrderDetails` est unique par couple `(order_id, product_id)` (contrainte `uk_order_product`).
- Les relations utilisent `FetchType.LAZY` et des `cascade`/`orphanRemoval` pour maintenir l'intégrité du graphe.
- `@JsonIgnore` et `@JsonProperty(access = WRITE_ONLY)` évitent les cycles de sérialisation JSON.

---

## 🚀 Règles métier

- **Client** : l'email doit être valide et unique (vérifié à la création et à la mise à jour).
- **Produit** : le prix doit être supérieur ou égal à `0.01`.
- **Commande** :
  - la date est fixée à `now()` si elle n'est pas fournie ;
  - le client doit exister ;
  - `quantity` et `totalPrice` sont recalculés automatiquement à partir des lignes de détail.
- **Ligne de détail** :
  - la quantité doit être positive, le prix supérieur à `0.01` ;
  - la commande et le produit doivent exister ;
  - à chaque ajout/modification/suppression, le total de la commande est recalculé (`prix × quantité`).
- Les erreurs métier renvoient des `ResponseStatusException` avec le code HTTP approprié (`400`, `404`).

---

## 🌐 Endpoints de l'API

Base URL : `http://localhost:8080`

### Clients — `/api/clients`

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/clients` | Liste tous les clients |
| `GET` | `/api/clients/{id}` | Récupère un client |
| `POST` | `/api/clients` | Crée un client (`201`) |
| `PUT` | `/api/clients/{id}` | Met à jour un client |
| `DELETE` | `/api/clients/{id}` | Supprime un client (`204`) |

### Produits — `/api/products`

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/products` | Liste tous les produits |
| `GET` | `/api/products/{id}` | Récupère un produit |
| `POST` | `/api/products` | Crée un produit (`201`) |
| `PUT` | `/api/products/{id}` | Met à jour un produit |
| `DELETE` | `/api/products/{id}` | Supprime un produit (`204`) |

### Commandes — `/api/orders`

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/orders` | Liste toutes les commandes |
| `GET` | `/api/orders/client/{clientId}` | Commandes d'un client |
| `GET` | `/api/orders/{id}` | Récupère une commande |
| `POST` | `/api/orders` | Crée une commande (`201`) |
| `PUT` | `/api/orders/{id}` | Met à jour une commande |
| `DELETE` | `/api/orders/{id}` | Supprime une commande (`204`) |

### Lignes de détail — `/api/order-details`

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/order-details` | Liste toutes les lignes |
| `GET` | `/api/order-details/order/{orderId}` | Lignes d'une commande |
| `GET` | `/api/order-details/{id}` | Récupère une ligne |
| `POST` | `/api/order-details` | Crée une ligne (`201`) |
| `PUT` | `/api/order-details/{id}` | Met à jour une ligne |
| `DELETE` | `/api/order-details/{id}` | Supprime une ligne (`204`) |

---

## ⚙️ Configuration

### Application ( [`src/main/resources/application.properties`](src/main/resources/application.properties) )
- Connexion JDBC vers `jdbc:mysql://localhost:3306/demo` (base créée automatiquement si absente).
- Utilisateur : `root`, mot de passe vide (à adapter selon votre environnement).
- `spring.jpa.hibernate.ddl-auto=update` : le schéma est mis à jour automatiquement.
- `spring.jpa.show-sql=true` : les requêtes SQL sont affichées dans la console.

### Tests ( [`src/test/resources/application.properties`](src/test/resources/application.properties) )
- Base **H2 en mémoire** (`jdbc:h2:mem:demo`) en mode compatibilité MySQL.
- `ddl-auto=create-drop` : schéma créé puis détruit après les tests.
- Aucun serveur MySQL n'est nécessaire pour lancer les tests.

---

## ▶️ Lancement du projet

### Prérequis
- **JDK 21** installé
- **MySQL/MariaDB** en fonctionnement (pour l'application, pas pour les tests)
- Pas besoin d'installer Maven : le wrapper `mvnw` / `mvnw.cmd` est fourni.

### Commandes

```bash
# Lancer l'application
./mvnw spring-boot:run          # Windows : mvnw.cmd spring-boot:run

# Construire le JAR
./mvnw clean package

# Exécuter les tests
./mvnw test
```

L'application démarre alors sur **http://localhost:8080**.

---

## 🧪 Tests

| Fichier | Type | Ce qui est vérifié |
|---|---|---|
| `DemoApplicationTests` | Intégration | Le contexte Spring se charge correctement |
| `ClientRepositoryTest` | Intégration (H2) | Persistance du graphe Client → Order → OrderDetails et recherche par email |
| `OrderDetailsServiceTest` | Unitaire (Mockito) | Recalcul de `quantity`/`totalPrice` et gestion des erreurs (commande inexistante) |

---

## 📌 Points clés à retenir

- API REST **CRUD complète** sur 4 ressources.
- **Architecture en couches** clairement séparée.
- **Validation** automatique des données entrantes.
- **Cohérence métier** : les totaux de commande sont toujours à jour.
- **Tests** unitaires et d'intégration avec base H2.
- Configuration **séparée** entre exécution (MySQL) et tests (H2).
