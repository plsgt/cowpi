package net.cowpi.server;

import net.cowpi.server.config.RoutingServerConfig;
import net.cowpi.server.test.TestClient;
import net.cowpi.server.test.TestClientManager;
import net.cowpi.server.test.TestOes;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BaseIT {
    private static final String CONFIG_FILE = "cowpi.router.config.test";
    private static final Logger logger = LoggerFactory.getLogger(BaseIT.class);

    private RoutingServiceManager serviceManager;

    private TestClientManager testClients;

    protected TestComponent component;

    private TestServerConfig loadConfig() throws FileNotFoundException {
        String filename = System.getProperty(CONFIG_FILE);

        Yaml yaml = new Yaml(new Constructor(TestServerConfig.class));

        FileReader reader = new FileReader(filename);
        return yaml.load(reader);
    }

    @Before
    public void before() throws FileNotFoundException {
        TestServerConfig config = loadConfig();

        component = DaggerTestComponent.builder()
                .config(config)
                .build();

        component.flyway().clean();

        serviceManager = component.routingServiceManager();
        testClients = component.testClientManager();

        serviceManager.addService(testClients);

        serviceManager.start().toCompletableFuture().join();
    }

    @After
    public void after(){
        serviceManager.shutdown().toCompletableFuture().join();
    }


    public TestClient newTestClient(){
        return testClients.newTestClient();
    }

    public TestOes newTestOes(){
        return testClients.newTestOes();
    }
}
