package quickcarpet.test.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.fabricmc.api.EnvType;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestServer;
import net.minecraft.test.TestSet;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpet;
import quickcarpet.test.ServerStarter;

import java.net.Proxy;

@Mixin(TestServer.class)
public abstract class TestServerMixin extends MinecraftServer {
    @Shadow private TestSet testSet;

    public TestServerMixin(Thread serverThread, DynamicRegistryManager.Impl registryManager, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager dataPackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
        super(serverThread, registryManager, session, saveProperties, dataPackManager, proxy, dataFixer, serverResourceManager, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
    }

    @Inject(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/test/TestServer;loadWorld()V"))
    private void onSetupServerDedicated(CallbackInfoReturnable<Boolean> cir) {
        QuickCarpet.getInstance().onGameStarted(EnvType.SERVER);
        QuickCarpet.getInstance().onServerLoaded((TestServer) (Object) this);
    }

    @Inject(method = "runTestBatches", at = @At("RETURN"))
    private void addListener(ServerWorld world, CallbackInfo ci) {
        this.testSet.addListener(ServerStarter.TEST_LISTENER);
    }

    @Override
    public void shutdown() {
        ServerStarter.COMPLETION_LISTENER.onStopped();
        super.shutdown();
    }
}
