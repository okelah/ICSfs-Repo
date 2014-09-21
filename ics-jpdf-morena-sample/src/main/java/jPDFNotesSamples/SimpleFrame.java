/*
 * Created on Sep 24, 2008
 *
 */
package jPDFNotesSamples;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.qoppa.pdfNotes.PDFNotesBean;

public class SimpleFrame extends JFrame
{
	private JPanel jPanel = null;
	private PDFNotesBean pdfNotesBean = null;

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				final SimpleFrame sf = new SimpleFrame();
				sf.setVisible(true);
			}
		});
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
		this.setBounds(new Rectangle(0, 0, 900, 600));
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
			jPanel.add(getPDFNotesBean(), BorderLayout.CENTER);
		}
		return jPanel;
	}

	/**
	 * This method initializes PDFViewerBean
	 * 
	 * @return com.qoppa.pdfViewer.PDFViewerBean
	 */
	private PDFNotesBean getPDFNotesBean()
	{
		if (pdfNotesBean == null)
		{
			pdfNotesBean = new PDFNotesBean();
			
			// Buttons from the toolbar can be removed and added here:
            // pdfNotesBean.getAnnotToolbar().getjbAttachFile().setVisible(false);
		}
		return pdfNotesBean;
	}
}
