package com.echo.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.echo.logging.RosterWarning;
import com.echo.ui.component.TableColors;

/**
 * WarningPanel displays a set of RosterWarnings corresponding to a single WarningType, using a JTable to display context for each warning instance
 */
public class WarningDialogPanel extends JPanel{

    public final WarningDialog parent;
    private final ArrayList<RosterWarning> warnings;

    private final String[][] tableRows;
    private final String[] tableHeaders;


    private final String generalExplanationText;
    private final String secondaryExplanationText;


    private final Color tableGridColor = DialogConstants.WARNINGTABLE_COLOR_GRID;
    private final Color tableHeaderColor = DialogConstants.WARNINGTABLE_COLOR_HEADER;
    private final Color tableEvenColor = DialogConstants.WARNINGTABLE_COLOR_EVENROW;
    private final Color tableOddColor = DialogConstants.WARNINGTABLE_COLOR_ODDROW;

    /**
     * Constructor sets the text and table fields, then builds the panel.
     * @param _parent Parent dialog that displays this panel.
     * @param _warnings List of warnings displayed in this panel.
     */
    WarningDialogPanel(WarningDialog _parent,ArrayList<RosterWarning> _warnings) {
        parent = _parent;
        warnings = _warnings;

        //Set text to show in panel
        generalExplanationText = warnings.get(0).getType().getGeneralExplanation();
        secondaryExplanationText = warnings.get(0).getType().getSecondaryExplanation();

        //Set up table to display specific warning details
        tableHeaders = warnings.get(0).getType().getDisplayHeaders();
        tableRows = new String[warnings.size()][];
        for (int i = 0;i<warnings.size();i++){
            tableRows[i] = warnings.get(i).getDisplayData();
        }
        buildPanel();
    }



    /**
     * Sets up each panel elemenent and adds to a vertical boxlayout.
     */
    private void buildPanel(){
        setBackground(DialogConstants.DIALOG_COLOR_MAIN);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        //Explanation text at top
        JLabel explanationLabel = new JLabel(generalExplanationText);
        explanationLabel.setAlignmentX(LEFT_ALIGNMENT);
        int borderWidth = 10;
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
        add(explanationLabel);

        //Table displaying details for each warning instance
        JComponent tablecontainer = createTableComponent();
        add(tablecontainer);

        //Secondary explanation under table
        JLabel secondaryExplanationLabel = new JLabel(secondaryExplanationText);
        secondaryExplanationLabel.setAlignmentX(LEFT_ALIGNMENT);
        secondaryExplanationLabel.setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
        add(secondaryExplanationLabel);

        //Conditional checkbox, allowing user to make decision for orphan rows
        add(createCheckboxPanel(borderWidth));
    }

    /**
     * Adds a blank container at the bottom of the panel. Overridden in CheckBoxWarningPanel to create a panel featuring a checkbox.
     */
    protected JComponent createCheckboxPanel(int borderWidth){
        borderWidth++;
        JLabel spacer = new JLabel(" ");
        spacer.setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
        return spacer;
    }

    /**
     * Creates a JTable to show details specific to each warning instance.
     * @return Container holding the JTable
     */
    private JComponent createTableComponent(){

        //Table Model - holds data
        DefaultTableModel tableModel = new DefaultTableModel(){
            public boolean isCellEditable(int row,int col){
                return false;
            }
        };
        tableModel.setDataVector(tableRows,tableHeaders);


        //Table - displays model
        JTable table = new JTable(tableModel);
        table.setShowGrid(true);
        table.setGridColor(tableGridColor);
        table.getTableHeader().setBackground(tableHeaderColor);
        table.getTableHeader().setReorderingAllowed(false);

        //Disable focus for the table
        table.getTableHeader().setFocusable(false);
        table.setFocusable(false);

        //Default renderer - applies some aesthetic settings including alternating row colors, selection color, and disables selection border
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex){
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);

                //Disable cell selection border
                if (cell instanceof JLabel) {
                    ((JLabel)cell).setBorder(null);
                }

                if (isSelected){
                    cell.setBackground(rowIndex % 2 == 0 ? TableColors.getSelectedEvenColor() : TableColors.getSelectedOddColor());
                    cell.setForeground(Color.BLACK);
                }
                else{
                    cell.setBackground(rowIndex % 2 == 0 ? tableEvenColor : tableOddColor);
                }
                return cell;
            }
        });

        //Scroll Pane - allows the table to scroll
        JScrollPane tableScroller = new JScrollPane(table);
        tableScroller.setPreferredSize(DialogConstants.WARNINGTABLE_DIMS);
        tableScroller.setMaximumSize(DialogConstants.WARNINGTABLE_DIMS);
        tableScroller.setMinimumSize(DialogConstants.WARNINGTABLE_DIMS);
        tableScroller.getVerticalScrollBar().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //Container - centers the table because boxlayout aligning is nonsense
        JPanel tablecontainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tablecontainer.add(tableScroller);
        tablecontainer.setAlignmentX(LEFT_ALIGNMENT);

        return tablecontainer;
    }

}








