package com.echo;

import com.echo.service.ViewSettings;
import com.echo.ui.component.TableColors;

/**
 * Simple test program to verify that the ViewSettings class works correctly.
 */
public class ViewSettingsTest {
    
    public static void main(String[] args) {
        // Create a ViewSettings object
        ViewSettings settings = new ViewSettings();
        
        // Print the default values
        System.out.println("Default settings:");
        System.out.println("  Use Display Placeholder: " + settings.isUseDisplayPlaceholder());
        System.out.println("  Use Row Contrast: " + settings.isUseRowContrast());
        System.out.println("  Highlight Empty Data: " + settings.isHighlightEmptyData());
        
        // Change the settings
        settings.setUseDisplayPlaceholder(false);
        settings.setUseRowContrast(false);
        settings.setHighlightEmptyData(true);
        
        // Print the new values
        System.out.println("\nModified settings:");
        System.out.println("  Use Display Placeholder: " + settings.isUseDisplayPlaceholder());
        System.out.println("  Use Row Contrast: " + settings.isUseRowContrast());
        System.out.println("  Highlight Empty Data: " + settings.isHighlightEmptyData());
        
        // Apply the settings
        settings.apply();
        
        // Print the TableColors settings
        System.out.println("\nTableColors settings after apply:");
        System.out.println("  Alternate Shades Enabled: " + TableColors.isAlternateShadesEnabled());
        System.out.println("  Highlight Empty Data Enabled: " + TableColors.isHighlightEmptyDataEnabled());
    }
}
