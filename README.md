# Travel Planner API

A Spring Boot REST API that manages trips and expenses.

## Build

```bash
mvn clean package
```

## Run

```bash 
## local

export DEV_AUTH_ENABLED=true
export JWT_SECRET=$(openssl rand -hex 64)   # strong dev secret
 
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/travel
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

mvn clean package
java -jar target/travel-planner-api-0.0.1-SNAPSHOT.jar
```

## Health Check

`GET /health` returns `OK` when the service is running.



## Postgres setup local 

# add Bitnami charts (once)

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# install Postgres (namespace: db)
helm uninstall pg --namespace db
helm install pg bitnami/postgresql \
--namespace db --create-namespace \
--set auth.postgresPassword=postgres \
--set auth.database=travel

# in a separate terminal
kubectl port-forward svc/pg-postgresql -n db 5432:5432


