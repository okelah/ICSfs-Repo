package ics.pdf.swing.util;

import java.awt.Component;

import javax.swing.JButton;

import com.qoppa.pdfNotes.PDFNotesBean;

public class FillLabels {

    public static void fillPDFNotesBean(PDFNotesBean bean) {

        for (Component comp : bean.getToolbar().getComponents()) {
            setToolTipText(comp);
        }
        for (Component comp : bean.getAnnotToolbar().getComponents()) {
            setToolTipText(comp);
        }

    }

    public static void setToolTipText(Component comp) {
        if (comp == null || comp.getName() == null || !(comp instanceof JButton)) {
            return;
        }

        String name = comp.getName().replaceAll("\\s+", "");
        if ((comp instanceof JButton) && null != name) {
            try {
                ((JButton) comp).setToolTipText(LanguageUtil.getLabel(name));
            } catch (java.util.MissingResourceException e) {
                System.err.println(name + " is not found");
                ((JButton) comp).setToolTipText(">" + name);
            }
        }
    }
}
