package fr.atesab.specttp;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SpectTpMain.MOD_ID)
public class SpectTpMain {
    public static final String MOD_ID = "specttp";
    public static final String MOD_VERSION = "2.0.0";
    public static final String MOD_AUTHOR = "ATE47";
    public static final String COMMAND = "sptp";

    public SpectTpMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * send us to another player
     * 
     * @param p  us
     * @param to the other player
     */
    public static void teleport(LocalPlayer p, PlayerInfo to) {
        var mode = Minecraft.getInstance().getConnection().getPlayerInfo(p.getGameProfile().getId()).getGameMode();
        // ask for the mode
        if (mode != GameType.SPECTATOR) {
            p.chat("/gamemode " + GameType.SPECTATOR.getName());
        }
        // send tp request
        p.connection.send(new ServerboundTeleportToEntityPacket(to.getProfile().getId()));
        // reset our old mode
        if (mode != GameType.SPECTATOR) {
            p.chat("/gamemode " + mode.getName());
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void registerCommand(RegisterCommandsEvent ev) {
        ev.getDispatcher().register(Commands.literal("sptp")
                .then(Commands.argument("username", ConnectionPlayerArgument.player()).executes(c -> {
                    if (c.getSource().getEntity()instanceof LocalPlayer p) {
                        var to = ConnectionPlayerArgument.getPlayer(c, "username");
                        c.getSource().sendSuccess(new TranslatableComponent("specttp.tp", to.getProfile().getName()),
                                false);
                        teleport(p, to);
                    } else {
                        c.getSource().sendFailure(
                                new TranslatableComponent("specttp.noaplayer").withStyle(ChatFormatting.RED));
                    }
                    return 1;
                })).executes(c -> {
                    c.getSource()
                            .sendFailure(new TranslatableComponent("specttp.help", "/" + COMMAND)
                                    .withStyle(ChatFormatting.RED).withStyle(s -> s.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMAND + " "))));
                    return 1;
                }));
    }
}
