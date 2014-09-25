package ics.pdf.swing.util;

import ics.pdf.swing.action.AcquireBatchImageAction;
import ics.pdf.swing.action.SetupDeviceAction;

import javax.swing.ImageIcon;

public class IconUtil {

    public static ImageIcon getAcquireBatchImageActionIcon() {
        java.net.URL imgURL = AcquireBatchImageAction.class.getClassLoader().getResource("scanner1.png");
        if (imgURL != null) {
            return new ImageIcon(imgURL, null);
        }
        return null;
    }

    public static ImageIcon getAttachementIcon() {
        java.net.URL imgURL = AcquireBatchImageAction.class.getClassLoader().getResource("attachment.png");
        if (imgURL != null) {
            return new ImageIcon(imgURL, null);
        }
        return null;
    }

    public static ImageIcon getSetupDeviceActionIcon() {
        java.net.URL imgURL = SetupDeviceAction.class.getClassLoader().getResource("scanner-setup.png");
        if (imgURL != null) {
            return new ImageIcon(imgURL, null);
        }
        return null;
    }

    public static ImageIcon getSetupDevicePropertiesActionIcon() {
        java.net.URL imgURL = SetupDeviceAction.class.getClassLoader().getResource("scanner-install.png");
        if (imgURL != null) {
            return new ImageIcon(imgURL, null);
        }
        return null;
    }

}
