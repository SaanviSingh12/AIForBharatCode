#!/bin/bash
# ==============================================================================
# Sahayak Backend - AWS Lambda Deployment Script
# Deploys Spring Boot application to AWS Lambda with API Gateway
# ==============================================================================

set -euo pipefail

# ----------------------------- Configuration ----------------------------------
FUNCTION_NAME="${FUNCTION_NAME:-sahayak-backend}"
REGION="${AWS_REGION:-ap-south-1}"
STAGE="${STAGE:-prod}"
ROLE_NAME="${FUNCTION_NAME}-lambda-role"
API_NAME="${FUNCTION_NAME}-api"
JAR_FILE="target/sahayak-backend-1.0.0.jar"
HANDLER="com.sahayak.StreamLambdaHandler"
RUNTIME="java17"
MEMORY=2048
TIMEOUT=120
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text --region "$REGION" 2>/dev/null || echo "")

# ----------------------------- Colors -----------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()   { echo -e "${GREEN}[✓]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }

# ----------------------------- Pre-checks -------------------------------------
echo "=============================================="
echo "  Sahayak Backend → AWS Lambda Deployment"
echo "=============================================="

if [[ -z "$ACCOUNT_ID" ]]; then
    error "AWS credentials not configured. Run: aws configure"
fi
log "AWS Account: $ACCOUNT_ID  |  Region: $REGION"

if [[ ! -f "$JAR_FILE" ]]; then
    warn "JAR not found. Building..."
    mvn clean package -DskipTests -q
fi
log "Artifact: $JAR_FILE ($(du -h "$JAR_FILE" | cut -f1))"

# ----------------------------- Step 1: IAM Role -------------------------------
echo ""
echo "--- Step 1: IAM Role ---"

TRUST_POLICY='{
    "Version": "2012-10-17",
    "Statement": [{
        "Effect": "Allow",
        "Principal": { "Service": "lambda.amazonaws.com" },
        "Action": "sts:AssumeRole"
    }]
}'

ROLE_ARN=$(aws iam get-role --role-name "$ROLE_NAME" --query 'Role.Arn' --output text 2>/dev/null || echo "")

if [[ -z "$ROLE_ARN" || "$ROLE_ARN" == "None" ]]; then
    log "Creating IAM role: $ROLE_NAME"
    ROLE_ARN=$(aws iam create-role \
        --role-name "$ROLE_NAME" \
        --assume-role-policy-document "$TRUST_POLICY" \
        --query 'Role.Arn' --output text)

    # Attach policies
    aws iam attach-role-policy --role-name "$ROLE_NAME" \
        --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

    # Custom policy for Sahayak services
    CUSTOM_POLICY='{
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "bedrock:InvokeModel",
                    "bedrock:InvokeModelWithResponseStream"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "transcribe:StartTranscriptionJob",
                    "transcribe:GetTranscriptionJob",
                    "transcribe:StartStreamTranscription"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "textract:DetectDocumentText",
                    "textract:AnalyzeDocument"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": ["polly:SynthesizeSpeech"],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
                "Resource": "arn:aws:s3:::sahayak-*/*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "dynamodb:GetItem", "dynamodb:PutItem",
                    "dynamodb:Query", "dynamodb:Scan",
                    "dynamodb:UpdateItem", "dynamodb:BatchWriteItem"
                ],
                "Resource": "arn:aws:dynamodb:'"$REGION"':*:table/sahayak-*"
            }
        ]
    }'

    aws iam put-role-policy \
        --role-name "$ROLE_NAME" \
        --policy-name "${FUNCTION_NAME}-services-policy" \
        --policy-document "$CUSTOM_POLICY"

    log "Waiting for IAM role propagation (10s)..."
    sleep 10
else
    log "IAM role exists: $ROLE_ARN"
fi

# ----------------------------- Step 2: Lambda Function ------------------------
echo ""
echo "--- Step 2: Lambda Function ---"

FUNCTION_EXISTS=$(aws lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" 2>/dev/null && echo "yes" || echo "no")

if [[ "$FUNCTION_EXISTS" == "no" ]]; then
    log "Creating Lambda function: $FUNCTION_NAME"
    aws lambda create-function \
        --function-name "$FUNCTION_NAME" \
        --runtime "$RUNTIME" \
        --role "$ROLE_ARN" \
        --handler "$HANDLER" \
        --zip-file "fileb://$JAR_FILE" \
        --memory-size $MEMORY \
        --timeout $TIMEOUT \
        --environment "Variables={SPRING_PROFILES_ACTIVE=lambda,AWS_REGION_NAME=$REGION,SAHAYAK_USE_REAL_AWS=true}" \
        --region "$REGION" \
        --no-cli-pager

    log "Waiting for function to become Active..."
    aws lambda wait function-active-v2 --function-name "$FUNCTION_NAME" --region "$REGION"
    log "Function is Active."

    log "Enabling SnapStart for faster cold starts..."
    aws lambda update-function-configuration \
        --function-name "$FUNCTION_NAME" \
        --snap-start ApplyOn=PublishedVersions \
        --region "$REGION" \
        --no-cli-pager

    log "Waiting for configuration update to complete..."
    aws lambda wait function-updated-v2 --function-name "$FUNCTION_NAME" --region "$REGION"

    log "Publishing version for SnapStart..."
    aws lambda publish-version \
        --function-name "$FUNCTION_NAME" \
        --region "$REGION" \
        --no-cli-pager
else
    log "Updating Lambda function code: $FUNCTION_NAME"
    aws lambda update-function-code \
        --function-name "$FUNCTION_NAME" \
        --zip-file "fileb://$JAR_FILE" \
        --region "$REGION" \
        --no-cli-pager

    log "Waiting for update to complete..."
    aws lambda wait function-updated --function-name "$FUNCTION_NAME" --region "$REGION"

    log "Publishing new version..."
    aws lambda publish-version \
        --function-name "$FUNCTION_NAME" \
        --region "$REGION" \
        --no-cli-pager
fi

# ----------------------------- Step 3: API Gateway (REST API v1) ---------------
echo ""
echo "--- Step 3: API Gateway (REST API v1) ---"

# Check if REST API already exists
REST_API_ID=$(aws apigateway get-rest-apis --region "$REGION" \
    --query "items[?name=='$API_NAME'].id" --output text 2>/dev/null || echo "")

if [[ -z "$REST_API_ID" || "$REST_API_ID" == "None" ]]; then
    log "Creating REST API: $API_NAME"
    REST_API_ID=$(aws apigateway create-rest-api \
        --name "$API_NAME" \
        --endpoint-configuration types=REGIONAL \
        --region "$REGION" \
        --query 'id' --output text)

    # Get the root resource ID
    ROOT_RESOURCE_ID=$(aws apigateway get-resources \
        --rest-api-id "$REST_API_ID" \
        --region "$REGION" \
        --query 'items[?path==`/`].id' --output text)

    # Create {proxy+} resource
    PROXY_RESOURCE_ID=$(aws apigateway create-resource \
        --rest-api-id "$REST_API_ID" \
        --parent-id "$ROOT_RESOURCE_ID" \
        --path-part '{proxy+}' \
        --region "$REGION" \
        --query 'id' --output text)

    LAMBDA_ARN="arn:aws:lambda:${REGION}:${ACCOUNT_ID}:function:${FUNCTION_NAME}"

    # ANY method on {proxy+}
    aws apigateway put-method \
        --rest-api-id "$REST_API_ID" \
        --resource-id "$PROXY_RESOURCE_ID" \
        --http-method ANY \
        --authorization-type NONE \
        --region "$REGION" \
        --no-cli-pager > /dev/null

    aws apigateway put-integration \
        --rest-api-id "$REST_API_ID" \
        --resource-id "$PROXY_RESOURCE_ID" \
        --http-method ANY \
        --type AWS_PROXY \
        --integration-http-method POST \
        --uri "arn:aws:apigateway:${REGION}:lambda:path/2015-03-31/functions/${LAMBDA_ARN}/invocations" \
        --region "$REGION" \
        --no-cli-pager > /dev/null

    # ANY method on root /
    aws apigateway put-method \
        --rest-api-id "$REST_API_ID" \
        --resource-id "$ROOT_RESOURCE_ID" \
        --http-method ANY \
        --authorization-type NONE \
        --region "$REGION" \
        --no-cli-pager > /dev/null

    aws apigateway put-integration \
        --rest-api-id "$REST_API_ID" \
        --resource-id "$ROOT_RESOURCE_ID" \
        --http-method ANY \
        --type AWS_PROXY \
        --integration-http-method POST \
        --uri "arn:aws:apigateway:${REGION}:lambda:path/2015-03-31/functions/${LAMBDA_ARN}/invocations" \
        --region "$REGION" \
        --no-cli-pager > /dev/null

    # Deploy to stage
    aws apigateway create-deployment \
        --rest-api-id "$REST_API_ID" \
        --stage-name "$STAGE" \
        --region "$REGION" \
        --no-cli-pager > /dev/null

    # Grant API Gateway permission to invoke Lambda
    aws lambda add-permission \
        --function-name "$FUNCTION_NAME" \
        --statement-id "apigateway-invoke-proxy" \
        --action lambda:InvokeFunction \
        --principal apigateway.amazonaws.com \
        --source-arn "arn:aws:execute-api:${REGION}:${ACCOUNT_ID}:${REST_API_ID}/*/*/*" \
        --region "$REGION" \
        --no-cli-pager > /dev/null 2>&1 || true

    aws lambda add-permission \
        --function-name "$FUNCTION_NAME" \
        --statement-id "apigateway-invoke-root" \
        --action lambda:InvokeFunction \
        --principal apigateway.amazonaws.com \
        --source-arn "arn:aws:execute-api:${REGION}:${ACCOUNT_ID}:${REST_API_ID}/*/ANY/" \
        --region "$REGION" \
        --no-cli-pager > /dev/null 2>&1 || true

    log "REST API Gateway created: $REST_API_ID"
else
    log "REST API Gateway exists: $REST_API_ID"
fi

# ----------------------------- Done -------------------------------------------
API_URL="https://${REST_API_ID}.execute-api.${REGION}.amazonaws.com/${STAGE}"

echo ""
echo "=============================================="
echo -e "  ${GREEN}Deployment Complete!${NC}"
echo "=============================================="
echo ""
echo "  API URL:  $API_URL"
echo ""
echo "  Test endpoints:"
echo "    Health:       curl ${API_URL}/api/v1/health"
echo "    Triage:       curl -X POST ${API_URL}/api/v1/triage -F 'directText=headache' -F 'language=en-IN'"
echo "    Prescription: curl -X POST ${API_URL}/api/v1/prescription -F 'image=@prescription.jpg' -F 'language=hi-IN'"
echo ""
