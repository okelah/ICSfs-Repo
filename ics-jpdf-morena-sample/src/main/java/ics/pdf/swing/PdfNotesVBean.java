package ics.pdf.swing;

import ics.pdf.swing.IcsOracleFormMessages.IcsOracleFormPropertiesMsg;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdf.annotations.Circle;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdf.annotations.IAnnotationFactory;
import com.qoppa.pdf.dom.IPDFDocument;
import com.qoppa.pdfNotes.settings.FreeTextTool;

import eu.gnome.morena.Configuration;
import eu.gnome.morena.Manager;

/**
 * @author Ibrahim Sawalha
 *
 */
public class PdfNotesVBean extends Applet implements ActionListener {
    private static final long serialVersionUID = -1074599859191345830L;

    private IcsPdfNotesBean pDFVBean = null;
    protected static StringBuffer sb = new StringBuffer();
    private static String storeSave = null;
    private static int position = 0;

    private final static String FREETEXT_CORRECT = "FreeTextCorrect";
    private final static String STAMP_CORRECT = "StampCorrect";
    private final static String RED_CIRCLE = "RedCircle";

    private String userLanguage;

    private Manager manager = null;
    private String version = "ICSfs-jPDFNotes 1.2.0";

    public void addText(String value) {
        sb.append(value);
    }

    public void showPDF(String params) {
        System.out.println("showPDF(String)-ICSfs jPDF build version:" + version);
        IcsOracleFormPropertiesMsg message = IcsOracleFormMessages.decypherIcsOracleFormPrpertiesMsg(params);
        System.out.println("incoming properties msg:" + params);
        System.out.println("properties object:" + message);
        Locale.setDefault(new Locale(message.getLanguage()));

        setUserLanguage(message.getLanguage());

        try {
            setPDFNotes((IcsPdfNotesBean) this.getComponent(0));
            remove(this.getComponent(0));
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore
        }

        getPDFNotes().setProperties(message);
        getPDFNotes().setDocument(null);

        BASE64Decoder b64dc = new BASE64Decoder();
        byte[] b;
        try {
            b = b64dc.decodeBuffer(sb.toString());
            if (b.length > 0 && !message.getMode().equals(IcsPdfNotesBean.MODE_CREATE)) {
                // b = b64dc.decodeBuffer(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
                getPDFNotes().loadPDF(new ByteArrayInputStream(b));
            } else {
                System.err.println(" --- No Files was loaded in the application");
                // TODO: uncomment for testing
                // getPDFNotes().loadPDF("d:/test.pdf");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        removeAll();
        this.add(getPDFNotes(), BorderLayout.CENTER, 0);
        repaint();
    }

    public String getInitSave() {
        System.out.println("getInitSave...");
        BASE64Encoder b64en = new BASE64Encoder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            getPDFNotes().saveDocument(baos);
            storeSave = b64en.encodeBuffer(baos.toByteArray());
            position = 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return "" + storeSave.length();
    }

    public String getChunk() {
        String retstr;
        if (storeSave.length() > position + 32000) {
            retstr = storeSave.substring(position, position + 32000);
        } else {
            retstr = storeSave.substring(position);
        }
        position = position + 32000;
        return (retstr);
    }

    /**
     * This method initializes
     *
     */
    public PdfNotesVBean() {
        System.out.println("PdfNotesVBean() ... init version:" + version);
        try {
            BanksConfig.loadConfiguration();
            Configuration.setLogLevel(java.util.logging.Level.parse(BanksConfig.getInstance().getString(
                "morena.log.level")));
            @SuppressWarnings("unchecked")
            ArrayList<String> deviceTypes = (ArrayList<String>) BanksConfig.getInstance().getProperty(
                "morena.device.types");
            for (String deviceType : deviceTypes) {
                Configuration.addDeviceType(deviceType, true);
            }
            manager = Manager.getInstance();
            setLookAndFeel();
            setLayout(new BorderLayout());
            Locale.setDefault(new Locale("en"));
            // uncomment this for testing only
            // showPDF("MODE=edit,PRINT=1,STAMP=1,SIGN=1,USER=test user name, LANG=ar, current_date=01/02/2012");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDocument(String loadDoc) {
        System.out.println("loadDocument()...");
        if (loadDoc.startsWith("http:")) {
            try {
                getPDFNotes().loadPDF(new URL(loadDoc));
            } catch (PDFException e) {

                e.printStackTrace();
                System.exit(1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            try {
                getPDFNotes().loadPDF(loadDoc);
            } catch (PDFException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void setLookAndFeel() {
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
        System.out.println("VBean actionPerformed---> " + e.getActionCommand());
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
                    getPDFNotes().startEdit(factory.createRubberStamp(image), true, true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } else if (e.getActionCommand() == RED_CIRCLE) {
                Circle redCircle = factory.createCircle(null);
                redCircle.setColor(Color.red);
                redCircle.setInternalColor(Color.blue);
                getPDFNotes().startEdit(redCircle, false, false);
            }
        }
    }

    private void setPDFNotes(IcsPdfNotesBean pDFVBean) {
        this.pDFVBean = pDFVBean;
    }

    private IcsPdfNotesBean getPDFNotes() {
        if (pDFVBean == null) {
            System.out.println("getPDFNotes Init... pDFVBean == null");
            pDFVBean = new IcsPdfNotesBean(manager);

            // JButton ftCorrect = new JButton("Free Text");
            // ftCorrect.setActionCommand(FREETEXT_CORRECT);
            // ftCorrect.addActionListener(this);
            // pDFVBean.getAnnotToolbar().add(ftCorrect);
            //
            // JButton rsCorrect = new JButton("Stamp");
            // rsCorrect.setActionCommand(STAMP_CORRECT);
            // rsCorrect.addActionListener(this);
            // pDFVBean.getAnnotToolbar().add(rsCorrect);
            //
            // JButton jbRedCircle = new JButton("RC");
            // jbRedCircle.setActionCommand(RED_CIRCLE);
            // jbRedCircle.addActionListener(this);
            // pDFVBean.getAnnotToolbar().add(jbRedCircle);
            // pDFVBean.revalidate();
        }

        return pDFVBean;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public void setUserLanguage(String userLanguage) {
        this.userLanguage = userLanguage;
    }

    public void exitApplet() {
        System.exit(0);
    }
}
