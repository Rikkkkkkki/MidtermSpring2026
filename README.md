# UNO CLI Game

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+

### Build & Test

```bash
mvn clean compile
mvn test
mvn clean package
```

### Run

```bash
java -jar target/uno-cli.jar --bots 3 --games 1 --quiet
```

### Docker

```bash
docker build -t uno-cli:latest .
docker run uno-cli:latest --bots 2
```