package net.tsukers.wisteria;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public class WisteriaClient implements ClientModInitializer {
    public static PlayerData playerData = new PlayerData();
    public static int playerPoints;
    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(Wisteria.POINTS_EARNED, (client, handler, buf, responseSender) -> {
            int totalPointsEarned = buf.readInt();
            int playerSpecificPointsEarned = buf.readInt();

            playerPoints = playerSpecificPointsEarned;

            client.execute(() -> {
                client.player.sendMessage(Text.literal("Total points earned: " + totalPointsEarned));
                client.player.sendMessage(Text.literal("Player specific points earned: " + playerSpecificPointsEarned));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Wisteria.INITIAL_SYNC, (client, handler, buf, responseSender) -> {
            playerData.pointsEarned = buf.readInt();

            client.execute(() -> {
                client.player.sendMessage(Text.literal("Initial specific dirt blocks broken: " + playerData.pointsEarned));
            });
        });

    }
}
