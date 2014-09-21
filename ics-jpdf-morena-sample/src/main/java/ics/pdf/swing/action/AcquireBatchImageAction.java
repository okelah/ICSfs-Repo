package ics.pdf.swing.action;

import static ics.pdf.swing.BanksConfig.getInstance;
import ics.pdf.swing.BanksConfig;
import ics.pdf.swing.TIFFManager;
import ics.pdf.swing.morena.ScanSession;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdfNotes.PDFNotesBean;

import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;
import eu.gnome.morena.TransferListener;

public class AcquireBatchImageAction extends AbstractAction implements TransferListener {
    private static final long serialVersionUID = 1L;
    static Logger log = LogManager.getLogger(AcquireBatchImageAction.class.getName());

    private Scanner scanner = null;
    private PDFNotesBean pDFNotesBean = null;
    private Manager manager = null;
    private Component parent = null;

    public AcquireBatchImageAction(PDFNotesBean pDFNotesBean, Manager manager, Component parent) {
        super("Scan...");
        this.pDFNotesBean = pDFNotesBean;
        this.manager = manager;
        this.parent = parent;
    }

    public synchronized void actionPerformed(ActionEvent event) {
        try {
            Device device = setupDevice();
            log.info("Selected Device", device);
            List<File> filesToDelete = null;
            List<BufferedImage> images = null;
            if (device != null) {
                images = new ArrayList<BufferedImage>();
                List<String> imagesFileNames = new ArrayList<String>();
                filesToDelete = new ArrayList<File>();
                if (device instanceof Scanner) {
                    scanner = setupScanner(device);
                    int resolution = getInstance().getInt("scanner.resolution");
                    int scannerMode = getInstance().getInt("scanner.mode");

                    scanner.setMode(scannerMode);

                    scanner.setResolution(resolution);
                    log.debug("scanner.setResolution = " + resolution);
                    int jobPages = getInstance().getInt("job.pages");
                    // find feeder unit
                    int functionalUnit = getInstance().getInt("scanner.functionalUnit");
                    System.out.println("Feeder unit : "
                            + (functionalUnit >= 0 ? functionalUnit : "none found - trying 0"));
                    if (functionalUnit < 0) {
                        functionalUnit = 0; // 0 designates a default unit
                        getInstance().setProperty("scanner.functionalUnit", 0);
                    }
                    if (scanner.isDuplexSupported() && getInstance().getBoolean("scanner.duplexEnabled")) {
                        scanner.setDuplexEnabled(true);
                    }

                    int pageNo = 0;
                    ScanSession session = new ScanSession();
                    // start batch scan
                    try {
                        session.startSession(device, functionalUnit);
                        File file = null;
                        while (null != (file = session.getImageFile())) {
                            ++pageNo;
                            BufferedImage image = ImageIO.read(file);
                            if (!"jpg".equalsIgnoreCase(ScanSession.getExt(file))) { // convert to jpeg if
                                // not already in
                                // jpeg format
                                File jpgFile = new File(file.getParent(), "Morena_example_img_" + (pageNo++) + ".jpg");
                                FileOutputStream fout = new FileOutputStream(jpgFile);
                                ImageIO.write(image, "jpeg", fout);
                                fout.close();
                                filesToDelete.add(jpgFile);
                                images.add(ImageIO.read(jpgFile));

                                imagesFileNames.add(jpgFile.getAbsolutePath());
                            }

                            filesToDelete.add(file);
                            if (jobPages > 0) {
                                if (!session.isEmptyFeeder() && (pageNo == jobPages)) {
                                    scanner.cancelTransfer();
                                    break;
                                }
                            }
                            if (functionalUnit < 1) { // single page from flatbed
                                break;
                            }
                        }
                        log.debug("Scanned Pages [ " + (pageNo) + " ] page");
                        if (filesToDelete.isEmpty()) {
                            log.debug("Morena Error: [" + session.getErrorCode() + "] " + session.getErrorMessage());
                            JOptionPane
                                    .showMessageDialog(parent, "Feeder is empty", "Error", JOptionPane.ERROR_MESSAGE);
                            setEnabled(true);
                            return;
                        }

                    } catch (Exception ex) { // check if error is related to empty ADF
                        // ex.printStackTrace();
                        log.fatal("ERROR", ex);
                        JOptionPane.showMessageDialog(parent, session.getErrorMessage().toString(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                        if (session.isEmptyFeeder()) {
                            System.out.println("No more sheets in the document feeder");
                        } else {
                            // ex.printStackTrace();
                            log.fatal("ERROR", ex);
                        }
                    }
                } else {
                    System.out.println("Scanner is Camera...");
                    log.fatal("Scanner was found as Camera");
                    JOptionPane.showMessageDialog(parent, "Scanner was found as Camera", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                log.info("Scanning completed...");

                if (images != null && !images.isEmpty()) {
                    PDFNotesBean tempNotes = null;
                    try {
                        log.debug("Converting to TIFF...");
                        InputStream is = new ByteArrayInputStream(TIFFManager.compressTIFFFileList(imagesFileNames));
                        tempNotes = new PDFNotesBean();
                        File pdfFile = File.createTempFile("Morena_example", ".pdf");
                        tempNotes.loadDocument(is, "tif");
                        log.debug("Converting to PDF...");
                        tempNotes.save(tempNotes, "pdf", pdfFile);
                        log.debug("PDF was created [" + pdfFile.getAbsolutePath() + "] size: "
                                + (pdfFile.length() / 1024 / 1024) + " Mb");
                        tempNotes.invalidate();
                        is.close();

                        pDFNotesBean.loadPDF(new FileInputStream(pdfFile));
                        log.debug("Finish Loading PDF File [" + pdfFile.getName() + "]");

                    } catch (IOException e) {
                        log.fatal("ERROR", e);
                    } catch (PDFException e) {
                        log.fatal("ERROR", e);
                    } finally {
                        log.debug("Deleting scanned images ");
                        for (File delFile : filesToDelete) {
                            delFile.delete();
                        }
                        filesToDelete = null;
                        tempNotes = null;
                        images = null;
                    }
                }
            } else {
                log.fatal("No device connected!!!");
                JOptionPane.showMessageDialog(parent, "No device connected!!!", "Error", JOptionPane.ERROR_MESSAGE);

            }

            setEnabled(true);
        } catch (Throwable exception) {
            JOptionPane.showMessageDialog(parent, exception.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            // exception.printStackTrace();
            log.fatal("ERROR", exception);
            // System.out.println("Failed, try again ...");
            // cancelAction.setEnabled(false);
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
        log.debug("Setup Scanner");
        List<? extends Device> devices = manager.listDevices();
        log.debug("Devices Found are:" + devices);
        if (devices == null || devices.isEmpty()) {
            return null;
        }
        String deviceName = (String) getInstance().getProperty("device");
        if (deviceName.equals("-")) {
            Device device = manager.selectDevice(parent);
            log.debug("NEW Device was selected:" + device.toString());
            getInstance().setProperty("device", device.toString());
            return device;
        } else {
            for (Device d : devices) {
                if (d.toString().equals(deviceName)) {
                    log.debug("OLD Device was found:" + d.toString());
                    return d;
                }
            }

            // if predefined scanner was not found
            log.debug("OLD Device was NOT found, setting new Device");
            getInstance().setProperty("device", "-");
            getInstance().setProperty("device.setup.ready", false);
            return setupDevice();
        }
    }

    private Scanner setupScanner(Device device) {
        log.info("The Device was found as Scanner");
        scanner = (Scanner) device;

        if (!getInstance().getBoolean("device.setup.ready")) {
            if (scanner.setupDevice(parent)) {
                BanksConfig.setupDeviceProperties(scanner);
            }
        } else {
            scanner.setMode(getInstance().getInt("scanner.mode"));
            scanner.setResolution(getInstance().getInt("scanner.resolution"));
            try {
                @SuppressWarnings("unchecked")
                List scanFrame = (ArrayList) getInstance().getProperty("scanner.scanFrame");
                int x = Integer.valueOf((String) scanFrame.get(0));
                int y = Integer.valueOf((String) scanFrame.get(1));
                int w = Integer.valueOf((String) scanFrame.get(2));
                int h = Integer.valueOf((String) scanFrame.get(3));
                scanner.setFrame(x, y, w, h);
            } catch (Exception e) {
                scanner.setFrame(0, 0, 0, 0);
                // e.printStackTrace();
                log.fatal("ERROR", e);
            }
        }
        return scanner;
    }
}
