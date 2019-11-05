package net.cowpi.perf;

import net.cowpi.perf.config.PerfConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static PerfConfig loadConfig(String[] args){
        if(args.length < 1){
            logger.error("No config file supplied.");
            return null;
        }
        String filename = args[0];

        Yaml yaml = new Yaml(new Constructor(PerfConfig.class));

        try (FileReader reader = new FileReader(filename)){
            return yaml.load(reader);
        } catch (FileNotFoundException e) {
            logger.error("File not found. {}", filename);
        } catch (IOException e) {
            logger.error("Unable to read file.", e);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        PerfConfig config = loadConfig(args);

        if(config == null){
            return;
        }

        PerfServerComponent perfServerComponent = DaggerPerfServerComponent.builder()
                .config(config)
                .build();

        PerfExperiment experiment = perfServerComponent.experiment();
        experiment.run();

    }
}
