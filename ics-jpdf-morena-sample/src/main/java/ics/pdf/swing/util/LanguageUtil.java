package ics.pdf.swing.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LanguageUtil {

    public static String getLabel(String key) {
        if (key == null) {
            return key;
        }
        ResourceBundle rb = ResourceBundle.getBundle("NotesLabels", Locale.getDefault(),
            LanguageUtil.class.getClassLoader());
        try {
            return rb.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("LanguageUtil.getLabel(" + key + ") was not foudn");
            return key;
        }
    }

}
