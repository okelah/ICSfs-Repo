package ics.pdf.swing;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import eu.gnome.morena.Scanner;

public class BanksConfig {

    private static PropertiesConfiguration config = null;

    private static String propertiesDirectory = System.getProperty("java.io.tmpdir") + "banks";
    private static String propertiesFile = propertiesDirectory + "/usergui.properties";
    public static String logFile = propertiesDirectory + "/log4j2.xml";

    public static PropertiesConfiguration loadConfiguration() {
        try {
            File configFile = new File(propertiesFile);
            if (!configFile.exists()) {
                System.out.println("Create New properties file");
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
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
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

}
