package org.netbeans.estw;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

public class MarkSelectedOccurrencesHighlighter
        implements CaretListener, HighlightsChangeListener {

    private static final AttributeSet defaultColors
            = AttributesUtilities.createImmutable(
                    EditorStyleConstants.LeftBorderLineColor, Color.BLUE,
                    EditorStyleConstants.RightBorderLineColor, Color.BLUE,
                    EditorStyleConstants.TopBorderLineColor, Color.BLUE,
                    EditorStyleConstants.BottomBorderLineColor, Color.BLUE
//                    StyleConstants.Background, new Color(236, 235, 163)
            );

    private final OffsetsBag bag;
    private JTextComponent comp;
    private final WeakReference<Document> weakDoc;
    private final RequestProcessor rp;
    private final static int REFRESH_DELAY = 100;
    private RequestProcessor.Task lastRefreshTask;

    public MarkSelectedOccurrencesHighlighter(final Document doc) {
        rp = new RequestProcessor(MarkSelectedOccurrencesHighlighter.class);
        bag = new OffsetsBag(doc);
        bag.addHighlightsChangeListener(this);
        weakDoc = new WeakReference<Document>((Document) doc);
        DataObject dobj = NbEditorUtilities.getDataObject(weakDoc.get());
        if (dobj != null) {
            EditorCookie pane = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane[] panes = pane.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                comp = panes[0];
                comp.addCaretListener(this);
                comp.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(final KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            bag.clear();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        setupAutoRefresh();
    }

    public void setupAutoRefresh() {
        if (lastRefreshTask == null) {
            lastRefreshTask = rp.create(new Runnable() {
                @Override
                public void run() {
                    String selection = comp.getSelectedText();
                    if (selection != null) {
                        bag.addHighlight(
                                comp.getSelectionStart(),
                                comp.getSelectionEnd(),
                                defaultColors);
                    }
                }
            });
        }
        lastRefreshTask.schedule(REFRESH_DELAY);
    }

    public OffsetsBag getHighlightsBag() {
        return bag;
    }
    
    

    @Override
    public void highlightChanged(HighlightsChangeEvent hce) {
        try {
            int start = hce.getStartOffset();
            int end = hce.getEndOffset();
            String newlyHighlighted = comp.getText(start, end - start);
//            NotificationDisplayer.getDefault().notify(
//                    newlyHighlighted,
//                    ImageUtilities.loadImageIcon("org/netbeans/estw/blue.png", false),
//                    "You selected " + newlyHighlighted + " at " + start + " / " + end,
//                    null);
            StatusDisplayer.getDefault().setStatusText(newlyHighlighted);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
