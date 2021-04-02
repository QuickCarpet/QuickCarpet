package quickcarpet.client;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import quickcarpet.api.network.client.ClientPluginChannelHandler;
import quickcarpet.api.settings.ParsedRule;
import quickcarpet.helper.NBTHelper;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.network.impl.PacketSplitter;
import quickcarpet.settings.Settings;

public class ClientRulesChannel implements ClientPluginChannelHandler {
    @Override
    public Identifier[] getChannels() {
        return new Identifier[] {RulesChannel.CHANNEL};
    }

    @Override
    public void onCustomPayload(CustomPayloadS2CPacket packet, ClientPlayPacketListener netHandler) {
        PacketByteBuf buf = PacketSplitter.receive(netHandler, packet);
        if (buf == null) return;
        int packetId = buf.readVarInt();
        if (packetId == RulesChannel.PACKET_S2C_DATA) {
            NbtCompound data = buf.readCompound();
            if (data == null) return;
            int version = data.getInt("Version");
            if (version == 0 || version > RulesChannel.VERSION) return;
            NbtList rulesList = data.getList("Rules", NBTHelper.TAG_COMPOUND);
            for (NbtElement tag : rulesList) {
                NbtCompound ruleTag = (NbtCompound) tag;
                String id = ruleTag.getString("Id");
                try {
                    ParsedRule<?> rule = Settings.MANAGER.getRule(id);
                    rule.set(ruleTag.getString("Value"), false);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
}
