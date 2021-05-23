package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserMod;

public abstract class ToolPurchaserQuestionExtension extends Question {
    ToolPurchaserQuestionExtension(Creature aResponder, String aTitle, String aQuestion, int aType, long aTarget) {
        super(aResponder, aTitle, aQuestion, aType, aTarget);
    }

    String getPrefix() {
        String prefix = ToolPurchaserMod.getNamePrefix();
        if (prefix.isEmpty()) {
            return "";
        } else {
            return prefix + "_";
        }
    }

    String getNameWithoutPrefix(String name) {
        String prefix = ToolPurchaserMod.getNamePrefix();
        if (prefix.isEmpty() || name.length() < prefix.length() + 1) {
            return name;
        } else {
            return name.substring(prefix.length() + 1);
        }
    }
}
