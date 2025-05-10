# Sample-java-app

## Setup

**Start MS SQL in docker**
- Run: `docker-compose -f file.yaml up -d`
```
version: '3.8'
services:
  sqldata:
      image: mcr.microsoft.com/mssql/server:2019-latest
      environment:
        - SA_PASSWORD=Pass@word
        - SA_USER=sa
        - ACCEPT_EULA=Y
      ports:
        - "1433:1433"
```
**Run java app**
- Note: Update log file path in `logback.xml` if you want logs there
```
./gradlew run
```
