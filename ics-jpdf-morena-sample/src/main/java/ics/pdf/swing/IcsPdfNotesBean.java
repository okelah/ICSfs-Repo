package ics.pdf.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdf.PDFPassword;
import com.qoppa.pdf.SigningInformation;
import com.qoppa.pdf.annotations.Annotation;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdf.annotations.IAnnotationFactory;
import com.qoppa.pdf.annotations.Text;
import com.qoppa.pdf.dom.IPDFDocument;
import com.qoppa.pdf.form.SignatureField;
import com.qoppa.pdfNotes.PDFNotesBean;
import com.qoppa.pdfNotes.settings.AnnotationTools;
import com.qoppa.pdfNotes.settings.StickyNoteTool;

public class IcsPdfNotesBean extends PDFNotesBean {
    private static Logger log = LogManager.getLogger(IcsPdfNotesBean.class.getName());
    private static final long serialVersionUID = 4331766079865005754L;
    private static String testAction = "TEST-ACTION";

    public IcsPdfNotesBean() {
        JButton jbRedCircle = new JButton("test Action");
        jbRedCircle.setActionCommand(testAction);
        jbRedCircle.addActionListener(this);
        getAnnotToolbar().add(jbRedCircle);
        // getEditToolbar().add(new JButton("TEST"));
    }

    @Override
    public void startEdit(Annotation annot, boolean useDefault, boolean isSticky) {
        // Call PDFNotesBean to set its own properties
        super.startEdit(annot, true, isSticky);

        // Set type writer text color. A typewriter annotation is just a FreeText annotation
        // with the intent set to TypeWriter.
        if (annot instanceof FreeText && ((FreeText) annot).isIntentTypeWriter()) {
            ((FreeText) annot).setTextColor(Color.red);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log.debug("---> ActionID: " + e.getID() + " ActionCommand: " + e.getActionCommand());
        IPDFDocument doc = getDocument();

        String oldstring = "2011-01-18 00:00:00.0";
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(oldstring);
        } catch (ParseException e1) {
            log.fatal("ERROR", e1);
            return;
        }

        if (doc != null) {
            IAnnotationFactory factory = getDocument().getAnnotationFactory();

            if (e.getActionCommand().equals(testAction)) {

                try {
                    addSignatureField();
                    getDocument().getDocumentInfo().setAuthor("test-user");
                    getDocument().getDocumentInfo().setCreationDate(date);
                    getDocument().getDocumentInfo().setModifiedDate(date);
                    getDocument().getDocumentInfo().setTitle("test-title");
                    getDocument().getDocumentInfo().setSubject("test-subject");
                    getDocument().getDocumentInfo().setKeywords("keyword-1 keyword-2");
                    getDocument().getDocumentInfo().setProducer("test-producer");
                    getDocument().getDocumentInfo().setCustomProperty("test-CustomProperty", "Custom-Property");

                    setPasswordHandler(new PDFPassword("123456"));

                    getDocument().getPDFPermissions().getPasswordPermissions().setChangeDocumentAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions().setModifyAnnotsAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions().setPrintAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions().setAssembleDocumentAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions().setExtractTextGraphicsAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions()
                    .setExtractTextGraphicsForAccessibilityAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions().setFillFormFieldsAllowed(false);
                    getDocument().getPDFPermissions().getPasswordPermissions().setPrintHighResAllowed(false);

                    save();
                } catch (PDFException e1) {
                    e1.printStackTrace();
                }

            } else if (e.getActionCommand().equals("Note")) {
                Text t = factory.createText("test me", true, Text.ICON_KEY);
                t.setModifiedDate(date);
                t.setSubject("aloha... ha ha");
                t.setState("state test");
                t.setName("name test");
                t.setLocked(true);
                t.setBorderWidth(200);

                StickyNoteTool.setDefaultProperties(t);
                StickyNoteTool.setDefaultTransparency(ERROR);
                StickyNoteTool.setDefaultColor(Color.BLUE);
                StickyNoteTool.setShowPropDialog(true);
                StickyNoteTool.setToolSticky(true);

                AnnotationTools.setDefaultAuthor("ابراهيم");
                AnnotationTools.setAuthorEditable(false);
                AnnotationTools.setContextMenuEnabled(false);
                AnnotationTools.setDeleteEnabled(false);
                AnnotationTools.setReviewEnabled(false);

                // AnnotationTools.setSnapToContent(true);
            } else if (e.getActionCommand().equals("Save")) {
                try {

                    getDocument().getDocumentInfo().setModifiedDate(date);

                    getDocument().getDocumentInfo().setSubject("testing subject");

                    // Create a signature field on the first page
                    Rectangle2D signBounds = new Rectangle2D.Double(36, 36, 144, 48);
                    SignatureField signField = addSignatureField("signature", signBounds, 0);

                    // load keystore.pfx file from system
                    File pfxFile = new File("D:\\ibrahim.pfx");

                    // Load the keystore that contains the digital id to use in signing
                    // FileInputStream pkcs12Stream = new FileInputStream(
                    // "http://www.qoppa.com/files/pdfnotes/guide/sourcesamples/keystore.pfx");
                    FileInputStream pkcs12Stream = new FileInputStream(pfxFile);

                    // InputStream is = new
                    // URL("http://www.qoppa.com/files/pdfnotes/guide/sourcesamples/keystore.pfx")
                    // .openStream();

                    KeyStore store = KeyStore.getInstance("PKCS12");
                    store.load(pkcs12Stream, "123456".toCharArray());
                    pkcs12Stream.close();

                    // store.load(is, "store_pwd".toCharArray());
                    // is.close();

                    // Create signing information
                    SigningInformation signInfo = new SigningInformation(store, "ibrahim", "123456");

                    // Apply digital signature
                    signDocument(signField, signInfo, new File("d:\\temp_output.pdf"));
                    // signDocument(signField);

                    saveDocument("d:\\final_output.pdf");
                } catch (Exception e2) {
                    log.fatal("ERROR", e2);
                    return;
                }
            }
        }
        super.actionPerformed(e);
    }
}
