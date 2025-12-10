package presentation;

import domain.dto.LevelConfigurationDTO;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Dialog to configure Level parameters (Fruits, Enemies, HotTiles).
 */
public class LevelConfigurationDialog extends JDialog {

    private LevelConfigurationDTO configuration;
    private boolean confirmed = false;

    private Map<String, JSpinner> fruitSpinners = new HashMap<>();
    private Map<String, JSpinner> enemySpinners = new HashMap<>();
    private JSpinner hotTileSpinner;

    public LevelConfigurationDialog(Frame owner, LevelConfigurationDTO currentConfig, List<String> availableFruits,
            List<String> availableEnemies) {
        super(owner, "Configuración del Nivel", true);
        this.configuration = currentConfig;

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
            int currentVal = currentConfig.getFruitCounts().getOrDefault(fruit, 0);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentVal, 0, 50, 1));
            fruitPanel.add(new JLabel(fruit + ":"));
            fruitPanel.add(spinner);
            fruitSpinners.put(fruit, spinner);
        }
        mainPanel.add(fruitPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Enemies Section ---
        mainPanel.add(createSectionHeader("Enemigos"));
        JPanel enemyPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        for (String enemy : availableEnemies) {
            int currentVal = currentConfig.getEnemyCounts().getOrDefault(enemy, 0);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentVal, 0, 10, 1));
            enemyPanel.add(new JLabel(enemy + ":"));
            enemyPanel.add(spinner);
            enemySpinners.put(enemy, spinner);
        }
        mainPanel.add(enemyPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Obstacles Section ---
        mainPanel.add(createSectionHeader("Obstáculos"));
        JPanel obstaclePanel = new JPanel(new GridLayout(0, 2, 10, 5));
        hotTileSpinner = new JSpinner(new SpinnerNumberModel(currentConfig.getHotTileCount(), 0, 20, 1));
        obstaclePanel.add(new JLabel("Baldosas Calientes:"));
        obstaclePanel.add(hotTileSpinner);
        mainPanel.add(obstaclePanel);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Comenzar");
        JButton cancelButton = new JButton("Cancelar");

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
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void updateConfiguration() {
        // Update Fruit Counts
        for (Map.Entry<String, JSpinner> entry : fruitSpinners.entrySet()) {
            configuration.addFruit(entry.getKey(), (Integer) entry.getValue().getValue());
        }
        // Update Enemy Counts
        for (Map.Entry<String, JSpinner> entry : enemySpinners.entrySet()) {
            configuration.addEnemy(entry.getKey(), (Integer) entry.getValue().getValue());
        }
        // Update Hot Tiles
        configuration.setHotTileCount((Integer) hotTileSpinner.getValue());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LevelConfigurationDTO getConfiguration() {
        return configuration;
    }
}
