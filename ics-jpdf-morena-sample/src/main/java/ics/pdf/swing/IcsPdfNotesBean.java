package ics.pdf.swing;

import ics.pdf.swing.IcsOracleFormMessages.IcsOracleFormPropertiesMsg;
import ics.pdf.swing.action.AcquireBatchImageAction;
import ics.pdf.swing.action.SetupDeviceAction;
import ics.pdf.swing.action.SetupDevicePropertiesAction;
import ics.pdf.swing.util.FillLabels;
import ics.pdf.swing.util.IconUtil;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Date;

import javax.swing.JButton;

import com.qoppa.pdf.DocumentEvent;
import com.qoppa.pdf.IDocumentListener;
import com.qoppa.pdf.PDFException;
import com.qoppa.pdf.SigningInformation;
import com.qoppa.pdf.annotations.Annotation;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdf.annotations.IAnnotationFactory;
import com.qoppa.pdf.annotations.Text;
import com.qoppa.pdf.dom.IPDFDocument;
import com.qoppa.pdf.form.SignatureField;
import com.qoppa.pdfNotes.PDFNotesBean;
import com.qoppa.pdfNotes.settings.AnnotationTools;
import com.qoppa.pdfNotes.settings.SignatureTool;
import com.qoppa.pdfViewer.actions.IPDFActionHandler;

import eu.gnome.morena.Manager;

public class IcsPdfNotesBean extends PDFNotesBean implements IDocumentListener, IPDFActionHandler {
    private static final long serialVersionUID = 4331766079865005754L;

    public static String MODE_VIEW = "VIEW";
    public static String MODE_EDIT = "EDIT";
    public static String MODE_CREATE = "CREATE";

    private String userName;
    private Date currentDate;
    @SuppressWarnings("unused")
    private Manager manager;

    private String selectedMode = "---";

    private AcquireBatchImageAction acquireBatchImageAction;
    private SetupDeviceAction setupDeviceAction;
    private SetupDevicePropertiesAction setupDevicePropertiesAction;
    private JButton attachFileButton;

    public IcsPdfNotesBean(Manager manager) {
        this.manager = manager;
        getAnnotToolbar().getjbAttachFile().setVisible(false);

        attachFileButton = new JButton(IconUtil.getAttachementIcon());
        attachFileButton.setName("AttachFile");
        attachFileButton.setActionCommand("AttachFile");
        attachFileButton.addActionListener(this);
        FillLabels.setToolTipText(attachFileButton);
        getAnnotToolbar().add(attachFileButton);

        AnnotationTools.setFlatteningEnabled(false);

        getToolbar().getjbPrint().setEnabled(false);

        getSelectToolbar().getJbSnapShot().setVisible(false);
        getEditToolbar().getjbSave().setVisible(false);
        getAnnotToolbar().getjbSound().setVisible(false);

        acquireBatchImageAction = new AcquireBatchImageAction(this, manager, this);
        setupDeviceAction = new SetupDeviceAction(manager, this);
        setupDevicePropertiesAction = new SetupDevicePropertiesAction(manager, this);
        getToolbar().add(acquireBatchImageAction);
        getToolbar().add(setupDeviceAction);
        getToolbar().add(setupDevicePropertiesAction);

        FillLabels.fillPDFNotesBean(this);
        getAttachmentPanel().setActive(true);
        getAttachmentPanel().setPaneVisible(true);

        getCommentPanel().setActive(true);
        getCommentPanel().setPaneVisible(true);

    }

    @Override
    public void startEdit(Annotation annot, boolean useDefault, boolean isSticky) {
        // Call PDFNotesBean to set its own properties
        super.startEdit(annot, true, isSticky);
        System.out.println("Start Edit :" + annot.getCreator());

        if (annot instanceof Text) {
            Text t = (Text) annot;
            t.setModifiedDate(getCurrentDate());
        }

        // Set type writer text color. A typewriter annotation is just a FreeText annotation
        // with the intent set to TypeWriter.
        if (annot instanceof FreeText && ((FreeText) annot).isIntentTypeWriter()) {
            ((FreeText) annot).setTextColor(Color.red);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            System.out.println("---> ActionID: " + e.getID() + " ActionCommand: " + e.getActionCommand());

            IPDFDocument doc = getDocument();
            if (doc == null) {
                if (e.getActionCommand().equals("AttachFile")) {
                    try {
                        InputStream is = this.getClass().getClassLoader().getResourceAsStream("empty.pdf");
                        loadDocument(is, "pdf");
                        super.actionPerformed(new ActionEvent(this, 0, "AttachFile"));
                        getAttachmentPanel().setActive(true);
                        getAttachmentPanel().setPaneVisible(true);
                        return;
                    } catch (PDFException e2) {
                        e2.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } else {
                IAnnotationFactory factory = getDocument().getAnnotationFactory();

                if (e.getActionCommand().equals("open")) {
                    setDocument(null);
                } else if (e.getActionCommand().equals("Note")) {
                    Text t = factory.createText("test me", true, Text.ICON_KEY);
                    t.setModifiedDate(getCurrentDate());
                    t.setSubject("aloha... ha ha");
                    t.setState("state test");
                    t.setName("name test");
                    t.setLocked(true);
                    t.setBorderWidth(200);

                    // StickyNoteTool.setDefaultProperties(t);
                    // StickyNoteTool.setDefaultTransparency(ERROR);
                    // StickyNoteTool.setDefaultColor(Color.BLUE);
                    // StickyNoteTool.setShowPropDialog(true);
                    // StickyNoteTool.setToolSticky(true);
                    //

                    // AnnotationTools.setAuthorEditable(false);
                    // AnnotationTools.setContextMenuEnabled(false);
                    // AnnotationTools.setDeleteEnabled(false);
                    // AnnotationTools.setReviewEnabled(false);
                } else if (e.getActionCommand().equals("Save")) {
                    try {
                        getDocument().getDocumentInfo().setModifiedDate(getCurrentDate());
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

                        signDocument(signField, signInfo, getDocument().getFile());
                        // signDocument(signField);

                        saveDocument("d:\\final_output.pdf");
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return;
                    }
                } else if (e.getActionCommand().equals("AttachFile")) {
                    super.actionPerformed(new ActionEvent(this, 0, "AttachFile"));
                    getAttachmentPanel().setActive(true);
                    getAttachmentPanel().setPaneVisible(true);
                    return;
                }
            }
            super.actionPerformed(e);
        } catch (Exception exc) {
            System.err.println("exception on something");
        }
    }

    public void setProperties(IcsOracleFormPropertiesMsg properties) {
        System.out.println("NotesBean setProperties:" + properties);
        setPrintEnabled(properties.isPrint());
        setStampEnabled(properties.isStamp());
        setSignatureEnabled(properties.isSignature());
        setUserName(properties.getUserName());
        setCurrentDate(properties.getCurrentDate());
        setMode(properties.getMode());
        AnnotationTools.setDefaultAuthor(getUserName());
    }

    public String getMode() {
        return selectedMode;
    }

    public void setMode(String mode) {
        selectedMode = mode;
        setupDevicePropertiesAction.setEnabled(true);
        getThumbnailPanelNotes().enableEditing(true);
        AnnotationTools.setDeleteEnabled(true);

        if (mode.equals(MODE_EDIT)) {
            getToolbar().getjbOpen().setEnabled(false);
            getAnnotToolbar().setVisible(true);
            acquireBatchImageAction.setEnabled(false);
            setupDeviceAction.setEnabled(false);
            setupDevicePropertiesAction.setEnabled(false);
        } else if (mode.equals(MODE_CREATE)) {
            getToolbar().getjbOpen().setEnabled(true);
            getAnnotToolbar().setVisible(true);
            acquireBatchImageAction.setEnabled(true);
            setupDeviceAction.setEnabled(true);
            setupDevicePropertiesAction.setEnabled(true);
        } else { // MODE_VIEW
            acquireBatchImageAction.setEnabled(false);
            setupDeviceAction.setEnabled(false);
            setupDevicePropertiesAction.setEnabled(false);

            getToolbar().getjbOpen().setEnabled(false);
            getAnnotToolbar().setVisible(false);
            getThumbnailPanelNotes().enableEditing(false); //
            AnnotationTools.setDeleteEnabled(false);//
            AnnotationTools.setReviewEnabled(false);
            AnnotationTools.setContextMenuEnabled(false);
            SignatureTool.setAllowSign(false);
        }
    }

    public void setPrintEnabled(boolean flag) {
        getToolbar().getjbPrint().setEnabled(flag);
    }

    public void setStampEnabled(boolean flag) {
        getAnnotToolbar().getjbStamp().setVisible(flag);
    }

    public void setSignatureEnabled(boolean flag) {
        SignatureTool.setAllowSign(flag);
    }

    @Override
    public void documentChanged(DocumentEvent de) {
        try {
            if (de.getObject() instanceof Text) {
                System.out.println("Note was changed: " + de.getEventType());
                ((Text) de.getObject()).setModifiedDate(getCurrentDate());

                System.out.println("Note Creator: " + ((Text) de.getObject()).getContents());
                System.out.println("Note Creator: " + ((Text) de.getObject()).getCreator());
            }
        } catch (Exception exe) {
            System.err.println("docchange error");
        }
        super.documentChanged(de);
    }

    public String getUserName() {
        if (userName == null) {
            userName = "anonymous";
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCurrentDate() {
        if (currentDate == null) {
            currentDate = new Date();
        }
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public void deselectAnnotation(Annotation annot) {
        System.out.println("deselectAnnotation - Creator: " + annot.getCreator());
        if (!annot.getCreator().equals(getUserName())) {
            annot.setLocked(true);
            annot.setReadOnly(true);
        }
        super.deselectAnnotation(annot);
    }

    @Override
    public void selectAnnotation(Annotation annot) {
        System.out.println("selectAnnotation - Creator: " + annot.getCreator());
        System.out.println("selectAnnotation - Name: " + annot.getName());
        if (!annot.getCreator().equals(getUserName())) {
            annot.setLocked(true);
            annot.setReadOnly(true);
        }
        super.deselectAnnotation(annot);
    }

    // @Override
    // public void loadPDF(InputStream in) {
    // try {
    // super.loadPDF(in);
    // } catch (PDFException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // getAttachmentPanel().setActive(true);
    // getAttachmentPanel().setPaneVisible(true);
    // getCommentPanel().setActive(true);
    // getCommentPanel().setPaneVisible(true);
    //
    // }
}
