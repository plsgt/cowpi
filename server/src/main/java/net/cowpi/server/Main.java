package net.cowpi.server;

import net.cowpi.ServiceManager;
import net.cowpi.ServiceManagerShutdownHook;
import net.cowpi.server.config.RoutingServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static RoutingServerConfig loadConfig(String[] args){
        if(args.length < 1){
            logger.error("No config file supplied.");
            return null;
        }
        String filename = args[0];

        Yaml yaml = new Yaml(new Constructor(RoutingServerConfig.class));

        try (FileReader reader = new FileReader(filename)){
            return yaml.load(reader);
        } catch (FileNotFoundException e) {
            logger.error("File not found. {}", filename);
        } catch (IOException e) {
            logger.error("Unable to read file.", e);
        }
        return null;
    }

    public static void main(String[] args) {
        RoutingServerConfig config = loadConfig(args);

        if(config == null){
            return;
        }

        MainComponent component = DaggerMainComponent.builder()
                .routerServerConfig(config)
                .build();

        ServiceManager serviceManager = component.routingServiceManager();

        Runtime.getRuntime().addShutdownHook(new ServiceManagerShutdownHook(serviceManager));

        serviceManager.start();
        serviceManager.getShutdownFuture().toCompletableFuture().join();
    }

}
