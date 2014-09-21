package jPDFNotesSamples;

import java.awt.CardLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdfNotes.PDFNotesBean;

public class PDFNotesApplet extends JApplet 
{
	public final static String STRING_FALSE = "False";
	public final static String NUMBER_FALSE = "0";
	public final static String STRING_TRUE = "True";
	public final static String NUMBER_TRUE = "1";

	private JPanel jPanel = null;
	private PDFNotesBean notesBean = null;

    /**
	 * This method initializes this applet.
	 * 
	 * @return void
	 */
	public void init()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
        this.setContentPane(getJPanel());
	}
	
	/**
	 * The applet start method.  This implementation uses three parameters that can be passed
	 * from the HTML:<br><br>
	 * BrowseAllowed - Turn the open button visible / invisible on the toolbar (1 | 0) or (true | false)
	 * PrintAllowed - Turn the print button visible / invisible on the toolbar (1 | 0) or (true | false)
	 * 
	 * URL - The URL of a PDF file to load on startup
	 * 
	 */
	public void start()
	{
	    // Set open button visibility according to the BrowseAllowed flag.
	    getPDFNotesBean().getToolbar().getjbOpen().setVisible (toBoolean (getParameter("BrowseAllowed")));

	    // Set print button visibility according to the PrintAllowed flag.
	    getPDFNotesBean().getToolbar().getjbPrint().setVisible (toBoolean (getParameter ("PrintAllowed")));

	    final String url = getParameter("url");
		if (url != null && url.trim ().length() > 0)
		{
		    // Load the PDF
		    loadPDF (url);
		}
	}
	
	/**
	 * The applet's stop method.  This method is called when the user leaves the applet page.  The
	 * method clears the document currently loaded in the PDFNotesBean.
	 * 
	 * @see java.applet.Applet#stop()
	 */
	public void stop ()
	{
	    // Clear the current document
	    getPDFNotesBean().setDocument(null);
	}

	
    /**
     * Loads a PDF document into the PDFNotesBean.  This method loads the PDF document
     * inside a privileged action so that it can be called from Javascript.  Otherwise,
     * the method would throw a security exception because Javascript does not get much
     * permissions to start with.
     * 
     * @param pdfURLString The URL to the PDF document.
     * 
     * @return The number of pages in the document
     */
    public int loadPDF (final String pdfURLString)
    {
        // We need to enclose this in a privileged action so that it
        // can execute properly when called from Javascript
        Object rc = AccessController.doPrivileged(new java.security.PrivilegedAction() 
        {
            public Object run()
            {
                int pageCount = privLoadPDF (pdfURLString);
                return new Integer (pageCount);
            }
        });
        
        if (rc != null && rc instanceof Integer)
        {
            return ((Integer)rc).intValue();
        }
        return 0;
    }
    
    /**
     * Internal implementation of the load PDF method.  This method is safe
     * to call from the applet context, but not from Javascript.  If calling
     * from Javascript, then loadPDF() should be used instead.
     * 
     * @param pdfURLString The URL to the PDF document.
     * 
     * @return The number of pages in the document
     */
    private int privLoadPDF (String pdfURLString)
    {
        try
        {
            // Form URL object
            final URL pdfURL = new URL (pdfURLString);
            
            // Load the PDF in the swing thread
            if (SwingUtilities.isEventDispatchThread())
            {
                getPDFNotesBean().loadPDF(pdfURL);
            }
            else
            {
                // Call the loadPDF method in the Swing event thread
                SwingUtilities.invokeAndWait(new Runnable ()
                {
                    public void run()
                    {
                        try
                        {
                            getPDFNotesBean().loadPDF(pdfURL);
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
                });
            }
            
            return getPDFNotesBean().getPageCount();
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
        
        return 0;
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
			jPanel.add(getPDFNotesBean(), getPDFNotesBean().getName());
		}
		return jPanel;
	}
	/**
	 * This method initializes the notesBean.	
	 * 	
	 * @return com.qoppa.pdfNotes.PDFNotesBean
	 */    
	public PDFNotesBean getPDFNotesBean() {
		if (notesBean == null) {
		    notesBean = new PDFNotesBean();
		    notesBean.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray,1));
		    notesBean.setName("pdfEditor");
		}
		return notesBean;
	}
	
	private boolean toBoolean (String str)
	{
	    if (str == null || str.equalsIgnoreCase(STRING_TRUE) || str.equalsIgnoreCase(NUMBER_TRUE))
	    {
	        return true;
	    }
	    return false;
	}
	
    private void displayError (String errorMsg)
    {
        JOptionPane.showMessageDialog(this, errorMsg);
    }
}
