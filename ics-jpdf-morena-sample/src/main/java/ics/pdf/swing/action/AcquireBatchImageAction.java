package ics.pdf.swing.action;

import static ics.pdf.swing.BanksConfig.getInstance;
import ics.pdf.swing.BanksConfig;
import ics.pdf.swing.TIFFManager;
import ics.pdf.swing.morena.ScanSession;
import ics.pdf.swing.util.IconUtil;

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

import com.qoppa.pdf.PDFException;
import com.qoppa.pdfNotes.PDFNotesBean;

import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;
import eu.gnome.morena.TransferListener;

public class AcquireBatchImageAction extends AbstractAction implements TransferListener {
    private static final long serialVersionUID = 1L;

    private Scanner scanner = null;
    private PDFNotesBean pDFNotesBean = null;
    private Manager manager = null;
    private Component parent = null;

    public AcquireBatchImageAction(PDFNotesBean pDFNotesBean, Manager manager, Component parent) {
        super("Scan...", IconUtil.getAcquireBatchImageActionIcon());
        putValue(SHORT_DESCRIPTION, "Scan Document");
        this.pDFNotesBean = pDFNotesBean;
        this.manager = manager;
        this.parent = parent;
    }

    public synchronized void actionPerformed(ActionEvent event) {
        try {
            Device device = setupDevice();
            System.out.println("Selected Device:" + device);
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
                    System.out.println("scanner.setResolution = " + resolution);
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
                        System.out.println("Scanned Pages [ " + (pageNo) + " ] page");
                        if (filesToDelete.isEmpty()) {
                            System.out.println("Morena Error: [" + session.getErrorCode() + "] "
                                    + session.getErrorMessage());
                            JOptionPane
                                    .showMessageDialog(parent, "Feeder is empty", "Error", JOptionPane.ERROR_MESSAGE);
                            setEnabled(true);
                            return;
                        }

                    } catch (Exception ex) { // check if error is related to empty ADF
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(parent, session.getErrorMessage().toString(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                        if (session.isEmptyFeeder()) {
                            System.out.println("No more sheets in the document feeder");
                        } else {
                            // ex.printStackTrace();
                            ex.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Scanner is Camera...");
                    JOptionPane.showMessageDialog(parent, "Scanner was found as Camera", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.out.println("Scanning completed...");

                if (images != null && !images.isEmpty()) {
                    PDFNotesBean tempNotes = null;
                    try {
                        System.out.println("Converting to TIFF...");
                        InputStream is = new ByteArrayInputStream(TIFFManager.compressTIFFFileList(imagesFileNames));
                        tempNotes = new PDFNotesBean();
                        File pdfFile = File.createTempFile("Morena_example", ".pdf");
                        tempNotes.loadDocument(is, "tif");
                        System.out.println("Converting to PDF...");
                        tempNotes.save(tempNotes, "pdf", pdfFile);
                        System.out.println("PDF was created [" + pdfFile.getAbsolutePath() + "] size: "
                                + (pdfFile.length() / 1024 / 1024) + " Mb");
                        tempNotes.invalidate();
                        is.close();

                        pDFNotesBean.loadPDF(new FileInputStream(pdfFile));
                        System.out.println("Finish Loading PDF File [" + pdfFile.getName() + "]");

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (PDFException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Deleting scanned images ");
                        for (File delFile : filesToDelete) {
                            delFile.delete();
                        }
                        filesToDelete = null;
                        tempNotes = null;
                        images = null;
                    }
                }
            } else {
                System.out.println("No device connected!!!");
                JOptionPane.showMessageDialog(parent, "No device connected!!!", "Error", JOptionPane.ERROR_MESSAGE);

            }

            setEnabled(true);
        } catch (Throwable exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(parent, exception.toString(), "Error", JOptionPane.ERROR_MESSAGE);
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
        System.out.println("Devices Found are:" + devices);
        if (devices == null || devices.isEmpty()) {
            return null;
        }
        String deviceName = (String) getInstance().getProperty("device");
        if (deviceName.equals("-")) {
            Device device = manager.selectDevice(parent);
            System.out.println("NEW Device was selected:" + device.toString());
            getInstance().setProperty("device", device.toString());
            return device;
        } else {
            for (Device d : devices) {
                if (d.toString().equals(deviceName)) {
                    System.out.println("OLD Device was found:" + d.toString());
                    return d;
                }
            }

            // if predefined scanner was not found
            System.out.println("OLD Device was NOT found, setting new Device");
            getInstance().setProperty("device", "-");
            getInstance().setProperty("device.setup.ready", false);
            return setupDevice();
        }
    }

    private Scanner setupScanner(Device device) {
        System.out.println("The Device was found as Scanner");
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
                e.printStackTrace();
            }
        }
        return scanner;
    }

}
