#!/bin/bash

awslocal sqs create-queue --queue-name=sales

awslocal dynamodb create-table \
    --table-name event_processing_context  \
    --attribute-definitions \
        AttributeName=eventId,AttributeType=S \
    --key-schema \
        AttributeName=eventId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5
