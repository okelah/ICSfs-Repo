package jPDFNotesSamples.annotProperties;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.qoppa.pdfNotes.PDFNotesBean;

public class SimpleFrameWithToolbar extends JFrame
{
	private JPanel jPanel = null;
	private PDFNotesBean pdfNotesBean = null;

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				final SimpleFrameWithToolbar sf = new SimpleFrameWithToolbar();
				sf.setVisible(true);
			}
		});
	}

	/**
	 * This method initializes
	 * 
	 */
	public SimpleFrameWithToolbar()
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
		this.setBounds(new Rectangle(0, 0, 1200, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(getJPanel());
		this.setTitle("Qoppa Software - Annotation Properties Toolbar");
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
			pdfNotesBean.getToolbar().add(pdfNotesBean.getAnnotPropertiesToolBar().createSeparator());
			pdfNotesBean.getToolbar().add(pdfNotesBean.getAnnotPropertiesToolBar());
		}
		return pdfNotesBean;
	}
}

