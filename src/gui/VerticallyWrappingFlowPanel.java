package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Lớp Panel tùy chỉnh để FlowLayout tự động xuống dòng (wrap).
 */
public class VerticallyWrappingFlowPanel extends JPanel implements Scrollable {
    public VerticallyWrappingFlowPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public Dimension getPreferredSize() {
        Container parent = getParent();
        if (parent != null) {
            int parentWidth = parent.getWidth();
            if (parentWidth > 0) {
                return calculatePreferredSize(parentWidth);
            }
        }
        return super.getPreferredSize();
    }

    private Dimension calculatePreferredSize(int targetWidth) {
        FlowLayout layout = (FlowLayout) getLayout();
        Insets insets = getInsets();
        if (insets == null) insets = new Insets(0,0,0,0);

        int hgap = layout.getHgap();
        int vgap = layout.getVgap();
        int maxWidth = targetWidth - insets.left - insets.right;

        int preferredHeight = insets.top + vgap;
        int rowHeight = 0;
        int currentRowWidth = insets.left;

        for (Component comp : getComponents()) {
            if (comp.isVisible()) {
                Dimension compSize = comp.getPreferredSize();
                if (currentRowWidth == insets.left || (currentRowWidth + hgap + compSize.width) <= maxWidth) {
                    if (currentRowWidth != insets.left) {
                        currentRowWidth += hgap;
                    }
                    currentRowWidth += compSize.width;
                    rowHeight = Math.max(rowHeight, compSize.height);
                } else {
                    preferredHeight += rowHeight + vgap;
                    rowHeight = compSize.height;
                    currentRowWidth = insets.left + compSize.width;
                }
            }
        }
        preferredHeight += rowHeight + insets.bottom + vgap;
        return new Dimension(targetWidth, preferredHeight);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}