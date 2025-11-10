# CafeOrderTracker

CafeOrderTracker is a lightweight desktop application demonstrating how to integrate **Java Swing, JDBC/MySQL,** and **multithreading** in a clean and modular structure.
The program manages cafe orders stored in a MySQL database, while background threads simulate kitchen activity and periodic UI updates.

## Project structure

```
LatihanThread/
├── lib/
├── src/
│   ├── Main.java
│   ├── DB.java
│   ├── Order.java
│   ├── OrderDao.java
│   ├── OrderStatus.java
│   ├── OrderPanel.java
│   ├── KitchenService.java
│   └── RefreshService.java
└── README.md
```

### Highlights of Each Component

- `Main` – starts the application safely on the Swing Event Dispatch Thread.
- `OrderPanel` – provides an interactive form and table for managing orders, with thread-safe UI updates.
- `OrderDao` – encapsulates SQL queries and low-level database operations.
- `KitchenService` – updates order statuses at regular intervals using a scheduled executor.
- `RefreshService` – frequently polls the database and refreshes the UI to reflect new data.
- `DB` – handles database connectivity and table initialization.
- `Order / OrderStatus` – represent the core domain model of the system.

## Requirements

- Java 17 or newer
- A running MySQL server
- MySQL Connector/J JAR file in: `lib/mysql-connector-j-9.5.0.jar`

### Database Setup

Create a schema and table before running the application

```sql
CREATE DATABASE cafe CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE cafe;

CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(100) NOT NULL,
  item VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at_epoch BIGINT NOT NULL
);
```

Database credentials can be modified in `DB.java`.

## How to Run

1. Compile the project:
   Linux/macOS:

```bash
javac -cp lib/mysql-connector-j-9.5.0.jar -d bin src/*.java`
```

Windows:

```bash
javac -cp "lib\mysql-connector-j-9.5.0.jar" -d bin src\*.java`
```

2. Run the application
   Linux/macOS
   ```bash
   java -cp bin:lib/mysql-connector-j-9.5.0.jar Main
   ```
   Linux/macOS
   ```bash
   java -cp "bin;lib/mysql-connector-j-9.5.0.jar" Main
   ```

## System Workflow

1. **Main** invokes the UI creation process on the EDT to ensure Swing safety.
2. **OrderPanel** collects user inputs and delegates database writes to a background thread pool.
3. **KitchenService** runs periodically, simulating the transition from NEW → IN_PROGRESS → DONE.
4. **RefreshService** fetches updated data from MySQL every few seconds and redraws the table.
5. Swing component updates always occur using invokeLater to avoid concurrency issues.
6. Database logic is centralized inside OrderDao for clarity and maintainability.
