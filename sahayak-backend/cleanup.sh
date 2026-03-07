#!/bin/bash
set -e

echo "🧹 Cleaning up AWS resources..."
echo ""
read -p "Delete everything and stop all charges? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Cancelled."
    exit 0
fi

# Delete CloudFormation stack
echo "Deleting stack..."
sam delete --no-prompts 2>/dev/null || \
    aws cloudformation delete-stack --stack-name sahayak-backend

echo ""
echo "✅ Cleanup complete! You will not be charged."
echo ""
echo "To redeploy: ./deploy.sh"
