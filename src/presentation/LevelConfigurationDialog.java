package presentation;

import domain.GameFacade;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Dialog to configure Level parameters (Fruits, Enemies, HotTiles).
 */
public class LevelConfigurationDialog extends JDialog {

    private GameFacade gameFacade;
    private boolean confirmed = false;

    private Map<String, JSpinner> fruitSpinners = new HashMap<>();
    private Map<String, JSpinner> enemySpinners = new HashMap<>();
    private JSpinner hotTileSpinner;

    public LevelConfigurationDialog(Frame owner, GameFacade gameFacade) {
        super(owner, "Configuración del Nivel", true);
        this.gameFacade = gameFacade;

        // Fetch available types via facade
        List<String> availableFruits = gameFacade.getAvailableFruitTypes();
        List<String> availableEnemies = gameFacade.getAvailableEnemyTypes();

        setLayout(new BorderLayout());
        setSize(500, 600);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Fruits Section ---
        mainPanel.add(createSectionHeader("Frutas"));
        JPanel fruitPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        for (String fruit : availableFruits) {
            int currentVal = gameFacade.getFruitCountsConfig().getOrDefault(fruit, 0);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentVal, 0, 50, 1));
            styleSpinner(spinner);
            JLabel label = new JLabel(fruit + ":");
            label.setFont(FontLoader.getInstance().getPlainFont(16f));
            fruitPanel.add(label);
            fruitPanel.add(spinner);
            fruitSpinners.put(fruit, spinner);
        }
        mainPanel.add(fruitPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Enemies Section ---
        mainPanel.add(createSectionHeader("Enemigos"));
        JPanel enemyPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        for (String enemy : availableEnemies) {
            int currentVal = gameFacade.getEnemyCountsConfig().getOrDefault(enemy, 0);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentVal, 0, 10, 1));
            styleSpinner(spinner);
            JLabel label = new JLabel(enemy + ":");
            label.setFont(FontLoader.getInstance().getPlainFont(16f));
            enemyPanel.add(label);
            enemyPanel.add(spinner);
            enemySpinners.put(enemy, spinner);
        }
        mainPanel.add(enemyPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Obstacles Section ---
        mainPanel.add(createSectionHeader("Obstáculos"));
        JPanel obstaclePanel = new JPanel(new GridLayout(0, 2, 10, 5));
        hotTileSpinner = new JSpinner(new SpinnerNumberModel(gameFacade.getHotTileCountConfig(), 0, 20, 1));
        styleSpinner(hotTileSpinner);
        JLabel label = new JLabel("Baldosas Calientes:");
        label.setFont(FontLoader.getInstance().getPlainFont(16f));
        obstaclePanel.add(label);
        obstaclePanel.add(hotTileSpinner);
        mainPanel.add(obstaclePanel);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Comenzar");
        okButton.setFont(FontLoader.getInstance().getBoldFont(18f));
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.setFont(FontLoader.getInstance().getBoldFont(18f));

        okButton.addActionListener(e -> {
            updateConfiguration();
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontLoader.getInstance().getBoldFont(20f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        return label;
    }

    private void styleSpinner(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            defaultEditor.getTextField().setFont(FontLoader.getInstance().getPlainFont(16f));
        }
    }

    private void updateConfiguration() {
        // Update Fruit Counts
        for (Map.Entry<String, JSpinner> entry : fruitSpinners.entrySet()) {
            gameFacade.setFruitCountConfig(entry.getKey(), (Integer) entry.getValue().getValue());
        }
        // Update Enemy Counts
        for (Map.Entry<String, JSpinner> entry : enemySpinners.entrySet()) {
            gameFacade.setEnemyCountConfig(entry.getKey(), (Integer) entry.getValue().getValue());
        }
        // Update Hot Tiles
        gameFacade.setHotTileCountConfig((Integer) hotTileSpinner.getValue());
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
