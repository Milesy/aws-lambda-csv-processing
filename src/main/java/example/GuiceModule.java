package example;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class GuiceModule extends AbstractModule {
    private static final Regions DEFAULT_REGION = Regions.US_WEST_2;
    private final Context lambdaContext;

    public GuiceModule(Context lambdaContext) {
        this.lambdaContext = lambdaContext;
    }

    @Override
    protected void configure() {
        bind(HandlerDelegate.class).toInstance(handlerDelegate());
        bind(Gson.class).toInstance(gson());
        bind(AmazonS3.class).toInstance(amazonS3());
        bind(S3RecordHandler.class).toInstance(s3RecordHandler());
        bind(SqsMessageHandler.class).toInstance(sqsMessageHandler());
        bind(LambdaLogger.class).toInstance(lambdaContext.getLogger());

        AmazonSQS amazonSqs = amazonSqs();
        bind(AmazonSQS.class).toInstance(amazonSqs);

        bind(String.class)
                .annotatedWith(Names.named("queue"))
                .toInstance(amazonSqs.getQueueUrl("CsvQueue").getQueueUrl());

    }

    private HandlerDelegate handlerDelegate() {
        return new HandlerDelegate();
    }

    private S3RecordHandler s3RecordHandler() {
        return new S3RecordHandler();
    }

    private SqsMessageHandler sqsMessageHandler() {
        return new SqsMessageHandler();
    }

    private Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(DEFAULT_REGION).build();
    }

    private AmazonSQS amazonSqs() {
        return AmazonSQSClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(DEFAULT_REGION)
                .build();
    }
}
