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
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.logging.loghelpers.PacketCounter;
import quickcarpet.mixin.PlayerListHeaderS2CPacketAccessor;

import java.util.*;
import java.util.function.Consumer;

public class HUDController {
    public static final Map<PlayerEntity, List<Text>> PLAYER_HUDS = new WeakHashMap<>();

    private static final Map<Logger<?>, Consumer<Logger<?>>> HUD_LOGGERS = new LinkedHashMap<>();

    private static <T extends Logger.CommandParameters> void registerLogger(Logger<T> logger, Consumer<Logger<T>> log) {
        HUD_LOGGERS.put(logger, (Consumer) log);
    }

    static {
        registerLogger(LoggerRegistry.TPS, logger -> {
            double MSPT = TickSpeed.getCurrentMSPT();
            double TPS = TickSpeed.calculateTPS(MSPT);
            String color = Messenger.heatmap_color(MSPT, TickSpeed.mspt);
            Text[] message = new Text[]{Messenger.c(
                    "g TPS: ", String.format(Locale.US, "%s %.1f", color, TPS),
                    "g  MSPT: ", String.format(Locale.US, "%s %.1f", color, MSPT))};
            logger.log(() -> message, () -> TickSpeed.LogCommandParameters.INSTANCE);
        });

        registerLogger(LoggerRegistry.MOBCAPS, logger -> {
            /*
            TODO: make this work when CommandLogHandler is implemented
            List<Object> commandParams = new ArrayList<>();
            for (int dim = -1; dim <= 1; dim++)
            {
                for (EnumCreatureType type : EnumCreatureType.values())
                {
                    Tuple<Integer, Integer> counts = SpawnReporter.mobcaps.get(dim).getOrDefault(type, new Tuple<>(0, 0));
                    int actual = counts.getFirst(), limit = counts.getSecond();
                    Collections.addAll(commandParams, type.name() + "_ACTUAL_DIM_" + dim, actual, type.name() + "_ACTUAL_LIMIT_" + dim, limit);
                }
            }
            */
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
                            (actual + limit == 0) ? "g -" : Messenger.heatmap_color(actual, limit) + " " + actual,
                            Messenger.creatureTypeColor(e.getKey()) + " /" + ((actual + limit == 0) ? "-" : limit)
                    ));
                    components.add(Messenger.c("w  "));
                }
                components.remove(components.size() - 1);
                return new Text[]{Messenger.c(components.toArray(new Object[0]))};
            });
        });

        registerLogger(LoggerRegistry.COUNTER, logger -> logger.log(color -> {
            HopperCounter counter = HopperCounter.getCounter(color);
            List<Text> res = counter == null ? Collections.emptyList() : counter.format(QuickCarpet.minecraft_server, false, true);
            return new Text[]{Messenger.c(res.toArray(new Object[0]))};
        }, () -> HopperCounter.LogCommandParameters.INSTANCE));

        registerLogger(LoggerRegistry.PACKETS, logger -> logger.log(() -> {
            Text[] ret = new Text[]{
                    Messenger.c("w I/" + PacketCounter.totalIn + " O/" + PacketCounter.totalOut),
            };
            PacketCounter.reset();
            return ret;
        }, () -> PacketCounter.LogCommandParameters.INSTANCE));
    }

    public static void addMessage(PlayerEntity player, Text hudMessage) {
        if (!PLAYER_HUDS.containsKey(player)) {
            PLAYER_HUDS.put(player, new ArrayList<>());
        } else {
            PLAYER_HUDS.get(player).add(new LiteralText("\n"));
        }
        PLAYER_HUDS.get(player).add(hudMessage);
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
