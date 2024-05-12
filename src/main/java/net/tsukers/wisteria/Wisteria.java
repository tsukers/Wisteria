package net.tsukers.wisteria;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import static net.minecraft.server.command.CommandManager.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;

import static net.minecraft.server.command.CommandManager.literal;
import static net.tsukers.wisteria.WisteriaClient.playerData;
import static net.tsukers.wisteria.WisteriaClient.playerPoints;

public class Wisteria implements ModInitializer {
	public static final String MOD_ID = "wisteria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier POINTS_EARNED = new Identifier(MOD_ID, "points_earned");
	public static final Identifier INITIAL_SYNC = new Identifier(MOD_ID, "initial_sync");
	@Override
	public void onInitialize() {
		LOGGER.info("Hello from Tsukers!");

		//Command  -  /myPoints // returns your point amount
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("mypoints")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("You currently have " + playerPoints + " points."), false);
					return 1;
				})));
		// command - /pointinfo {user}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("pointinfo")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("player", EntityArgumentType.player())
						.executes(context -> {
							context.getSource().sendFeedback(() -> Text.literal("They have " + playerPoints + " points."), false);
							return 0;
                        }))));

		//point add		/pointadd {user} {amount}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("pointadd")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("player", EntityArgumentType.player())
						.executes(context -> {
							return 0;
						})
						.then(argument("amount", IntegerArgumentType.integer())
								.executes(context -> {
									final int value = IntegerArgumentType.getInteger(context, "amount");
									playerData.pointsEarned += value;
									context.getSource().sendFeedback(() -> Text.literal("You have added, " + value + " points."), false);
									context.getSource().sendFeedback(() -> Text.literal("they now have " + playerPoints), false);
									return 0;
								})))));


		//point remove		/pointremove {user} {amount}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("pointremove")
				.then(argument("player", EntityArgumentType.player())
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							return 0;
						})
						.then(argument("amount", IntegerArgumentType.integer())
								.requires(source -> source.hasPermissionLevel(2))
								.executes(context -> {
									final int value2 = IntegerArgumentType.getInteger(context, "amount");
									final int result = playerPoints -= value2;
									playerPoints = playerPoints -= value2;
									context.getSource().sendFeedback(() -> Text.literal("You have removed, " + value2 + " points. they now have, " + playerPoints + " points."), false);
									return result;
								})))));


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
			PacketByteBuf data = PacketByteBufs.create();
			data.writeInt(playerState.pointsEarned);
			server.execute(() -> {
				ServerPlayNetworking.send(handler.getPlayer(), INITIAL_SYNC, data);
			});
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
				StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(world.getServer());

				serverState.totalPointsEarned += 1;

				PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
				playerState.pointsEarned += 1;

				MinecraftServer server = world.getServer();

				PacketByteBuf data = PacketByteBufs.create();
				data.writeInt(serverState.totalPointsEarned);
				data.writeInt(playerState.pointsEarned);

				ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
				server.execute(() -> {
					ServerPlayNetworking.send(playerEntity, POINTS_EARNED, data);
				});
			}
		});


	}
}