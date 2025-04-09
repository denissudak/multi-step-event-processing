package org.denissudak.eventprocessing.model.context;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class StepProcessingStatus {

  public enum Status {
    NOT_STARTED,
    PROCESSING,
    SUCCESS,
    TRY_AGAIN
  }

  private String stepCode;

//  @DynamoDBTypeConvertedEnum
  private Status status;
}
