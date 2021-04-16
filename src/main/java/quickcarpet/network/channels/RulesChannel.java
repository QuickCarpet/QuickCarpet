package quickcarpet.network.channels;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import quickcarpet.api.network.server.ServerPluginChannelHandler;
import quickcarpet.api.settings.ParsedRule;
import quickcarpet.network.impl.PacketSplitter;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Translations;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class RulesChannel implements ServerPluginChannelHandler {
    public static final Identifier CHANNEL = new Identifier("carpet:rules");
    public static final int VERSION = 1;

    public static final int PACKET_S2C_DATA = 0;

    public static RulesChannel instance;

    private final Set<ServerPlayerEntity> players = new LinkedHashSet<>();

    public RulesChannel() {
        instance = this;
    }

    @Override
    public Identifier[] getChannels() {
        return new Identifier[] {CHANNEL};
    }

    @Override
    public boolean register(Identifier channel, ServerPlayerEntity player) {
        players.add(player);
        sendRuleUpdate(player, Settings.MANAGER.getRules());
        return true;
    }

    @Override
    public void unregister(Identifier channel, ServerPlayerEntity player) {
        players.remove(player);
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {

    }

    public void sendRuleUpdate(Set<ParsedRule<?>> rules) {
        for (ServerPlayerEntity player : players) sendRuleUpdate(player, rules);
    }

    public void sendRuleUpdate(ServerPlayerEntity player, Collection<ParsedRule<?>> rules) {
        NbtCompound data = serializeRuleUpdate(player, rules);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(PACKET_S2C_DATA);
        buf.writeNbt(data);
        PacketSplitter.send(player.networkHandler, CHANNEL, buf);
    }

    public static NbtCompound serializeRuleUpdate(ServerPlayerEntity player, Collection<ParsedRule<?>> rules) {
        NbtCompound data = new NbtCompound();
        data.putInt("Version", VERSION);
        NbtList rulesList = new NbtList();
        for (ParsedRule<?> rule : rules) {
            NbtCompound ruleTag = new NbtCompound();
            ruleTag.putString("Id", rule.getName());
            ruleTag.putString("Type", rule.getType().getName());
            ruleTag.putString("DefaultValue", rule.getDefaultAsString());
            ruleTag.putString("Value", rule.getAsString());
            ruleTag.putString("Description", Translations.translate(rule.getDescription(), player).getString());
            NbtList extraList = new NbtList();
            if (rule.getExtraInfo() != null) {
                String[] extraInfo = Translations.translate(rule.getExtraInfo(), player).getString().split("\n");
                for (String extra : extraInfo) extraList.add(NbtString.of(extra));
            }
            ruleTag.put("ExtraInfo", extraList);
            rulesList.add(ruleTag);
        }
        data.put("Rules", rulesList);
        return data;
    }
}
