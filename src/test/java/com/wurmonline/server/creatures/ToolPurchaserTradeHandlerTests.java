package com.wurmonline.server.creatures;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.ItemMaterials;
import mod.wurmunlimited.npcs.toolpurchaser.Prices;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserTest;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mod.wurmunlimited.Assert.*;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ToolPurchaserTradeHandlerTests extends ToolPurchaserTest {
    private Creature purchaser;
    private Trade trade;
    private ToolPurchaserTradeHandler handler;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        purchaser = factory.createNewToolPurchaser(mock(VolaTile.class), "Bob", (byte)0, (byte)0);
        trade = new Trade(factory.createNewPlayer(), purchaser);
        handler = new ToolPurchaserTradeHandler(purchaser, trade);
        ReflectionUtil.setPrivateField(purchaser, Creature.class.getDeclaredField("tradeHandler"), handler);
    }

    @Test
    void testAddItemsToTradeDoesNothing() {
        handler.addItemsToTrade();

        assertEquals(0, trade.getTradingWindow(1).getAllItems().length);
    }

    @Test
    void testNotAcceptedItemsValuedAtZero() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.sand);
        item.setQualityLevel(25);

        assertEquals(0, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testToolsAccepted() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(25);

        assertEquals(25, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testWeaponsAccepted() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.swordLong);
        item.setQualityLevel(25);

        assertEquals(25, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceQLOnly() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.rake);

        item.setQualityLevel(25);
        assertEquals(25, handler.getTraderBuyPriceForItem(item));

        item.setQualityLevel(35);
        assertEquals(35, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceMaterialModifier() {
        Prices.ql = 1;
        setMaterialModifier(ItemMaterials.MATERIAL_IRON, 2.0f);
        setMaterialModifier(ItemMaterials.MATERIAL_STEEL, 3.0f);
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(25);

        item.setMaterial(ItemMaterials.MATERIAL_IRON);
        assertEquals(50, handler.getTraderBuyPriceForItem(item));

        item.setMaterial(ItemMaterials.MATERIAL_STEEL);
        assertEquals(75, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceMaterialFlatRate() {
        Prices.ql = 1;
        setMaterialFlatRate(ItemMaterials.MATERIAL_IRON, 2.0f);
        setMaterialFlatRate(ItemMaterials.MATERIAL_STEEL, 3.0f);
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(25);

        item.setMaterial(ItemMaterials.MATERIAL_IRON);
        assertEquals(27, handler.getTraderBuyPriceForItem(item));

        item.setMaterial(ItemMaterials.MATERIAL_STEEL);
        assertEquals(28, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceMaterialModifierAndFlatRate() {
        Prices.ql = 1;
        setMaterialModifier(ItemMaterials.MATERIAL_IRON, 2.0f);
        setMaterialModifier(ItemMaterials.MATERIAL_STEEL, 3.0f);
        setMaterialFlatRate(ItemMaterials.MATERIAL_IRON, 2.0f);
        setMaterialFlatRate(ItemMaterials.MATERIAL_STEEL, 3.0f);
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(25);

        item.setMaterial(ItemMaterials.MATERIAL_IRON);
        assertEquals(52, handler.getTraderBuyPriceForItem(item));

        item.setMaterial(ItemMaterials.MATERIAL_STEEL);
        assertEquals(78, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceEnchantment() {
        Prices.ql = 0;
        setEnchantmentPrice(Spell.BUFF_CIRCLE_CUNNING, 1);
        setEnchantmentPrice(Spell.BUFF_WIND_OF_AGES, 2);
        Item item = factory.createNewItem(ItemList.rake);
        new ItemSpellEffects(item.getWurmId());

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_CIRCLE_CUNNING, 6, 1));
        assertEquals(6, handler.getTraderBuyPriceForItem(item));

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_WIND_OF_AGES, 5, 1));
        assertEquals(16, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceMaterialModifierPlusEnchantment() {
        Prices.ql = 1;
        setMaterialModifier(ItemMaterials.MATERIAL_IRON, 2.0f);
        setEnchantmentPrice(Spell.BUFF_CIRCLE_CUNNING, 1);
        setEnchantmentPrice(Spell.BUFF_WIND_OF_AGES, 2);
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(25);
        item.setMaterial(ItemMaterials.MATERIAL_IRON);
        new ItemSpellEffects(item.getWurmId());

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_CIRCLE_CUNNING, 6, 1));
        assertEquals(56, handler.getTraderBuyPriceForItem(item));

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_WIND_OF_AGES, 5, 1));
        assertEquals(66, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testEnchantmentEnchantmentPricesAdded() {
        setEnchantmentPrice(Spell.ENCHANT_ANIMAL_HATE, 1f);
        setEnchantmentPrice(Spell.ENCHANT_HUMAN_HATE, 2f);
        setEnchantmentPrice(Spell.ENCHANT_REGENERATION_HATE, 3f);
        setEnchantmentPrice(Spell.ENCHANT_DRAGON_HATE, 4f);
        Item item = factory.createNewItem(ItemList.swordShort);

        item.enchantment = Spell.ENCHANT_ANIMAL_HATE;
        assertEquals(1f, handler.getTraderBuyPriceForItem(item));

        item.enchantment = Spell.ENCHANT_HUMAN_HATE;
        assertEquals(2f, handler.getTraderBuyPriceForItem(item));

        item.enchantment = Spell.ENCHANT_REGENERATION_HATE;
        assertEquals(3f, handler.getTraderBuyPriceForItem(item));

        item.enchantment = Spell.ENCHANT_DRAGON_HATE;
        assertEquals(4f, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceEnchantmentFlatRate() {
        Prices.ql = 0;
        Prices.enchantmentFlatRate = 10;
        Item item = factory.createNewItem(ItemList.rake);
        new ItemSpellEffects(item.getWurmId());

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_CIRCLE_CUNNING, 6, 1));
        assertEquals(10, handler.getTraderBuyPriceForItem(item));

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_WIND_OF_AGES, 5, 1));
        assertEquals(20, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceEnchantmentAndFlatRate() {
        Prices.ql = 0;
        Prices.enchantmentFlatRate = 10;
        setEnchantmentPrice(Spell.BUFF_CIRCLE_CUNNING, 1);
        setEnchantmentPrice(Spell.BUFF_WIND_OF_AGES, 2);
        Item item = factory.createNewItem(ItemList.rake);
        new ItemSpellEffects(item.getWurmId());

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_CIRCLE_CUNNING, 6, 1));
        assertEquals(16, handler.getTraderBuyPriceForItem(item));

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_WIND_OF_AGES, 5, 1));
        assertEquals(36, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testItemPriceIgnoredEnchantment() {
        Prices.ql = 0;
        Prices.enchantmentFlatRate = 10;
        addIgnoredEnchantment(Spell.BUFF_CIRCLE_CUNNING);
        setEnchantmentPrice(Spell.BUFF_CIRCLE_CUNNING, 1);
        setEnchantmentPrice(Spell.BUFF_WIND_OF_AGES, 2);
        Item item = factory.createNewItem(ItemList.rake);
        new ItemSpellEffects(item.getWurmId());

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_CIRCLE_CUNNING, 6, 1));
        assertEquals(0, handler.getTraderBuyPriceForItem(item));

        item.getSpellEffects().addSpellEffect(new SpellEffect(-10, Spell.BUFF_WIND_OF_AGES, 5, 1));
        assertEquals(20, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testEnchantmentEnchantmentFlatRate() {
        Prices.ql = 0;
        Prices.enchantmentFlatRate = 10;
        setEnchantmentPrice(Spell.ENCHANT_ANIMAL_HATE, 3f);
        setEnchantmentPrice(Spell.ENCHANT_HUMAN_HATE, 4f);
        Item item = factory.createNewItem(ItemList.swordShort);

        item.enchantment = Spell.ENCHANT_ANIMAL_HATE;
        assertEquals(13f, handler.getTraderBuyPriceForItem(item));

        item.enchantment = Spell.ENCHANT_HUMAN_HATE;
        assertEquals(14f, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testEnchantmentEnchantmentIgnored() {
        Prices.ql = 1;
        setEnchantmentPrice(Spell.ENCHANT_ANIMAL_HATE, 3f);
        addIgnoredEnchantment(Spell.ENCHANT_ANIMAL_HATE);
        Item item = factory.createNewItem(ItemList.swordShort);
        item.setQualityLevel(1);

        item.enchantment = Spell.ENCHANT_ANIMAL_HATE;
        assertEquals(1f, handler.getTraderBuyPriceForItem(item));
    }

    @Test
    void testBalanceAddsCorrectCoins() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(10);
        trade.getTradingWindow(2).addItem(item);

        handler.balance();

        assertThat(trade.getTradingWindow(3), windowContainsCoinsOfValue(10));
    }

    @Test
    void testBalanceUpdatesCoinsProperly() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(10);
        trade.getTradingWindow(2).addItem(item);

        handler.balance();

        assertThat(trade.getTradingWindow(3), windowContainsCoinsOfValue(10));

        Item item2 = factory.createNewItem(ItemList.rake);
        item2.setQualityLevel(15);
        trade.getTradingWindow(2).addItem(item2);

        handler.tradeChanged();
        handler.balance();

        assertThat(trade.getTradingWindow(3), windowContainsCoinsOfValue(25));
    }

    @Test
    void testBalanceRemovesCoinsCorrectly() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(10);
        trade.getTradingWindow(2).addItem(item);

        handler.balance();
        assert trade.getTradingWindow(3).getAllItems().length > 0;
        trade.getTradingWindow(4).removeItem(item);

        handler.tradeChanged();
        handler.balance();

        assertEquals(0, trade.getTradingWindow(3).getAllItems().length);
    }

    @Test
    void testBalanceOnlyHasAcceptedItems() {
        Prices.ql = 1;
        Item item = factory.createNewItem(ItemList.rake);
        item.setQualityLevel(10);
        trade.getTradingWindow(2).addItem(item);
        Item item2 = factory.createNewItem(ItemList.dirtPile);
        item2.setQualityLevel(15);
        trade.getTradingWindow(2).addItem(item2);

        handler.balance();

        assertThat(trade.getTradingWindow(3), windowContainsCoinsOfValue(10));
        assertEquals(1, trade.getTradingWindow(2).getAllItems().length);
    }
}
