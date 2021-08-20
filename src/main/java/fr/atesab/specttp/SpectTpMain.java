package fr.atesab.specttp;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
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

    private static CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    private static CommandDispatcher<SharedSuggestionProvider> SharedSuggestionProvider;
    private static Set<String> commandSet = new HashSet<>();

    public SpectTpMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ACT command System

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void createSuggestion(CommandNode<CommandSourceStack> dispatcher,
            CommandNode<SharedSuggestionProvider> rootCommandNode, CommandSourceStack player,
            Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> suggestions) {
        for (CommandNode<CommandSourceStack> child : dispatcher.getChildren()) {
            ArgumentBuilder<SharedSuggestionProvider, ?> argumentbuilder = (ArgumentBuilder) child.createBuilder();
            argumentbuilder.requires((ctx) -> true);
            if (argumentbuilder.getCommand() != null)
                argumentbuilder.executes((ctx) -> 0);

            if (argumentbuilder instanceof RequiredArgumentBuilder) {
                RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredargumentbuilder = (RequiredArgumentBuilder) argumentbuilder;
                if (requiredargumentbuilder.getSuggestionsProvider() != null) {
                    requiredargumentbuilder
                            .suggests(SuggestionProviders.safelySwap(requiredargumentbuilder.getSuggestionsProvider()));
                }
            }

            if (argumentbuilder.getRedirect() != null) {
                argumentbuilder.redirect(suggestions.get(argumentbuilder.getRedirect()));
            }

            CommandNode<SharedSuggestionProvider> commandnode1 = argumentbuilder.build();
            suggestions.put(child, commandnode1);
            rootCommandNode.addChild(commandnode1);
            if (!child.getChildren().isEmpty()) {
                this.createSuggestion(child, commandnode1, player, suggestions);
            }
        }

    }

    private void injectSuggestions() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            CommandDispatcher<SharedSuggestionProvider> current = mc.player.connection.getCommands();
            if (current != SharedSuggestionProvider) {
                SharedSuggestionProvider = current;
                if (current != null) {
                    Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newHashMap();
                    RootCommandNode<SharedSuggestionProvider> root = SharedSuggestionProvider.getRoot();
                    map.put(dispatcher.getRoot(), root);
                    createSuggestion(dispatcher.getRoot(), root, mc.player.createCommandSourceStack(), map);
                }
            }
        }
    }

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post ev) {
        injectSuggestions();
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatEvent ev) {
        String msg = ev.getMessage();
        // check if it is a command
        if (!msg.startsWith("/"))
            return;

        final String command = msg.substring(1);

        // check if we know it
        if (!commandSet.contains(command.split(" ", 2)[0].toLowerCase()))
            return;

        // we know it, we remove it
        ev.setCanceled(true);
        Minecraft.getInstance().gui.getChat().addRecentChat(msg);

        StringReader reader = new StringReader(msg);
        if (reader.canRead())
            reader.skip(); // remove the '/'
        CommandSourceStack source = Minecraft.getInstance().player.createCommandSourceStack();

        try {
            ParseResults<CommandSourceStack> parse = dispatcher.parse(reader, source);
            dispatcher.execute(parse);
        } catch (CommandSyntaxException e) {
            source.sendFailure(ComponentUtils.fromMessage(e.getRawMessage()));
            if (e.getInput() != null && e.getCursor() >= 0) {
                int messageSize = Math.min(e.getInput().length(), e.getCursor());
                MutableComponent error = (new TextComponent("")).withStyle(ChatFormatting.GRAY)
                        .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, msg)));
                if (messageSize > 10) {
                    error.append("...");
                }

                error.append(e.getInput().substring(Math.max(0, messageSize - 10), messageSize));
                if (messageSize < e.getInput().length()) {
                    MutableComponent Component2 = (new TextComponent(e.getInput().substring(messageSize)))
                            .withStyle(new ChatFormatting[] { ChatFormatting.RED, ChatFormatting.UNDERLINE });
                    error.append(Component2);
                }

                error.append((new TranslatableComponent("command.context.here"))
                        .withStyle(new ChatFormatting[] { ChatFormatting.RED, ChatFormatting.ITALIC }));
                source.sendFailure(error);
            }
        } catch (Exception e) {
            MutableComponent error = new TextComponent(
                    e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            source.sendFailure((new TranslatableComponent("command.failed")).withStyle((style) -> {
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, error));
                return style;
            }));
        }
    }

    /**
     * send us to another player
     * 
     * @param p  us
     * @param to the other player
     */
    public static void teleport(PlayerInfo to) {
        var p = Minecraft.getInstance().player;
        if (p == null)
            return;
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
        commandSet.add(COMMAND);
        dispatcher.register(Commands.literal(COMMAND)
                .then(Commands.argument("username", ConnectionPlayerArgument.player()).executes(c -> {
                    var to = ConnectionPlayerArgument.getPlayer(c, "username");
                    c.getSource().sendSuccess(new TranslatableComponent("specttp.tp", to.getProfile().getName()),
                            false);
                    teleport(to);
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
