package presentation;

import java.awt.*;
import java.io.File;

public class FontLoader {

    private Font customFont;
    private static FontLoader instance;

    private FontLoader() {
        loadCustomFont();
    }

    public static FontLoader getInstance() {
        if (instance == null) {
            instance = new FontLoader();
        }
        return instance;
    }

    private void loadCustomFont() {
        try {
            // Buscar archivos .ttf en la carpeta font
            File fontDir = new File("Resources/font");
            File[] fontFiles = fontDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));

            if (fontFiles != null && fontFiles.length > 0) {
                // Cargar el primer archivo .ttf encontrado
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontFiles[0]);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);

                domain.BadDopoLogger.logInfo("✓ Fuente personalizada cargada: " + fontFiles[0].getName());
            } else {
                domain.BadDopoLogger.logSevere("⚠ No se encontró archivo .ttf en Resources/font");
                customFont = new Font("Arial", Font.PLAIN, 24); // Fuente por defecto
            }
        } catch (Exception e) {
            domain.BadDopoLogger.logError("Error cargando fuente personalizada: " + e.getMessage(), e);
            customFont = new Font("Arial", Font.PLAIN, 24); // Fuente por defecto
        }
    }

    public Font getFont(float size) {
        return customFont.deriveFont(size);
    }

    public Font getFont(int style, float size) {
        return customFont.deriveFont(style, size);
    }

    public Font getBoldFont(float size) {
        return customFont.deriveFont(Font.BOLD, size);
    }

    public Font getPlainFont(float size) {
        return customFont.deriveFont(Font.PLAIN, size);
    }
}