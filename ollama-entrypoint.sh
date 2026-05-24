#!/bin/sh
set -e

echo "Starting Ollama service..."
ollama serve &
OLLAMA_PID=$!

echo "Waiting for Ollama to start..."
sleep 5

echo "Pulling gemma4:e4b model..."
ollama pull gemma4:e4b

echo "Model pulled successfully!"

# Keep the container running
wait $OLLAMA_PID

