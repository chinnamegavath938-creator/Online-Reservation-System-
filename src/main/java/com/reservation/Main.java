package com.reservation;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Database.initialize();
            SwingUtilities.invokeLater(() -> new RoleSelectionFrame().setVisible(true));
        } catch (Exception exception) {
            exception.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "The reservation database could not be started:\n" + exception.getMessage(),
                    "Startup error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
