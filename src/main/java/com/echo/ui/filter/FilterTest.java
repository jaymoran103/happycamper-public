package com.echo.ui.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Test class for the filter panel components.
 */
public class FilterTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Filter Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            JPanel mainPanel = new JPanel(new BorderLayout());

            // Create a test filter panel
            CollapsibleFilterPanel filterPanel = createTestFilterPanel();
            mainPanel.add(filterPanel, BorderLayout.CENTER);

            // Add a button to test the filter
            JButton testButton = new JButton("Test Filter");
            testButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Testing filter...");
                    filterPanel.notifyFilterChanged();
                    System.out.println("Filter test complete!");
                }
            });
            mainPanel.add(testButton, BorderLayout.SOUTH);

            frame.getContentPane().add(mainPanel);
            frame.setVisible(true);
        });
    }

    private static CollapsibleFilterPanel createTestFilterPanel() {
        CollapsibleFilterPanel panel = new CollapsibleFilterPanel("Test Filter");

        // Create a simple panel with a button
        JPanel contentPanel = new JPanel();
        JButton button = new JButton("Toggle Filter");
        button.addActionListener(e -> {
            System.out.println("Filter toggled!");
            panel.notifyFilterChanged();
        });
        contentPanel.add(button);

        panel.addContent(contentPanel);
        return panel;
    }
}
