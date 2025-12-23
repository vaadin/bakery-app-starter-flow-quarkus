# Bakery App Starter for Vaadin Flow and Quarkus

A full-stack web application demonstrating enterprise patterns for order management, built with **Vaadin Flow 25** and **Quarkus 3.30**.

## Features

- **Storefront View** - Browse and manage customer orders with card-based UI
- **Dashboard** - Analytics and charts powered by Vaadin Charts
- **Products Management** - CRUD operations with master/detail pattern
- **Users Management** - User administration with role-based access
- **Order Editor** - Complex forms with line items and order history

## Technology Stack

- **Java 21**
- **Vaadin Flow 25** - Full-stack web framework
- **Quarkus 3.30** - Supersonic Subatomic Java Framework
- **Hibernate ORM with Panache** - Simplified persistence layer
- **H2 Database** - Embedded database for development

## Running the Application

### Development Mode

Run with hot reload enabled:

```shell
./mvnw quarkus:dev
```

The application will be available at http://localhost:8080

**Demo accounts:**
- Admin: `admin@vaadin.com` / `admin`
- Barista: `barista@vaadin.com` / `barista`

> Quarkus Dev UI is available at http://localhost:8080/q/dev/

### Running Tests

```shell
# Unit tests
./mvnw test

# Integration tests (TestBench)
./mvnw verify -Pit -Dcom.vaadin.testbench.Parameters.headless=true
```

## Packaging

### Standard JAR

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Uber JAR

```shell
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### Native Executable

```shell
# With GraalVM installed
./mvnw package -Dnative

# Without GraalVM (uses container)
./mvnw package -Dnative -Dquarkus.native.container-build=true

./target/vq-temp-1.0.0-SNAPSHOT-runner
```

## Project Structure

```
src/main/java/com/vaadin/starter/bakery/
├── app/
│   ├── security/          # Authentication and authorization
│   └── DataGenerator.java # Demo data initialization
├── backend/
│   ├── data/entity/       # JPA entities (Panache)
│   ├── repositories/      # Panache repositories
│   └── service/           # Business logic
└── ui/
    ├── views/             # Vaadin Flow views
    ├── crud/              # Reusable CRUD components
    └── components/        # Shared UI components
```

## Configuration

- `src/main/resources/application.properties` - Quarkus and database settings
- `src/main/resources/ValidationMessages.properties` - Custom validation messages
- `src/main/frontend/themes/bakery/` - Application theme

## Documentation

- [Vaadin Flow Documentation](https://vaadin.com/docs/latest/flow)
- [Vaadin Quarkus Integration](https://vaadin.com/docs/latest/flow/integrations/quarkus)
- [Quarkus Guides](https://quarkus.io/guides/)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
