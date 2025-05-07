package com.echo.ui.elements;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

public class CancelButton extends HoverButton {
    public CancelButton(String text){
        super(text);
        setupEscListener();
    }
    public CancelButton(){
        super();
        setupEscListener();
    }

    private void setupEscListener() {
        // Add global ESC listener when button is shown
        addAncestorListener(new javax.swing.event.AncestorListener() {
            private KeyEventDispatcher escapeDispatcher = null;

            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                if (escapeDispatcher == null) {
                    escapeDispatcher = e -> {
                        if (e.getID() == KeyEvent.KEY_PRESSED && 
                            e.getKeyCode() == KeyEvent.VK_ESCAPE && 
                            isShowing()) {
                            SwingUtilities.invokeLater(() -> doClick());
                            return true;
                        }
                        return false;
                    };
                    KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .addKeyEventDispatcher(escapeDispatcher);
                }
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                if (escapeDispatcher != null) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .removeKeyEventDispatcher(escapeDispatcher);
                    escapeDispatcher = null;
                }
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });
    }

}
