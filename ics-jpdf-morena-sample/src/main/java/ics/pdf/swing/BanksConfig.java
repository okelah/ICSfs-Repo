package ics.pdf.swing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import eu.gnome.morena.Scanner;

public class BanksConfig {

    private static PropertiesConfiguration config = null;
    static Logger log = LogManager.getLogger(BanksConfig.class);

    private static String propertiesDirectory = System.getProperty("java.io.tmpdir") + "banks";
    private static String propertiesFile = propertiesDirectory + "/usergui.properties";
    public static String logFile = propertiesDirectory + "/log4j2.xml";

    private static PropertiesConfiguration loadConfiguration() {
        try {
            File configFile = new File(propertiesFile);
            if (!configFile.exists()) {
                log.info("Create New properties file");
                new File(propertiesDirectory).mkdir();
                configFile.createNewFile();
                PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
                config = new PropertiesConfiguration(propertiesFile);
                config.setHeader("Header");
                config.setProperty("scanner.resolution", 0);
                config.setProperty("scanner.mode", 0);
                config.setProperty("device", "-");
                config.setProperty("device.setup.ready", false);
                config.setProperty("job.pages", -1);
                config.setProperty("scanner.functionalUnit", -1);
                config.setProperty("scanner.scanFrame", new int[] { 0, 0, 0, 0 });
                config.setProperty("morena.device.types", new String[] { ".*HP.*", ".*hp.*", ".*canojet.*",
                        ".*officejet.*" });
                config.setProperty("morena.log.level", Level.OFF);

                StringBuffer footer = new StringBuffer(
                        "job.pages number of pages will be scanned per job, if <1 then no limit");
                config.setFooter(footer.toString());
                config.save();
                return config;
            } else {
                return new PropertiesConfiguration(propertiesFile);
            }

        } catch (ConfigurationException e1) {
            log.fatal("ERROR", e1);
        } catch (IOException e1) {
            log.fatal("ERROR", e1);
        }
        return null;
    }

    public static void setProperty(String key, Object value) {
        getInstance().setProperty(key, value);
    }

    public static Configuration getInstance() {
        if (config == null) {
            config = loadConfiguration();
            config.setAutoSave(true);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
        }
        return config;
    }

    public static void setupDeviceProperties(Scanner scanner) {
        setProperty("scanner.mode", scanner.getMode());
        setProperty("scanner.resolution", scanner.getResolution());
        setProperty("device.setup.ready", true);
        setProperty("scanner.functionalUnit", scanner.getFunctionalUnit());
        setProperty("scanner.duplexEnabled", scanner.isDuplexEnabled());

        StringBuffer str = new StringBuffer();
        str.append("GRAY_8:");
        str.append(Scanner.GRAY_8);
        str.append(",");
        str.append("GRAY_16:");
        str.append(Scanner.GRAY_16);
        str.append(",");
        str.append("RGB_8:");
        str.append(Scanner.RGB_8);
        str.append(",");
        str.append("RGB_16:");
        str.append(Scanner.RGB_16);
        str.append(",");
        str.append("BLACK_AND_WHITE:");
        str.append(Scanner.BLACK_AND_WHITE);
        setProperty("all.modes", str.toString());
        setProperty("scanner.SupportedResolutions", scanner.getSupportedResolutions());
    }

    public static void loadLog4jConfiguration() {
        try {
            File logConfigFile = new File(logFile);
            if (!logConfigFile.exists()) {
                log.info("Create New log4j2 file");
                InputStream inputStream = BanksConfig.class.getClassLoader().getResourceAsStream("log4j2.xml");
                FileOutputStream outputStream = new FileOutputStream(logConfigFile);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
            URI source = logConfigFile.toURI();
            Configurator.initialize("contextLog4J", null, source);
        } catch (Exception e) {
            log.fatal("ERROR", e);
        }
    }

}
