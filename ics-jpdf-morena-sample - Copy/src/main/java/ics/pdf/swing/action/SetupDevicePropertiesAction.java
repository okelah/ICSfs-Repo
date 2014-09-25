package ics.pdf.swing.action;

import static ics.pdf.swing.BanksConfig.getInstance;
import ics.pdf.swing.BanksConfig;
import ics.pdf.swing.util.IconUtil;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;
import eu.gnome.morena.TransferListener;

public class SetupDevicePropertiesAction extends AbstractAction implements TransferListener {
    private static final long serialVersionUID = 1L;

    private Scanner scanner = null;
    private Manager manager = null;
    private Component parent = null;

    public SetupDevicePropertiesAction(Manager manager, Component parent) {
        super("Setup Device Prperties", IconUtil.getSetupDevicePropertiesActionIcon());
        putValue(SHORT_DESCRIPTION, "Setup Device Prperties");
        putValue(NAME, "Setup Device Prperties");
        this.manager = manager;
        this.parent = parent;
    }

    public synchronized void actionPerformed(ActionEvent event) {
        try {
            Device device = setupDevice();
            System.out.println("Selected Device" + device);
            if (device != null) {
                if (device instanceof Scanner) {
                    scanner = setupScanner(device);
                } else {
                    System.out.println("Scanner is Camera...");
                }
            } else {
                System.out.println("No device connected!!!");
                JOptionPane.showMessageDialog(parent, "No device connected!!!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Throwable exception) {
            JOptionPane.showMessageDialog(parent, exception.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("ERROR" + exception);
        }
    }

    public void transferDone(File file) {
        setEnabled(true);
    }

    public void transferFailed(int code, String message) {
        System.out.println(message + " [0x" + Integer.toHexString(code) + "]");
        setEnabled(true);
        // cancelAction.setEnabled(false);
    }

    public void transferProgress(int percent) {
        // System.out.println(percent + "% ... y");
    }

    private Device setupDevice() {
        System.out.println("Setup Scanner");
        List<? extends Device> devices = manager.listDevices();

        if (devices == null || devices.isEmpty()) {
            return null;
        }
        String deviceName = (String) getInstance().getProperty("device");
        if (deviceName.equals("-")) {
            Device device = manager.selectDevice(parent);
            getInstance().setProperty("device", device.toString());
            return device;
        } else {
            for (Device d : devices) {
                if (d.toString().equals(deviceName)) {
                    return d;
                }
            }

            // if predefined scanner was not found
            getInstance().setProperty("device", "-");
            getInstance().setProperty("device", false);
            return setupDevice();
        }
    }

    private Scanner setupScanner(Device device) {
        System.out.println("The Device was found as Scanner");
        scanner = (Scanner) device;

        if (scanner.setupDevice(parent)) {
            BanksConfig.setupDeviceProperties(scanner);
        }
        return scanner;
    }

}
