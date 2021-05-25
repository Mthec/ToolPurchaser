package com.wurmonline.server.questions;

import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.util.StringUtilities;
import mod.wurmunlimited.bml.BMLBuilder;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserTemplate;

import java.util.Properties;
import java.util.Random;

public class PlaceToolPurchaserQuestion extends ToolPurchaserQuestionExtension {
    private static final Random r = new Random();
    private final VolaTile tile;
    private final int floorLevel;

    public PlaceToolPurchaserQuestion(Creature performer, VolaTile tile, int floorLevel) {
        super(performer, "Set Up Tool Purchaser", "", MANAGETRADER, -10);
        this.tile = tile;
        this.floorLevel = floorLevel;
    }

    @Override
    public void answer(Properties properties) {
        setAnswer(properties);

        Creature responder = getResponder();

        byte sex = 0;
        String val = properties.getProperty("gender");
        if (val != null && val.equals("female"))
            sex = 1;

        String name = StringUtilities.raiseFirstLetter(getStringProp("name"));
        if (name.isEmpty() || name.length() > 20 || QuestionParser.containsIllegalCharacters(name)) {
            if (sex == 0) {
                name = QuestionParser.generateGuardMaleName();
                responder.getCommunicator().sendSafeServerMessage("The trader didn't like the name, so he chose a new one.");
            } else {
                name = QuestionParser.generateGuardFemaleName();
                responder.getCommunicator().sendSafeServerMessage("The trader didn't like the name, so she chose a new one.");
            }
        }

        if (locationIsValid(responder)) {
            try {
                String fullName = getPrefix() + name;
                Creature trader = ToolPurchaserTemplate.createNewTrader(tile, floorLevel, fullName, sex, responder.getKingdomId());
                logger.info(responder.getName() + " created a tool purchaser: " + trader.getWurmId());
            } catch (Exception e) {
                responder.getCommunicator().sendAlertServerMessage("An error occurred in the rifts of the void. The trader was not created.");
                e.printStackTrace();
            }
        }
    }

    private boolean locationIsValid(Creature responder) {
        if (tile != null) {
            if (!Methods.isActionAllowed(responder, Actions.MANAGE_TRADERS)) {
                return false;
            }
            for (Creature creature : tile.getCreatures()) {
                if (!creature.isPlayer()) {
                    responder.getCommunicator().sendNormalServerMessage("The trader will only set up shop where no other creatures except you are standing.");
                    return false;
                }
            }

            Structure struct = tile.getStructure();
            if (struct != null && !struct.mayPlaceMerchants(responder)) {
                responder.getCommunicator().sendNormalServerMessage("You do not have permission to place a trader in this building.");
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendQuestion() {
        boolean gender = r.nextBoolean();

        String bml = new BMLBuilder(id)
                             .text("Place Tool Purchaser").bold()
                             .text("Place a trader that will buy tools and weapons.")
                             .newLine()
                             .harray(b -> b.label("Name:").entry("name", 20))
                             .text("Leave blank for a random name.").italic()
                             .newLine()
                             .text("Gender:")
                             .radio("gender", "male", "Male", gender)
                             .radio("gender", "female", "Female", !gender)
                             .newLine()
                             .harray(b -> b.button("Send"))
                             .build();

        getResponder().getCommunicator().sendBml(400, 350, true, true, bml, 200, 200, 200, title);
    }
}
