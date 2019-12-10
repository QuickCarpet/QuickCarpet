package quickcarpet.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import quickcarpet.mixin.accessor.ServerPlayerEntityAccessor;
import quickcarpet.module.QuickCarpetModule;

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

public class Translations {
    public static final String DEFAULT_LOCALE = "en_us";
    private static final Gson GSON = new Gson();
    private static final Map<String, String> DEFAULT = new HashMap<>();
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();

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

    public static Text translate(Text text, ServerPlayerEntity player) {
        return translate(text, ((ServerPlayerEntityAccessor) player).getClientLanguage());
    }

    public static Text translate(Text text, String locale) {
        Text translated = translatedCopy(text, locale);
        Style style = text.getStyle().deepCopy();
        HoverEvent hover = style.getHoverEvent();
        if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
            style.setHoverEvent(new HoverEvent(hover.getAction(), translate(hover.getValue(), locale)));
        }
        translated.setStyle(style);
        for (Text sibling : text.getSiblings()) {
            translated.append(translate(sibling, locale));
        }
        return translated;
    }

    private static Text translatedCopy(Text text, String locale) {
        if (!(text instanceof TranslatableText)) return text.copy();
        TranslatableText translatable = (TranslatableText) text;
        String key = translatable.getKey();
        if (!DEFAULT.containsKey(key)) return text.copy();
        locale = locale.toLowerCase(Locale.ROOT);
        if (!TRANSLATIONS.containsKey(locale)) locale = "en_us";
        String translated = TRANSLATIONS.get(locale).get(key);
        if (translated == null) translated = DEFAULT.getOrDefault(key, key);
        Matcher matcher = TranslatableText.ARG_FORMAT.matcher(translated);
        List<Text> texts = new LinkedList<>();
        int previousEnd = 0;
        int argIndex = 0;
        while (matcher.find(previousEnd)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > previousEnd) {
                Text between = new LiteralText(translated.substring(previousEnd, start));
                between.getStyle().setParent(translatable.getStyle());
                texts.add(between);
            }
            String completeMatch = translated.substring(start, end);
            if ("%%".equals(completeMatch)) {
                Text percent = new LiteralText("%");
                percent.getStyle().setParent(translatable.getStyle());
                texts.add(percent);
            } else {
                String format = matcher.group(2);
                if (!"s".equals(format)) throw new TranslationException(translatable, "Unsupported format: '" + format + "'");
                String alternateIndex = matcher.group(1);
                int index = alternateIndex != null ? Integer.parseInt(alternateIndex) - 1 : argIndex++;
                if (index < translatable.getArgs().length) {
                    texts.add(getArg(translatable, index, locale));
                }
            }
            previousEnd = end;
        }
        if (previousEnd < translated.length()) {
            Text end = new LiteralText(translated.substring(previousEnd));
            end.getStyle().setParent(translatable.getStyle());
            texts.add(end);
        }
        Text base = texts.remove(0);
        texts.forEach(base::append);
        return base;
    }

    private static Text getArg(TranslatableText text, int index, String locale) {
        Object[] args = text.getArgs();
        if (index >= args.length) {
            throw new TranslationException(text, index);
        } else {
            Object arg = args[index];
            if (arg instanceof Text) return translate((Text) arg, locale);
            Text argFormatted = new LiteralText(String.valueOf(arg));
            argFormatted.getStyle().setParent(text.getStyle());
            return argFormatted;
        }
    }

    public static boolean hasTranslation(String key) {
        return DEFAULT.containsKey(key);
    }
}
