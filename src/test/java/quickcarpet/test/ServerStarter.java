package quickcarpet.test;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.test.*;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ServerStarter {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws Throwable {
        SharedConstants.createGameVersion();
        SharedConstants.isDevelopment = true;
        CrashReport.initCrashReport();
        Bootstrap.initialize();
        Bootstrap.logMissing();
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        argList.remove("nogui");
        if (!argList.isEmpty()) {
            StructureTestUtil.testStructuresDirectoryName = Path.of(argList.get(0)).toAbsolutePath().toString();
        }
        DynamicRegistryManager.Impl registryManager = DynamicRegistryManager.create();
        Path worldPath = Path.of(".");
        LevelStorage storage = LevelStorage.create(worldPath);
        LevelStorage.Session storageSession = storage.createSession("gametestworld");
        ResourcePackManager resourcePackManager = new ResourcePackManager(ResourceType.SERVER_DATA,
            new VanillaDataPackProvider(),
            new ModResourcePackCreator(ResourceType.SERVER_DATA)
        );
        DataPackSettings dataPackSettings = new DataPackSettings(Collections.emptyList(), Collections.emptyList());
        MinecraftServer.loadDataPacks(resourcePackManager, dataPackSettings, false);
        ServerResourceManager serverResourceManager = ServerResourceManager.reload(resourcePackManager.createResourcePacks(), registryManager, CommandManager.RegistrationEnvironment.DEDICATED, 4, Util.getMainWorkerExecutor(), Runnable::run).get();
        serverResourceManager.loadRegistryTags();
        Collection<TestFunction> testFunctions = collectTestFunctions();
        Collection<GameTestBatch> batches = TestUtil.createBatches(testFunctions);
        LOGGER.info("Found {} test functions in {} batches", testFunctions.size(), batches.size());
        if (batches.isEmpty()) {
            LOGGER.info("No tests to run");
            System.exit(0);
        }
        BlockPos spawnPos = new BlockPos(0, 5, 0);
        MinecraftServer.startServer(serverThread -> new TestServer(serverThread, storageSession, resourcePackManager, serverResourceManager, batches, spawnPos, registryManager));
    }

    private static Collection<TestFunction> collectTestFunctions() throws URISyntaxException, IOException {
        String classFile = "/" + ServerStarter.class.getName().replace('.', '/') + ".class";
        Path serverStarterPath = Path.of(ServerStarter.class.getResource(classFile).toURI());
        Path root = Path.of(new URI(serverStarterPath.toUri().toString().replace(classFile, "")));
        LOGGER.info("Searching for test classes in {}", root);
        Files.walk(root)
            .filter(p -> p.getFileName().toString().endsWith(".class"))
            .map(p -> root.relativize(p).toString())
            .filter(s -> !s.toLowerCase(Locale.ROOT).contains("mixin"))
            .map(s -> s.substring(0, s.length() - 6).replace('/', '.'))
            .map(className -> {
                try {
                    return Class.forName(className, false, ServerStarter.class.getClassLoader());
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("Could not load {}", className);
                    return null;
                }
            }).filter(Objects::nonNull)
            .forEach(TestFunctions::register);
        return TestFunctions.getTestFunctions();
    }
}
