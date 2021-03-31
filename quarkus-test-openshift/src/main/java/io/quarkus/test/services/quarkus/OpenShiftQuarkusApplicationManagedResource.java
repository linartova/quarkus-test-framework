package io.quarkus.test.services.quarkus;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.http.HttpStatus;

import io.quarkus.test.bootstrap.OpenShiftExtensionBootstrap;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.logging.LoggingHandler;
import io.quarkus.test.logging.OpenShiftLoggingHandler;

public abstract class OpenShiftQuarkusApplicationManagedResource implements QuarkusManagedResource {

    private static final String EXPECTED_OUTPUT_FROM_SUCCESSFULLY_STARTED = "features";
    private static final int EXTERNAL_PORT = 80;

    protected final QuarkusApplicationManagedResourceBuilder model;
    protected final OpenShiftClient client;

    private LoggingHandler loggingHandler;
    private boolean init;
    private boolean running;

    public OpenShiftQuarkusApplicationManagedResource(QuarkusApplicationManagedResourceBuilder model) {
        this.model = model;
        this.client = model.getContext().get(OpenShiftExtensionBootstrap.CLIENT);
    }

    protected abstract void doInit();

    protected abstract void doUpdate();

    @Override
    public void validate() {
        if (model.isSslEnabled()) {
            fail("SSL is not supported for OpenShift tests yet");
        }
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        if (!init) {
            doInit();
            init = true;
        } else {
            doUpdate();
        }

        client.scaleTo(model.getContext().getOwner(), 1);
        running = true;

        loggingHandler = new OpenShiftLoggingHandler(model.getContext());
        loggingHandler.startWatching();
    }

    @Override
    public void stop() {
        if (loggingHandler != null) {
            loggingHandler.stopWatching();
        }

        client.scaleTo(model.getContext().getOwner(), 0);
        running = false;
    }

    @Override
    public String getHost() {
        return client.url(model.getContext().getOwner());
    }

    @Override
    public int getPort(Protocol protocol) {
        return EXTERNAL_PORT;
    }

    @Override
    public boolean isRunning() {
        return appIsStarted() && routeIsReachable();
    }

    @Override
    public List<String> logs() {
        return loggingHandler.logs();
    }

    @Override
    public void restart() {
        stop();
        if (model.containsBuildProperties()) {
            init = false;
            model.build();
        }

        start();
    }

    private boolean appIsStarted() {
        return loggingHandler != null && loggingHandler.logsContains(EXPECTED_OUTPUT_FROM_SUCCESSFULLY_STARTED);
    }

    private boolean routeIsReachable() {
        return given().baseUri(getHost()).basePath("/").port(getPort(Protocol.HTTP)).get()
                .getStatusCode() != HttpStatus.SC_SERVICE_UNAVAILABLE;
    }
}