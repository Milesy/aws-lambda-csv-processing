package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.google.gson.Gson;
import com.google.inject.Inject;

public class HandlerDelegate implements RequestHandler<S3Event, String> {
    private static final String RESPONSE_SUCCESS = "Successfully processed file.";

    @Inject
    private LambdaLogger logger;

    @Inject
    private Gson gson;

    @Inject
    private S3RecordHandler s3RecordHandler;

    @Override
    public String handleRequest(S3Event event, Context context) {
        logger.log("Handling S3Event: " + gson.toJson(event));

        s3RecordHandler.handle(getS3NotificationRecord(event));

        return RESPONSE_SUCCESS;
    }

    private S3EventNotification.S3EventNotificationRecord getS3NotificationRecord(S3Event event) {
        if (hasEventGotRecord(event)) {
            return event.getRecords().get(0);
        } else {
            throw new IllegalArgumentException("Unable to process event. No record contained.");
        }
    }

    private boolean hasEventGotRecord(S3Event event) {
        return event.getRecords() != null && event.getRecords().size() == 1;
    }
}
