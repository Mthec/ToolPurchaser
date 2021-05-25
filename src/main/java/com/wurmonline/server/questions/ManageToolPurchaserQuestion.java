package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.shared.util.StringUtilities;
import mod.wurmunlimited.bml.BMLBuilder;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserMod;

import java.io.IOException;
import java.util.Properties;

import static com.wurmonline.server.creatures.CreaturePackageCaller.saveCreatureName;

public class ManageToolPurchaserQuestion extends ToolPurchaserQuestionExtension {
    private final Creature toolPurchaser;

    public ManageToolPurchaserQuestion(Creature responder, Creature toolPurchaser) {
        super(responder, "Manage Tool Purchaser", "", QuestionTypes.MANAGETRADER, toolPurchaser.getWurmId());
        this.toolPurchaser = toolPurchaser;
    }

    @Override
    public void answer(Properties properties) {
        setAnswer(properties);
        Creature responder = getResponder();

        String name = getStringProp("name");
        if (name != null && !name.isEmpty()) {
            String fullName = getPrefix() + StringUtilities.raiseFirstLetter(name);
            if (QuestionParser.containsIllegalCharacters(name)) {
                responder.getCommunicator().sendNormalServerMessage("The trader didn't like that name, so they shall remain " + toolPurchaser.getName() + ".");
            } else if (!fullName.equals(toolPurchaser.getName())) {
                try {
                    saveCreatureName(toolPurchaser, fullName);
                    toolPurchaser.refreshVisible();
                    responder.getCommunicator().sendNormalServerMessage("The trader will now be known as " + toolPurchaser.getName() + ".");
                } catch (IOException e) {
                    logger.warning("Failed to set name (" + fullName + ") for creature (" + toolPurchaser.getWurmId() + ").");
                    responder.getCommunicator().sendNormalServerMessage("The trader looks confused, what exactly is a database?");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        String bml = new BMLBuilder(id)
                             .text("Manage Tool Purchaser").bold()
                             .harray(b -> b.label("Name: " + getPrefix()).entry("name", getNameWithoutPrefix(toolPurchaser.getName()), ToolPurchaserMod.maxNameLength))
                             .newLine()
                             .harray(b -> b.button("Send"))
                             .build();

        getResponder().getCommunicator().sendBml(400, 350, true, true, bml, 200, 200, 200, title);
    }
}
