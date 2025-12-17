package presentation;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Helper class for shared UI components and factories.
 * Reduces code duplication across panels.
 */
public class UIHelper {

    /**
     * Creates and adds a standardized Back Button to a container.
     * Handles hover effects and positioning.
     *
     * @param container    The container (JPanel) to add the button to.
     * @param resources    The ResourceLoader instance.
     * @param windowHeight The height of the window (for positioning).
     * @param action       The action to perform when clicked.
     */
    public static void addBackButton(JPanel container, ResourceLoader resources, int windowHeight, Runnable action) {
        int backButtonWidth = 200;
        int backButtonHeight = 100;
        int backButtonWidthHover = 220;
        int backButtonHeightHover = 110;

        Image normalButton = resources.backImage.getScaledInstance(backButtonWidth, backButtonHeight,
                Image.SCALE_SMOOTH);
        Image hoverButton = resources.backImage.getScaledInstance(backButtonWidthHover, backButtonHeightHover,
                Image.SCALE_SMOOTH);

        JLabel backButton = new JLabel(new ImageIcon(normalButton));
        int backX = 30;
        int backY = windowHeight - backButtonHeight - 30;
        backButton.setBounds(backX, backY, backButtonWidth, backButtonHeight);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final int originalX = backX;
        final int originalY = backY;

        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setIcon(new ImageIcon(hoverButton));
                int newX = originalX - (backButtonWidthHover - backButtonWidth) / 2;
                int newY = originalY - (backButtonHeightHover - backButtonHeight) / 2;
                backButton.setBounds(newX, newY, backButtonWidthHover, backButtonHeightHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setIcon(new ImageIcon(normalButton));
                backButton.setBounds(originalX, originalY, backButtonWidth, backButtonHeight);
            }
        });

        container.add(backButton);
    }
}
