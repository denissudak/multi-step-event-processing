Start LocalStack on your machine

    docker compose up

Install [AWS CLI](https://docs.localstack.cloud/user-guide/integrations/aws-cli/) to interact with it:

    python -m venv .venv
    source venv/bin/activate
    pip install awscli-local

Run `localstack.sh` to create SQS queue and DynamoDB table.

Install [LocalStack Docker extension](https://docs.localstack.cloud/user-guide/tools/localstack-docker-extension/) to
later see the DynamoDB records 

