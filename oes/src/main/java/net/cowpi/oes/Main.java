package net.cowpi.oes;

import net.cowpi.ServiceManager;
import net.cowpi.ServiceManagerShutdownHook;
import net.cowpi.oes.config.OesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static OesConfig loadConfig(String[] args){
        if(args.length < 1){
            logger.error("No config file supplied.");
            return null;
        }
        String filename = args[0];

        Yaml yaml = new Yaml(new Constructor(OesConfig.class));

        try (FileReader reader = new FileReader(filename)){
            return yaml.load(reader);
        } catch (FileNotFoundException e) {
            logger.error("File not found. {}", filename);
        } catch (IOException e) {
            logger.error("Unable to read file.", e);
        }
        return null;
    }

    public static void main(String[] args){
        OesConfig config = loadConfig(args);

        OesComponent component = DaggerOesComponent.builder()
                .config(config)
                .build();

        ServiceManager oesServiceManager = component.oesServiceManager();

        Runtime.getRuntime().addShutdownHook(new ServiceManagerShutdownHook(oesServiceManager));

        oesServiceManager.start();
        oesServiceManager.getShutdownFuture().toCompletableFuture().join();

    }
}
