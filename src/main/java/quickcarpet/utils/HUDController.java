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
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Logger;
import quickcarpet.logging.Loggers;
import quickcarpet.logging.PacketCounter;

import java.util.*;
import java.util.function.Consumer;

import static quickcarpet.utils.Messenger.*;

public class HUDController {
    private static final Map<Logger, Consumer<Logger>> HUD_LOGGERS = new LinkedHashMap<>();
    public static final Map<PlayerEntity, List<MutableText>> PLAYER_HUDS = new WeakHashMap<>();

    static {
        HUD_LOGGERS.put(Loggers.TPS, HUDController::logTps);
        HUD_LOGGERS.put(Loggers.MOBCAPS, HUDController::logMobcaps);
        HUD_LOGGERS.put(Loggers.COUNTER, HUDController::logCounter);
        HUD_LOGGERS.put(Loggers.PACKETS, HUDController::logPackets);
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

    private static void sendHUD(PlayerEntity player, Text header, Text footer) {
        ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerListHeaderS2CPacket(header, footer));
    }


    public static void update(MinecraftServer server) {
        if (server.getTicks() % 20 != 0)
            return;

        PLAYER_HUDS.clear();

        for (Map.Entry<Logger, Consumer<Logger>> hudLogger : HUD_LOGGERS.entrySet()) {
            hudLogger.getValue().accept(hudLogger.getKey());
        }

        for (Map.Entry<PlayerEntity, List<MutableText>> playerHud : PLAYER_HUDS.entrySet()) {
            sendHUD(playerHud.getKey(), new LiteralText(""), c(playerHud.getValue().toArray(new MutableText[0])));
        }
    }

    private static void logTps(Logger logger) {
        TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();
        double MSPT = tickSpeed.getCurrentMSPT();
        double TPS = tickSpeed.calculateTPS(MSPT);
        Formatting color = getHeatmapColor(MSPT, tickSpeed.msptGoal);
        logger.log(() -> c(
                s("TPS: ", Formatting.GRAY), formats("%.1f", color, TPS),
                s(" MSPT: ", Formatting.GRAY), formats("%.1f", color, MSPT)
        ), () -> Arrays.asList(
            new LogParameter("MSPT", tickSpeed::getCurrentMSPT),
            new LogParameter("TPS", tickSpeed::getTPS)
        ));
    }

    private static void logMobcaps(Logger logger) {
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
            return c(components.toArray(new MutableText[0]));
        }, () -> Mobcaps.getCommandParameters(QuickCarpetServer.getMinecraftServer()));
    }

    private static void logCounter(Logger logger) {
        logger.log(color -> {
            HopperCounter counter = HopperCounter.getCounter(color);
            List<MutableText> res = counter == null ? Collections.emptyList() : counter.format(QuickCarpetServer.getMinecraftServer(), false, true);
            return c(res.toArray(new MutableText[0]));
        }, () -> HopperCounter.COMMAND_PARAMETERS);
    }

    private static void logPackets(Logger logger) {
        logger.log(() -> {
            PacketCounter.reset();
            return s("I/" + PacketCounter.getPreviousIn() + " O/" + PacketCounter.getPreviousOut());
        }, () -> Arrays.asList(
            new LogParameter("in", PacketCounter::getPreviousIn),
            new LogParameter("out", PacketCounter::getPreviousOut)
        ));
    }
}
