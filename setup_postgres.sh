
helm uninstall pg --namespace db
helm install pg bitnami/postgresql \
--namespace db --create-namespace \
--set auth.postgresPassword=postgres \
--set auth.database=travel  --wait --timeout 30s

# in a separate terminal
kubectl port-forward svc/pg-postgresql -n db 5432:5432