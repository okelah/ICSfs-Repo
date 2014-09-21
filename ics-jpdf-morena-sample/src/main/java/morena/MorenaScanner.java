package morena;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import oracle.forms.handler.IHandler;
import oracle.forms.properties.ID;
import oracle.forms.ui.CustomEvent;
import oracle.forms.ui.VBean;
import SK.gnome.morena.Morena;
import SK.gnome.morena.MorenaSource;
import eu.gnome.morena.Camera;
import eu.gnome.morena.Configuration;
import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;
import eu.gnome.morena.TransferDoneListener;

public class MorenaScanner extends VBean implements TransferDoneListener {

    protected static final ID do_scan = ID.registerProperty("DO_SCAN");
    protected static final ID set_frame = ID.registerProperty("SET_FRAME");
    protected static final ID sig_name = ID.registerProperty("SIG_NAME");
    protected static final ID show_image = ID.registerProperty("SHOW_IMAGE");
    protected static final ID CLIPBOARD_IMAGE = ID.registerProperty("CLIPBOARD_IMAGE");

    protected static final ID scan_complete = ID.registerProperty("SCAN_COMPLETE");

    private IHandler m_handler;
    private String scanFrame = "0,0,0,0";
    private String sigName = "";

    public MorenaScanner() {
        super();
    }

    @Override
    public final void init(IHandler handler) {
        m_handler = handler;
        super.init(handler);

    }

    @Override
    public boolean setProperty(ID _ID, Object _args) {
        if (_ID == do_scan) {
            doScan();
        } else if (_ID == set_frame) {
            scanFrame = (String) _args;
        } else if (_ID == sig_name) {
            sigName = (String) _args;
        } else if (_ID == show_image) {
            showImage((String) _args);
        } else if (_ID == CLIPBOARD_IMAGE) {
            saveClipBoardToFile((String) _args);
        }

        return true;
    }

    @Override
    public Object getProperty(ID _iD) {
        return super.getProperty(_iD);
    }

    public void saveClipBoardToFile(String fileName) {
        ImageClipBoard clipBoard = new ImageClipBoard();
        clipBoard.saveClipBoardToFile(fileName);
    }

    public void showImage(String path) {
        JFrame f = new JFrame();
        f.getContentPane().add(new javax.swing.JLabel(new javax.swing.ImageIcon(path)));
        f.setSize(200, 200);
        f.setVisible(true);
    }

    public void doScan() {
        Configuration.addDeviceType(".*HP.*", true);
        List devices = Manager.getInstance().listDevices();
        System.out.println("devices found=" + devices.size());
        System.out.println(devices);

        if (devices.size() > 0) {

            Device device = (Device) devices.get(0);
            System.out.println("device class=" + device.getClass());

            if (device != null) {
                boolean showUI = false;
                int resolution = 300; // default

                // for scanner device set the scanning parameters
                if (device instanceof Scanner) {
                    Scanner scanner = (Scanner) device;
                    scanner.setFunctionalUnit(1);
                    scanner.setMode(Scanner.RGB_16);
                    List resolutions = scanner.getSupportedResolutions();
                    // try to find the best resolution close to 300
                    try {
                        System.out.println("supported resolutions=" + resolutions);
                        if (resolutions.contains(new Integer(300))) {
                            resolution = 300;
                        } else {
                            resolution = (Integer) resolutions.get(resolutions.size() / 2);
                        }
                    } catch (Exception e) {
                        // failed to calculate resoltion
                        e.printStackTrace();
                        resolution = 300;
                    }

                    System.out.println("setting resolution to " + resolution);
                    scanner.setResolution(resolution);

                    try {
                        String[] frames = scanFrame.split(",");
                        int x = Integer.parseInt(frames[0]);
                        int y = Integer.parseInt(frames[1]);
                        int w = Integer.parseInt(frames[2]);
                        int h = Integer.parseInt(frames[3]);
                        scanner.setFrame(x, y, w, h);
                    } catch (Exception e) {
                        scanner.setFrame(0, 0, 0, 0);
                        e.printStackTrace();
                    }
                }
                // for camera device show native UI
                else if (device instanceof Camera) {
                    showUI = true;
                }

                // starting the scanning (transfered image is handled in the transferDone(file) method
                try {
                    // device.startTransfer(showUI, this);
                    device.startTransfer(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No device connected!!!");
        }
    }

    public void transferDone(File file) {
        try {
            if (file != null) {
                BufferedImage image = ImageIO.read(file);
                System.out.println("image (" + image.getWidth() + ", " + image.getHeight() + ") color model="
                        + image.getColorModel());

                ImageIO.write(image, "jpg", new File(sigName));

                CustomEvent ce = new CustomEvent(m_handler, scan_complete);
                dispatchCustomEvent(ce);
            } else {
                System.out.println("nothing scanned");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Manager.getInstance() != null) {
                Manager.getInstance().close();
            }
        }
    }

    public void transferFailed(int i, String string) {
        System.out.println("Scanning failed:" + i + "," + string);
        if (Manager.getInstance() != null) {
            Manager.getInstance().close();
        }
    }

    public static void main(String[] args) {
        MorenaScanner scan = new MorenaScanner();
        scan.sigName = "C:\\bank\\sig.jpg";
        scan.scanFrame = "1473,67,843,279";
        scan.scanFrame = "0,0,0,0";
        scan.doScan();
        // scan.doScan2();

    }

    public void doScan2() {
        MorenaSource source = Morena.selectSource(null);

    }

}
