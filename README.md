This supporting code for my article – [How to build multi-step idempotent message processing](https://medium.com/@denissudak/how-to-build-multi-step-message-processing-72dd5c392050)

[![Watch the code walkthrough video](https://img.youtube.com/vi/-7NUO6QRfss/0.jpg)](https://www.youtube.com/watch?v=-7NUO6QRfss)

## Setup
Start LocalStack on your machine

    docker compose up

Install [AWS CLI](https://docs.localstack.cloud/user-guide/integrations/aws-cli/) to interact with it:

    python3 -m venv .venv
    source venv/bin/activate
    pip install awscli-local

Run `localstack.sh` to create SQS queue and DynamoDB table.

Install [LocalStack Docker extension](https://docs.localstack.cloud/user-guide/tools/localstack-docker-extension/) to
later see the DynamoDB records 

