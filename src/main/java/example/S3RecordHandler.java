package example;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.inject.Inject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class S3RecordHandler {
    @Inject
    private LambdaLogger logger;

    @Inject
    private AmazonS3 amazonS3;

    @Inject
    private Gson gson;

    @Inject
    private SqsMessageHandler sqsMessageHandler;

    public void handle(S3EventNotification.S3EventNotificationRecord record) {
        logger.log("S3 record: " + gson.toJson(record));

        Stopwatch timer = Stopwatch.createStarted();

        S3Object s3Object = getS3Object(record);

        int index = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
            String line;
            String header = null;

            sqsMessageHandler.init();

            while ((line = bufferedReader.readLine()) != null) {
                if (index++ == 0) {
                    header = line;
                } else {
                    Map<String, String> output = new LinkedHashMap<>();
                    output.put("header", header);
                    output.put("record", line);

                    sqsMessageHandler.processOutputBatch(output);
                }
            }

            sqsMessageHandler.sendAndClear();

        } catch (IOException e) {
            throw new RuntimeException("Unable to open S3 object.", e);
        }

        logger.log("Processing of " + (index - 1) + " rows took: " + timer.stop());
    }

    private S3Object getS3Object(S3EventNotification.S3EventNotificationRecord record) {
        String bucketName = record.getS3().getBucket().getName();
        String bucketKey = record.getS3().getObject().getUrlDecodedKey();

        logger.log("Bucket Name: " + bucketName);

        return amazonS3.getObject(new GetObjectRequest(bucketName, bucketKey));
    }
}
