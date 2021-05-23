package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.PlaceToolPurchaserQuestion;
import com.wurmonline.server.zones.VolaTile;

public class PlaceToolPurchaserAction implements NpcMenuEntry {
    public PlaceToolPurchaserAction() {
        PlaceNpcMenu.addNpcAction(this);
    }

    @Override
    public String getName() {
        return "Tool Purchaser";
    }

    @Override
    public boolean doAction(Action action, short num, Creature performer, Item source, VolaTile tile, int floorLevel) {
        new PlaceToolPurchaserQuestion(performer, tile, floorLevel).sendQuestion();
        return true;
    }
}
