package quickcarpet.test.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.fabricmc.api.EnvType;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestCompletionListener;
import net.minecraft.test.TestServer;
import net.minecraft.test.TestSet;
import net.minecraft.util.UserCache;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;
import quickcarpet.test.ServerStarter;

import java.net.Proxy;

@Mixin(TestServer.class)
public abstract class TestServerMixin extends MinecraftServer {
    @Shadow private TestSet testSet;

    public TestServerMixin(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
        super(serverThread, session, dataPackManager, saveLoader, proxy, dataFixer, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
    }

    @Inject(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/test/TestServer;loadWorld()V"))
    private void onSetupServerDedicated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onGameStarted(EnvType.SERVER);
        QuickCarpet.getInstance().onServerLoaded((TestServer) (Object) this);
        Settings.MANAGER.getRule("spawnChunkLevel").set("1", false);
    }

    @Inject(method = "runTestBatches", at = @At("RETURN"))
    private void addListener(ServerWorld world, CallbackInfo ci) {
        this.testSet.addListener(ServerStarter.TEST_LISTENER);
    }

    @Override
    public void shutdown() {
        for (TestCompletionListener l : ServerStarter.COMPLETION_LISTENERS) {
            l.onStopped();
        }
        super.shutdown();
    }
}
