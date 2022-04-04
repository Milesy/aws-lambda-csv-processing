package example;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqsMessageHandler {
    public static final int BATCH_SIZE_DEFAULT = 10;

    private final List<SendMessageBatchRequestEntry> entries = new ArrayList<>();

    @Inject
    private LambdaLogger logger;

    @Inject
    private Gson gson;

    @Inject
    private AmazonSQS sqs;

    @Inject
    @Named("queue")
    private String queueUrl;

    private void processOutputSingleMessage(Map<String, String> output) {
        SendMessageRequest messageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(gson.toJson(output));

        sqs.sendMessage(messageRequest);
    }

    public void init() {
        logger.log("Queue URL: " + queueUrl);
    }
    public void processOutputBatch(Map<String, String> output) {
        if (batchBufferIsFull()) {
            sendAndClear();
        } else {
            entries.add(new SendMessageBatchRequestEntry(UUID.randomUUID().toString(), gson.toJson(output)));
        }
    }

    public void sendAndClear() {
        SendMessageBatchRequest messageRequest = new SendMessageBatchRequest()
                .withQueueUrl(queueUrl)
                .withEntries(entries);

        sqs.sendMessageBatch(messageRequest);
        entries.clear();
    }

    private boolean batchBufferIsFull() {
        return entries.size() == BATCH_SIZE_DEFAULT;
    }
}
