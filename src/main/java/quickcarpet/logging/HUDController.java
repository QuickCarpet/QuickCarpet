package quickcarpet.logging;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import quickcarpet.utils.Translations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static quickcarpet.utils.Messenger.c;
import static quickcarpet.utils.Messenger.s;

public class HUDController {
    public static final Map<PlayerEntity, List<MutableText>> PLAYER_HUDS = new WeakHashMap<>();

    public static void addMessage(ServerPlayerEntity player, MutableText hudMessage) {
        if (!PLAYER_HUDS.containsKey(player)) {
            PLAYER_HUDS.put(player, new ArrayList<>());
        } else {
            PLAYER_HUDS.get(player).add(s("\n"));
        }
        PLAYER_HUDS.get(player).add(Translations.translate(hudMessage, player));
    }

    public static void clearPlayerHUD(PlayerEntity player) {
        sendHUD(player, s(""), s(""));
    }

    private static void sendHUD(PlayerEntity player, Text header, Text footer) {
        ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerListHeaderS2CPacket(header, footer));
    }


    public static void update() {
        for (Map.Entry<PlayerEntity, List<MutableText>> playerHud : PLAYER_HUDS.entrySet()) {
            sendHUD(playerHud.getKey(), new LiteralText(""), c(playerHud.getValue().toArray(new MutableText[0])));
        }
        PLAYER_HUDS.clear();
    }
}
