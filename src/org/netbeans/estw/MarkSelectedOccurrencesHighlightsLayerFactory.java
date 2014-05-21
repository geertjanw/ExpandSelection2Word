package org.netbeans.estw;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/plain", service = HighlightsLayerFactory.class),
    @MimeRegistration(mimeType = "text/html", service = HighlightsLayerFactory.class),
    @MimeRegistration(mimeType = "text/x-java", service = HighlightsLayerFactory.class),
    @MimeRegistration(mimeType = "text/xml", service = HighlightsLayerFactory.class)
})
public class MarkSelectedOccurrencesHighlightsLayerFactory implements HighlightsLayerFactory {
    public static MarkSelectedOccurrencesHighlighter getMarkOccurrencesHighlighter(Document doc) {
        MarkSelectedOccurrencesHighlighter highlighter
                = (MarkSelectedOccurrencesHighlighter) doc.getProperty(MarkSelectedOccurrencesHighlighter.class);
        if (highlighter == null) {
            doc.putProperty(MarkSelectedOccurrencesHighlighter.class,
                    highlighter = new MarkSelectedOccurrencesHighlighter(doc));
        }
        return highlighter;
    }
    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[]{
            HighlightsLayer.create(
            MarkSelectedOccurrencesHighlighter.class.getName(),
            ZOrder.CARET_RACK.forPosition(2000),
            true,
            getMarkOccurrencesHighlighter(context.getDocument()).getHighlightsBag())
        };
    }
}
