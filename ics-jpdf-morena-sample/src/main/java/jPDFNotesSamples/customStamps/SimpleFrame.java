/*
 * Created on Sep 24, 2008
 *
 */
package jPDFNotesSamples.customStamps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.qoppa.pdf.annotations.Circle;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdf.annotations.IAnnotationFactory;
import com.qoppa.pdf.dom.IPDFDocument;
import com.qoppa.pdfNotes.PDFNotesBean;
import com.qoppa.pdfNotes.settings.FreeTextTool;

public class SimpleFrame extends JFrame implements ActionListener
{
    private JPanel jPanel = null;
    private PDFNotesBean m_NotesBean = null;
    
    private final static String FREETEXT_CORRECT = "FreeTextCorrect";
    private final static String STAMP_CORRECT = "StampCorrect";
    private final static String RED_CIRCLE = "RedCircle";

    public static void main (String [] args)
    {
        SimpleFrame sf = new SimpleFrame();
        sf.setVisible(true);
    }
    /**
     * This method initializes 
     * 
     */
    public SimpleFrame() 
    {
    	super();
    	initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() 
    {
        this.setBounds(new Rectangle(0, 0, 800, 600));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJPanel());
    	this.setTitle("Qoppa Software - jPDFNotes Sample");
    	this.setLocationRelativeTo(null);
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel()
    {
        if (jPanel == null)
        {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getNotesBean(), BorderLayout.CENTER);
        }
        return jPanel;
    }

    /**
     * This method initializes PDFViewerBean	
     * 	
     * @return com.qoppa.pdfViewer.PDFViewerBean	
     */
    private PDFNotesBean getNotesBean()
    {
        if (m_NotesBean == null)
        {
            m_NotesBean = new MyNotesBean();
            
            // Create custom buttons
            JButton ftCorrect = new JButton ("FT");
            ftCorrect.setActionCommand(FREETEXT_CORRECT);
            ftCorrect.addActionListener(this);
            m_NotesBean.getAnnotToolbar().add (ftCorrect);
            
            JButton rsCorrect = new JButton ("RS");
            rsCorrect.setActionCommand(STAMP_CORRECT);
            rsCorrect.addActionListener(this);
            m_NotesBean.getAnnotToolbar().add (rsCorrect);
            
            // Red Circle
            JButton jbRedCircle = new JButton ("RC");
            jbRedCircle.setActionCommand(RED_CIRCLE);
            jbRedCircle.addActionListener(this);
            m_NotesBean.getAnnotToolbar().add (jbRedCircle);
        }
        return m_NotesBean;
    }
    
    public void actionPerformed(ActionEvent e)
    {
    	IPDFDocument doc = m_NotesBean.getDocument();
    	
    	if(doc != null)
    	{
    		IAnnotationFactory factory = doc.getAnnotationFactory();
        	
            if (e.getActionCommand() == FREETEXT_CORRECT)
            {
                FreeTextTool.setShowPropDialog(false);
                FreeText correctAnnot = factory.createFreeText("Correct");
                m_NotesBean.startEdit (correctAnnot, false, false);
            }
            else if (e.getActionCommand() == STAMP_CORRECT)
            {
                m_NotesBean.startEdit (factory.createRubberStamp ("Correct", Color.blue), false, false);
            }
            else if (e.getActionCommand() == RED_CIRCLE)
            {
                Circle redCircle = factory.createCircle(null);
                redCircle.setColor(Color.red);
                redCircle.setInternalColor(Color.blue);
                m_NotesBean.startEdit(redCircle, false, false);
            }
    	}
    	
    	
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
