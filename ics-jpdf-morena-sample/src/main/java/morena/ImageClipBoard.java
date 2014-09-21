package morena;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageClipBoard implements ClipboardOwner {

    public static void main(String[] aArguments) {
        ImageClipBoard clipboard = new ImageClipBoard();

        // display what is currently on the clipboard
        clipboard.saveClipBoardToFile("C:\\Users\\Ahmad\\Downloads\\savedee.jpg");

    }

    public void saveClipBoardToFile(String fileName) {
        if (fileName == null || fileName.equalsIgnoreCase("")) {
            throw new IllegalArgumentException("file name cannot be null...");
        }

        BufferedImage img = getClipboardContents();
        if (img != null) {
            try {
                BufferedImage bi = img;
                File outputfile = new File(fileName);
                ImageIO.write(bi, "JPG", outputfile);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Empty implementation of the ClipboardOwner interface.
     */
    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
        // do nothing
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any image found on the Clipboard; if none found, return null
     *
     */
    private BufferedImage getClipboardContents() {
        BufferedImage result = null;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);

        boolean hasTransferableImage = (contents != null) && contents.isDataFlavorSupported(DataFlavor.imageFlavor);
        if (hasTransferableImage) {
            try {
                result = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
            } catch (UnsupportedFlavorException ex) {
                // highly unlikely since we are using a standard DataFlavor
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
