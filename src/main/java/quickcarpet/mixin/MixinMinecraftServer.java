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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.helper.TickSpeed;

import java.io.File;
import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
    
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private volatile boolean running;
    @Shadow
    private long timeReference;
    @Shadow
    private long field_4557;
    @Shadow
    private boolean field_4597;
    @Shadow
    @Final
    private DisableableProfiler profiler;
    @Shadow
    private volatile boolean field_4547;
    
    @Shadow
    protected abstract void method_3748(BooleanSupplier booleanSupplier_1);
    
    @Shadow
    protected abstract boolean shouldKeepTicking();
    
    @Shadow
    protected abstract void method_16208();
    
    @Shadow private boolean field_19249;
    
    @Shadow private long field_19248;
    
    // Called during game start
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onMinecraftServerCTOR(File file_1, Proxy proxy_1, DataFixer dataFixer_1,
            CommandManager serverCommandManager_1, YggdrasilAuthenticationService yggdrasilAuthenticationService_1,
            MinecraftSessionService minecraftSessionService_1, GameProfileRepository gameProfileRepository_1,
            UserCache userCache_1, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory_1,
            String string_1, CallbackInfo ci)
    {
        QuickCarpet.init((MinecraftServer) (Object) this);
    }
    
    // Called during game start
    /*
    @Inject(method = "method_3748", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/MinecraftServer;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void carpetTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci)
    {
        QuickCarpet.tick((MinecraftServer) (Object) this);
    }
    */
    
    // Cancel a while statement
    @Redirect(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
    private boolean cancelRunLoop(MinecraftServer server)
    {
        return false;
    }
    
    // Replaced the above cancelled while statement with this one
    @Inject(method = "run", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
    private void modifiedRunLoop(CallbackInfo ci)
    {
        while (this.running)
        {
            long long_1 = SystemUtil.getMeasuringTimeMs() - this.timeReference;
            if (long_1 > TickSpeed.warn_time/*2000L*/ && this.timeReference - this.field_4557 >= 15000L)
            {
                long long_2 = long_1 / TickSpeed.ms_per_tick;//50L;
                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", long_1, long_2);
                this.timeReference += long_2 * TickSpeed.ms_per_tick;//50L;
                this.field_4557 = this.timeReference;
            }
        
            this.timeReference += TickSpeed.ms_per_tick;//50L;
            if (this.field_4597)
            {
                this.field_4597 = false;
                this.profiler.getController().enable();
            }
        
            this.profiler.startTick();
            this.profiler.push("tick");
            TickSpeed.processWarp((MinecraftServer)(Object)this);
            this.method_3748(this::shouldKeepTicking);
            this.profiler.swap("nextTickWait");
            this.field_19249 = true;
            this.field_19248 = Math.max(SystemUtil.getMeasuringTimeMs() + 50L, this.timeReference);
            this.method_16208();
            this.profiler.pop();
            this.profiler.endTick();
            this.field_4547 = true;
        }
        
    }
    
}
