package org.denissudak.eventprocessing.services;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.denissudak.eventprocessing.model.context.EventProcessingContext.KEY_ATTRIBUTE_NAME;
import static org.denissudak.eventprocessing.model.context.EventProcessingContext.LOCKED_ATTRIBUTE_NAME;

/**
 * @see <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-items.html">Work with items in DynamoDB</a>
 */
@Service
@RequiredArgsConstructor
public class ProcessingContextDAO {
    private final DynamoDbTemplate dynamoDbTemplate;
    private final DynamoDbTable<EventProcessingContext> contextTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public Optional<EventProcessingContext> fetchByEventId(String eventId) {
        checkNotNull(eventId);

        Key key = Key.builder().partitionValue(eventId).build();
        return Optional.ofNullable(dynamoDbTemplate.load(key, EventProcessingContext.class));
    }

    public void saveNew(EventProcessingContext context) {
        checkNotNull(context);

        putOnCondition(context, Expression.builder().expression("attribute_not_exists(" + KEY_ATTRIBUTE_NAME + ")").build());
    }

    public void update(EventProcessingContext context) {
        checkNotNull(context);
        checkArgument(context.isLocked());

        putOnCondition(context, existingLockedItemExpression());
    }

    public void acquireLock(EventProcessingContext context) {
        checkNotNull(context);
        checkArgument(!context.isLocked());

        context.setLocked(true);
        putOnCondition(context, existingNotLockedItemExpression());

    }

    public void releaseLock(EventProcessingContext context) {
        checkNotNull(context);
        checkArgument(context.isLocked());

        context.setLocked(false);
        putOnCondition(context, existingLockedItemExpression());
    }

    private void putOnCondition(EventProcessingContext context, Expression conditionExpression) {
        TransactWriteItemsEnhancedRequest putReq = TransactWriteItemsEnhancedRequest
                .builder()
                .addPutItem(contextTable, buildPutItemRequest(context, conditionExpression))
                .build();
        dynamoDbEnhancedClient.transactWriteItems(putReq);
    }

    private TransactPutItemEnhancedRequest<EventProcessingContext> buildPutItemRequest(EventProcessingContext context, Expression contidionExpression) {
        return TransactPutItemEnhancedRequest.builder(EventProcessingContext.class)
                .item(context)
                .conditionExpression(contidionExpression)
                .build();
    }

    private static Expression existingLockedItemExpression() {
        return Expression.builder()
                .expression(LOCKED_ATTRIBUTE_NAME + " = :trueVal and attribute_exists(" + KEY_ATTRIBUTE_NAME + ")")
                .expressionValues(Map.of(":trueVal", AttributeValue.builder().bool(true).build()))
                .build();
    }

    private static Expression existingNotLockedItemExpression() {
        return Expression.builder()
                .expression("(attribute_not_exists(" + LOCKED_ATTRIBUTE_NAME + ") or " + LOCKED_ATTRIBUTE_NAME + " = :falseVal) and attribute_exists(" + KEY_ATTRIBUTE_NAME + ")")
                .expressionValues(Map.of(":falseVal", AttributeValue.builder().bool(false).build()))
                .build();
    }
}
