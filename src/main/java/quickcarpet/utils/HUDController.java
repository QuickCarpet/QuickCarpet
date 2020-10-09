package quickcarpet.utils;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import quickcarpet.QuickCarpetServer;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.Mobcaps;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.Logger;
import quickcarpet.logging.Loggers;
import quickcarpet.logging.loghelpers.PacketCounter;
import quickcarpet.mixin.accessor.PlayerListHeaderS2CPacketAccessor;

import java.util.*;
import java.util.function.Consumer;

import static quickcarpet.utils.Messenger.*;

public class HUDController {
    public static final Map<PlayerEntity, List<MutableText>> PLAYER_HUDS = new WeakHashMap<>();

    private static final Map<Logger<?>, Consumer<Logger<?>>> HUD_LOGGERS = new LinkedHashMap<>();

    private static <T extends Logger.CommandParameters> void registerLogger(Logger<T> logger, Consumer<Logger<T>> log) {
        HUD_LOGGERS.put(logger, (Consumer) log);
    }

    static {
        registerLogger(Loggers.TPS, logger -> {
            TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();
            double MSPT = tickSpeed.getCurrentMSPT();
            double TPS = tickSpeed.calculateTPS(MSPT);
            Formatting color = Messenger.getHeatmapColor(MSPT, tickSpeed.msptGoal);
            MutableText[] message = {c(
                s("TPS: ", Formatting.GRAY), formats("%.1f", color, TPS),
                s(" MSPT: ", Formatting.GRAY), formats("%.1f", color, MSPT)
            )};
            logger.log(() -> message, () -> tickSpeed.LOG_COMMAND_PARAMETERS);
        });

        registerLogger(Loggers.MOBCAPS, logger -> {
            logger.log((option, player) -> {
                ServerWorld world = (ServerWorld) player.world;
                MinecraftServer server = world.getServer();
                RegistryKey<World> dim = world.getRegistryKey();
                switch (option) {
                    case "overworld":
                        dim = World.OVERWORLD;
                        break;
                    case "nether":
                        dim = World.NETHER;
                        break;
                    case "end":
                        dim = World.END;
                        break;
                }
                List<MutableText> components = new ArrayList<>();
                Map<SpawnGroup, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(server.getWorld(dim));
                for (Map.Entry<SpawnGroup, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
                    Pair<Integer, Integer> pair = e.getValue();
                    int actual = pair.getLeft();
                    int limit = pair.getRight();
                    if (actual + limit == 0) {
                        components.add(s("-/-", Formatting.GRAY));
                    } else {
                        components.add(s(Integer.toString(actual), getHeatmapColor(actual, limit)));
                        components.add(s("/", Formatting.GRAY));
                        components.add(s(Integer.toString(limit), creatureTypeColor(e.getKey())));
                    }
                    components.add(s(" "));
                }
                components.remove(components.size() - 1);
                return new MutableText[]{c(components.toArray(new MutableText[0]))};
            }, () -> Mobcaps.LogCommandParameters.INSTANCE);
        });

        registerLogger(Loggers.COUNTER, logger -> logger.log(color -> {
            HopperCounter counter = HopperCounter.getCounter(color);
            List<MutableText> res = counter == null ? Collections.emptyList() : counter.format(QuickCarpetServer.getMinecraftServer(), false, true);
            return new MutableText[]{c(res.toArray(new MutableText[0]))};
        }, () -> HopperCounter.LogCommandParameters.INSTANCE));

        registerLogger(Loggers.PACKETS, logger -> logger.log(() -> {
            MutableText[] ret = new MutableText[]{
                s("I/" + PacketCounter.totalIn + " O/" + PacketCounter.totalOut),
            };
            PacketCounter.reset();
            return ret;
        }, () -> PacketCounter.LogCommandParameters.INSTANCE));
    }

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

        for (Map.Entry<PlayerEntity, List<MutableText>> playerHud : PLAYER_HUDS.entrySet()) {
            sendHUD(playerHud.getKey(), new LiteralText(""), c(playerHud.getValue().toArray(new MutableText[0])));
        }
    }
}
