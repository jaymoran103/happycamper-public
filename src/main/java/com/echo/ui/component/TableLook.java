package com.echo.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import com.echo.domain.DataConstants;

public class TableLook {

    // These settings are managed through TableColors and ViewSettings classes
    // This class provides utility methods for applying those settings to tables

   /**
     * Configures the appearance of table headers.
     * Sets up tooltips, cursor, and custom rendering.
     */
    public static void doHeaderLook(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.setToolTipText(TextConstants.TABLE_HEADER_TOOLTIP);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel headerLabel = new JLabel(value.toString());
                headerLabel.setHorizontalAlignment(JLabel.CENTER);
                headerLabel.setBackground(TableColors.getHeaderColor());
                headerLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
                headerLabel.setOpaque(true);
                return headerLabel;
            }
        });
    }

    /**
     * Configures the appearance of table cells:
     * - Grid colors
     * - Alternating row colors
     * - Selection highlighting
     * - Empty field highlighting
     */
    public static void doCellLook(JTable table) {
        table.setGridColor(TableColors.getGridColor());

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Convert the value for display
                String displayValue = DataConstants.getDisplayValue((String)value);
                Component cell = super.getTableCellRendererComponent(table, displayValue,
                        isSelected, hasFocus, row, column);

                boolean useAltColor = (row % 2 == 0) || !TableColors.isAlternateShadesEnabled();

                // Disable cell selection border
                if (cell instanceof JLabel jLabel) {
                    jLabel.setBorder(null);
                }

                // Apply appropriate background color based on state and even/odd row status

                //If the cell is selected, apply the selected cell color
                if (isSelected) {
                    cell.setBackground(useAltColor ? TableColors.getSelectedEvenColor()
                                                   : TableColors.getSelectedOddColor());
                }

                //If the cell is empty and should be highlighted, apply the flagged color
                else if (TableColors.isHighlightEmptyDataEnabled() && DataConstants.isEmpty((String)value) ) {
                    cell.setBackground(useAltColor ? TableColors.getFlaggedEvenColor()
                                                   : TableColors.getFlaggedOddColor());
                }

                //If the cell is neither selected nor flagged, apply the default table colors
                else {
                    cell.setBackground( useAltColor ? TableColors.getTableEvenColor()
                                                    : TableColors.getTableOddColor());
                }

                cell.setForeground(Color.BLACK);
                return cell;
            }
        });
    }
}
