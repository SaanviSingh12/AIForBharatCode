#!/bin/bash
set -e

echo "🚀 Deploying Sahayak to AWS Lambda..."
echo ""

# Check prerequisites
command -v aws >/dev/null 2>&1 || { echo "Install: brew install awscli"; exit 1; }
command -v sam >/dev/null 2>&1 || { echo "Install: brew install aws-sam-cli"; exit 1; }
docker info >/dev/null 2>&1 || { echo "Start Docker Desktop"; exit 1; }

# Build
echo "📦 Building..."
sam build --use-container

# Deploy
echo "🚀 Deploying..."
if [ -f samconfig.toml ]; then
    sam deploy
else
    sam deploy --guided
fi

# Get URL
URL=$(aws cloudformation describe-stacks \
    --stack-name sahayak-backend 2>/dev/null \
    --query "Stacks[0].Outputs[?OutputKey=='ApiUrl'].OutputValue" \
    --output text 2>/dev/null || echo "")

if [ ! -z "$URL" ]; then
    echo ""
    echo "✅ Deployed!"
    echo "API URL: $URL"
    echo ""
    echo "Test: curl ${URL}api/health"
fi
