package quickcarpet.utils;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.Mobcaps;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.Logger;
import quickcarpet.logging.Loggers;
import quickcarpet.logging.loghelpers.PacketCounter;
import quickcarpet.mixin.accessor.PlayerListHeaderS2CPacketAccessor;

import java.util.*;
import java.util.function.Consumer;

public class HUDController {
    public static final Map<PlayerEntity, List<Text>> PLAYER_HUDS = new WeakHashMap<>();

    private static final Map<Logger<?>, Consumer<Logger<?>>> HUD_LOGGERS = new LinkedHashMap<>();

    private static <T extends Logger.CommandParameters> void registerLogger(Logger<T> logger, Consumer<Logger<T>> log) {
        HUD_LOGGERS.put(logger, (Consumer) log);
    }

    static {
        registerLogger(Loggers.TPS, logger -> {
            TickSpeed tickSpeed = QuickCarpet.getInstance().tickSpeed;
            double MSPT = tickSpeed.getCurrentMSPT();
            double TPS = tickSpeed.calculateTPS(MSPT);
            char color = Messenger.getHeatmapColor(MSPT, tickSpeed.msptGoal);
            Text[] message = new Text[]{Messenger.c(
                    "g TPS: ", String.format(Locale.US, "%s %.1f", color, TPS),
                    "g  MSPT: ", String.format(Locale.US, "%s %.1f", color, MSPT))};
            logger.log(() -> message, () -> tickSpeed.LOG_COMMAND_PARAMETERS);
        });

        registerLogger(Loggers.MOBCAPS, logger -> {
            logger.log((option, player) -> {
                DimensionType dim = player.dimension;
                switch (option) {
                    case "overworld":
                        dim = DimensionType.OVERWORLD;
                        break;
                    case "nether":
                        dim = DimensionType.THE_NETHER;
                        break;
                    case "end":
                        dim = DimensionType.THE_END;
                        break;
                }
                List<Text> components = new ArrayList<>();
                Map<EntityCategory, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(dim);
                for (Map.Entry<EntityCategory, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
                    Pair<Integer, Integer> pair = e.getValue();
                    int actual = pair.getLeft();
                    int limit = pair.getRight();
                    components.add(Messenger.c(
                            (actual + limit == 0) ? "g -" : Messenger.getHeatmapColor(actual, limit) + " " + actual,
                            Messenger.creatureTypeColor(e.getKey()) + " /" + ((actual + limit == 0) ? "-" : limit)
                    ));
                    components.add(Messenger.c("w  "));
                }
                components.remove(components.size() - 1);
                return new Text[]{Messenger.c(components.toArray(new Object[0]))};
            }, () -> Mobcaps.LogCommandParameters.INSTANCE);
        });

        registerLogger(Loggers.COUNTER, logger -> logger.log(color -> {
            HopperCounter counter = HopperCounter.getCounter(color);
            List<Text> res = counter == null ? Collections.emptyList() : counter.format(QuickCarpet.minecraft_server, false, true);
            return new Text[]{Messenger.c(res.toArray(new Object[0]))};
        }, () -> HopperCounter.LogCommandParameters.INSTANCE));

        registerLogger(Loggers.PACKETS, logger -> logger.log(() -> {
            Text[] ret = new Text[]{
                    Messenger.c("w I/" + PacketCounter.totalIn + " O/" + PacketCounter.totalOut),
            };
            PacketCounter.reset();
            return ret;
        }, () -> PacketCounter.LogCommandParameters.INSTANCE));
    }

    public static void addMessage(ServerPlayerEntity player, Text hudMessage) {
        if (!PLAYER_HUDS.containsKey(player)) {
            PLAYER_HUDS.put(player, new ArrayList<>());
        } else {
            PLAYER_HUDS.get(player).add(new LiteralText("\n"));
        }
        PLAYER_HUDS.get(player).add(Translations.translate(hudMessage, player));
    }

    public static void clearPlayerHUD(PlayerEntity player) {
        sendHUD(player, new LiteralText(""), new LiteralText(""));
    }

    public static void sendHUD(PlayerEntity player, Text header, Text footer) {
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
        ((PlayerListHeaderS2CPacketAccessor) packet).setHeader(header);
        ((PlayerListHeaderS2CPacketAccessor) packet).setFooter(footer);
        ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
    }


    public static void update(MinecraftServer server) {
        if (server.getTicks() % 20 != 0)
            return;

        PLAYER_HUDS.clear();

        for (Map.Entry<Logger<?>, Consumer<Logger<?>>> hudLogger : HUD_LOGGERS.entrySet()) {
            hudLogger.getValue().accept(hudLogger.getKey());
        }

        for (Map.Entry<PlayerEntity, List<Text>> playerHud : PLAYER_HUDS.entrySet()) {
            sendHUD(playerHud.getKey(), new LiteralText(""), Messenger.c(playerHud.getValue().toArray(new Object[0])));
        }
    }
}
