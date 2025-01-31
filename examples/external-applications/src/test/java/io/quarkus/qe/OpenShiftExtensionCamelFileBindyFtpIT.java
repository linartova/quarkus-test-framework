package io.quarkus.qe;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

// TODO: enable test when Camel Quarkus Examples migrate to Quarkus 3.0
@DisabledOnQuarkusVersion(version = "(3\\.[0-9]\\..*)", reason = "Camel Quarkus Examples is using Quarkus 2.16")
@DisabledOnQuarkusSnapshot(reason = "Camel Quarkus 999-SNAPSHOT is not available in maven repository") // f.e. 'quarkus-camel-bom:pom:999-SNAPSHOT' is not available
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftExtensionCamelFileBindyFtpIT {

    @GitRepositoryQuarkusApplication(repo = "https://github.com/apache/camel-quarkus-examples.git", contextDir = "file-bindy-ftp", mavenArgs = "-Dopenshift")
    static final RestService app = new RestService();

    @Test
    public void test() {
        app.given()
                .get("/q/health")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
