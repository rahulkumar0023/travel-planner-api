# verify_image.sh start
#!/usr/bin/env bash
set -euo pipefail
IMAGE="docker.io/rahulkumartonk/travel-planner-api:latest"
echo "Checking manifest for $IMAGE ..."
docker manifest inspect "$IMAGE" >/dev/null && echo "OK: tag exists" || {
  echo "ERROR: tag not found. Push the image first (CI or scripts/push_to_dockerhub.sh).";
  exit 1;
}
# verify_image.sh end