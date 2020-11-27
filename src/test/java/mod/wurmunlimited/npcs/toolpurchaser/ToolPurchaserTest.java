package mod.wurmunlimited.npcs.toolpurchaser;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

public abstract class ToolPurchaserTest {
    protected ToolPurchaserObjectsFactory factory;

    @BeforeEach
    protected void setUp() throws Exception {
        factory = new ToolPurchaserObjectsFactory();
        Prices.ql = 0;
        Prices.materials.clear();
        Prices.materialFlatRates.clear();
        Prices.enchantmentFlatRate = 0;
        Prices.enchantments.clear();
        Prices.ignoredEnchantments.clear();
        Prices.ignoredEnchantments.add((byte)0);
    }

    protected void setMaterialModifier(byte material, float modifier) {
        Prices.materials.put(material, modifier);
    }

    protected void setMaterialFlatRate(byte material, float rate) {
        Prices.materialFlatRates.put(material, rate);
    }

    protected void setEnchantmentPrice(byte type, float price) {
        Prices.enchantments.put(type, price);
    }

    protected void addIgnoredEnchantment(byte type) {
        Prices.ignoredEnchantments.add(type);
    }
}
