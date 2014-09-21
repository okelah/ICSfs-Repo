package jPDFNotesSamples.annotProperties;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.qoppa.pdfNotes.PDFNotesBean;

public class SimpleFrameWithDialog extends JFrame
{
	private JPanel jPanel = null;
	private PDFNotesBean pdfNotesBean = null;

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				final SimpleFrameWithDialog sf = new SimpleFrameWithDialog();
				sf.setVisible(true);
			}
		});
	}

	/**
	 * This method initializes
	 * 
	 */
	public SimpleFrameWithDialog()
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
		this.setBounds(new Rectangle(0, 0, 1100, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(getJPanel());
		this.setTitle("Qoppa Software - Annotation Properties Dialog");
		this.setLocationRelativeTo(null);
		
		// Add a button for AnnotPropertiesToolbar dialog
		JButton annotPropButton = new JButton();
		annotPropButton.setBorderPainted(false);
		annotPropButton.setToolTipText("Annotation Properties");
		try	{
			annotPropButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("annot_props_toolbar24.png"))));
		}
		catch (Throwable t) {
			
		}
		
		annotPropButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				getPDFNotesBean().getAnnotPropertiesToolBar().setDefaultDialogVisible(!getPDFNotesBean().getAnnotPropertiesToolBar().isShowing());
			}
		});
		getPDFNotesBean().getAnnotToolbar().add(getPDFNotesBean().getAnnotPropertiesToolBar().createSeparator(), 0);
		getPDFNotesBean().getAnnotToolbar().add(annotPropButton, 0);
		

		Window window = getPDFNotesBean().getAnnotPropertiesToolBar().setDefaultDialogVisible(false);

		// Arbitrarily position the window
		Point p = new Point(0, 0);
		SwingUtilities.convertPointToScreen(p, getPDFNotesBean());
		p.x += (getWidth() - window.getPreferredSize().width - 30);
		p.y += 120;
		window.setLocation(p);
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
		}
		return pdfNotesBean;
	}
}

