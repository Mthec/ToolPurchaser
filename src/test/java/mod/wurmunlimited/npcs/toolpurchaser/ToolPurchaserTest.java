package mod.wurmunlimited.npcs.toolpurchaser;

import mod.wurmunlimited.WurmObjectsFactory;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

public abstract class ToolPurchaserTest {
    protected ToolPurchaserObjectsFactory factory;

    @SuppressWarnings("unchecked")
    @BeforeEach
    protected void setUp() throws Exception {
        factory = new ToolPurchaserObjectsFactory();
        Prices.ql = 0;
        ((Map<Byte, Float>)ReflectionUtil.getPrivateField(null, Prices.class.getDeclaredField("materials"))).clear();
        ((Map<Byte, Float>)ReflectionUtil.getPrivateField(null, Prices.class.getDeclaredField("enchantments"))).clear();
    }

    protected void setMaterialModifier(byte material, float modifier) {
        try {
            Map<Byte, Float> map = ReflectionUtil.getPrivateField(null, Prices.class.getDeclaredField("materials"));
            map.put(material, modifier);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setEnchantmentPrice(byte type, float price) {
        try {
            Map<Byte, Float> map = ReflectionUtil.getPrivateField(null, Prices.class.getDeclaredField("enchantments"));
            map.put(type, price);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
