package mod.wurmunlimited.npcs.toolpurchaser;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.PlaceNpcMenu;
import com.wurmonline.server.behaviours.PlaceToolPurchaserAction;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.TradeHandler;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.questions.CreatureCreationQuestion;
import com.wurmonline.server.questions.PlaceToolPurchaserQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MaterialUtilities;
import javassist.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreatures;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class ToolPurchaserMod implements WurmServerMod, Configurable, Initable, PreInitable, ServerStartedListener {
    private static final Logger logger = Logger.getLogger(ToolPurchaserMod.class.getName());
    private static boolean printPrices = false;

    @Override
    public void configure(Properties properties) {
        String val = properties.getProperty("print_prices");
        printPrices = val != null && val.equals("true");
        val = properties.getProperty("ql_price");
        try {
            if (val != null)
                Prices.ql = Float.parseFloat(val);
            else
                throw new NumberFormatException("No value found for ql_prices.");
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        try {
            Properties materials = new Properties();
            materials.load(Files.newInputStream(Paths.get("mods", "toolpurchaser", "MaterialPrices.properties")));

            for (String name : materials.stringPropertyNames()) {
                try {
                    if (name.startsWith("flat")) {
                        val = materials.getProperty(name);
                        if (val != null) {
                            Prices.materialFlatRates.put(Byte.parseByte(name.substring(4)), Float.parseFloat(val));
                        }
                    } else {
                        val = materials.getProperty(name);
                        if (val != null) {
                            Prices.materials.put(Byte.parseByte(name), Float.parseFloat(val));
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Incorrect id/value found (" + name + "=" + val + "), ignoring.");
                }
            }
        } catch (IOException e) {
            logger.warning("Could not find MaterialPrices.properties file.");
            throw new RuntimeException(e);
        }

        try {
            Properties enchantments = new Properties();
            enchantments.load(Files.newInputStream(Paths.get("mods", "toolpurchaser", "EnchantmentPrices.properties")));

            for (String name : enchantments.stringPropertyNames()) {
                if (name.equals("flat_rate")) {
                    try {
                        val = enchantments.getProperty(name);
                        Prices.enchantmentFlatRate = Float.parseFloat(val);
                    } catch (NumberFormatException e) {
                        logger.warning("Invalid flat_rate value found (" + val + "), setting to 0.");
                    }
                } else if (name.equals("ignored")) {
                    val = enchantments.getProperty(name);
                    for (String sub : val.split(",")) {
                        try {
                            Prices.ignoredEnchantments.add(Byte.parseByte(sub));
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid value found in ignored (" + val + "), ignoring.");
                        }
                    }
                } else {
                    try {
                        val = enchantments.getProperty(name);
                        if (val != null) {
                            Prices.enchantments.put(Byte.parseByte(name), Float.parseFloat(val));
                        }
                    } catch (NumberFormatException e) {
                        logger.warning("Incorrect id/value found (" + name + "=" + val + "), ignoring.");
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Could not find EnchantmentPrices.properties file.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preInit() {
        ClassPool pool = HookManager.getInstance().getClassPool();

        try {
            // Remove final and add empty constructor to TradeHandler.
            CtClass tradeHandler = pool.get("com.wurmonline.server.creatures.TradeHandler");
            tradeHandler.defrost();
            tradeHandler.setModifiers(Modifier.clear(tradeHandler.getModifiers(), Modifier.FINAL));
            if (tradeHandler.getConstructors().length == 1)
                tradeHandler.addConstructor(CtNewConstructor.make(tradeHandler.getSimpleName() + "(){}", tradeHandler));

            // Remove final and add empty constructor to TradingWindow.
            CtClass tradingWindow = pool.get("com.wurmonline.server.items.TradingWindow");
            tradingWindow.defrost();
            tradingWindow.setModifiers(Modifier.clear(tradingWindow.getModifiers(), Modifier.FINAL));
            if (tradingWindow.getConstructors().length == 1)
                tradingWindow.addConstructor(CtNewConstructor.make(tradingWindow.getSimpleName() + "(){}", tradingWindow));

            // Remove final and add empty constructor to Trade.
            CtClass trade = pool.get("com.wurmonline.server.items.Trade");
            trade.defrost();
            if (trade.getConstructors().length == 1)
                trade.addConstructor(CtNewConstructor.make(trade.getSimpleName() + "(){}", trade));
            // Remove final from public fields.
            CtField creatureOne = trade.getDeclaredField("creatureOne");
            creatureOne.setModifiers(Modifier.clear(creatureOne.getModifiers(), Modifier.FINAL));
            CtField creatureTwo = trade.getDeclaredField("creatureTwo");
            creatureTwo.setModifiers(Modifier.clear(creatureTwo.getModifiers(), Modifier.FINAL));
        } catch (CannotCompileException | NotFoundException e) {
            throw new RuntimeException(e);
        }

        ModActions.init();
    }

    @Override
    public void init() {
        HookManager manager = HookManager.getInstance();

        manager.registerHook("com.wurmonline.server.creatures.Creature",
                "getTradeHandler",
                "()Lcom/wurmonline/server/creatures/TradeHandler;",
                () -> this::getTradeHandler);

        manager.registerHook("com.wurmonline.server.items.Trade",
                "makeTrade",
                "()Z",
                () -> this::makeTrade);

        manager.registerHook("com.wurmonline.server.questions.QuestionParser",
                "parseCreatureCreationQuestion",
                "(Lcom/wurmonline/server/questions/CreatureCreationQuestion;)V",
                () -> this::creatureCreation);

        ModCreatures.init();
        ModCreatures.addCreature(new ToolPurchaserTemplate());
    }

    @Override
    public void onServerStarted() {
        new PlaceToolPurchaserAction();
        PlaceNpcMenu.registerAction();

        if (printPrices) {
            StringBuilder sb = new StringBuilder("Tool Purchaser Prices:\nQL - " + Prices.ql + "\nMaterials:\n");
            for (Map.Entry<Byte, Float> material : Prices.materials.entrySet()) {
                sb.append(MaterialUtilities.getMaterialString(material.getKey())).append(" - ").append(material.getValue()).append("\n");
            }
            sb.append("Flat Rate Materials:\n");
            for (Map.Entry<Byte, Float> material : Prices.materialFlatRates.entrySet()) {
                sb.append(MaterialUtilities.getMaterialString(material.getKey())).append(" - ").append(material.getValue()).append("\n");
            }
            sb.append("Enchantment Flat Rate - ").append(Prices.enchantmentFlatRate).append("\n");
            sb.append("Enchantments:\n");
            for (Map.Entry<Byte, Float> enchantment : Prices.enchantments.entrySet()) {
                sb.append(Spells.getEnchantment(enchantment.getKey())).append(" - ").append(enchantment.getValue()).append("\n");
            }
            sb.append("Ignored Enchantments:\n");
            for (Byte type : Prices.ignoredEnchantments) {
                sb.append(Spells.getEnchantment(type)).append("\n");
            }

            logger.info(sb.toString());
        }
    }

    private Object getTradeHandler(Object o, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        Creature creature = (Creature)o;
        if (!ToolPurchaserTemplate.is(creature))
            return method.invoke(o, args);
        Field tradeHandler = Creature.class.getDeclaredField("tradeHandler");
        tradeHandler.setAccessible(true);
        TradeHandler handler = (TradeHandler)tradeHandler.get(creature);

        if (handler == null) {
            Class<?> ServiceHandler;

            ServiceHandler = Class.forName("com.wurmonline.server.creatures.ToolPurchaserTradeHandler");
            handler = (TradeHandler)ServiceHandler.getConstructor(Creature.class, Trade.class).newInstance(creature, creature.getTrade());
            tradeHandler.set(o, handler);
        }

        return handler;
    }

    Object makeTrade(Object o, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Creature creature = ((Trade)o).creatureTwo;
        boolean tradeCompleted = (boolean)method.invoke(o, args);

        if (tradeCompleted && ToolPurchaserTemplate.is(creature)) {
            // Remove all bought items.
            for (Item item : creature.getInventory().getItemsAsArray()) {
                Items.destroyItem(item.getWurmId());
            }

            creature.getShop().setMoney(MonetaryConstants.COIN_GOLD * 100);
        }

        //noinspection SuspiciousInvocationHandlerImplementation
        return tradeCompleted;
    }

    Object creatureCreation(Object o, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        CreatureCreationQuestion question = (CreatureCreationQuestion)args[0];
        Properties answers = ReflectionUtil.getPrivateField(question, Question.class.getDeclaredField("answer"));
        try {
            String templateIndexString = answers.getProperty("data1");
            String name = answers.getProperty("cname");
            if (name == null)
                answers.setProperty("name", "");
            else
                answers.setProperty("name", name);
            if (templateIndexString != null) {
                int templateIndex = Integer.parseInt(templateIndexString);
                List<CreatureTemplate> templates = ReflectionUtil.getPrivateField(question, CreatureCreationQuestion.class.getDeclaredField("cretemplates"));
                CreatureTemplate template = templates.get(templateIndex);

                Creature responder = question.getResponder();
                int floorLevel = responder.getFloorLevel();
                VolaTile tile = Zones.getOrCreateTile(question.getTileX(), question.getTileY(), responder.isOnSurface());
                if (ToolPurchaserTemplate.is(template)) {
                    new PlaceToolPurchaserQuestion(responder, tile, floorLevel).answer(answers);
                    return null;
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            question.getResponder().getCommunicator().sendAlertServerMessage("An error occurred in the rifts of the void. The trader was not created.");
            e.printStackTrace();
        }

        return method.invoke(o, args);
    }
}
