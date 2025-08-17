# push_to_dockerhub.sh start
#!/usr/bin/env bash
set -euo pipefail

IMAGE="docker.io/rahulkumartonk/travel-planner-api"
SHA_TAG=$(git rev-parse --short HEAD || echo "local")

# 1) Login (one-time per machine session)
# docker login -u "rahulkumartonk" docker.io

# 2) Build for Render-compatible arch and push
# Requires: Docker Buildx (Docker Desktop includes it)
docker buildx build \
  --platform linux/amd64 \
  -t "$IMAGE:latest" \
  -t "$IMAGE:$SHA_TAG" \
  -f Dockerfile \
  --push .
# push_to_dockerhub.sh end