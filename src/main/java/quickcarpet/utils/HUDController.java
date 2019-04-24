package quickcarpet.utils;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.Mobcaps;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.logging.loghelpers.PacketCounter;
import quickcarpet.mixin.IPlayerListHeaderS2CPacket;

import java.util.*;

public class HUDController
{
    public static Map<PlayerEntity, List<TextComponent>> player_huds = new HashMap<>();

    public static void addMessage(PlayerEntity player, TextComponent hudMessage)
    {
        if (!player_huds.containsKey(player))
        {
            player_huds.put(player, new ArrayList<>());
        }
        else
        {
            player_huds.get(player).add(new StringTextComponent("\n"));
        }
        player_huds.get(player).add(hudMessage);
    }
    public static void clear_player(PlayerEntity player)
    {
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
        ((IPlayerListHeaderS2CPacket)packet).setHeader(new StringTextComponent(""));
        ((IPlayerListHeaderS2CPacket)packet).setFooter(new StringTextComponent(""));
        ((ServerPlayerEntity)player).networkHandler.sendPacket(packet);
    }


    public static void update_hud(MinecraftServer server)
    {
        if(server.getTicks() % 20 != 0)
            return;

        player_huds.clear();

        if (LoggerRegistry.__tps)
            log_tps(server);

        if (LoggerRegistry.__mobcaps)
            log_mobcaps();

        if(LoggerRegistry.__counter)
            log_counter(server);

        if (LoggerRegistry.__packets)
            LoggerRegistry.getLogger("packets").log(HUDController::packetCounter,
                    "TOTAL_IN", PacketCounter.totalIn,
                    "TOTAL_OUT", PacketCounter.totalOut);

        for (PlayerEntity player: player_huds.keySet())
        {
            PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
            ((IPlayerListHeaderS2CPacket)packet).setHeader(new StringTextComponent(""));
            ((IPlayerListHeaderS2CPacket)packet).setFooter(Messenger.c(player_huds.get(player).toArray(new Object[0])));
            ((ServerPlayerEntity)player).networkHandler.sendPacket(packet);
        }
    }
    private static void log_tps(MinecraftServer server)
    {
        double MSPT = MathHelper.average(server.lastTickLengths) * 1.0E-6D;
        double TPS = 1000.0 / Math.max(TickSpeed.isWarping ? 0.0 : (1000.0 / TickSpeed.serverTPS), MSPT);
        String color = Messenger.heatmap_color(MSPT, TickSpeed.mspt);
        TextComponent[] message = new TextComponent[]{Messenger.c(
                "g TPS: ", String.format(Locale.US, "%s %.1f",color, TPS),
                "g  MSPT: ", String.format(Locale.US,"%s %.1f", color, MSPT))};
        LoggerRegistry.getLogger("tps").log(() -> message, "MSPT", MSPT, "TPS", TPS);
    }
    
    private static void log_mobcaps()
    {
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
        LoggerRegistry.getLogger("mobcaps").log((option, player) -> {
            DimensionType dim = player.dimension;
            switch (option)
            {
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
            return send_mobcap_display(dim);
        } /*, commandParams.toArray() */ );
    }

    private static TextComponent [] send_mobcap_display(DimensionType dim)
    {
        List<TextComponent> components = new ArrayList<>();
        Map<EntityCategory, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(dim);
        for (Map.Entry<EntityCategory, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
            Pair<Integer, Integer> pair = e.getValue();
            int actual = pair.getLeft();
            int limit = pair.getRight();
            components.add(Messenger.c(
                    (actual+limit == 0)?"g -":Messenger.heatmap_color(actual,limit)+" "+actual,
                    Messenger.creatureTypeColor(e.getKey())+" /"+((actual+limit == 0)?"-":limit)
            ));
            components.add(Messenger.c("w  "));
        }
        components.remove(components.size()-1);
        return new TextComponent[]{Messenger.c(components.toArray(new Object[0]))};
    }

    private static void log_counter(MinecraftServer server)
    {
        List<Object> commandParams = new ArrayList<>();
        for (HopperCounter counter : HopperCounter.COUNTERS.values())
            Collections.addAll(commandParams, counter.color.name(), counter.getTotalItems());
        LoggerRegistry.getLogger("counter").log((option) -> send_counter_info(server, option), commandParams);
    }

    private static TextComponent [] send_counter_info(MinecraftServer server, String color)
    {
        HopperCounter counter = HopperCounter.getCounter(color);
        List<TextComponent> res = counter == null ? Collections.emptyList() : counter.format(server, false, true);
        return new TextComponent[]{ Messenger.c(res.toArray(new Object[0]))};
    }

    private static TextComponent [] packetCounter()
    {
        TextComponent [] ret =  new TextComponent[]{
                Messenger.c("w I/" + PacketCounter.totalIn + " O/" + PacketCounter.totalOut),
        };
        PacketCounter.reset();
        return ret;
    }
}
