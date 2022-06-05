package quickcarpet.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.mixin.accessor.RegistryKeyAccessor;
import quickcarpet.utils.extensions.PlayerWithLanguage;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static quickcarpet.utils.Messenger.s;
import static quickcarpet.utils.Messenger.t;

public class Translations {
    public static final String DEFAULT_LOCALE = "en_us";
    private static final Gson GSON = new Gson();
    private static final Map<String, String> DEFAULT = new HashMap<>();
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public static void init() throws IOException {
        TRANSLATIONS.clear();
        loadModuleTranslations("quickcarpet");
    }

    private static void loadModuleTranslations(String moduleName) throws IOException {
        URL enUrl = Translations.class.getResource("/assets/" + moduleName + "/lang/" + DEFAULT_LOCALE + ".json");
        try {
            Path directory = Paths.get(enUrl.toURI()).getParent();
            Files.list(directory).forEach(path -> {
                String locale = path.getFileName().toString();
                int extIndex = locale.lastIndexOf(".json");
                if (extIndex < 0) return;
                locale = locale.substring(0, extIndex).toLowerCase(Locale.ROOT);
                try {
                    load(locale, Files.newBufferedReader(path));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (URISyntaxException | UncheckedIOException e) {
            e.printStackTrace();
        }
    }

    public static void loadModuleTranslations(QuickCarpetModule module) throws IOException {
        loadModuleTranslations(module.getId());
    }

    private static void load(String locale, Reader reader) {
        Map<String, String> translations = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
        if (locale.equalsIgnoreCase(DEFAULT_LOCALE)) DEFAULT.putAll(translations);
        TRANSLATIONS.put(locale, translations);
    }

    public static MutableText translate(Text text, ServerPlayerEntity player) {
        return translate(text, ((PlayerWithLanguage) player).quickcarpet$getLanguage());
    }

    public static MutableText translate(Text text, String locale) {
        MutableText translated = translatedCopy(text, locale);
        Style style = text.getStyle();
        HoverEvent hover = style.getHoverEvent();
        if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate(hover.getValue(HoverEvent.Action.SHOW_TEXT), locale)));
        }
        translated.setStyle(style);
        for (Text sibling : text.getSiblings()) {
            translated.append(translate(sibling, locale));
        }
        return translated;
    }

    private static MutableText translatedCopy(Text text, String locale) {
        if (!(text.getContent() instanceof TranslatableTextContent translatable)) return text.copyContentOnly();
        String key = translatable.getKey();
        if (!DEFAULT.containsKey(key)) return text.copyContentOnly();
        locale = locale.toLowerCase(Locale.ROOT);
        if (!TRANSLATIONS.containsKey(locale)) locale = "en_us";
        String translated = TRANSLATIONS.get(locale).get(key);
        if (translated == null) translated = DEFAULT.getOrDefault(key, key);
        Matcher matcher = ARG_FORMAT.matcher(translated);
        List<MutableText> texts = new ArrayList<>();
        int previousEnd = 0;
        int argIndex = 0;
        while (matcher.find(previousEnd)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > previousEnd) {
                texts.add(s(translated.substring(previousEnd, start)));
            }
            String completeMatch = translated.substring(start, end);
            if ("%%".equals(completeMatch)) {
                texts.add(s("%"));
            } else {
                String format = matcher.group(2);
                if (!"s".equals(format)) throw new TranslationException(translatable, "Unsupported format: '" + format + "'");
                String alternateIndex = matcher.group(1);
                int index = alternateIndex != null ? Integer.parseInt(alternateIndex) - 1 : argIndex++;
                if (index < translatable.getArgs().length) {
                    texts.add(getArg(translatable, index, locale, text.getStyle()));
                }
            }
            previousEnd = end;
        }
        if (previousEnd < translated.length()) {
            MutableText end = s(translated.substring(previousEnd));
            end.setStyle(end.getStyle().withParent(text.getStyle()));
            texts.add(end);
        }
        MutableText base = texts.remove(0);
        texts.forEach(base::append);
        return base;
    }

    private static MutableText getArg(TranslatableTextContent content, int index, String locale, Style style) {
        Object[] args = content.getArgs();
        if (index >= args.length) {
            throw new TranslationException(content, index);
        } else {
            Object arg = args[index];
            if (arg instanceof MutableText) return translate((MutableText) arg, locale);
            MutableText argFormatted = s(String.valueOf(arg));
            argFormatted.setStyle(argFormatted.getStyle().withParent(style));
            return argFormatted;
        }
    }

    public static String get(String key) {
        return DEFAULT.get(key);
    }

    public static boolean hasTranslation(String key) {
        return DEFAULT.containsKey(key);
    }

    public static MutableText translate(RegistryKey<?> key) {
        Identifier reg = ((RegistryKeyAccessor) key).getRegistry();
        Identifier value = key.getValue();
        String translationKey = ("minecraft".equals(reg.getNamespace())
                ? reg.getPath()
                : reg.getNamespace() + "." + reg.getPath())
                + "." + value.getNamespace()
                + "." + value.getPath();
        return t(translationKey);
    }
}
