package com.aegis.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public final class Mini {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private Mini() {}

    public static Component parse(String input, TagResolver... resolvers) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        return MM.deserialize(input, resolvers)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static List<Component> parseList(List<String> lines, TagResolver... resolvers) {
        return lines.stream()
                .map(line -> parse(line, resolvers))
                .collect(Collectors.toList());
    }

    public static String stripTags(String input) {
        if (input == null) return "";
        return PLAIN.serialize(MM.deserialize(input));
    }

    public static String serialize(Component component) {
        return MM.serialize(component);
    }
}
