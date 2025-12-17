package domain.entity.enemy;

import java.awt.Point;

public class EnemyFactory {

    public static Enemy createEnemy(Point position, String type) {
        if (type == null) {
            throw new IllegalArgumentException("Enemy type cannot be null");
        }

        switch (type.toUpperCase()) {
            case "TROLL":
                return new Troll(position);
            case "MACETA":
                return new Maceta(position);
            case "CALAMAR":
                return new Calamar(position);
            case "NARVAL":
                return new Narval(position);
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }

    public static String[] getSupportedTypes() {
        return new String[] { "TROLL", "MACETA", "CALAMAR", "NARVAL" };
    }
}
