#!/usr/bin/env bash

set -euo pipefail

DATA_BUCKET=gs://deeppow-data-bucket

ARGS=(
    wordTreePath=${DATA_BUCKET}/wordle/word-tree.avro
    wordEliminatedPath=${DATA_BUCKET}/wordle/outputs/average-eliminated
    maxNumWorkers=123
)
JOINED=$(IFS=,; printf '%s' "${ARGS[*]}")

gcloud dataflow jobs run CreateMostEliminatedPipeline \
    --gcs-location=gs://deeppow-dataflow-templates/templates/CreateMostEliminatedPipeline \
    --region=us-central1 \
    --parameters="${JOINED}"
