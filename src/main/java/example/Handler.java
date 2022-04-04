package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class Handler implements RequestHandler<S3Event, String> {
    private Injector injector;

    @Inject
    private HandlerDelegate handlerDelegate;

    @Override
    public String handleRequest(S3Event event, Context context) {
        configureGuice(context);

        return handlerDelegate.handleRequest(event, context);
    }

    private void configureGuice(Context context) {
        this.injector = Guice.createInjector(new GuiceModule(context));
        this.injector.injectMembers(this);
    }
}