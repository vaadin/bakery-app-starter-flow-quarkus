# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bakery App Starter is a Vaadin Flow 25 + Quarkus 3.30 application demonstrating enterprise patterns for order management. Uses Java 21 with Hibernate ORM Panache for persistence and H2 as the embedded database.

## Build Commands

```bash
# Development mode (hot reload at http://localhost:8080)
./mvnw quarkus:dev

# Run unit tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ProductTest

# Run integration tests (TestBench)
./mvnw verify -Pit -Dcom.vaadin.testbench.Parameters.headless=true

# Package application
./mvnw package

# Build native executable (requires GraalVM)
./mvnw package -Dnative
```

## Architecture

### Package Structure
- `backend/data/entity/` - JPA entities extending `AbstractEntity` (which extends Quarkus `PanacheEntity`)
- `backend/repositories/` - Quarkus Panache repositories implementing `PanacheRepository<T>` with `@ApplicationScoped`
- `backend/service/` - Business logic implementing `CrudService<T>` or `FilterableCrudService<T>`
- `app/security/` - Quarkus HTTP Security with form-based authentication
- `ui/views/` - Vaadin Flow views and components
- `ui/crud/` - Reusable CRUD patterns (`CrudEntityPresenter`, `CrudEntityDataProvider`)

### Quarkus Patterns (Use These)

**Repositories** (already migrated - see `UserRepository` as reference):
```java
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByEmailIgnoreCase(String email) {
        return find("where upper(email) = upper(?1)", email).singleResult();
    }

    public List<User> findBy(Page pageable) {
        return findAll().page(pageable).list();
    }
}
```

**CDI annotations for services**:
- `@ApplicationScoped` for service beans
- `@Inject` for dependency injection
- `@Transactional` from `jakarta.transaction` for transaction management
- `io.quarkus.panache.common.Page` for pagination

### Views
- `LoginView` (Route: `/`) - Authentication entry point
- `StorefrontView` - Customer order browsing with cards
- `DashboardView` - Analytics with Vaadin Charts
- `ProductsView`, `UsersView` - Admin CRUD views using master/detail pattern
- `OrderEditor` - Complex form with items and history

## Testing

Unit tests are in `src/test/java/` using JUnit 5 with `quarkus-junit5`. Integration tests (`*IT.java`) use Vaadin TestBench with `@QuarkusIntegrationTest` and custom page objects in `testbench/elements/`.

Run integration tests with: `./mvnw verify -Pit -Dcom.vaadin.testbench.Parameters.headless=true`

## Configuration

- `src/main/resources/application.properties` - Quarkus/database configuration
- `src/main/resources/ValidationMessages.properties` - Custom validation messages (prefixed with `bakery.`)
- Frontend theme: `src/main/frontend/themes/bakery/`
