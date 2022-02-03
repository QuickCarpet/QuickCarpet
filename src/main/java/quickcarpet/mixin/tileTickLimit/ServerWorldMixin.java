package quickcarpet.mixin.tileTickLimit;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 65536))
    private int quickcarpet$tileTickLimit(int vanilla) {
        return Settings.tileTickLimit;
    }
}
