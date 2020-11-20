package mod.wurmunlimited.npcs.toolpurchaser;

import com.wurmonline.server.spells.Spell;
import com.wurmonline.shared.constants.ItemMaterials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricesTests {
    @BeforeEach
    void setUp() {
        Prices.ql = 0;
        Prices.materials.clear();
        Prices.enchantments.clear();
    }

    @Test
    void testGetMaterialModifier() {
        Prices.materials.put(ItemMaterials.MATERIAL_GOLD, 123f);

        assertEquals(123f, Prices.getMaterialModifier(ItemMaterials.MATERIAL_GOLD));
    }

    @Test
    void testGetMissingMaterialModifier() {
        assert Prices.materials.isEmpty();

        assertEquals(1f, Prices.getMaterialModifier(ItemMaterials.MATERIAL_GOLD));
    }

    @Test
    void testGetEnchantmentPrice() {
        Prices.enchantments.put(Spell.BUFF_CIRCLE_CUNNING, 12f);

        assertEquals(12f, Prices.enchantment(Spell.BUFF_CIRCLE_CUNNING));
    }

    @Test
    void testGetMissingEnchantmentPrice() {
        assert Prices.enchantments.isEmpty();

        assertEquals(0f, Prices.enchantment(Spell.BUFF_CIRCLE_CUNNING));
    }
}
