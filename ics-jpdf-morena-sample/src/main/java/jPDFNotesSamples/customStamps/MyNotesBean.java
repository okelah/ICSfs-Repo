/*
 * Created on Sep 30, 2008
 *
 */
package jPDFNotesSamples.customStamps;

import java.awt.Color;

import com.qoppa.pdf.annotations.Annotation;
import com.qoppa.pdf.annotations.FreeText;
import com.qoppa.pdfNotes.PDFNotesBean;

public class MyNotesBean extends PDFNotesBean
{
    public void startEdit(Annotation annot, boolean useDefault, boolean isSticky)
    {
        // Call PDFNotesBean to set its own properties
        super.startEdit(annot, useDefault, isSticky);
        
        // Set type writer text color.  A typewriter annotation is just a FreeText annotation
        // with the intent set to TypeWriter.
        if (annot instanceof FreeText && ((FreeText)annot).isIntentTypeWriter())
        {
            ((FreeText)annot).setTextColor(Color.red);
        }
    }
}
