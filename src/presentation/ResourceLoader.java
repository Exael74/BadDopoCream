package presentation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import domain.entity.FruitState;

public class ResourceLoader {

    // Imágenes de fondo y títulos
    public Image wallpaperImage;
    public ImageIcon titleGif;
    public ImageIcon characterSelectionGif;
    public ImageIcon levelSelectionGif;

    // Botones
    public Image startButtonImage;
    public Image startGameImage;
    public Image onePlayerImage;
    public Image twoPlayerImage;
    public Image machineVsMachineImage;
    public Image p1VsP2Image; // New
    public Image p1VsMachineImage; // New
    public Image optionsImage;
    public Image backImage;

    // Botones de niveles
    public Image nivel1Image;
    public Image nivel2Image;
    public Image nivel3Image;
    public Image nivel4Image;

    // Personajes (selección)
    public Image chocolateImage;
    public Image fresaImage;
    public Image vainillaImage;
    public Image marcoSeleccionImage;

    // === RECURSOS DEL JUEGO ===

    // Fondo del mapa
    public Image fondoMapa;

    // GIFs de personajes - CHOCOLATE
    public ImageIcon chocolateIdleUpGif;
    public ImageIcon chocolateIdleDownGif;
    public ImageIcon chocolateIdleLeftGif;
    public ImageIcon chocolateIdleRightGif;
    public ImageIcon chocolateWalkUpGif;
    public ImageIcon chocolateWalkDownGif;
    public ImageIcon chocolateWalkLeftGif;
    public ImageIcon chocolateWalkRightGif;
    public ImageIcon chocolateSneezeUpGif;
    public ImageIcon chocolateSneezeDownGif;
    public ImageIcon chocolateSneezeLeftGif;
    public ImageIcon chocolateSneezeRightGif;
    public ImageIcon chocolateKickGif;
    public ImageIcon chocolateDeathGif;
    public ImageIcon chocolateVictoryGif;

    // GIFs de personajes - ROSA/FRESA
    public ImageIcon rosaIdleUpGif;
    public ImageIcon rosaIdleDownGif;
    public ImageIcon rosaIdleLeftGif;
    public ImageIcon rosaIdleRightGif;
    public ImageIcon rosaWalkUpGif;
    public ImageIcon rosaWalkDownGif;
    public ImageIcon rosaWalkLeftGif;
    public ImageIcon rosaWalkRightGif;
    public ImageIcon rosaSneezeUpGif;
    public ImageIcon rosaSneezeDownGif;
    public ImageIcon rosaSneezeLeftGif;
    public ImageIcon rosaSneezeRightGif;
    public ImageIcon rosaKickGif;
    public ImageIcon rosaDeathGif;
    public ImageIcon rosaVictoryGif;

    // GIFs de personajes - VAINILLA
    public ImageIcon vainillaIdleUpGif;
    public ImageIcon vainillaIdleDownGif;
    public ImageIcon vainillaIdleLeftGif;
    public ImageIcon vainillaIdleRightGif;
    public ImageIcon vainillaWalkUpGif;
    public ImageIcon vainillaWalkDownGif;
    public ImageIcon vainillaWalkLeftGif;
    public ImageIcon vainillaWalkRightGif;
    public ImageIcon vainillaSneezeUpGif;
    public ImageIcon vainillaSneezeDownGif;
    public ImageIcon vainillaSneezeLeftGif;
    public ImageIcon vainillaSneezeRightGif;
    public ImageIcon vainillaKickGif;
    public ImageIcon vainillaDeathGif;
    public ImageIcon vainillaVictoryGif;

    // Enemigos - TROLL
    public ImageIcon trollIdleGif;
    public ImageIcon trollWalkUpGif;
    public ImageIcon trollWalkDownGif;
    public ImageIcon trollWalkLeftGif;
    public ImageIcon trollWalkRightGif;

    // Enemigos - MACETA
    public ImageIcon macetaWalkUpGif;
    public ImageIcon macetaWalkDownGif;
    public ImageIcon macetaWalkLeftGif;
    public ImageIcon macetaWalkRightGif;

    // Enemigos - CALAMAR
    public ImageIcon calamarWalkUpGif;
    public ImageIcon calamarWalkDownGif;
    public ImageIcon calamarWalkLeftGif;
    public ImageIcon calamarWalkRightGif;
    public ImageIcon calamarBreakUpGif;
    public ImageIcon calamarBreakDownGif;
    public ImageIcon calamarBreakLeftGif;
    public ImageIcon calamarBreakRightGif;

    // Enemigos - NARVAL
    public ImageIcon narvalWalkUpGif;
    public ImageIcon narvalWalkDownGif;
    public ImageIcon narvalWalkLeftGif;
    public ImageIcon narvalWalkRightGif;
    public ImageIcon narvalBreakUpGif;
    public ImageIcon narvalBreakDownGif;
    public ImageIcon narvalBreakLeftGif;
    public ImageIcon narvalBreakRightGif;
    public ImageIcon narvalDrillUpGif;
    public ImageIcon narvalDrillDownGif;
    public ImageIcon narvalDrillLeftGif;
    public ImageIcon narvalDrillRightGif;

    // Hielo
    public Image iceBlockNormalImage;
    public Image iceBlockBrokenImage;

    // Frutas (Animated)
    private java.util.Map<String, javax.swing.ImageIcon> fruitGifs; // To store dynamic states? No, individual fields
                                                                    // easier for now.

    // Frutas (Legacy/Backwards Compat)
    public ImageIcon uvaImage;
    public ImageIcon platanoImage;
    public ImageIcon pinaImage;
    public ImageIcon cerezaImage; // Will reuse idle

    // Cactus
    public javax.swing.ImageIcon cactusSpawnGif;
    public javax.swing.ImageIcon cactusIdleGif;
    public javax.swing.ImageIcon cactusSpikesWarningGif; // generando_espinas
    public javax.swing.ImageIcon cactusSpikesActiveGif; // con_espinas
    public javax.swing.ImageIcon cactusSpikesCooldownGif; // quitando_espinas
    public javax.swing.ImageIcon cactusCollectedGif;

    // Cereza
    public javax.swing.ImageIcon cherrySpawnGif;
    public javax.swing.ImageIcon cherryIdleGif;
    public javax.swing.ImageIcon cherryTeleportOutGif;
    public javax.swing.ImageIcon cherryTeleportInGif; // llegando
    public javax.swing.ImageIcon cherryCollectedGif;

    // Piña
    public javax.swing.ImageIcon pineappleSpawnGif;
    public javax.swing.ImageIcon pineappleIdleGif;
    public javax.swing.ImageIcon pineappleCollectedGif;

    // Platano
    public javax.swing.ImageIcon bananaSpawnGif;
    public javax.swing.ImageIcon bananaIdleGif;
    public javax.swing.ImageIcon bananaCollectedGif;

    // Uva
    public javax.swing.ImageIcon grapeSpawnGif;
    public javax.swing.ImageIcon grapeIdleGif;
    public javax.swing.ImageIcon grapeCollectedGif;

    // Baldosa caliente
    public ImageIcon hotTileImage;

    // Iglu y Bloques
    public ImageIcon igluImage;
    public ImageIcon unbreakableBlockImage;

    public ResourceLoader() {
        loadAllImages();
    }

    private void loadAllImages() {
        try {
            // ========== RECURSOS DE MENÚS ==========

            wallpaperImage = ImageIO.read(new File("Resources/Wallpaper.jpg"));
            titleGif = new ImageIcon("Resources/Titulo/BAD-DOPO-CREAM-11-21-2025 (1).gif");
            characterSelectionGif = new ImageIcon("Resources/Titulo/character_selection.gif");
            levelSelectionGif = new ImageIcon("Resources/Titulo/level_selection.gif");

            startButtonImage = ImageIO.read(new File("Resources/Buttons/Start.png"));
            startGameImage = ImageIO.read(new File("Resources/Buttons/Start Game.png"));
            onePlayerImage = ImageIO.read(new File("Resources/Buttons/1 Player.png"));
            twoPlayerImage = ImageIO.read(new File("Resources/Buttons/2 Player.png"));
            machineVsMachineImage = ImageIO.read(new File("Resources/Buttons/Machine vs Machine.png"));
            p1VsP2Image = ImageIO.read(new File("Resources/Buttons/Player 1 vs Player 2.png"));
            p1VsMachineImage = ImageIO.read(new File("Resources/Buttons/Player 1 vs Machine.png"));
            optionsImage = ImageIO.read(new File("Resources/Buttons/Options.png"));
            backImage = ImageIO.read(new File("Resources/Buttons/Back.png"));

            nivel1Image = ImageIO.read(new File("Resources/Buttons/Nivel 1.png"));
            nivel2Image = ImageIO.read(new File("Resources/Buttons/nivel 2.png"));
            nivel3Image = ImageIO.read(new File("Resources/Buttons/nivel 3.png"));
            nivel4Image = ImageIO.read(new File("Resources/Buttons/nivel 4.png"));

            chocolateImage = ImageIO
                    .read(new File("Resources/Helados/Chocolate/Gif/chocolate_quieto_abajo animation.gif"));
            fresaImage = ImageIO.read(new File("Resources/Helados/Rosa/Gif/rosa_quieto_abajo animation.gif"));
            vainillaImage = ImageIO
                    .read(new File("Resources/Helados/Vainilla/Gif/vainilla_quieto_abajo animation.gif"));
            marcoSeleccionImage = ImageIO.read(new File("Resources/Seleccion de personaje/Marco_Seleccion.png"));

            // ========== RECURSOS DEL JUEGO ==========

            fondoMapa = ImageIO.read(new File("Resources/Map/fondo mapa.png"));

            // CHOCOLATE
            chocolateIdleUpGif = new ImageIcon("Resources/Helados/Chocolate/Gif/chocolate_quieto_arriba animation.gif");
            chocolateIdleDownGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_quieto_abajo animation.gif");
            chocolateIdleLeftGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_quieto_izquierda animation.gif");
            chocolateIdleRightGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_quieto_derecha animation.gif");
            chocolateWalkUpGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_caminando_arriba animation.gif");
            chocolateWalkDownGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_caminando_abajo animation.gif");
            chocolateWalkLeftGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_caminando_izquierda animation.gif");
            chocolateWalkRightGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_caminando_derecha animation.gif");
            chocolateSneezeUpGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_estornudo_arriba animation.gif");
            chocolateSneezeDownGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_estornudo_abajo animation.gif");
            chocolateSneezeLeftGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_estornudo_izquierda animation.gif");
            chocolateSneezeRightGif = new ImageIcon(
                    "Resources/Helados/Chocolate/Gif/chocolate_estornudo_derecha animation.gif");
            chocolateKickGif = new ImageIcon("Resources/Helados/Chocolate/Gif/chocolate_patada animation.gif");
            chocolateDeathGif = new ImageIcon("Resources/Helados/Chocolate/Gif/chocolate_muerte animation.gif");
            chocolateVictoryGif = new ImageIcon("Resources/Helados/Chocolate/Gif/chocolate_victoria animation.gif");

            // ROSA
            rosaIdleUpGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_quieto_arriba animation.gif");
            rosaIdleDownGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_quieto_abajo animation.gif");
            rosaIdleLeftGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_quieto_izquierda animation.gif");
            rosaIdleRightGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_quieto_derecha animation.gif");
            rosaWalkUpGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_caminando_arriba animation.gif");
            rosaWalkDownGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_caminando_abajo animation.gif");
            rosaWalkLeftGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_caminando_izquierda animation.gif");
            rosaWalkRightGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_caminando_derecha animation.gif");
            rosaSneezeUpGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_estornudo_arriba animation.gif");
            rosaSneezeDownGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_estornudo_abajo animation.gif");
            rosaSneezeLeftGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_estornudo_izquierda animation.gif");
            rosaSneezeRightGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_estornudo_derecha animation.gif");
            rosaKickGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_patada animation.gif");
            rosaDeathGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_muerte animation.gif");
            rosaVictoryGif = new ImageIcon("Resources/Helados/Rosa/Gif/rosa_victoria animation.gif");

            // VAINILLA
            vainillaIdleUpGif = new ImageIcon("Resources/Helados/Vainilla/Gif/vainilla_quieto_arriba animation.gif");
            vainillaIdleDownGif = new ImageIcon("Resources/Helados/Vainilla/Gif/vainilla_quieto_abajo animation.gif");
            vainillaIdleLeftGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_quieto_izquierda animation.gif");
            vainillaIdleRightGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_quieto_derecha animation.gif");
            vainillaWalkUpGif = new ImageIcon("Resources/Helados/Vainilla/Gif/vainilla_caminando_arriba animation.gif");
            vainillaWalkDownGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_caminando_abajo animation.gif");
            vainillaWalkLeftGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_caminando_izquierda animation.gif");
            vainillaWalkRightGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_caminando_derecha animation.gif");
            vainillaSneezeUpGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_estornudo_arriba animation.gif");
            vainillaSneezeDownGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_estornudo_abajo animation.gif");
            vainillaSneezeLeftGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_estornudo_izquierda animation.gif");
            vainillaSneezeRightGif = new ImageIcon(
                    "Resources/Helados/Vainilla/Gif/vainilla_estornudo_derecha animation.gif");
            vainillaKickGif = new ImageIcon("Resources/Helados/Vainilla/Gif/vainilla_patada animation.gif");
            vainillaDeathGif = new ImageIcon("Resources/Helados/Vainilla/Gif/vainilla_muerte animation.gif");
            vainillaVictoryGif = new ImageIcon("Resources/Helados/Vainilla/Gif/vainilla_victoria animation.gif");

            // TROLLS
            trollIdleGif = new ImageIcon("Resources/Enemigos/Troll/GIF/troll_preguntas animation.gif");
            trollWalkUpGif = new ImageIcon("Resources/Enemigos/Troll/GIF/troll_caminando_arriba animation.gif");
            trollWalkDownGif = new ImageIcon("Resources/Enemigos/Troll/GIF/troll_caminando_abajo animation.gif");
            trollWalkLeftGif = new ImageIcon("Resources/Enemigos/Troll/GIF/troll_caminando_izquierda animation.gif");
            trollWalkRightGif = new ImageIcon("Resources/Enemigos/Troll/GIF/troll_caminando_derecha animation.gif");

            // MACETAS
            macetaWalkUpGif = new ImageIcon("Resources/Enemigos/Maceta/GIF/maceta-caminando-arriba.gif");
            macetaWalkDownGif = new ImageIcon("Resources/Enemigos/Maceta/GIF/maceta_caminando_abajo.gif");
            macetaWalkLeftGif = new ImageIcon("Resources/Enemigos/Maceta/GIF/maceta-caminando_izquierda.gif");
            macetaWalkRightGif = new ImageIcon("Resources/Enemigos/Maceta/GIF/maceta-caminando-derecha.gif");

            // CALAMARES
            calamarWalkUpGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_caminando_arriba animation.gif");
            calamarWalkDownGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_caminando_abajo animation.gif");
            calamarWalkLeftGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_caminando_izquierda animation.gif");
            calamarWalkRightGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_caminando_derecha animation.gif");
            calamarBreakUpGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_rompe_hielo_arriba animation.gif");
            calamarBreakDownGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_ropmpe_hielo_abajo animation.gif");
            calamarBreakLeftGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_rompe_hielo_izquierda animation.gif");
            calamarBreakRightGif = new ImageIcon(
                    "Resources/Enemigos/Calamar/SpriteSheet_Without_Background/calamar_rompe_hielo_derecha animation.gif");

            // NARVAL
            narvalWalkUpGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_caminando_arriba.gif");
            narvalWalkDownGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_caminando_abajo.gif");
            narvalWalkLeftGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_caminando_izquierda.gif");
            narvalWalkRightGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_caminando_derecha.gif");

            narvalBreakUpGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_rompiendoHielo_arriba.gif");
            narvalBreakDownGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_rompiendoHielo_abajo.gif");
            narvalBreakLeftGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_rompiendoHielo_izquierda.gif");
            narvalBreakRightGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_rompiendoHielo_derecha.gif");

            narvalDrillUpGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_drill_arriba.gif");
            narvalDrillDownGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_drill_abajo.gif");
            narvalDrillLeftGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_drill_izquierda.gif"); // Uppercase
                                                                                                            // 'N'
                                                                                                            // requested
                                                                                                            // by User
            narvalDrillRightGif = new ImageIcon("Resources/Enemigos/Narval/GIF/Narval_drill_derecha.gif");

            // HIELO
            iceBlockNormalImage = ImageIO.read(new File("Resources/Hielo/GIF/Screenshot 2025-11-23 005254.png"));
            iceBlockBrokenImage = ImageIO.read(new File("Resources/Hielo/GIF/Screenshot 2025-11-23 005304.png"));

            // FRUTAS - CACTUS
            cactusSpawnGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_apareciendo.gif");
            cactusIdleGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_normal.gif");
            cactusSpikesWarningGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_generando_espinas.gif");
            cactusSpikesActiveGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_con_espinas.gif");
            cactusSpikesCooldownGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_quitando_espinas.gif");
            cactusCollectedGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_recolectado.gif");

            // FRUTAS - CEREZA
            cherrySpawnGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_apareciendo.gif");
            cherryIdleGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_normal.gif");
            cherryTeleportOutGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_teletransportando.gif");
            cherryTeleportInGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_llegando_teletransporte.gif");
            cherryCollectedGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_recolectada.gif");
            cerezaImage = cherryIdleGif; // Backward compat

            // FRUTAS - PIÑA
            pineappleSpawnGif = new ImageIcon("Resources/Frutas/GIF/Piña/piña_apareciendo.gif");
            pineappleIdleGif = new ImageIcon("Resources/Frutas/GIF/Piña/piña_normal.gif");
            pineappleCollectedGif = new ImageIcon("Resources/Frutas/GIF/Piña/piña_recolectado.gif");
            pinaImage = pineappleIdleGif;

            // FRUTAS - PLATANO
            bananaSpawnGif = new ImageIcon("Resources/Frutas/GIF/Platano/platano_apareciendo.gif");
            bananaIdleGif = new ImageIcon("Resources/Frutas/GIF/Platano/platano_normal.gif");
            bananaCollectedGif = new ImageIcon("Resources/Frutas/GIF/Platano/platano_recolectado.gif");
            platanoImage = bananaIdleGif;

            // FRUTAS - UVA
            grapeSpawnGif = new ImageIcon("Resources/Frutas/GIF/Uvas/uva_apareciendo.gif");
            grapeIdleGif = new ImageIcon("Resources/Frutas/GIF/Uvas/uva_normal.gif");
            grapeCollectedGif = new ImageIcon("Resources/Frutas/GIF/Uvas/uva_recolectada.gif");
            uvaImage = grapeIdleGif;

            // FRUTAS - CACTUS
            cactusSpawnGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_apareciendo.gif");
            cactusIdleGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_normal.gif");
            cactusSpikesWarningGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_generando_espinas.gif");
            cactusSpikesActiveGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_con_espinas.gif");
            cactusSpikesCooldownGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_quitando_espinas.gif");
            cactusCollectedGif = new ImageIcon("Resources/Frutas/GIF/Cactus/cactus_recolectado.gif");

            // FRUTAS - CEREZA
            cherrySpawnGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_apareciendo.gif");
            cherryIdleGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_normal.gif");
            cherryTeleportOutGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_teletransportando.gif");
            cherryTeleportInGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_llegando_teletransporte.gif");
            cherryCollectedGif = new ImageIcon("Resources/Frutas/GIF/Cereza/cereza_recolectada.gif");
            cerezaImage = cherryIdleGif; // Backward compat

            // FRUTAS - PIÑA
            pineappleSpawnGif = new ImageIcon("Resources/Frutas/GIF/Piña/piña_apareciendo.gif");
            pineappleIdleGif = new ImageIcon("Resources/Frutas/GIF/Piña/piña_normal.gif");
            pineappleCollectedGif = new ImageIcon("Resources/Frutas/GIF/Piña/piña_recolectado.gif");
            pinaImage = pineappleIdleGif;

            // FRUTAS - PLATANO
            bananaSpawnGif = new ImageIcon("Resources/Frutas/GIF/Platano/platano_apareciendo.gif");
            bananaIdleGif = new ImageIcon("Resources/Frutas/GIF/Platano/platano_normal.gif");
            bananaCollectedGif = new ImageIcon("Resources/Frutas/GIF/Platano/platano_recolectado.gif");
            platanoImage = bananaIdleGif;

            // FRUTAS - UVA
            grapeSpawnGif = new ImageIcon("Resources/Frutas/GIF/Uvas/uva_apareciendo.gif");
            grapeIdleGif = new ImageIcon("Resources/Frutas/GIF/Uvas/uva_normal.gif");
            grapeCollectedGif = new ImageIcon("Resources/Frutas/GIF/Uvas/uva_recolectada.gif");
            uvaImage = grapeIdleGif;

            // BALDOSA CALIENTE
            hotTileImage = new ImageIcon("Resources/Baldosa caliente/baldosa_caliente.gif");

            // IGLU Y BLOQUES
            igluImage = new ImageIcon("Resources/iglu_wallpaper.png");
            unbreakableBlockImage = new ImageIcon("Resources/Bloque_Irrompible/Bloque_irrompible.jpg");

        } catch (Exception e) {
            domain.BadDopoLogger.logError("Error cargando imágenes: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public ImageIcon getPlayerGif(String characterType, String direction, boolean isMoving, boolean isSneezing,
            boolean isKicking, boolean isDying, boolean isCelebrating) {
        String character = characterType.toLowerCase();

        if (isDying) {
            if (character.equals("chocolate")) {
                return chocolateDeathGif;
            } else if (character.equals("fresa")) {
                return rosaDeathGif;
            } else {
                return vainillaDeathGif;
            }
        } else if (isCelebrating) {
            if (character.equals("chocolate")) {
                return chocolateVictoryGif;
            } else if (character.equals("fresa")) {
                return rosaVictoryGif;
            } else {
                return vainillaVictoryGif;
            }
        } else if (isKicking) {
            if (character.equals("chocolate")) {
                return chocolateKickGif;
            } else if (character.equals("fresa")) {
                return rosaKickGif;
            } else {
                return vainillaKickGif;
            }
        } else if (isSneezing) {
            switch (direction) {
                case "UP":
                    return character.equals("chocolate") ? chocolateSneezeUpGif
                            : character.equals("fresa") ? rosaSneezeUpGif : vainillaSneezeUpGif;
                case "DOWN":
                    return character.equals("chocolate") ? chocolateSneezeDownGif
                            : character.equals("fresa") ? rosaSneezeDownGif : vainillaSneezeDownGif;
                case "LEFT":
                    return character.equals("chocolate") ? chocolateSneezeLeftGif
                            : character.equals("fresa") ? rosaSneezeLeftGif : vainillaSneezeLeftGif;
                case "RIGHT":
                    return character.equals("chocolate") ? chocolateSneezeRightGif
                            : character.equals("fresa") ? rosaSneezeRightGif : vainillaSneezeRightGif;
            }
        } else if (isMoving) {
            switch (direction) {
                case "UP":
                    return character.equals("chocolate") ? chocolateWalkUpGif
                            : character.equals("fresa") ? rosaWalkUpGif : vainillaWalkUpGif;
                case "DOWN":
                    return character.equals("chocolate") ? chocolateWalkDownGif
                            : character.equals("fresa") ? rosaWalkDownGif : vainillaWalkDownGif;
                case "LEFT":
                    return character.equals("chocolate") ? chocolateWalkLeftGif
                            : character.equals("fresa") ? rosaWalkLeftGif : vainillaWalkLeftGif;
                case "RIGHT":
                    return character.equals("chocolate") ? chocolateWalkRightGif
                            : character.equals("fresa") ? rosaWalkRightGif : vainillaWalkRightGif;
            }
        } else {
            switch (direction) {
                case "UP":
                    return character.equals("chocolate") ? chocolateIdleUpGif
                            : character.equals("fresa") ? rosaIdleUpGif : vainillaIdleUpGif;
                case "DOWN":
                    return character.equals("chocolate") ? chocolateIdleDownGif
                            : character.equals("fresa") ? rosaIdleDownGif : vainillaIdleDownGif;
                case "LEFT":
                    return character.equals("chocolate") ? chocolateIdleLeftGif
                            : character.equals("fresa") ? rosaIdleLeftGif : vainillaIdleLeftGif;
                case "RIGHT":
                    return character.equals("chocolate") ? chocolateIdleRightGif
                            : character.equals("fresa") ? rosaIdleRightGif : vainillaIdleRightGif;
            }
        }

        return vainillaIdleDownGif;
    }

    public ImageIcon getEnemyGif(String enemyType, String direction, boolean isBreakingIce) {
        if (enemyType.equals("TROLL")) {
            return getTrollGif(direction);
        } else if (enemyType.equals("MACETA")) {
            return getMacetaGif(direction);
        } else if (enemyType.equals("CALAMAR")) {
            return getCalamarGif(direction, isBreakingIce);
        } else if (enemyType.equals("NARVAL")) {
            return getNarvalGif(direction, isBreakingIce, false); // Default no drill if called generally
        }
        return trollIdleGif;
    }

    public ImageIcon getNarvalGif(String direction, boolean isBreakingIce, boolean isDrilling) {
        if (isDrilling) {
            switch (direction) {
                case "UP":
                    return narvalDrillUpGif;
                case "DOWN":
                    return narvalDrillDownGif;
                case "LEFT":
                    return narvalDrillLeftGif;
                case "RIGHT":
                    return narvalDrillRightGif;
                default:
                    return narvalDrillDownGif;
            }
        }
        if (isBreakingIce) {
            switch (direction) {
                case "UP":
                    return narvalBreakUpGif;
                case "DOWN":
                    return narvalBreakDownGif;
                case "LEFT":
                    return narvalBreakLeftGif;
                case "RIGHT":
                    return narvalBreakRightGif;
                default:
                    return narvalBreakDownGif;
            }
        } else {
            switch (direction) {
                case "UP":
                    return narvalWalkUpGif;
                case "DOWN":
                    return narvalWalkDownGif;
                case "LEFT":
                    return narvalWalkLeftGif;
                case "RIGHT":
                    return narvalWalkRightGif;
                default:
                    return narvalWalkDownGif;
            }
        }
    }

    public ImageIcon getTrollGif(String direction) {
        switch (direction) {
            case "UP":
                return trollWalkUpGif;
            case "DOWN":
                return trollWalkDownGif;
            case "LEFT":
                return trollWalkLeftGif;
            case "RIGHT":
                return trollWalkRightGif;
            default:
                return trollIdleGif;
        }
    }

    public ImageIcon getMacetaGif(String direction) {
        switch (direction) {
            case "UP":
                return macetaWalkUpGif;
            case "DOWN":
                return macetaWalkDownGif;
            case "LEFT":
                return macetaWalkLeftGif;
            case "RIGHT":
                return macetaWalkRightGif;
            default:
                return macetaWalkDownGif;
        }
    }

    public ImageIcon getCalamarGif(String direction, boolean isBreakingIce) {
        if (isBreakingIce) {
            switch (direction) {
                case "UP":
                    return calamarBreakUpGif;
                case "DOWN":
                    return calamarBreakDownGif;
                case "LEFT":
                    return calamarBreakLeftGif;
                case "RIGHT":
                    return calamarBreakRightGif;
                default:
                    return calamarBreakDownGif;
            }
        } else {
            switch (direction) {
                case "UP":
                    return calamarWalkUpGif;
                case "DOWN":
                    return calamarWalkDownGif;
                case "LEFT":
                    return calamarWalkLeftGif;
                case "RIGHT":
                    return calamarWalkRightGif;
                default:
                    return calamarWalkDownGif;
            }
        }
    }

    public ImageIcon getFruitImage(String fruitType) {
        switch (fruitType) {
            case "UVA":
                return uvaImage;
            case "PLATANO":
                return platanoImage;
            case "PIÑA":
                return pinaImage;
            case "CEREZA":
                return cerezaImage;
            default:
                return uvaImage;
        }
    }

    public ImageIcon getFruitGif(String fruitType, String state) {
        FruitState fruitState;
        try {
            fruitState = domain.entity.FruitState.valueOf(state);
        } catch (IllegalArgumentException e) {
            fruitState = domain.entity.FruitState.IDLE;
        }

        switch (fruitType) {
            case "CACTUS":
                switch (fruitState) {
                    case SPAWNING:
                        return cactusSpawnGif;
                    case SPIKES_WARNING:
                        return cactusSpikesWarningGif;
                    case SPIKES_ACTIVE:
                        return cactusSpikesActiveGif;
                    case SPIKES_COOLDOWN:
                        return cactusSpikesCooldownGif;
                    case COLLECTED:
                        return cactusCollectedGif;
                    default:
                        return cactusIdleGif;
                }
            case "CEREZA":
                switch (fruitState) {
                    case SPAWNING:
                        return cherrySpawnGif;
                    case TELEPORT_OUT:
                        return cherryTeleportOutGif;
                    case TELEPORT_IN:
                        return cherryTeleportInGif;
                    case COLLECTED:
                        return cherryCollectedGif;
                    default:
                        return cherryIdleGif;
                }
            case "PIÑA":
                switch (fruitState) {
                    case SPAWNING:
                        return pineappleSpawnGif;
                    case COLLECTED:
                        return pineappleCollectedGif;
                    default:
                        return pineappleIdleGif;
                }
            case "PLATANO":
                switch (fruitState) {
                    case SPAWNING:
                        return bananaSpawnGif;
                    case COLLECTED:
                        return bananaCollectedGif;
                    default:
                        return bananaIdleGif;
                }
            case "UVA":
                switch (fruitState) {
                    case SPAWNING:
                        return grapeSpawnGif;
                    case COLLECTED:
                        return grapeCollectedGif;
                    default:
                        return grapeIdleGif;
                }
            default:
                return uvaImage;
        }
    }
}