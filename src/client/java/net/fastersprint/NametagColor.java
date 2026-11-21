package net.fastersprint;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/** Resolve the player's name text, rather than a differently colored rank/prefix. */
public final class NametagColor {
    private NametagColor() { }

    public static int resolve(Component displayName, String playerName, int teamRgb) {
        Optional<Integer> nameColor = displayName.visit((style, text) -> {
            if (!playerName.isEmpty() && text.contains(playerName) && style.getColor() != null) {
                return Optional.of(0xFF000000 | style.getColor().getValue());
            }
            return Optional.empty();
        }, Style.EMPTY);
        if (nameColor.isPresent()) return nameColor.get();
        var rootColor = displayName.getStyle().getColor();
        return 0xFF000000 | (rootColor == null ? teamRgb : rootColor.getValue());
    }
}
