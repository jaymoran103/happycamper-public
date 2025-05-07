package com.echo.ui.selector;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.echo.ui.dialog.ColumnSizingOption;
import com.echo.ui.dialog.DialogUtils;

/**
 * InputSelector for selecting one option from a list of enum values.
 * Uses radio buttons to represent the options.
 * 
 * @param <E> The enum type this selector works with
 */
public class RadioButtonSelector<E extends Enum<E>> extends InputSelector<E> {
    private final E[] enumValues;
    private final String[] displayNames;
    private E currentSelection;
    private JRadioButton[] radioButtons;

    /**
     * Creates a new RadioButtonSelector with the specified enum values.
     *
     * @param title The title for this selector
     * @param enumValues The available enum values
     * @param defaultSelection The initially selected enum value
     * @param displayNameFunction Function to get display name from enum value
     */
    public RadioButtonSelector(String title, E[] enumValues, E defaultSelection, java.util.function.Function<E, String> displayNameFunction) {
        super(title);
        this.enumValues = enumValues;
        this.currentSelection = defaultSelection;
        
        // Create display names array
        this.displayNames = new String[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            this.displayNames[i] = displayNameFunction.apply(enumValues[i]);
        }
    }
    
    /**
     * Creates a new RadioButtonSelector specifically for ColumnSizingOption.
     * This is a convenience constructor for backward compatibility.
     *
     * @param title The title for this selector
     * @param defaultSelection The initially selected option
     * @return A new RadioButtonSelector for ColumnSizingOption
     */
    public static RadioButtonSelector<ColumnSizingOption> forColumnSizing(String title, ColumnSizingOption defaultSelection) {
        return new RadioButtonSelector<>(
            title, 
            ColumnSizingOption.values(), 
            defaultSelection, 
            ColumnSizingOption::getDisplayName
        );
    }

    @Override
    protected void buildSelectorPanel(JPanel panel) {
        // Create a panel for the radio buttons using our helper method
        JPanel radioPanel = DialogUtils.createAlignedBoxPanel();

        // Create button group and radio buttons
        ButtonGroup group = new ButtonGroup();
        radioButtons = new JRadioButton[enumValues.length];

        for (int i = 0; i < enumValues.length; i++) {
            radioButtons[i] = new JRadioButton(displayNames[i]);
            radioButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            // Set selected if this is the current selection
            if (enumValues[i].equals(currentSelection)) {
                radioButtons[i].setSelected(true);
            }

            // Add action listener to update selection
            final int index = i;
            radioButtons[i].addActionListener(e -> {
                currentSelection = enumValues[index];
                notifyUpdateCallback();
            });

            // Add to group and panel
            group.add(radioButtons[i]);
            radioPanel.add(radioButtons[i]);
        }

        // Add a small amount of bottom padding
        DialogUtils.addVerticalSpacing(radioPanel, 5);

        // Set preferred size based on number of options
        int radioButtonHeight = 25; // Height of each radio button
        this.componentHeight = radioButtons.length * radioButtonHeight + 35; // button heights + padding

        panel.add(radioPanel);
    }

    @Override
    public E getValue() {
        return currentSelection;
    }

    @Override
    public void setValue(E value) {
        if (value != null) {
            for (int i = 0; i < enumValues.length; i++) {
                if (enumValues[i].equals(value)) {
                    currentSelection = value;

                    if (radioButtons != null) {
                        radioButtons[i].setSelected(true);
                    }

                    break;
                }
            }
        }
    }

    @Override
    public boolean hasSelection() {
        return currentSelection != null;
    }
}
