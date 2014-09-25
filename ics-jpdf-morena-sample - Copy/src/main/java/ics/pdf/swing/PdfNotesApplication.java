package ics.pdf.swing;

import ics.pdf.swing.action.AcquireBatchImageAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdf.PrintSettings;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdf.annotations.IAnnotationFactory;
import com.qoppa.pdf.dom.IPDFDocument;
import com.qoppa.pdf.source.URLPDFSource;
import com.qoppa.pdfNotes.PDFNotesBean;
import com.qoppa.pdfNotes.settings.FreeTextTool;
import com.qoppa.pdfNotes.undo.UndoAction;
import com.qoppa.pdfNotes.undo.UndoListener;
import com.qoppa.pdfNotes.undo.UndoManager;
import com.qoppa.pdfViewer.PDFViewerBean;

import eu.gnome.morena.Configuration;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;
import eu.gnome.morena.TransferDoneListener;

/**
 * @author Ibrahim Sawalha
 *
 */
public class PdfNotesApplication extends JFrame implements DropTargetListener, ActionListener, TransferDoneListener {
    private static final long serialVersionUID = -1438653878066393492L;

    private JPanel jPanel = null;
    private PDFNotesBean pdfNotes = null;

    private final static String OS_WINDOWS_START = "windows";
    private final static String OS_MAC = "mac";

    private JMenuBar jmbFrameMenu = null;
    private JMenu jmFile = null;
    private JMenu jmEdit = null;
    private JMenu jmView = null;
    private JMenu jmToolbars = null;
    private JMenuItem jmiOpen = null;
    private JMenuItem jmiOpenURL = null;
    private JMenuItem jmiPrint = null;
    private JMenuItem jmiSaveAs = null;
    private JMenuItem jmiClose = null;
    private JMenuItem jmiExit = null;

    private JMenuItem jmiUndo = null;
    private JMenuItem jmiRedo = null;

    private JCheckBoxMenuItem jmiShowBookmarks;
    private JCheckBoxMenuItem jmiShowPages;
    private JCheckBoxMenuItem jmiShowAttach;
    private JCheckBoxMenuItem jmiShowComments;
    private JCheckBoxMenuItem jmiShowSignatures;
    private JCheckBoxMenuItem jmiShowLayers;

    private JMenuItem jmiActualSize;
    private JMenuItem jmiFitToWidth;
    private JMenuItem jmiFitToPage;

    private JCheckBoxMenuItem jcbmiInvertColors;
    private boolean binvert = false;
    private JMenuItem jmiPreviousView;
    private JMenuItem jmiNextView;
    private JMenu jmPageMode;
    private ButtonGroup bgPageMode;
    private JCheckBoxMenuItem jcmiPMContinuous;
    private JCheckBoxMenuItem jcmiPMSingle;
    private JCheckBoxMenuItem jcmiPMFacing;
    private JCheckBoxMenuItem jcmiPMFacingCont;

    private JCheckBoxMenuItem jcbShowToolbar = null;
    private JCheckBoxMenuItem jcbShowAnnotToolbar = null;
    private JCheckBoxMenuItem jcbShowOpen = null;
    private JCheckBoxMenuItem jcbShowSave = null;
    private JCheckBoxMenuItem jcbShowPrint = null;

    private final static String FREETEXT_CORRECT = "FreeTextCorrect";
    private final static String STAMP_CORRECT = "StampCorrect";
    private final static String RED_CIRCLE = "RedCircle";
    private final static String PRINT_ENABLED = "printEnabled";

    private Manager manager = null;
    private Scanner scanner = null;

    private List<BufferedImage> imagesList;

    /**
     * This method initializes
     *
     */
    public PdfNotesApplication(String loadDoc) {
        super();
        initialize(loadDoc);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize(final String loadDoc) {
        // Configuration.setLogLevel(Level.ALL);
        Configuration.addDeviceType(".*HP.*", true);
        // Configuration.setMode(Configuration.MODE_NATIVE_UI);
        manager = Manager.getInstance();
        this.setJMenuBar(getjmbFrameMenu());
        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJPanel());

        DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        this.setSize((int) Math.min(1100, dm.getWidth() * 0.90), (int) Math.min(768, dm.getHeight() * 0.90));
        this.setLocationRelativeTo(null);

        try {
            // Try to set multiple images, java 1.6 only
            Method setIconImages = this.getClass().getMethod("setIconImages", new Class[] { List.class });
            Vector<BufferedImage> imageList = new Vector<BufferedImage>();
            imageList.add(ImageIO.read(getClass().getResourceAsStream("jPDFNotes16.png")));
            imageList.add(ImageIO.read(getClass().getResourceAsStream("jPDFNotes32.png")));
            setIconImages.invoke(this, new Object[] { imageList });
        } catch (Throwable t1) {
            try {
                this.setIconImage(ImageIO.read(getClass().getResourceAsStream("jPDFNotes16.png")));
            } catch (Throwable t2) {
                // ignore
            }
        }

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                // Title bar
                setTitle("jPDFNotes Sample - " + PDFNotesBean.getVersion());

                // Load an initial document
                if (loadDoc == null) {
                    // loadSampleDoc();
                } else {
                    loadDocument(loadDoc);
                }

                // Set ourselves as a drop target, to receive PDF files
                setDropTarget(new DropTarget(PdfNotesApplication.this, PdfNotesApplication.this));

                // Create an UndoListener to listen to the UndoManager
                UndoListener undoListener = new UndoListener() {
                    public void undoListModified() {
                        enableUndoRedoMenus();
                    }
                };

                getPDFNotes().getUndoManager().addUndoListener(undoListener);

                // Optional call to allow keystroke navigation (up/down arrows, page up/down, ...)
                getPDFNotes().getRootPane().getContentPane().requestFocus();
            }
        });
    }

    private void loadDocument(String loadDoc) {
        try {
            if (loadDoc.startsWith("http:")) {
                getPDFNotes().loadPDF(new URL(loadDoc));
            } else {
                getPDFNotes().loadPDF(loadDoc);
            }
        } catch (PDFException pdfE) {
            JOptionPane.showMessageDialog(this, pdfE.getMessage());
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this, "Error opening document: " + t.getMessage());
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
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getPDFNotes(), BorderLayout.CENTER);
        }
        return jPanel;
    }

    /**
     * This method initializes PDFViewer
     *
     * @return com.qoppa.pdfViewer.PDFViewer
     */
    private PDFNotesBean getPDFNotes() {
        if (pdfNotes == null) {

            // pdfNotes = new PDFNotesBean();
            pdfNotes = new IcsPdfNotesBean(manager);

            // Create custom buttons
            JButton ftCorrect = new JButton("FT");
            ftCorrect.setActionCommand(FREETEXT_CORRECT);
            ftCorrect.addActionListener(this);
            pdfNotes.getAnnotToolbar().add(ftCorrect);

            JButton rsCorrect = new JButton("RS");
            rsCorrect.setActionCommand(STAMP_CORRECT);
            rsCorrect.addActionListener(this);
            pdfNotes.getAnnotToolbar().add(rsCorrect);

            // Red Circle
            JButton jbRedCircle = new JButton("RC");
            jbRedCircle.setActionCommand(RED_CIRCLE);
            jbRedCircle.addActionListener(this);
            pdfNotes.getAnnotToolbar().add(jbRedCircle);

            // Enable Printing
            JButton jbPrintEnabled = new JButton("Enable Printing?");
            jbPrintEnabled.setActionCommand(PRINT_ENABLED);
            jbPrintEnabled.addActionListener(this);
            pdfNotes.getAnnotToolbar().add(jbPrintEnabled);

            pdfNotes.getToolbar().add(new AcquireBatchImageAction(getPDFNotes(), manager, this));

            javax.swing.border.Border empty = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10);
            javax.swing.border.Border line = javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1);
            javax.swing.border.Border border = javax.swing.BorderFactory.createCompoundBorder(empty, line);
            pdfNotes.setBorder(border);
        }
        return pdfNotes;
    }

    /**
     * This method initializes jMenuBar1
     *
     * @return javax.swing.JMenuBar
     */
    private JMenuBar getjmbFrameMenu() {
        if (jmbFrameMenu == null) {
            jmbFrameMenu = new JMenuBar();
            jmbFrameMenu.add(getJmFile());
            jmbFrameMenu.add(getJmEdit());
            jmbFrameMenu.add(getJmToolbars());
            jmbFrameMenu.add(getJmView());
        }
        return jmbFrameMenu;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJmFile() {
        if (jmFile == null) {
            jmFile = new JMenu();
            jmFile.setText("File");
            jmFile.add(getjmiOpen());
            jmFile.add(getjmiOpenURL());
            jmFile.add(getjmiSaveAs());
            jmFile.add(getJmiPrint());
            jmFile.add(getJmiClose());
            jmFile.add(new JSeparator());
            jmFile.add(getJmiExit());
        }
        return jmFile;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJmEdit() {
        if (jmEdit == null) {
            jmEdit = new JMenu();
            jmEdit.setText("Edit");
            jmEdit.add(getJmiUndo());
            jmEdit.add(getJmiRedo());
        }
        return jmEdit;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJmView() {
        if (jmView == null) {
            jmView = new JMenu();
            jmView.setText("View");
            jmView.add(getJmiShowAttachments());
            jmView.add(getJmiShowBookmarks());
            jmView.add(getJmiShowComments());
            jmView.add(getJmiShowLayers());
            jmView.add(getJmiShowPages());
            jmView.add(getJmiShowSignatures());
            jmView.addSeparator();
            jmView.add(getJmiInvertColors());
            jmView.addSeparator();
            jmView.add(getJmiActualSize());
            jmView.add(getJmiFitToWidth());
            jmView.add(getJmiFitToPage());
            jmView.addSeparator();
            jmView.add(getJmiPreviousView());
            jmView.add(getJmiNextView());
            jmView.addSeparator();
            jmView.add(getjmPageMode());
        }
        return jmView;
    }

    /**
     * This method initializes jMenu1
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJmToolbars() {
        if (jmToolbars == null) {
            jmToolbars = new JMenu();
            jmToolbars.setText("Toolbar");
            jmToolbars.add(getJcbShowToolbar());
            jmToolbars.add(getJcbShowAnnotToolbar());
            jmToolbars.add(new JSeparator());
            jmToolbars.add(getJcbShowSave());
            jmToolbars.add(getJcbShowOpen());
            jmToolbars.add(getJcbShowPrint());
        }
        return jmToolbars;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getjmiOpen() {
        if (jmiOpen == null) {
            jmiOpen = new JMenuItem();
            jmiOpen.setText("Open");
            jmiOpen.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File pdfFile = getFile();
                    if (pdfFile != null) {
                        try {
                            getPDFNotes().loadPDF(pdfFile.getAbsolutePath());
                        } catch (PDFException pdfE) {
                            JOptionPane.showMessageDialog(getContentPane(), pdfE.getMessage());
                        }
                    }
                }
            });
        }
        return jmiOpen;
    }

    private JMenuItem getjmiOpenURL() {
        if (jmiOpenURL == null) {
            jmiOpenURL = new JMenuItem();
            jmiOpenURL.setText("Open URL");
            jmiOpenURL.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String urlString = JOptionPane.showInputDialog(getContentPane(), "Enter URL", "http://");
                    if (urlString != null) {
                        try {
                            getPDFNotes().loadPDF(new URLPDFSource(new URL(urlString)));
                        } catch (MalformedURLException badURL) {
                            badURL.printStackTrace();
                            JOptionPane.showMessageDialog(getContentPane(), badURL.getMessage());
                        } catch (PDFException pdfE) {
                            pdfE.printStackTrace();
                            JOptionPane.showMessageDialog(getContentPane(), pdfE.getMessage());
                        }
                    }
                }
            });
        }
        return jmiOpenURL;
    }

    private JMenuItem getjmiSaveAs() {
        if (jmiSaveAs == null) {
            jmiSaveAs = new JMenuItem();
            jmiSaveAs.setText("Save As...");
            jmiSaveAs.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().saveAs();
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiSaveAs;
    }

    /**
     * This method initializes jMenuItem1
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJmiPrint() {
        if (jmiPrint == null) {
            jmiPrint = new JMenuItem();
            jmiPrint.setText("Print");
            jmiPrint.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().print((PrintSettings) null);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiPrint;
    }

    /**
     * This method initializes jmiClose
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJmiClose() {
        if (jmiClose == null) {
            jmiClose = new JMenuItem();
            jmiClose.setText("Close");
            jmiClose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getPDFNotes().close(true);
                }
            });
        }
        return jmiClose;
    }

    /**
     * This method initializes jMenuItem2
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJmiExit() {
        if (jmiExit == null) {
            jmiExit = new JMenuItem();
            jmiExit.setText("Exit");
            jmiExit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return jmiExit;
    }

    /**
     * This method initializes jmiUndo
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJmiUndo() {
        if (jmiUndo == null) {
            jmiUndo = new JMenuItem();
            jmiUndo.setText("Undo");
            jmiUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()));
            jmiUndo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getPDFNotes().getUndoManager().undo();
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
    private JMenuItem getJmiRedo() {
        if (jmiRedo == null) {
            jmiRedo = new JMenuItem();
            jmiRedo.setText("Redo");
            if (isSystemMac()) {
                jmiRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask() + InputEvent.SHIFT_DOWN_MASK));
            } else {
                jmiRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
            }

            jmiRedo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getPDFNotes().getUndoManager().redo();
                }
            });
            jmiRedo.setEnabled(false);
        }
        return jmiRedo;
    }

    /**
     * This method initializes jCheckBoxMenuItem
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJcbShowToolbar() {
        if (jcbShowToolbar == null) {
            jcbShowToolbar = new JCheckBoxMenuItem();
            jcbShowToolbar.setText("Show Toolbar");
            jcbShowToolbar.setSelected(true);
            jcbShowToolbar.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    getPDFNotes().getToolbar().setVisible(jcbShowToolbar.isSelected());
                }
            });
        }
        return jcbShowToolbar;
    }

    /**
     * This method initializes jCheckBoxMenuItem
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJcbShowAnnotToolbar() {
        if (jcbShowAnnotToolbar == null) {
            jcbShowAnnotToolbar = new JCheckBoxMenuItem();
            jcbShowAnnotToolbar.setText("Show Annotation Toolbar");
            jcbShowAnnotToolbar.setSelected(true);
            jcbShowAnnotToolbar.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    getPDFNotes().getAnnotToolbar().setVisible(jcbShowAnnotToolbar.isSelected());
                }
            });
        }
        return jcbShowAnnotToolbar;
    }

    /**
     * This method initializes jcbShowOpen
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJcbShowOpen() {
        if (jcbShowOpen == null) {
            jcbShowOpen = new JCheckBoxMenuItem();
            jcbShowOpen.setText("Show Open Button");
            jcbShowOpen.setSelected(true);
            jcbShowOpen.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    getPDFNotes().getToolbar().getjbOpen().setVisible(jcbShowOpen.isSelected());
                }
            });
        }
        return jcbShowOpen;
    }

    /**
     * This method initializes jcbShowSave
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJcbShowSave() {
        if (jcbShowSave == null) {
            jcbShowSave = new JCheckBoxMenuItem();
            jcbShowSave.setText("Show Save Button");
            jcbShowSave.setSelected(true);
            jcbShowSave.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    getPDFNotes().getEditToolbar().getjbSave().setVisible(jcbShowSave.isSelected());
                }
            });
        }
        return jcbShowSave;
    }

    /**
     * This method initializes jCheckBoxMenuItem2
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJcbShowPrint() {
        if (jcbShowPrint == null) {
            jcbShowPrint = new JCheckBoxMenuItem();
            jcbShowPrint.setText("Show Print Button");
            jcbShowPrint.setSelected(true);
            jcbShowPrint.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    getPDFNotes().getToolbar().getjbPrint().setVisible(jcbShowPrint.isSelected());
                }
            });
        }
        return jcbShowPrint;
    }

    public static void main(final String[] args) {
        // Set the look and feel
        setLookAndFeel();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Load document from command line args
                String loadDoc = null;
                if (args != null && args.length > 0) {
                    loadDoc = args[0];
                }

                // Create frame and show it
                PdfNotesApplication frame = new PdfNotesApplication(loadDoc);
                frame.setVisible(true);
            }
        });
    }

    private boolean isSystemMac() {
        // Check the OS
        String osName = System.getProperty("os.name");
        osName = osName.toLowerCase();
        int firstIndexOfMac = osName.indexOf(OS_MAC);
        if (firstIndexOfMac == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Insert the method's description here. Creation date: (11/10/2003 9:16:17 PM)
     *
     * @return boolean
     */
    private boolean isSystemWindows() {
        // Check the OS
        String osName = System.getProperty("os.name");
        osName = osName.toLowerCase();
        return osName.startsWith(OS_WINDOWS_START);
    }

    /**
     * This method shows a JFileChooser with a PDF filter and returns the chosen file. If the user hits
     * cancel, the method returns null.
     *
     * @param currentFile The file chooser will select this file when it comes up.
     *
     * @return Chosen file.
     */
    private File getFile() {
        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();

        // // PDF File filter
        // PDFFileFilter pdfFilter = new PDFFileFilter();
        //
        // // Set the filter
        // fileChooser.setFileFilter(pdfFilter);

        // Show the JFileChooser
        int rc = fileChooser.showOpenDialog(this);
        if (rc == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    /**
     * This method initializes jmiShowAttach
     *
     * @return javax.swing.JMenuItem
     */
    public JCheckBoxMenuItem getJmiShowAttachments() {
        if (jmiShowAttach == null) {
            jmiShowAttach = new JCheckBoxMenuItem();
            jmiShowAttach.setText("Attachments");

            jmiShowAttach.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (getJmiShowAttachments().isSelected()) {
                            getPDFNotes().getAttachmentPanel().setPaneVisible(true);
                        } else {
                            getPDFNotes().setSplitOpen(false);
                        }
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiShowAttach;
    }

    /**
     * This method initializes jmiShowBookmarks
     *
     * @return javax.swing.JMenuItem
     */
    public JCheckBoxMenuItem getJmiShowBookmarks() {
        if (jmiShowBookmarks == null) {
            jmiShowBookmarks = new JCheckBoxMenuItem();
            jmiShowBookmarks.setText("Bookmarks");
            jmiShowBookmarks.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (getJmiShowBookmarks().isSelected()) {
                            getPDFNotes().getBookmarkPanel().setPaneVisible(true);
                        } else {
                            getPDFNotes().setSplitOpen(false);
                        }
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiShowBookmarks;
    }

    /**
     * This method initializes jmiShowComments
     *
     * @return javax.swing.JMenuItem
     */
    public JCheckBoxMenuItem getJmiShowComments() {
        if (jmiShowComments == null) {
            jmiShowComments = new JCheckBoxMenuItem();
            jmiShowComments.setText("Comments");
            jmiShowComments.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (getJmiShowComments().isSelected()) {
                            getPDFNotes().getCommentPanel().setPaneVisible(true);
                        } else {
                            getPDFNotes().setSplitOpen(false);
                        }
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiShowComments;
    }

    /**
     * This method initializes jmiShowLayers
     *
     * @return javax.swing.JMenuItem
     */
    public JCheckBoxMenuItem getJmiShowLayers() {
        if (jmiShowLayers == null) {
            jmiShowLayers = new JCheckBoxMenuItem();
            jmiShowLayers.setText("Layers");
            jmiShowLayers.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (getJmiShowLayers().isSelected()) {
                            getPDFNotes().getLayerPanel().setPaneVisible(true);
                        } else {
                            getPDFNotes().setSplitOpen(false);
                        }
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiShowLayers;
    }

    /**
     * This method initializes jmiShowThumbs
     *
     * @return javax.swing.JMenuItem
     */
    public JCheckBoxMenuItem getJmiShowPages() {
        if (jmiShowPages == null) {
            jmiShowPages = new JCheckBoxMenuItem();
            jmiShowPages.setText("Pages");
            jmiShowPages.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (getJmiShowPages().isSelected()) {
                            getPDFNotes().getThumbnailPanel().setPaneVisible(true);
                        } else {
                            getPDFNotes().setSplitOpen(false);
                        }
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiShowPages;
    }

    /**
     * This method initializes jmiShowSignatures
     *
     * @return javax.swing.JMenuItem
     */
    public JCheckBoxMenuItem getJmiShowSignatures() {
        if (jmiShowSignatures == null) {
            jmiShowSignatures = new JCheckBoxMenuItem();
            jmiShowSignatures.setText("Signatures");
            jmiShowSignatures.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (getJmiShowSignatures().isSelected()) {
                            getPDFNotes().getSignaturePanel().setPaneVisible(true);
                        } else {
                            getPDFNotes().setSplitOpen(false);
                        }
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiShowSignatures;
    }

    /**
     * This method initializes jmiActualSize
     *
     * @return javax.swing.JMenuItem
     */
    public JMenuItem getJmiActualSize() {
        if (jmiActualSize == null) {
            jmiActualSize = new JMenuItem();
            jmiActualSize.setText("Actual Size");
            jmiActualSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()));
            jmiActualSize.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setZoomMode(PDFViewerBean.ZOOMMODE_NORMAL);
                        getPDFNotes().setScale2D(100);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiActualSize;
    }

    /**
     * This method initializes jmiFitToPage
     *
     * @return javax.swing.JMenuItem
     */
    public JMenuItem getJmiFitToPage() {
        if (jmiFitToPage == null) {
            jmiFitToPage = new JMenuItem();
            jmiFitToPage.setText("Fit To Page");
            jmiFitToPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()));
            jmiFitToPage.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setZoomMode(PDFViewerBean.ZOOMMODE_FITPAGE);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiFitToPage;
    }

    /**
     * This method initializes jmiFitToWidth
     *
     * @return javax.swing.JMenuItem
     */
    public JMenuItem getJmiFitToWidth() {
        if (jmiFitToWidth == null) {
            jmiFitToWidth = new JMenuItem();
            jmiFitToWidth.setText("Fit To Width");
            jmiFitToWidth.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()));
            jmiFitToWidth.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setZoomMode(PDFViewerBean.ZOOMMODE_FITWIDTH);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiFitToWidth;
    }

    public JMenuItem getJmiNextView() {
        if (jmiNextView == null) {
            jmiNextView = new JMenuItem();
            jmiNextView.setText("Next view");
            jmiNextView.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().gotoNextView();
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiNextView;
    }

    public JMenuItem getJmiPreviousView() {
        if (jmiPreviousView == null) {
            jmiPreviousView = new JMenuItem();
            jmiPreviousView.setText("Previous view");
            jmiPreviousView.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().gotoPreviousView();
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jmiPreviousView;
    }

    /**
     * Set the look and feel. The jPDFNotes library is look and feel independent, so it will inherit any look
     * and feel set by the host application. In this sample, we are setting the look and feel to the system
     * default look and feel.
     */
    private static void setLookAndFeel() {
        // Set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (UIManager.getSystemLookAndFeelClassName() != null
                    && UIManager.getSystemLookAndFeelClassName().toLowerCase().indexOf("windows") != -1) {
                UIManager.put("TextArea.font", UIManager.get("TextField.font"));
            }
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable tt) {
                // ignore
            }
        }
    }

    public JCheckBoxMenuItem getJmiInvertColors() {
        if (jcbmiInvertColors == null) {
            jcbmiInvertColors = new JCheckBoxMenuItem();
            jcbmiInvertColors.setText("Invert Colors");
            jcbmiInvertColors.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        binvert = !binvert;
                        getPDFNotes().setInvertColorsMode(binvert);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jcbmiInvertColors;
    }

    public JMenu getjmPageMode() {
        if (jmPageMode == null) {
            jmPageMode = new JMenu();
            jmPageMode.setText("Page Layout");
            jmPageMode.add(getjcmiPMSingle());
            jmPageMode.add(getjcmiPMContinuous());
            jmPageMode.add(getjcmiPMFacing());
            jmPageMode.add(getjcmiPMFacingCont());
        }
        return jmPageMode;
    }

    public JCheckBoxMenuItem getjcmiPMContinuous() {
        if (jcmiPMContinuous == null) {
            jcmiPMContinuous = new JCheckBoxMenuItem("Single Continuous");
            getbgPageMode().add(jcmiPMContinuous);
            getbgPageMode().setSelected(jcmiPMContinuous.getModel(), true);
            jcmiPMContinuous.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setPageMode(PDFViewerBean.PAGEMODE_CONTINUOUS);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jcmiPMContinuous;
    }

    public JCheckBoxMenuItem getjcmiPMFacing() {
        if (jcmiPMFacing == null) {
            jcmiPMFacing = new JCheckBoxMenuItem("Facing");
            getbgPageMode().add(jcmiPMFacing);
            jcmiPMFacing.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setPageMode(PDFViewerBean.PAGEMODE_FACING);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jcmiPMFacing;
    }

    public JCheckBoxMenuItem getjcmiPMFacingCont() {
        if (jcmiPMFacingCont == null) {
            jcmiPMFacingCont = new JCheckBoxMenuItem("Facing Continuous");
            getbgPageMode().add(jcmiPMFacingCont);
            jcmiPMFacingCont.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setPageMode(PDFViewerBean.PAGEMODE_FACING_CONTINUOUS);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jcmiPMFacingCont;
    }

    public JCheckBoxMenuItem getjcmiPMSingle() {
        if (jcmiPMSingle == null) {
            jcmiPMSingle = new JCheckBoxMenuItem("Single");
            getbgPageMode().add(jcmiPMSingle);
            jcmiPMSingle.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getPDFNotes().setPageMode(PDFViewerBean.PAGEMODE_SINGLEPAGE);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(getContentPane(), exc.getMessage());
                    }
                }
            });
        }
        return jcmiPMSingle;
    }

    public ButtonGroup getbgPageMode() {
        if (bgPageMode == null) {
            bgPageMode = new ButtonGroup();
        }
        return bgPageMode;
    }

    //
    // Implementation of drop target methods
    //
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
        // Get the action and accept the drop
        int action = dtde.getDropAction();
        dtde.acceptDrop(action);

        // Get the transferable
        Transferable t = dtde.getTransferable();
        if (t != null) {
            try {
                // We can take files as dropped objects
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    // Get the list of files
                    @SuppressWarnings({ "rawtypes" })
                    List list = (List) t.getTransferData(DataFlavor.javaFileListFlavor);

                    File f = (File) list.get(0);
                    if (f.getName() != null && f.getName().toLowerCase().endsWith(".pdf")) {
                        getPDFNotes().loadPDF(f.getAbsolutePath());
                    }
                }
            } catch (Throwable te) {
                JOptionPane.showMessageDialog(this, te.getMessage());
            }
        }
    }

    private void enableUndoRedoMenus() {
        UndoManager undoMgr = getPDFNotes().getUndoManager();
        enableUndoRedoMenu(getJmiUndo(), undoMgr.getNextUndoAction(), "Undo");
        enableUndoRedoMenu(getJmiRedo(), undoMgr.getNextRedoAction(), "Redo");
    }

    private void enableUndoRedoMenu(JMenuItem menuItem, UndoAction undoAction, String defaultText) {
        if (undoAction != null) {
            menuItem.setEnabled(true);
            if (undoAction.getDescription() != null && undoAction.getDescription().length() > 0) {
                menuItem.setText(defaultText + " " + undoAction.getDescription());
            } else {
                menuItem.setText(defaultText);
            }
        } else {
            menuItem.setText(defaultText);
            menuItem.setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent e) {

        IPDFDocument doc = pdfNotes.getDocument();

        if (doc != null) {
            IAnnotationFactory factory = doc.getAnnotationFactory();
            System.out.println(doc.getDocumentInfo().getAuthor());

            if (e.getActionCommand() == FREETEXT_CORRECT) {
                FreeTextTool.setShowPropDialog(false);
                FreeText correctAnnot = factory.createFreeText("صحيح");
                pdfNotes.startEdit(correctAnnot, false, false);
            } else if (e.getActionCommand() == STAMP_CORRECT) {
                // pdfNotes.startEdit(factory.createRubberStamp("Correct", Color.blue), false, false);
                BufferedImage image;
                try {
                    image = ImageIO.read(new File("yes.png"));
                    pdfNotes.startEdit(factory.createRubberStamp(image), true, true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getActionCommand() == RED_CIRCLE) {
                // Circle redCircle = factory.createCircle(null);
                // redCircle.setColor(Color.red);
                // redCircle.setInternalColor(Color.blue);
                // pdfNotes.startEdit(redCircle, false, false);
                pdfNotes.startEdit(factory.createRubberStamp("ICSfs Approval... checked", Color.red), false, false);
            } else if (e.getActionCommand() == PRINT_ENABLED) {
                System.out.println(">>>" + pdfNotes.getToolbar().getjbPrint().isEnabled());
                if (pdfNotes.getToolbar().getjbPrint().isEnabled()) {
                    pdfNotes.getToolbar().getjbPrint().setEnabled(false);
                } else {
                    pdfNotes.getToolbar().getjbPrint().setEnabled(true);
                }
            }
        }
    }

    public void transferDone(File file) {
        try {
            if (file != null) {
                PDFNotesBean tempNotes = new PDFNotesBean();
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    System.out.println("the output file is image");
                    System.out.println("image (" + image.getWidth() + ", " + image.getHeight() + ") color model="
                            + image.getColorModel());
                    getPDFNotes().loadPDF(new FileInputStream(file));
                } else {
                    System.out.println("the output file is NOT image");
                    File pdfFile = File.createTempFile(file.getName(), ".pdf");
                    InputStream is = new FileInputStream(file);
                    tempNotes.loadDocument(is, "tif");
                    tempNotes.save(tempNotes, "pdf", pdfFile);
                    tempNotes.invalidate();

                    getPDFNotes().loadPDF(new FileInputStream(pdfFile));

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Manager.getInstance() != null) {
                Manager.getInstance().close();
            }
        }
    }

    public void transferFailed(int arg0, String arg1) {
        // TODO Auto-generated method stub
    }

}
