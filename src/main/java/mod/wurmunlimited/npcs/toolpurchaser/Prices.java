package mod.wurmunlimited.npcs.toolpurchaser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Prices {
    public static float ql = 0.0f;
    public static float enchantmentFlatRate = 0.0f;
    static final Map<Byte, Float> materials = new HashMap<>();
    static final Map<Byte, Float> materialFlatRates = new HashMap<>();
    static final Map<Byte, Float> enchantments = new HashMap<>();
    static final Set<Byte> ignoredEnchantments = new HashSet<>();

    static {
        ignoredEnchantments.add((byte)0);
    }

    public static float getMaterialModifier(byte material) {
        return materials.getOrDefault(material, 1f);
    }

    public static float getMaterialFlatRate(byte material) {
        return materialFlatRates.getOrDefault(material, 0f);
    }

    private static float getEnchantment(byte type) {
        return enchantments.getOrDefault(type, 0f);
    }

    public static float enchantment(byte type) {
        if (ignoredEnchantments.contains(type))
            return 0;
        return getEnchantment(type) + enchantmentFlatRate;
    }

    public static float enchantment(byte type, float power) {
        if (ignoredEnchantments.contains(type))
            return 0;
        float price = getEnchantment(type) * power;
        price += enchantmentFlatRate;
        return price;
    }
}
