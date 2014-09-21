/**
 * This class implements a simple standalone program (JFrame) that overrides the jPDFNotes
 * saver to save a PDF file to a web server.  The intent of this program is to make it easier
 * to debug the server script by running an application on the client rather than an applet. 
 *
 */
package jPDFNotesSamples.upload;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.qoppa.pdfNotes.IPDFSaver;
import com.qoppa.pdfNotes.PDFNotesBean;

public class TestUpload extends JFrame implements IPDFSaver
{
    private JPanel jpContentPane = null;
    private PDFNotesBean NotesBean = null;

    public static void main (String [] args)
    {
        TestUpload test = new TestUpload();
        test.setVisible(true);
    }
    /**
     * This method initializes 
     * 
     */
    public TestUpload() {
    	super();
    	initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new Dimension(863, 559));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJpContentPane());
    		
    }

    public boolean save(PDFNotesBean notesBean, String docName, File pdfFile)
    {
        try
        {
            // Create URL to the document
            URL saveURL = new URL ("http://mydomain.com/upload/upload.php");
            FileUploadPOST upload = new FileUploadPOST ();
            boolean rc = upload.upload (notesBean, docName, saveURL);
            if (rc)
            {
                JOptionPane.showMessageDialog(this, "The PDF document has been saved to the server.");
            }
            return rc;
        }
        catch (MalformedURLException mue)
        {
            JOptionPane.showMessageDialog(this, mue.getMessage());
        }

        return true;
    }

    /**
     * This method initializes jpContentPane	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJpContentPane()
    {
        if (jpContentPane == null)
        {
            CardLayout cardLayout = new CardLayout();
            cardLayout.setHgap(10);
            cardLayout.setVgap(10);
            jpContentPane = new JPanel();
            jpContentPane.setLayout(cardLayout);
            jpContentPane.add(getNotesBean(), getNotesBean().getName());
        }
        return jpContentPane;
    }

    /**
     * This method initializes NotesBean	
     * 	
     * @return com.qoppa.pdfNotes.PDFNotesBean	
     */
    private PDFNotesBean getNotesBean()
    {
        if (NotesBean == null)
        {
            NotesBean = new PDFNotesBean();
            NotesBean.setName("NotesBean");
            NotesBean.setPDFSaver(this);
        }
        return NotesBean;
    }
}  //  @jve:decl-index=0:visual-constraint="13,4"
