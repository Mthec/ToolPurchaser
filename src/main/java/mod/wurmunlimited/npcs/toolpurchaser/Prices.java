package mod.wurmunlimited.npcs.toolpurchaser;

import java.util.HashMap;
import java.util.Map;

public class Prices {
    public static float ql = 0.0f;
    static final Map<Byte, Float> materials = new HashMap<>();
    static final Map<Byte, Float> enchantments = new HashMap<>();

    public static float getMaterialModifier(byte material) {
        return materials.getOrDefault(material, 1f);
    }

    public static float enchantment(byte type) {
        return enchantments.getOrDefault(type, 0f);
    }
}
