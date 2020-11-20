package com.wurmonline.server.creatures;

import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.*;
import com.wurmonline.server.spells.SpellEffect;
import mod.wurmunlimited.npcs.toolpurchaser.Prices;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ToolPurchaserTradeHandler extends TradeHandler {
    private static final Logger logger = Logger.getLogger(ToolPurchaserTradeHandler.class.getName());
    private final Creature trader;
    private final Trade trade;
    private boolean balanced = false;

    public ToolPurchaserTradeHandler(Creature creature, Trade trade) {
        super();
        trader = creature;
        this.trade = trade;
        trade.creatureOne.getCommunicator().sendSafeServerMessage(trader.getName() + " says 'I have nothing to sell, but will buy tools and weapons.'");
    }

    @Override
    void addToInventory(Item item, long inventoryWindow) {
        if (this.trade != null) {
            if (inventoryWindow == 2L) {
                this.tradeChanged();
                if (logger.isLoggable(Level.FINEST) && item != null) {
                    logger.finest("Added " + item.getName() + " to his offer window.");
                }
            } else if (inventoryWindow == 1L) {
                if (logger.isLoggable(Level.FINEST) && item != null) {
                    logger.finest("Added " + item.getName() + " to my offer window.");
                }
            } else if (inventoryWindow == 3L) {
                if (logger.isLoggable(Level.FINEST) && item != null) {
                    logger.finest("Added " + item.getName() + " to his request window.");
                }
            } else if (inventoryWindow == 4L && logger.isLoggable(Level.FINEST) && item != null) {
                logger.finest("Added " + item.getName() + " to my request window.");
            }
        }

    }

    @Override
    public void addItemsToTrade() {
    }

    @Override
    public int getTraderSellPriceForItem(Item item, TradingWindow window) {
        return 0;
    }

    private boolean isAccepted(Item item) {
        return item.isTool() || item.isWeapon();
    }

    @Override
    public int getTraderBuyPriceForItem(Item item) {
        if (isAccepted(item)) {
            float price = (item.getQualityLevel() * Prices.ql) * Prices.getMaterialModifier(item.getMaterial());

            ItemSpellEffects effects = item.getSpellEffects();
            if (effects != null) {
                for (SpellEffect effect : effects.getEffects()) {
                    price += Prices.enchantment(effect.type) * effect.power;
                }
            }

            if (item.enchantment != 0) {
                price += Prices.enchantment(item.enchantment);
            }

            if (price > 0)
                return Integer.max(1, (int)price);
        }

        return 0;
    }

    @Override
    void tradeChanged() {
        balanced = false;
    }

    @Override
    public void balance() {
        if (!balanced) {
            TradingWindow offerWindow = trade.getTradingWindow(2);
            TradingWindow coinWindow = trade.getTradingWindow(3);
            TradingWindow toolsWindow = trade.getTradingWindow(4);

            for (Item item : offerWindow.getAllItems()) {
                if (isAccepted(item)) {
                    offerWindow.removeItem(item);
                    toolsWindow.addItem(item);
                }
            }

            for (Item item : coinWindow.getAllItems()) {
                if (item.isCoin()) {
                    coinWindow.removeItem(item);
                }
            }

            int cost = Arrays.stream(toolsWindow.getAllItems()).mapToInt(this::getTraderBuyPriceForItem).sum();
            for (Item coin : Economy.getEconomy().getCoinsFor(cost)) {
                coinWindow.addItem(coin);
            }

            trade.setSatisfied(trader, true, trade.getCurrentCounter());
            balanced = true;
        }
    }
}
