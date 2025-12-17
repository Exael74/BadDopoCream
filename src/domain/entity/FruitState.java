package domain.entity;

/**
 * Estados posibles para una fruta durante su ciclo de vida.
 */
public enum FruitState {
    SPAWNING, // Animación de aparición
    IDLE, // Estado normal (recolectable)
    COLLECTED, // Animación de recolección (al ser tocada)

    // Estados específicos de Cereza
    TELEPORT_OUT, // Desapareciendo para teletransporte
    TELEPORT_IN, // Apareciendo después de teletransporte

    // Estados específicos de Cactus
    SPIKES_WARNING, // Advertencia antes de sacar espinas
    SPIKES_ACTIVE, // Espinas fuera (Mortal)
    SPIKES_COOLDOWN // Guardando espinas
}
