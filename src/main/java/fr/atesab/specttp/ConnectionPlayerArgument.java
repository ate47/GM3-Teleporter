package fr.atesab.specttp;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

public class ConnectionPlayerArgument implements ArgumentType<PlayerInfo> {
    public static final SimpleCommandExceptionType ERROR_UNKNOW_PLAYER = new SimpleCommandExceptionType(
            new TranslatableComponent("specttp.unknown"));

    public static ConnectionPlayerArgument player() {
        return new ConnectionPlayerArgument();
    }

    public static PlayerInfo getPlayer(CommandContext<CommandSourceStack> context, String name)
            throws CommandSyntaxException {
        return context.getArgument(name, PlayerInfo.class);
    }

    private ConnectionPlayerArgument() {
    }

    @Override
    public PlayerInfo parse(StringReader reader) throws CommandSyntaxException {
        var user = reader.readString();
        var info = Minecraft.getInstance().getConnection().getPlayerInfo(user);

        if (info == null)
            throw ERROR_UNKNOW_PLAYER.createWithContext(reader);

        return info;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (var info : Minecraft.getInstance().getConnection().getOnlinePlayers())
            builder.suggest(info.getProfile().getName());
        return builder.buildFuture();
    }
}
