package quickcarpet.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.UserCache;
import net.minecraft.util.profiler.DisableableProfiler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.helper.TickSpeed;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetProfiler;

import java.io.File;
import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow private volatile boolean running;
    @Shadow private long timeReference;
    @Shadow private long field_4557;
    @Shadow private boolean profilerStartQueued;
    @Shadow @Final private DisableableProfiler profiler;
    @Shadow private volatile boolean loading;
    @Shadow private boolean field_19249;
    @Shadow private long field_19248;

    @Shadow protected abstract void tick(BooleanSupplier booleanSupplier_1);

    @Shadow protected abstract boolean shouldKeepTicking();

    @Shadow protected abstract void method_16208();

    // Called during game start
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onMinecraftServerCTOR(File file_1, Proxy proxy_1, DataFixer dataFixer_1,
                                       CommandManager serverCommandManager_1, YggdrasilAuthenticationService yggdrasilAuthenticationService_1,
                                       MinecraftSessionService minecraftSessionService_1, GameProfileRepository gameProfileRepository_1,
                                       UserCache userCache_1, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory_1,
                                       String string_1, CallbackInfo ci) {
        QuickCarpet.getInstance().init((MinecraftServer) (Object) this);
    }

    // Cancel a while statement
    @Redirect(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
    private boolean cancelRunLoop(MinecraftServer server) {
        return false;
    }

    // Replaced the above cancelled while statement with this one
    // could possibly just inject that mspt selection at the beginning of the loop, but then adding all mspt's to
    // replace 50L will be a hassle
    @Inject(method = "run", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
    private void modifiedRunLoop(CallbackInfo ci) {
        TickSpeed tickSpeed = QuickCarpet.getInstance().tickSpeed;
        float partialTimeReference = 0;
        while (this.running) {
            //long long_1 = SystemUtil.getMeasuringTimeMs() - this.timeReference;
            //CM deciding on tick speed
            float mspt = 0;
            long behind = 0L;
            if (tickSpeed.tickWarpStartTime != 0 && tickSpeed.continueWarp()) {
                //making sure server won't flop after the warp or if the warp is interrupted
                this.timeReference = this.field_4557 = SystemUtil.getMeasuringTimeMs();
            } else {
                mspt = tickSpeed.msptGoal; // regular tick
                behind = SystemUtil.getMeasuringTimeMs() - this.timeReference;
            }
            //end tick deciding
            //smoothed out delay to include mspt component. With 50L gives defaults.
            if (behind > /*2000L*/1000L + 20 * mspt && this.timeReference - this.field_4557 >= /*15000L*/10000L + 100 * mspt) {
                float ticks = behind / mspt;//50L;
                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", behind, ticks);
                this.timeReference += ticks * mspt;//50L;
                this.field_4557 = this.timeReference;
            }

            partialTimeReference += mspt - (long) mspt;
            this.timeReference += mspt;//50L;
            if (partialTimeReference > 1) {
                partialTimeReference--;
                timeReference++;
            }
            if (this.profilerStartQueued) {
                this.profilerStartQueued = false;
                this.profiler.getController().enable();
            }

            this.profiler.startTick();
            this.profiler.push("tick");
            this.tick(this::shouldKeepTicking);
            this.profiler.swap("nextTickWait");
            this.field_19249 = true;
            this.field_19248 = Math.max(SystemUtil.getMeasuringTimeMs() + /*50L*/ (long) mspt, this.timeReference);
            this.method_16208();
            this.profiler.pop();
            this.profiler.endTick();
            this.loading = true;
        }

    }

    @Inject(
            method = "tick",
            at = @At(value = "FIELD", target = "net/minecraft/server/MinecraftServer.ticks:I", shift = At.Shift.AFTER, ordinal = 0)
    )
    private void onTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        QuickCarpet.getInstance().tick((MinecraftServer) (Object) this);
        CarpetProfiler.startTick();
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void endTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endTick((MinecraftServer) (Object) this);
    }

    @Inject(
            method = "tick",
            at = @At(value = "CONSTANT", args = "stringValue=save")
    )
    private void startAutosave(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.AUTOSAVE);
    }

    @Inject(
            method = "tick",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=save")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/DisableableProfiler;pop()V",
                    ordinal = 0)
    )
    private void endAutosave(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(null);
    }

    @Inject(
            method = "tickWorlds",
            at = @At(value = "CONSTANT", args = "stringValue=connection")
    )
    private void startNetwork(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startSection(null, CarpetProfiler.SectionType.NETWORK);
    }

    @Inject(
            method = "tickWorlds",
            at = @At(value = "CONSTANT", args = "stringValue=server gui refresh")
    )
    private void endNetwork(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(null);
    }

    @ModifyConstant(method = "prepareStartRegion", constant = @Constant(intValue = 11), require = 1)
    private int adjustSpawnChunkLevel(int level) {
        return Settings.spawnChunkLevel;
    }

    @ModifyConstant(method = "prepareStartRegion", constant = @Constant(intValue = 441), require = 1)
    private int adjustSpawnChunkCount(int count) {
        int sideLength = Settings.spawnChunkLevel * 2 - 1;
        return sideLength * sideLength;
    }
}
