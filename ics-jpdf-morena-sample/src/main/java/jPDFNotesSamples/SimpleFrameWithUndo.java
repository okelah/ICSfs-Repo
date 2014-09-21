package jPDFNotesSamples;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.qoppa.pdfNotes.PDFNotesBean;
import com.qoppa.pdfNotes.undo.UndoAction;
import com.qoppa.pdfNotes.undo.UndoListener;
import com.qoppa.pdfNotes.undo.UndoManager;

public class SimpleFrameWithUndo extends JFrame
{
	private JPanel jPanel = null;
	private PDFNotesBean pdfNotesBean = null;

	private JMenuBar jmbFrameMenu = null;
	private JMenu jmEdit = null;
	private JMenuItem jmiUndo = null;
	private JMenuItem jmiRedo = null;

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				final SimpleFrameWithUndo sf = new SimpleFrameWithUndo();
				sf.setVisible(true);
			}
		});
	}

	/**
	 * This method initializes
	 * 
	 */
	public SimpleFrameWithUndo()
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
		this.setJMenuBar(getFrameMenuBar());
		this.setContentPane(getJPanel());
		this.setTitle("Qoppa Software - jPDFNotes Sample");
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jmbFrameMenu
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getFrameMenuBar()
	{
		if (jmbFrameMenu == null)
		{
			jmbFrameMenu = new JMenuBar();
			jmbFrameMenu.add(getJmEdit());
		}
		return jmbFrameMenu;
	}

	/**
	 * This method initializes jmEdit
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getJmEdit()
	{
		if (jmEdit == null)
		{
			jmEdit = new JMenu("Edit");
			jmEdit.add(getJmiUndo());
			jmEdit.add(getJmiRedo());
		}
		return jmEdit;
	}

	/**
	 * This method initializes jmiUndo
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJmiUndo()
	{
		if (jmiUndo == null)
		{
			jmiUndo = new JMenuItem();
			jmiUndo.setText("Undo");
			jmiUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			jmiUndo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					getPDFNotesBean().getUndoManager().undo();
				}
			});
			jmiUndo.setEnabled(false);
		}
		return jmiUndo;
	}

	/**
	 * This method initializes jmiRedo
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJmiRedo()
	{
		if (jmiRedo == null)
		{
			jmiRedo = new JMenuItem();
			jmiRedo.setText("Redo");
			jmiRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			jmiRedo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					getPDFNotesBean().getUndoManager().redo();
				}
			});
			jmiRedo.setEnabled(false);
		}
		return jmiRedo;
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
	 * This method initializes pdfNotesBean
	 * 
	 * @return com.qoppa.pdfNotes.PDFNotesBean
	 */
	private PDFNotesBean getPDFNotesBean()
	{
		if (pdfNotesBean == null)
		{
			pdfNotesBean = new PDFNotesBean();
			
			pdfNotesBean.getUndoManager().addUndoListener(new UndoListener() {
				public void undoListModified()
				{
					enableUndoRedoMenus();
				}
			});
		}
		return pdfNotesBean;
	}
	
	private void enableUndoRedoMenus()
	{
		UndoManager undoMgr = getPDFNotesBean().getUndoManager();
		
		enableUndoRedoMenu(getJmiUndo(), undoMgr.getNextUndoAction(), "Undo");
		enableUndoRedoMenu(getJmiRedo(), undoMgr.getNextRedoAction(), "Redo");
	}
    
    private void enableUndoRedoMenu(JMenuItem menuItem, UndoAction undoAction, String defaultText)
    {
    	if (undoAction != null)
		{
    		menuItem.setEnabled(true);
			if (undoAction.getDescription() != null && undoAction.getDescription().length() > 0)
			{
				menuItem.setText(defaultText + " " + undoAction.getDescription());
			}
			else
			{
				menuItem.setText(defaultText);
			}
		}
		else
		{
			menuItem.setText(defaultText);
			menuItem.setEnabled(false);
		}
    }
}
