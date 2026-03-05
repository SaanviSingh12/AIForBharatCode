🚀 AWS Setup Guide for Sahayak
Your backend uses 7 AWS services. Here's exactly how to set each one up.

Step 1: Install & Configure AWS CLI
If you don't have the AWS CLI installed:

Then configure with your credentials:

Enter:

Prompt	Value
AWS Access Key ID	Your access key
AWS Secret Access Key	Your secret key
Default region	ap-south-1 (Mumbai)
Output format	json
⚠️ If you don't have an IAM user yet, see Step 3 below first.

Step 2: AWS Console — Enable/Create Services
Log into https://console.aws.amazon.com and switch region to ap-south-1 (Mumbai).

🔴 2a. Amazon Bedrock — Enable Claude 3.5 Sonnet (MOST IMPORTANT)
Your code uses model ID: anthropic.claude-3-5-sonnet-20240620-v1:0

Go to Amazon Bedrock → Left sidebar → Model access
Click Modify model access
Check ✅ Anthropic → Claude 3.5 Sonnet
Click Save changes
Wait for status → Access granted (1–5 minutes)
⚠️ Bedrock may not be available in ap-south-1. If so, change the region to us-east-1 in your application.yml — your AwsConfig.java reads it from ${aws.region}.

🔴 2b. Amazon S3 — Create Temp Bucket
Your code references bucket: sahayak-prescriptions-temp (used by S3Service, TranscribeService, TextractService)

Go to S3 → Create bucket
Bucket name: sahayak-prescriptions-temp (must be globally unique — if taken, use sahayak-prescriptions-temp-<your-name> and update application.yml)
Region: ap-south-1
Block all public access: ✅ Keep enabled
Click Create bucket
(Optional) Add lifecycle rule to auto-delete after 1 day:
Bucket → Management → Create lifecycle rule
Name: delete-temp-files
Apply to all objects
Expiration: 1 day
🟡 2c. Amazon DynamoDB — Create Sessions Table
Your code references table: sahayak-sessions

Go to DynamoDB → Create table
Table name: sahayak-sessions
Partition key: sessionId (String)
Table settings: On-demand (pay per request)
Click Create table
✅ 2d. Amazon Transcribe, Textract, Polly
These are on-demand services — no setup needed. They work automatically once your IAM user has permissions.

Transcribe — used for voice-to-text (Hindi/Tamil audio → text)
Textract — used for prescription image OCR
Polly — used for text-to-speech responses
Step 3: Create IAM User & Policy
3a. Create the IAM Policy
Go to IAM → Policies → Create policy
Click JSON tab, paste this:
Policy name: SahayakBackendPolicy
Click Create policy
3b. Create IAM User (for local development)
IAM → Users → Create user
User name: sahayak-dev
Click Next → Attach policies directly → search and check SahayakBackendPolicy
Click Create user
Go to the user → Security credentials → Create access key
Select Command Line Interface (CLI) → Next → Create
SAVE the Access Key ID and Secret Access Key — you won't see the secret again!
3c. Configure credentials locally
This creates ~/.aws/credentials which the AWS SDK in your Java app reads automatically (your AwsConfig.java uses the default credential chain).

Step 4: Update application.yml (if needed)
If you changed the S3 bucket name, update it:

To switch from mock mode to real AWS, set the environment variable:

Or change application.yml:

Your VoiceTriageService and PrescriptionService both check this flag. When false, they use mock data and skip S3/Transcribe/Textract calls. When true, they call real AWS services.

Step 5: Run the Application
Backend:
Frontend (separate terminal):
Step 6: Test the Setup
Verify the backend is running:

Test triage with direct text (no audio needed):

Test prescription (with an image file):

Summary — AWS Services Checklist
#	Service	Action Required	Used For
1	Bedrock	Enable Claude 3.5 Sonnet model access	AI symptom analysis & medicine mapping
2	S3	Create bucket sahayak-prescriptions-temp	Temp storage for audio/images
3	DynamoDB	Create table sahayak-sessions	Session caching
4	Transcribe	No setup (just IAM permissions)	Voice → Text (Hindi/Tamil)
5	Textract	No setup (just IAM permissions)	Prescription OCR
6	Polly	No setup (just IAM permissions)	Text → Speech responses
7	IAM	Create user + policy	Authentication for all services
💡 Incremental Approach (Recommended)
Start with mock mode (use-real-aws: false) — the app works fully with mock data, no AWS needed
Enable Bedrock first — it's the AI brain. Set use-real-aws: true and test triage
Enable S3 + Textract — for real prescription OCR
Enable S3 + Transcribe — for real voice input
Polly — for audio responses (the app falls back to text-only if Polly fails)