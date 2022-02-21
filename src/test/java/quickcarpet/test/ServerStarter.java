package quickcarpet.test;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.*;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import quickcarpet.settings.Settings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ServerStarter {
    private static final Path OUTPUT_DIR = Path.of("../build/test-results/runTestServer");
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|TestManager");
    public static final List<TestCompletionListener> COMPLETION_LISTENERS = new ArrayList<>();

    static {
        try {
            Files.createDirectories(OUTPUT_DIR);
            COMPLETION_LISTENERS.add(new XmlReportingTestCompletionListener(ServerStarter.OUTPUT_DIR.resolve("TEST-gametest.xml").toFile()));
            COMPLETION_LISTENERS.add(new TestCompletionListener() {
                @Override
                public void onTestFailed(GameTestState test) {
                    test.getThrowable().printStackTrace();
                }

                @Override
                public void onTestPassed(GameTestState test) {}
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final TestListener TEST_LISTENER = new TestListener() {
        @Override
        public void onStarted(GameTestState test) {

        }

        @Override
        public void onPassed(GameTestState test) {
            for (TestCompletionListener l : COMPLETION_LISTENERS) {
                l.onTestPassed(test);
            }
        }

        @Override
        public void onFailed(GameTestState test) {
            for (TestCompletionListener l : COMPLETION_LISTENERS) {
                l.onTestFailed(test);
            }
        }
    };

    public static void main(String[] args) throws Throwable {
        SharedConstants.createGameVersion();
        SharedConstants.isDevelopment = true;
        CrashReport.initCrashReport();
        Bootstrap.initialize();
        Bootstrap.logMissing();
        MixinEnvironment.getCurrentEnvironment().audit();
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        argList.remove("nogui");
        if (!argList.isEmpty()) {
            StructureTestUtil.testStructuresDirectoryName = Path.of(argList.get(0)).toAbsolutePath().toString();
        }
        Path runDir = Path.of(".");
        Path worldPath = runDir.resolve("gametestworld");
        if (Files.exists(worldPath)) {
            Files.walkFileTree(worldPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) throw exc;
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        LevelStorage storage = LevelStorage.create(runDir);
        LevelStorage.Session storageSession = storage.createSession(worldPath.getFileName().toString());
        ResourcePackManager resourcePackManager = new ResourcePackManager(ResourceType.SERVER_DATA,
            new VanillaDataPackProvider(),
            new ModResourcePackCreator(ResourceType.SERVER_DATA)
        );
        DataPackSettings dataPackSettings = new DataPackSettings(Collections.emptyList(), Collections.emptyList());
        MinecraftServer.loadDataPacks(resourcePackManager, dataPackSettings, false);
        Collection<TestFunction> testFunctions = collectTestFunctions();
        Collection<GameTestBatch> batches = TestUtil.createBatches(testFunctions);
        LOGGER.info("Found {} test functions in {} batches", testFunctions.size(), batches.size());
        if (batches.isEmpty()) {
            LOGGER.info("No tests to run");
            System.exit(0);
        }
        List<GameTestBatch> processedBatches = new ArrayList<>();
        for (GameTestBatch batch : batches) {
            processedBatches.add(processBatch(batch));
        }
        BlockPos spawnPos = new BlockPos(0, 5, 0);
        MinecraftServer.startServer(serverThread -> TestServer.create(serverThread, storageSession, resourcePackManager, processedBatches, spawnPos));
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

    private static GameTestBatch processBatch(GameTestBatch batch) {
        String id = batch.getId();
        if (!id.startsWith("rules/")) return batch;
        String[] ruleSpecs = id.substring(6, id.indexOf(':')).split(",");
        Map<String, String> rules = new LinkedHashMap<>();
        for (String ruleSpec : ruleSpecs) {
            String[] parts = ruleSpec.split("=", 2);
            rules.put(parts[0], parts[1]);
        }
        return new GameTestBatch(id, batch.getTestFunctions(), world -> {
            LOGGER.info("Setting {}", rules);
            for (var e : rules.entrySet()) {
                Settings.MANAGER.getRule(e.getKey()).set(e.getValue(), true);
            }
            batch.startBatch(world);
        }, world -> {
            LOGGER.info("Resetting {}", rules.keySet());
            for (String rule : rules.keySet()) {
                Settings.MANAGER.getRule(rule).resetToDefault(true);
            }
            batch.finishBatch(world);
        });
    }
}
