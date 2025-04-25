# Food Ordering System - JavaFX Desktop Application

A cross-platform JavaFX desktop application that enables internal staff to manage every entity in a MySQL "Food Ordering System" database.

## Features

- **Customers Management**: Add, edit, delete, and search for customers
- **Restaurants Management**: View and manage restaurants and their menus
- **Menu & Items Management**: Create and edit menus and menu items
- **Orders Management**: Track orders and their status, assign riders
- **Promotions Management**: Create and manage promotional offers
- **Riders & Vehicles Management**: Assign vehicles to riders for deliveries

## Technical Stack

- **Java 17+** with **JavaFX 21** for the UI
- **FXML** with SceneBuilder compatibility
- **MySQL 8.0** database backend
- **HikariCP** for connection pooling
- **Maven** build system

## Database Setup

1. Install MySQL 8.0 or higher
2. Create a new database:
   ```sql
   CREATE DATABASE food;
   ```
3. Create a user and grant permissions:
   ```sql
   CREATE USER 'food_admin'@'localhost' IDENTIFIED BY 'secret';
   GRANT ALL PRIVILEGES ON food.* TO 'food_admin'@'localhost';
   FLUSH PRIVILEGES;
   ```
4. Import the database schema (SQL file is provided in the `database` directory)

## Application Configuration

Database connection settings can be configured in `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/food?useSSL=false&serverTimezone=Africa/Cairo
db.username=food_admin
db.password=secret
db.poolSize=10
```

## Building and Running

### Prerequisites
- Java JDK 17 or newer
- Maven 3.8 or newer

### Build and Run with Maven

Build and run the application using Maven:

```bash
mvn clean javafx:run
```

### Create an executable JAR

Build an executable JAR file:

```bash
mvn package
```

The packaged JAR will be created in the `target` directory.

### Run the JAR file

```bash
java -jar target/food-ordering-system-1.0-SNAPSHOT.jar
```

## Code Architecture

The application follows a layered architecture:

1. **View Layer**: FXML files and CSS for UI, located in resources
2. **Controller Layer**: JavaFX controllers in `com.foodapp.controller`
3. **ViewModel Layer**: JavaFX Properties connecting the view and business logic in `com.foodapp.viewmodel`
4. **Service Layer**: Business logic and transactions in `com.foodapp.service`
5. **DAO Layer**: Database access in `com.foodapp.dao`
6. **Model Layer**: POJOs representing database entities in `com.foodapp.model`

## License

Copyright © 2023 Food Ordering System. All rights reserved. 