package ics.pdf.swing;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

public class PdfNotesVBeanSwingApp {

    private static PdfNotesVBean sf = null;

    public static void main(String[] args) {

        System.out.println("ICSfs jPDFNotes");
        JFrame jf = new JFrame("ICSfs jPDFNotes");

        jf.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        jf.setSize((int) Math.min(1024, dm.getWidth() * 0.90), (int) Math.min(768, dm.getHeight() * 0.90));
        jf.setLocationRelativeTo(null);

        sf = new PdfNotesVBean();
        jf.add(sf);
        jf.setVisible(true);

        // sf.showPDF("MODE=create,PRINT=true,STAMP=1,SIGN=1,USER=test user name, LANG=ar, current_date=01/02/2012");
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-help") || arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-?")) {
                System.out.println("java com.sun.awc.PDFViewer [flags] [file]");
                System.out.println("flags: [-noThumb] [-help or -h or -?]");
                System.exit(0);
            }
        }

        jf.addComponentListener(new ComponentAdapter() {
            String properties = "MODE=create,PRINT=true,STAMP=true,SIGN=true,USER=test user name, LANG=ar, current_date=01/02/2012";

            @Override
            public void componentResized(ComponentEvent e) {
                sf.setLocation(10, 10);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                sf.setLocation(10, 10);
                FileInputStream fin;
                try {
                    fin = new FileInputStream("c://temp//oid.txt");
                    int ch;
                    while ((ch = fin.read()) != -1) {
                        sf.sb.append((char) ch);
                    }
                    fin.close();
                } catch (FileNotFoundException f) {
                    // TODO
                } catch (IOException f) {
                    // TODO
                }
                System.out.println("Show it");
                sf.showPDF(properties);
            }
        });

    }

}
