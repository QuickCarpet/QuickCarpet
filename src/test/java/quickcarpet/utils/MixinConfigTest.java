package quickcarpet.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import quickcarpet.QuickCarpet;
import quickcarpet.api.settings.ParsedRule;
import quickcarpet.settings.Settings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MixinConfigTest {

    @Test
    public void validate() {
        Settings.MANAGER.parse();
        Set<String> realRules = Settings.MANAGER.getRules().stream()
            .map(ParsedRule::getName)
            .collect(Collectors.toSet());
        for (String rule : MixinConfig.MIXIN_TO_RULES.values()) {
            int slash = rule.indexOf('/');
            String baseRule = slash < 0 ? rule : rule.substring(0, slash);
            assertTrue(realRules.contains(baseRule), "Unknown rule '" + baseRule + "'");
            if (slash >= 0) {
                String arg = rule.substring(slash + 1);
                List<String> options = Settings.MANAGER.getRule(baseRule).getOptions();
                assertTrue(options.contains(arg), "Unknown argument '" + arg + "' for rule '" + baseRule + "', must be one of " + options);
            }
        }
        Set<String> realPackages = getMixinPackages();
        Set<String> packages = new HashSet<>(MixinConfig.MIXIN_TO_RULES.keySet());
        packages.addAll(MixinConfig.MIXINS_WITHOUT_RULES);
        packages.addAll(MixinConfig.CORE_MIXINS);
        for (String pkg : packages) {
            assertTrue(realPackages.contains(pkg), "Unknown package '" + pkg + "'");
        }
        for (String pkg : realPackages) {
            assertTrue(packages.contains(pkg), "Package '" + pkg + "' is missing a mapping");
        }
    }

    private static Set<String> getMixinPackages() {
        return useClassesFileSystem(path -> {
            try {
                Path mixinPath = path.resolve(Paths.get("quickcarpet", "mixin"));
                return Files.walk(path)
                    .filter(Files::isDirectory)
                    .filter(file -> file.startsWith(mixinPath))
                    .map(mixinPath::relativize)
                    .map(p -> p.getName(0).toString())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static <T> T useClassesFileSystem(Function<Path, T> fn) {
        URL root = QuickCarpet.class.getClassLoader().getResource("quickcarpet/QuickCarpet.class");
        if (root == null) throw new IllegalStateException("Could not find resource root");
        URI uri;
        try {
            uri = root.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return switch (uri.getScheme()) {
            case "file" -> fn.apply(Paths.get(uri).getParent().getParent());
            case "jar" -> {
                try (FileSystem fs = FileSystems.newFileSystem(uri, ImmutableMap.of())) {
                    yield fn.apply(fs.getPath("/"));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            default -> throw new IllegalStateException("Cannot get file system for scheme '${uri.scheme}'");
        };
    }
}
