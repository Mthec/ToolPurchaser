package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.ManageToolPurchaserQuestion;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserTemplate;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.Collections;
import java.util.List;

public class ManageToolPurchaserAction implements ModAction, ActionPerformer, BehaviourProvider {
    private final short actionId;
    private final List<ActionEntry> entries;
    private final List<ActionEntry> empty = Collections.emptyList();

    public ManageToolPurchaserAction() {
        actionId = (short)ModActions.getNextActionId();
        ActionEntry actionEntry = new ActionEntryBuilder(actionId, "Manage", "managing").build();
        ModActions.registerAction(actionEntry);
        entries = Collections.singletonList(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Creature target) {
        if (performer.getPower() >= 2 && subject.isWand() && ToolPurchaserTemplate.is(target)) {
            return entries;
        }

        return null;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
        if (num == actionId && performer.getPower() >= 2 && source.isWand() && ToolPurchaserTemplate.is(target)) {
            new ManageToolPurchaserQuestion(performer, target).sendQuestion();
        }

        return true;
    }

    @Override
    public short getActionId() {
        return actionId;
    }
}
