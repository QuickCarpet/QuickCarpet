package quickcarpet.test.mixin;

import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.test.ServerStarter;

@Mixin(Main.class)
public class MainMixin {
    @Inject(method = "main", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mainHead(String[] args, CallbackInfo ci) {
        if (Boolean.parseBoolean(System.clearProperty("quickcarpet.test"))) {
            try {
                ServerStarter.main(args);
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(-1);
            }
            ci.cancel();
        }
    }
}
