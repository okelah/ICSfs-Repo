package ics.pdf.swing.action;

import static ics.pdf.swing.BanksConfig.getInstance;
import ics.pdf.swing.BanksConfig;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;
import eu.gnome.morena.TransferListener;

public class SetupDeviceAction extends AbstractAction implements TransferListener {
    private static final long serialVersionUID = 1L;
    static Logger log = LogManager.getLogger(SetupDeviceAction.class.getName());

    private Scanner scanner = null;
    private Manager manager = null;
    private Component parent = null;

    public SetupDeviceAction(Manager manager, Component parent) {
        super("Setup Device");
        this.manager = manager;
        this.parent = parent;
    }

    public synchronized void actionPerformed(ActionEvent event) {
        try {
            Device device = setupDevice();
            log.info("Selected Device", device);
            if (device != null) {
                if (device instanceof Scanner) {
                    scanner = setupScanner(device);
                } else {
                    System.out.println("Scanner is Camera...");
                }
            } else {
                log.fatal("No device connected!!!");
                JOptionPane.showMessageDialog(parent, "No device connected!!!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Throwable exception) {
            JOptionPane.showMessageDialog(parent, exception.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            log.fatal("ERROR", exception);
        }
    }

    public void transferDone(File file) {
        setEnabled(true);
    }

    public void transferFailed(int code, String message) {
        log.fatal(message + " [0x" + Integer.toHexString(code) + "]");
        setEnabled(true);
        // cancelAction.setEnabled(false);
    }

    public void transferProgress(int percent) {
        // System.out.println(percent + "% ... y");
    }

    private Device setupDevice() {
        log.info("Setup Scanner");
        List<? extends Device> devices = manager.listDevices();

        if (devices == null || devices.isEmpty()) {
            return null;
        }
        Device device = manager.selectDevice(parent);
        getInstance().setProperty("device", device.toString());
        return device;
    }

    private Scanner setupScanner(Device device) {
        log.info("The Device was found as Scanner");
        scanner = (Scanner) device;

        if (scanner.setupDevice(parent)) {
            BanksConfig.setupDeviceProperties(scanner);
        }
        return scanner;
    }
}
