package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.FakeCreatureStatus;
import com.wurmonline.server.players.Player;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserMod;
import mod.wurmunlimited.npcs.toolpurchaser.ToolPurchaserTest;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Properties;

import static mod.wurmunlimited.Assert.receivedBMLContaining;
import static mod.wurmunlimited.Assert.receivedMessageContaining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManageToolPurchaserQuestionTests extends ToolPurchaserTest {
    private static final long face = 12345;
    private Player gm;
    private Creature toolPurchaser;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        gm = factory.createNewPlayer();
        gm.setPower((byte)2);
        toolPurchaser = factory.createNewToolPurchaser(gm.getCurrentTile(), "Trader_Bob", (byte)0, (byte)0);
    }

    // sendQuestion

    @Test
    public void testNameCorrectlySet() throws SQLException {
        new ManageToolPurchaserQuestion(gm, toolPurchaser).sendQuestion();
        assertThat(gm, receivedBMLContaining("input{text=\"Bob\";id=\"name\";maxchars=\"" + ToolPurchaserMod.maxNameLength + "\"}"));
    }

    @Test
    public void testNameWithBlankPrefixCorrectlySet() throws SQLException, NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setPrivateField(null, ToolPurchaserMod.class.getDeclaredField("namePrefix"), "");

        new ManageToolPurchaserQuestion(gm, toolPurchaser).sendQuestion();
        assertThat(gm, receivedBMLContaining("input{text=\"Trader_Bob\";id=\"name\";maxchars=\"" + ToolPurchaserMod.maxNameLength + "\"}"));
    }

    // answer

    @Test
    public void testAnswerName() {
        String name = "Robin";
        String fullName = "Trader_" + name;
        assert !toolPurchaser.getName().equals(fullName);
        Properties properties = new Properties();
        properties.setProperty("name", name);
        new ManageToolPurchaserQuestion(gm, toolPurchaser).answer(properties);

        assertEquals(fullName, toolPurchaser.getName());
        assertEquals(fullName, ((FakeCreatureStatus)toolPurchaser.getStatus()).savedName);
        assertThat(gm, receivedMessageContaining(toolPurchaser.getName()));
    }

    @Test
    public void testAnswerNameIllegalCharacters() {
        String name = "Dave%^";
        String fullName = "Trader_" + name;
        String oldName = toolPurchaser.getName();
        assert !toolPurchaser.getName().equals(fullName);
        Properties properties = new Properties();
        properties.setProperty("name", name);
        new ManageToolPurchaserQuestion(gm, toolPurchaser).answer(properties);

        assertEquals(oldName, toolPurchaser.getName());
        assertEquals(FakeCreatureStatus.unset, ((FakeCreatureStatus)toolPurchaser.getStatus()).savedName);
        assertThat(gm, receivedMessageContaining("remain " + oldName));
    }
}
