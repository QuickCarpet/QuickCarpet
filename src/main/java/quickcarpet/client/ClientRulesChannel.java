package quickcarpet.client;

import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import quickcarpet.helper.NBTHelper;
import quickcarpet.network.ClientPluginChannelHandler;
import quickcarpet.network.PacketSplitter;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.settings.ParsedRule;
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
            CompoundTag data = buf.readCompoundTag();
            if (data == null) return;
            int version = data.getInt("Version");
            if (version == 0 || version > RulesChannel.VERSION) return;
            ListTag rulesList = data.getList("Rules", NBTHelper.TAG_COMPOUND);
            for (Tag tag : rulesList) {
                CompoundTag ruleTag = (CompoundTag) tag;
                String id = ruleTag.getString("Id");
                try {
                    ParsedRule rule = Settings.MANAGER.getRule(id);
                    rule.set(ruleTag.getString("Value"), false);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
}
