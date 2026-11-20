package net.fastersprint;

import java.io.IOException;
import java.math.BigDecimal;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;

public final class ClientMenu {
    private static final int WIDTH = 460;
    private static final int HEIGHT = 300;
    private static final int TEXT = 0xFFF2FAF7;
    private static final int MUTED = 0xFFAAC3BE;
    private static final int ACCENT = 0xFFB8EAB6;
    private static boolean open;
    private static boolean speedEnabled;
    private static int category;
    private static Setting editing = Setting.NONE;
    private static String input = "";
    private static String error = "";

    private ClientMenu() {
    }

    public static boolean isOpen() {
        return open;
    }

    public static boolean speedEnabled() {
        return speedEnabled;
    }

    public static void toggle() {
        Minecraft game = Minecraft.getInstance();
        open = !open;
        editing = Setting.NONE;
        error = "";
        if (open) {
            game.options.keyAttack.setDown(false);
            game.options.keyUse.setDown(false);
            while (game.options.keyAttack.consumeClick()) {
                // Discard queued clicks when opening the menu.
            }
            while (game.options.keyUse.consumeClick()) {
                // Discard queued clicks when opening the menu.
            }
            game.mouseHandler.releaseMouse();
        } else {
            game.mouseHandler.grabMouse();
        }
    }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        if (open && (game.player == null || game.gui.screen() != null)) {
            open = false;
            editing = Setting.NONE;
        }
    }

    public static boolean handleKey(int action, KeyEvent event) {
        Minecraft game = Minecraft.getInstance();
        if (game.player == null || game.gui.screen() != null || game.gui.overlay() != null) {
            return false;
        }
        if (event.key() == InputConstants.KEY_P && !event.hasControlDown()
                && !event.hasAltDown() && !game.options.keyDebugModifier.isDown()) {
            if (action == InputConstants.PRESS) {
                toggle();
            }
            return true;
        }
        if (!open) {
            return false;
        }
        if (event.isEscape()) {
            if (action == InputConstants.PRESS) {
                if (editing != Setting.NONE) {
                    editing = Setting.NONE;
                    error = "";
                } else {
                    toggle();
                }
            }
            return true;
        }
        if (editing == Setting.NONE) {
            return false;
        }
        int keycode = event.keycode();
        if (keycode >= '0' && keycode <= '9') {
            if (action != InputConstants.RELEASE && input.length() < 10) {
                input += (char) keycode;
            }
            return true;
        }
        if (editing == Setting.SPEED && keycode == '.') {
            if (action != InputConstants.RELEASE && !input.contains(".") && input.length() < 10) {
                input += ".";
            }
            return true;
        }
        if (event.key() == InputConstants.KEY_BACKSPACE) {
            if (action != InputConstants.RELEASE && !input.isEmpty()) {
                input = input.substring(0, input.length() - 1);
            }
            return true;
        }
        if (event.isConfirmation()) {
            if (action == InputConstants.PRESS) {
                saveInput();
            }
            return true;
        }
        return false;
    }

    public static void click(double guiX, double guiY) {
        Layout layout = layout();
        int x = (int) ((guiX - layout.x) / layout.scale);
        int y = (int) ((guiY - layout.y) / layout.scale);
        if (inside(x, y, WIDTH - 32, 9, 22, 20)) {
            toggle();
            return;
        }
        for (int index = 0; index < 3; index++) {
            if (inside(x, y, 10, 48 + index * 30, 92, 26)) {
                category = index;
                editing = Setting.NONE;
                error = "";
                return;
            }
        }
        for (Tool tool : Tool.values()) {
            if (visible(tool) && inside(x, y, 116, toolTop(tool), WIDTH - 128, 48)) {
                toggleTool(tool);
                return;
            }
        }
        for (Setting setting : Setting.values()) {
            int top = settingTop(setting);
            if (top < 0) {
                continue;
            }
            if (inside(x, y, 303, top, 23, 24)) {
                adjust(setting, -1);
                return;
            }
            if (inside(x, y, 425, top, 23, 24)) {
                adjust(setting, 1);
                return;
            }
            if (inside(x, y, 333, top, 85, 24)) {
                editing = setting;
                input = "";
                error = "";
                return;
            }
        }
        if (editing != Setting.NONE) {
            saveInput();
        }
    }

    private static boolean visible(Tool tool) {
        return category == 0 || (category == 1 && tool != Tool.PLAYERS)
            || (category == 2 && tool == Tool.PLAYERS);
    }

    private static int toolTop(Tool tool) {
        if (category == 0) {
            return 51 + tool.ordinal() * 56;
        }
        return tool == Tool.CRAFTING ? 177 : 51;
    }

    private static int settingTop(Setting setting) {
        return switch (setting) {
            case SPEED -> category == 1 ? 115 : -1;
            case RANGE -> category == 2 ? 115 : -1;
            case NAMES -> category == 2 ? 177 : -1;
            case NONE -> -1;
        };
    }

    private static boolean toolEnabled(Tool tool) {
        return switch (tool) {
            case SPEED -> speedEnabled;
            case CRAFTING -> PlayerDisplaySettings.craftingMovementEnabled();
            case PLAYERS -> PlayerDisplaySettings.enabled();
        };
    }

    private static void toggleTool(Tool tool) {
        try {
            switch (tool) {
                case SPEED -> speedEnabled = !speedEnabled;
                case CRAFTING -> PlayerDisplaySettings.setCraftingMovementEnabled(
                    !PlayerDisplaySettings.craftingMovementEnabled());
                case PLAYERS -> PlayerDisplaySettings.setEnabled(!PlayerDisplaySettings.enabled());
            }
            error = "";
        } catch (IOException failure) {
            error = "Could not save settings.";
        }
    }

    private static void adjust(Setting setting, int direction) {
        editing = setting;
        if (setting == Setting.SPEED) {
            BigDecimal next = BigDecimal.valueOf(PlayerDisplaySettings.speedMultiplier())
                .add(BigDecimal.valueOf(direction * 0.1)).max(BigDecimal.ONE);
            input = next.stripTrailingZeros().toPlainString();
        } else {
            int current = setting == Setting.RANGE
                ? PlayerDisplaySettings.range() : PlayerDisplaySettings.nameLimit();
            long next = (long) current + direction * (setting == Setting.RANGE ? 25 : 1);
            input = Long.toString(Math.clamp(next, setting == Setting.RANGE ? 1L : 0L, Integer.MAX_VALUE));
        }
        saveInput();
    }

    private static void saveInput() {
        try {
            switch (editing) {
                case SPEED -> PlayerDisplaySettings.setSpeedMultiplier(Double.parseDouble(input));
                case RANGE -> PlayerDisplaySettings.setRange(Integer.parseInt(input));
                case NAMES -> PlayerDisplaySettings.setNameLimit(Integer.parseInt(input));
                case NONE -> {
                    return;
                }
            }
            editing = Setting.NONE;
            error = "";
        } catch (IllegalArgumentException invalid) {
            error = editing == Setting.SPEED ? "Enter a multiplier of 1 or more."
                : editing == Setting.RANGE ? "Enter a positive whole number."
                : "Enter a whole number of zero or more.";
        } catch (IOException failure) {
            error = "Could not save settings.";
        }
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!open) {
            return;
        }
        Minecraft game = Minecraft.getInstance();
        Layout layout = layout();
        double mouseX = (game.mouseHandler.getScaledXPos(game.getWindow()) - layout.x) / layout.scale;
        double mouseY = (game.mouseHandler.getScaledYPos(game.getWindow()) - layout.y) / layout.scale;
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().translate(layout.x, layout.y);
        graphics.pose().scale(layout.scale);
        graphics.fill(3, 5, WIDTH + 3, HEIGHT + 5, 0x55000000);
        graphics.fillGradient(0, 0, WIDTH, HEIGHT, 0xF0162A39, 0xEF234D40);
        graphics.outline(0, 0, WIDTH, HEIGHT, 0xFF668E87);
        graphics.fill(1, 1, WIDTH - 1, 35, 0xB30C1B28);
        graphics.fill(1, 35, 108, HEIGHT - 1, 0x5C10252D);
        graphics.fill(107, 35, 108, HEIGHT - 1, 0x5582ABA3);
        graphics.text(game.font, "FASTER MOVEMENT", 13, 14, TEXT);
        graphics.text(game.font, "P to close", WIDTH - 104, 14, MUTED);
        for (int step = 0; step < 7; step++) {
            graphics.fill(WIDTH - 26 + step, 12 + step, WIDTH - 25 + step, 13 + step, MUTED);
            graphics.fill(WIDTH - 20 - step, 12 + step, WIDTH - 19 - step, 13 + step, MUTED);
        }
        String[] categories = {"All tools", "Movement", "Players"};
        for (int index = 0; index < categories.length; index++) {
            int top = 48 + index * 30;
            boolean hovered = inside(mouseX, mouseY, 10, top, 92, 26);
            if (category == index || hovered) {
                graphics.fill(10, top, 102, top + 26,
                    category == index ? 0x3869BBA8 : 0x204A8D83);
                graphics.outline(10, top, 92, 26, category == index ? 0xFF82B8AA : 0x506B9890);
            }
            drawIcon(graphics, index, 16, top + 7, category == index ? ACCENT : MUTED);
            graphics.text(game.font, categories[index], 34, top + 9, TEXT);
        }
        graphics.text(game.font, "WASD to move", 13, HEIGHT - 31, MUTED);
        graphics.text(game.font, "Esc to close", 13, HEIGHT - 18, MUTED);
        for (Tool tool : Tool.values()) {
            if (visible(tool)) {
                drawModule(graphics, tool, toolTop(tool), mouseX, mouseY);
            }
        }
        for (Setting setting : Setting.values()) {
            int top = settingTop(setting);
            if (top >= 0) {
                drawSetting(graphics, setting, top, mouseX, mouseY);
            }
        }
        if (category == 0) {
            graphics.text(game.font, "Movement and Players contain settings.", 127, 239, MUTED);
        }
        if (!error.isEmpty()) {
            graphics.text(game.font, error, 127, HEIGHT - 19, 0xFFFFB6A3);
        }
        graphics.pose().popMatrix();
    }

    private static void drawModule(GuiGraphicsExtractor graphics, Tool tool,
            int top, double mouseX, double mouseY) {
        Minecraft game = Minecraft.getInstance();
        boolean enabled = toolEnabled(tool);
        boolean hovered = inside(mouseX, mouseY, 116, top, WIDTH - 128, 48);
        graphics.fill(116, top, WIDTH - 12, top + 48, hovered ? 0x80345C56 : 0x70243F43);
        graphics.outline(116, top, WIDTH - 128, 48, hovered ? 0xFF95C5B7 : 0x55658C87);
        graphics.fill(125, top + 10, 153, top + 38, 0x80385755);
        drawIcon(graphics, tool.icon, 133, top + 18, enabled ? ACCENT : MUTED);
        graphics.text(game.font, tool.title, 163, top + 10, TEXT);
        String description = tool == Tool.SPEED
            ? "Walk and sprint " + multiplierText() + "x" : tool.description;
        graphics.text(game.font, description, 163, top + 28, MUTED);
        graphics.text(game.font, enabled ? "ON" : "OFF", WIDTH - 70, top + 11,
            enabled ? ACCENT : MUTED);
        graphics.fill(WIDTH - 44, top + 10, WIDTH - 22, top + 20,
            enabled ? 0xAA8CC9A0 : 0xAA526B6E);
        int knob = enabled ? WIDTH - 31 : WIDTH - 42;
        graphics.fill(knob, top + 12, knob + 7, top + 18, TEXT);
    }

    private static void drawSetting(GuiGraphicsExtractor graphics, Setting setting,
            int top, double mouseX, double mouseY) {
        Minecraft game = Minecraft.getInstance();
        String label = switch (setting) {
            case SPEED -> "Speed multiplier";
            case RANGE -> "ESP range (blocks)";
            case NAMES -> "Name limit";
            case NONE -> "";
        };
        String value = switch (setting) {
            case SPEED -> multiplierText() + "x";
            case RANGE -> Integer.toString(PlayerDisplaySettings.range());
            case NAMES -> Integer.toString(PlayerDisplaySettings.nameLimit());
            case NONE -> "";
        };
        String hint = setting == Setting.NAMES ? "Hide names above this player count."
            : "Click the number to type a value.";
        graphics.text(game.font, label, 127, top + 8, TEXT);
        drawAdjustment(graphics, 303, top, false, mouseX, mouseY);
        drawAdjustment(graphics, 425, top, true, mouseX, mouseY);
        graphics.fill(333, top, 418, top + 24, 0x99203435);
        graphics.outline(333, top, 85, 24, editing == setting ? ACCENT : 0x705B8780);
        String displayed = editing == setting ? input + "_" : value;
        displayed = game.font.plainSubstrByWidth(displayed, 77);
        graphics.centeredText(game.font, displayed, 375, top + 8, TEXT);
        graphics.text(game.font, editing == setting ? "Type a value, then Enter." : hint,
            127, top + 31, MUTED);
    }

    private static String multiplierText() {
        return BigDecimal.valueOf(PlayerDisplaySettings.speedMultiplier())
            .stripTrailingZeros().toPlainString();
    }

    private static void drawAdjustment(GuiGraphicsExtractor graphics,
            int left, int top, boolean plus, double mouseX, double mouseY) {
        graphics.fill(left, top, left + 23, top + 24,
            inside(mouseX, mouseY, left, top, 23, 24) ? 0xAA4A7667 : 0x88344F4B);
        graphics.fill(left + 7, top + 11, left + 16, top + 12, TEXT);
        if (plus) {
            graphics.fill(left + 11, top + 7, left + 12, top + 16, TEXT);
        }
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, int icon, int x, int y, int color) {
        if (icon == 0) {
            graphics.fill(x, y, x + 4, y + 4, color);
            graphics.fill(x + 6, y, x + 10, y + 4, color);
            graphics.fill(x, y + 6, x + 4, y + 10, color);
            graphics.fill(x + 6, y + 6, x + 10, y + 10, color);
        } else if (icon == 1) {
            graphics.fill(x + 4, y, x + 8, y + 6, color);
            graphics.fill(x + 3, y + 5, x + 10, y + 9, color);
            graphics.fill(x, y + 8, x + 11, y + 10, color);
            graphics.fill(x, y + 3, x + 2, y + 4, color);
            graphics.fill(x - 1, y + 5, x + 2, y + 6, color);
        } else if (icon == 2) {
            graphics.fill(x + 3, y, x + 7, y + 4, color);
            graphics.fill(x + 1, y + 5, x + 9, y + 10, color);
            graphics.fill(x - 2, y + 2, x - 1, y + 8, color);
            graphics.fill(x + 11, y + 2, x + 12, y + 8, color);
            graphics.fill(x - 2, y + 2, x, y + 3, color);
            graphics.fill(x + 10, y + 2, x + 12, y + 3, color);
        } else {
            graphics.fill(x - 1, y + 1, x + 11, y + 4, color);
            graphics.fill(x, y + 4, x + 2, y + 11, color);
            graphics.fill(x + 8, y + 4, x + 10, y + 11, color);
            graphics.fill(x + 3, y + 5, x + 7, y + 6, color);
            graphics.fill(x + 4, y + 4, x + 5, y + 8, color);
        }
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && y >= top && x < left + width && y < top + height;
    }

    private static Layout layout() {
        Minecraft game = Minecraft.getInstance();
        int width = game.getWindow().getGuiScaledWidth();
        int height = game.getWindow().getGuiScaledHeight();
        float scale = Math.min(1.0F, Math.min((width - 20.0F) / WIDTH, (height - 20.0F) / HEIGHT));
        return new Layout((width - WIDTH * scale) / 2, (height - HEIGHT * scale) / 2, scale);
    }

    private enum Tool {
        SPEED("Speed", "", 1),
        CRAFTING("CraftingMovement", "Move with crafting table open", 3),
        PLAYERS("Player ESP", "Boxes, tracers + distances", 2);

        final String title;
        final String description;
        final int icon;

        Tool(String title, String description, int icon) {
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
    }

    private enum Setting {
        NONE, SPEED, RANGE, NAMES
    }

    private record Layout(float x, float y, float scale) {
    }
}
