/*
 * Created on Jan 17, 2008
 *
 */
package jPDFNotesSamples.upload;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdf.PDFPassword;
import com.qoppa.pdfNotes.IPDFSaver;
import com.qoppa.pdfNotes.PDFNotesBean;

public class AppletWithUpload extends JApplet implements IPDFSaver
{
    public final static String STRING_FALSE = "False";
    public final static String NUMBER_FALSE = "0";
    public final static String STRING_TRUE = "True";
    public final static String NUMBER_TRUE = "1";
    
    private JPanel jPanel = null;
    private PDFNotesBean pdfEditor = null;
    
    /**
     * This method initializes 
     * 
     */
    public AppletWithUpload() 
    {
        super();
    }
    /**
     * This method initializes this
     * 
     * @return void
     */
    public void init()
    {
        this.setContentPane(getJPanel());
        getPDFEditor().setPDFSaver(this);
    }
    
    /**
     * Sets the open button in the toolbar to visible / not visible.
     * 
     * @param visible
     */
    public void showOpenButton (boolean visible)
    {
        getPDFEditor().getToolbar().getjbOpen().setVisible (visible);
    }
    
    /**
     * Sets the save button in the toolbar to visible / not visible
     * 
     * @param visible
     */
    public void showSaveButton (boolean visible)
    {
        getPDFEditor().getEditToolbar().getjbSave().setVisible(visible);
    }

    public void start()
    {
        // Set open button visibility according to the BrowseAllowed flag.
        showOpenButton (toBoolean (getParameter("BrowseAllowed")));

        // Set print button visibility according to the PrintAllowed flag.
        getPDFEditor().getToolbar().getjbPrint().setVisible (toBoolean (getParameter ("PrintAllowed")));
        
        // Set save button visibility according to the SaveAllowed flag
        getPDFEditor().getEditToolbar().getjbSave().setVisible(toBoolean(getParameter("SaveAllowed")));
        
        // Set a custom password handler, if there is one in the HTML
        String password = getParameter("PDFPassword");
        if (password != null && password.trim().length() > 0)
        {
            PDFPassword pwd = new PDFPassword (password);
            getPDFEditor().setPasswordHandler(pwd);
        }
        else
        {
            getPDFEditor().setPasswordHandler(getPDFEditor());
        }

        // Load document on start
        String urlString = getParameter("url");
        if (urlString != null && urlString.trim ().length() > 0)
        {
            try
            {
                // Create URL to the document
                URL url;
                if (urlString.startsWith("http://"))
                {
                    url = new URL (urlString);
                }
                else
                {
                    url = new URL (getDocumentBase(), urlString);
                }
                
                // Load the document
                getPDFEditor().loadPDF(url);
            }
            catch (MalformedURLException mue)
            {
                JOptionPane.showMessageDialog(AppletWithUpload.this, mue.getMessage());
            }
            catch (PDFException pdfE)
            {
                JOptionPane.showMessageDialog(AppletWithUpload.this, pdfE.getMessage());
            }
        }
    }
    
    /**
     * This method initializes jPanel   
     *  
     * @return javax.swing.JPanel   
     */    
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new CardLayout());
            jPanel.add(getPDFEditor(), getPDFEditor().getName());
        }
        return jPanel;
    }
    /**
     * This method initializes PDFViewer    
     *  
     * @return com.qoppa.pdfViewer.PDFViewer    
     */    
    private PDFNotesBean getPDFEditor() {
        if (pdfEditor == null) {
            pdfEditor = new PDFNotesBean();
            pdfEditor.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray,1));
            pdfEditor.setName("pdfEditor");
        }
        return pdfEditor;
    }
    
    private boolean toBoolean (String str)
    {
        if (str == null || str.equalsIgnoreCase(STRING_TRUE) || str.equalsIgnoreCase(NUMBER_TRUE))
        {
            return true;
        }
        return false;
    }
    
    public boolean save(PDFNotesBean notesBean, String docName, File pdfFile)
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String saveURLString = getParameter("saveurl");
        try
        {
            // Create URL to the document
            URL saveURL;
            if (saveURLString.startsWith("http://"))
            {
                saveURL = new URL (saveURLString);
            }
            else
            {
                saveURL = new URL (getDocumentBase(), saveURLString);
            }

            // Upload the file
            FileUploadPOST upload = new FileUploadPOST ();
            boolean rc = upload.upload (notesBean, docName, saveURL);
            if (rc)
            {
                JOptionPane.showMessageDialog(this, "The PDF document has been saved to the server.");
            }
            setCursor (Cursor.getDefaultCursor());
            return rc;
        }
        catch (MalformedURLException mue)
        {
            JOptionPane.showMessageDialog(this, mue.getMessage());
        }

        setCursor (Cursor.getDefaultCursor());

        return true;
    }

    public void loadPDF (String pdfURLString)
    {
        try
        {
            // Form URL object
            URL pdfURL;
            if (pdfURLString.startsWith("http://"))
            {
                pdfURL = new URL (pdfURLString);
            }
            else
            {
                pdfURL = new URL (getDocumentBase(), pdfURLString);
            }

            // Load the PDF in the swing thread
            if (SwingUtilities.isEventDispatchThread())
            {
                getPDFEditor().loadPDF(pdfURL);
            }
            else
            {
                // Create PDF loader and call it in the Swing event thread
                LoadPDFRunnable loader = new LoadPDFRunnable (pdfURL);
                SwingUtilities.invokeAndWait(loader);
            }
        }
        catch (MalformedURLException mURL)
        {
            displayError ("Invalid PDF URL: " + mURL.getMessage());
        }
        catch (Throwable t)
        {
            if (t.getCause() instanceof PDFException)
            {
                displayError (t.getCause().getMessage());
            }
            else
            {
                displayError ("Error loading PDF: " + t.getMessage());
            }
        }
    }
    
    private class LoadPDFRunnable implements Runnable
    {
        private URL m_PDFURL;
        
        public LoadPDFRunnable (URL pdfURL)
        {
            m_PDFURL = pdfURL;
        }

        public void run()
        {
            try
            {
                getPDFEditor().loadPDF(m_PDFURL);
            }
            catch (PDFException pdfE)
            {
                displayError (pdfE.getMessage());
            }
            catch (Throwable t)
            {
                displayError ("Error loading PDF: " + t.getMessage());
            }
        }
    }
    
    private void displayError (String errorMsg)
    {
        JOptionPane.showMessageDialog(AppletWithUpload.this, errorMsg);
    }
}
