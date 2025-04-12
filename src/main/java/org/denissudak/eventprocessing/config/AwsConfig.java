package org.denissudak.eventprocessing.config;

import io.awspring.cloud.core.region.StaticRegionProvider;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

/**
 * @see <a href="https://docs.awspring.io/spring-cloud-aws/docs/3.0.0/reference/html/index.html">Spring Cloud for Amazon Web Services</a>
 */
@Configuration
public class AwsConfig {
    @Bean
    public AwsCredentialsProvider credentialsProvider() {
        return AnonymousCredentialsProvider.create();
    }

    @Bean
    public AwsRegionProvider regionProvider() {
        return new StaticRegionProvider("us-east-1");
    }

    @Bean
    public DynamoDbTable<EventProcessingContext> contextTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("event_processing_context", TableSchema.fromBean(EventProcessingContext.class));
    }
}
