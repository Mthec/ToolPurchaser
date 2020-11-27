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
        Prices.materialFlatRates.clear();
        Prices.enchantmentFlatRate = 0;
        Prices.enchantments.clear();
        Prices.ignoredEnchantments.clear();
        Prices.ignoredEnchantments.add((byte)0);
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
    void testGetMaterialFlatRate() {
        Prices.materialFlatRates.put(ItemMaterials.MATERIAL_GOLD, 123f);

        assertEquals(123f, Prices.getMaterialFlatRate(ItemMaterials.MATERIAL_GOLD));
    }

    @Test
    void testGetMissingMaterialFlatRate() {
        assert Prices.materialFlatRates.isEmpty();

        assertEquals(0f, Prices.getMaterialFlatRate(ItemMaterials.MATERIAL_GOLD));
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

    @Test
    void testGetMissingEnchantmentPrice2() {
        assert Prices.enchantments.isEmpty();

        assertEquals(0f, Prices.enchantment(Spell.BUFF_CIRCLE_CUNNING, 1));
    }

    @Test
    void testGetEnchantmentPriceWithIgnoredSpell() {
        Prices.enchantments.put(Spell.BUFF_OPULENCE, 12f);
        Prices.ignoredEnchantments.add(Spell.BUFF_OPULENCE);

        assertEquals(0f, Prices.enchantment(Spell.BUFF_CIRCLE_CUNNING, 1));
    }

    @Test
    void testGetEnchantmentFlatRate() {
        Prices.enchantmentFlatRate = 1f;

        assertEquals(1f, Prices.enchantment(Spell.BUFF_CIRCLE_CUNNING, 1));
    }

    @Test
    void testGetEnchantmentFlatRatePlusVariableRate() {
        Prices.enchantments.put(Spell.BUFF_CIRCLE_CUNNING, 1f);
        Prices.enchantmentFlatRate = 1f;

        assertEquals(2f, Prices.enchantment(Spell.BUFF_CIRCLE_CUNNING, 1));
    }
}
