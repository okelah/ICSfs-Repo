package ics.pdf.swing;

import ics.pdf.swing.action.AcquireBatchImageAction;
import ics.pdf.swing.action.SetupDeviceAction;
import ics.pdf.swing.action.SetupDevicePropertiesAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import oracle.forms.properties.ID;
import oracle.forms.ui.VBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdf.annotations.Circle;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdf.annotations.IAnnotationFactory;
import com.qoppa.pdf.dom.IPDFDocument;
import com.qoppa.pdfNotes.PDFNotesBean;
import com.qoppa.pdfNotes.settings.FreeTextTool;

import eu.gnome.morena.Configuration;
import eu.gnome.morena.Manager;

/**
 * @author Ibrahim Sawalha
 *
 */
public class PdfNotesVBean extends VBean implements ActionListener {

    private static Logger log = LogManager.getLogger(PdfNotesVBean.class.getName());

    private PDFNotesBean pDFVBean = null;
    private static PdfNotesVBean sf = null;
    private static String fileName = null;
    private static StringBuffer sb = new StringBuffer();
    private static String storeSave = null;
    private static int position = 0;

    private final static String FREETEXT_CORRECT = "FreeTextCorrect";
    private final static String STAMP_CORRECT = "StampCorrect";
    private final static String RED_CIRCLE = "RedCircle";

    private final static String PRINT_ENABLED = "printEnabled";

    public final static ID SetPrint = ID.registerProperty("SET_PRINT");

    private Manager manager = null;

    public void addText(String value) {
        System.out.println("addText");
        sb.append(value);
    }

    //
    // display the whole text
    //
    public void showPDF() {
        log.debug("showPDF");
        BASE64Decoder b64dc = new BASE64Decoder();
        byte[] b;
        try {
            b = b64dc.decodeBuffer(sb.toString());
            // b = b64dc.decodeBuffer( new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
            getPDFNotes().loadPDF(new ByteArrayInputStream(b));
        } catch (IOException e) {
            // TODO
        } catch (PDFException e) {
            // TODO
        }
    }

    public String getInitSave() {
        System.out.println("getLength");
        BASE64Encoder b64en = new BASE64Encoder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.out.println("baos");
            getPDFNotes().saveDocument(baos);
            System.out.println(baos.size());

            storeSave = b64en.encodeBuffer(baos.toByteArray());
            position = 0;
            System.out.println("test.length" + storeSave.length());
        } catch (IOException e) {
            // TODO
        } catch (PDFException e) {
            // TODO
        }

        return "" + storeSave.length();
    }

    public String getChunk() {
        System.out.println("getChunk");
        String retstr;
        if (storeSave.length() > position + 32000) {
            retstr = storeSave.substring(position, position + 32000);
        } else {
            retstr = storeSave.substring(position);
        }
        position = position + 32000;
        return (retstr);
    }

    public static void main(String[] args) {
        JFrame jf = new JFrame("Oracle Forms Demo... ");

        jf.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        jf.setSize((int) Math.min(1024, dm.getWidth() * 0.90), (int) Math.min(768, dm.getHeight() * 0.90));
        jf.setLocationRelativeTo(null);

        sf = new PdfNotesVBean();
        jf.add(sf);
        jf.setVisible(true);

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-help") || arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-?")) {
                System.out.println("java com.sun.awc.PDFViewer [flags] [file]");
                System.out.println("flags: [-noThumb] [-help or -h or -?]");
                System.exit(0);
            } else {
                fileName = arg;
            }
        }
        System.out.println(fileName);

        jf.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                sf.setLocation(10, 10);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                sf.setLocation(10, 10);
                // Load an initial document
                /*
                 * if (fileName == null) { System.out.println("Help!"); } else { sf.loadDocument(fileName); }
                 */
                FileInputStream fin;
                try {
                    fin = new FileInputStream("c://temp//oid.txt");
                    int ch;
                    while ((ch = fin.read()) != -1) {
                        sf.sb.append((char) ch);
                    }
                    fin.close();
                } catch (FileNotFoundException f) {
                    // TODO
                } catch (IOException f) {
                    // TODO
                }
                sf.showPDF();
            }
        });
    }

    /**
     * This method initializes
     *
     */
    public PdfNotesVBean() {
        Configuration.setLogLevel(java.util.logging.Level
            .parse(BanksConfig.getInstance().getString("morena.log.level")));
        @SuppressWarnings("unchecked")
        ArrayList<String> deviceTypes = (ArrayList<String>) BanksConfig.getInstance()
                .getProperty("morena.device.types");
        for (String deviceType : deviceTypes) {
            Configuration.addDeviceType(deviceType, true);
        }
        setLookAndFeel();
        manager = Manager.getInstance();
        getPDFNotes().setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

        JButton ftCorrect = new JButton("Free Text");
        ftCorrect.setActionCommand(FREETEXT_CORRECT);
        ftCorrect.addActionListener(this);
        getPDFNotes().getAnnotToolbar().add(ftCorrect);

        JButton rsCorrect = new JButton("Stamp");
        rsCorrect.setActionCommand(STAMP_CORRECT);
        rsCorrect.addActionListener(this);
        getPDFNotes().getAnnotToolbar().add(rsCorrect);

        // Red Circle
        JButton jbRedCircle = new JButton("RC");
        jbRedCircle.setActionCommand(RED_CIRCLE);
        jbRedCircle.addActionListener(this);
        getPDFNotes().getAnnotToolbar().add(jbRedCircle);
        getPDFNotes().revalidate();

        JButton jbPrintEnabled = new JButton("Enable Printing?");
        jbPrintEnabled.setActionCommand(PRINT_ENABLED);
        jbPrintEnabled.addActionListener(this);
        getPDFNotes().getAnnotToolbar().add(jbPrintEnabled);

        getPDFNotes().getToolbar().add(new AcquireBatchImageAction(getPDFNotes(), manager, this));
        getPDFNotes().getToolbar().add(new SetupDeviceAction(manager, this));
        getPDFNotes().getToolbar().add(new SetupDevicePropertiesAction(manager, this));

        setLayout(new BorderLayout());

        add(getPDFNotes(), BorderLayout.CENTER);
    }

    /**
     * Open a local file, given a string filename
     *
     * @param name the name of the file to open
     */
    public void loadDocument(String loadDoc) {
        if (loadDoc.startsWith("http:")) {
            try {
                getPDFNotes().loadPDF(new URL(loadDoc));
            } catch (PDFException e) {
                log.fatal("ERROR", e);
                System.exit(1);
            } catch (MalformedURLException e) {
                log.fatal("ERROR", e);
                System.exit(1);
            }
        } else {
            try {
                getPDFNotes().loadPDF(loadDoc);
            } catch (PDFException e) {
                log.fatal("ERROR", e);
                System.exit(1);
            }
        }
    }

    @Override
    public boolean setProperty(ID property, Object value) {
        if (property == SetPrint) {
            String label = value.toString().trim();
            if (label.equalsIgnoreCase("true")) {
                getPDFNotes().getToolbar().getjbPrint().setEnabled(true);
            } else {
                getPDFNotes().getToolbar().getjbPrint().setEnabled(false);
            }

            return true;
        } else {
            return super.setProperty(property, value);
        }
    }

    private static void setLookAndFeel() {
        // Set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (UIManager.getSystemLookAndFeelClassName() != null
                    && UIManager.getSystemLookAndFeelClassName().toLowerCase().indexOf("windows") != -1) {
                UIManager.put("TextArea.font", UIManager.get("TextField.font"));
            }
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable tt) {
                // ignore
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("---> " + e.getActionCommand());
        IPDFDocument doc = getPDFNotes().getDocument();

        if (doc != null) {
            IAnnotationFactory factory = doc.getAnnotationFactory();

            if (e.getActionCommand() == FREETEXT_CORRECT) {
                FreeTextTool.setShowPropDialog(false);
                FreeText correctAnnot = factory.createFreeText("صحيح");
                getPDFNotes().startEdit(correctAnnot, false, false);
            } else if (e.getActionCommand() == STAMP_CORRECT) {
                // getPDFNotes().startEdit(factory.createRubberStamp("صحيح", Color.blue), false, false);

                // BufferedImage image;
                try {
                    InputStream resource = this.getClass().getClassLoader().getResourceAsStream("check.png");
                    BufferedImage image = ImageIO.read(resource);
                    // getPDFNotes().startEdit(factory.createRubberStamp(image), true);
                    getPDFNotes().startEdit(factory.createRubberStamp(image), true, true);
                } catch (IOException e1) {
                    log.fatal("ERROR", e1);
                }

            } else if (e.getActionCommand() == RED_CIRCLE) {
                Circle redCircle = factory.createCircle(null);
                redCircle.setColor(Color.red);
                redCircle.setInternalColor(Color.blue);
                getPDFNotes().startEdit(redCircle, false, false);
            } else if (e.getActionCommand() == PRINT_ENABLED) {
                if (getPDFNotes().getToolbar().getjbPrint().isEnabled()) {
                    getPDFNotes().getToolbar().getjbPrint().setEnabled(false);
                } else {
                    getPDFNotes().getToolbar().getjbPrint().setEnabled(true);
                }
            }
        }
    }

    private PDFNotesBean getPDFNotes() {
        if (pDFVBean == null) {
            pDFVBean = new IcsPdfNotesBean();
        }
        return pDFVBean;
    }
}
