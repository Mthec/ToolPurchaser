package mod.wurmunlimited.npcs.toolpurchaser;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.zones.VolaTile;
import mod.wurmunlimited.WurmObjectsFactory;

public class ToolPurchaserObjectsFactory extends WurmObjectsFactory {
    public ToolPurchaserObjectsFactory() throws Exception {
        super();
        new ToolPurchaserTemplate().createCreateTemplateBuilder().build();
    }

    public Creature createNewToolPurchaser(VolaTile tile, String name, byte sex, byte kingdom) {
        try {
            Creature purchaser = ToolPurchaserTemplate.createNewTrader(tile, 0, name, sex, kingdom);
            creatures.put(purchaser.getWurmId(), purchaser);
            purchaser.createPossessions();
            attachFakeCommunicator(purchaser);

            return purchaser;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
