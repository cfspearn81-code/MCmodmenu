package net.fastersprint;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;

public final class ClientMenu {
    private static final int WIDTH = 460;
    private static final int HEIGHT = 410;
    private static final int TEXT = 0xFFF3F1FF;
    private static final int MUTED = 0xFF9996B5;
    private static final int ACCENT = 0xFFC6B1FF;
    private static final float[] TOGGLES = new float[7];
    private static long lastFrame;
    private static float smoothing;
    private static boolean bindingBridgeKey;
    private static boolean bindingFreeCamKey;
    private static boolean searchFocused;
    private static boolean searchSelected;
    private static String query = "";
    private static int searchOffset;
    private static final int SEARCH_ROWS = 5;
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
        bindingBridgeKey = false;
        bindingFreeCamKey = false;
        editing = Setting.NONE;
        error = "";
        if (open) {
            category = 0; query = ""; searchOffset = 0; focusSearch();
            game.options.keyAttack.setDown(false);
            game.options.keyUse.setDown(false);
            while (game.options.keyAttack.consumeClick()) {
                // Discard queued clicks when opening the menu.
            }
            while (game.options.keyUse.consumeClick()) {
                // Discard queued clicks when opening the menu.
            }
            game.mouseHandler.releaseMouse();
            MenuCursor.begin();
            lastFrame = 0;
        } else {
            MenuCursor.end();
            game.mouseHandler.grabMouse();
        }
    }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        if (open) {
            if (game.isWindowActive() && game.mouseHandler.isMouseGrabbed()) game.mouseHandler.releaseMouse();
            MenuCursor.tick(game.isWindowActive());
        }
        if (open && (game.player == null || game.gui.screen() != null)) {
            MenuCursor.end();
            open = false;
            bindingBridgeKey = false;
            bindingFreeCamKey = false;
            searchFocused = false;
            editing = Setting.NONE;
        }
    }

    public static boolean handleKey(int action, KeyEvent event) {
        Minecraft game = Minecraft.getInstance();
        if (game.player == null || game.gui.screen() != null || game.gui.overlay() != null) {
            return false;
        }
        if (open && (bindingBridgeKey || bindingFreeCamKey)) {
            if (action == InputConstants.PRESS) {
                if (event.isEscape()) {
                    bindingBridgeKey = false; bindingFreeCamKey = false;
                } else {
                    try {
                        if (bindingFreeCamKey) FreeCamSettings.setKey(event.key());
                        else PlayerDisplaySettings.setAutoBridgeKey(event.key());
                        bindingBridgeKey = false; bindingFreeCamKey = false;
                        error = "";
                    } catch (IllegalArgumentException invalid) {
                        error = "Choose a key other than P or Esc.";
                    } catch (IOException failure) {
                        error = "Could not save settings.";
                    }
                }
            }
            return true;
        }
        if (open && event.hasControlDown() && event.key() == InputConstants.KEY_F) {
            if (action == InputConstants.PRESS) { category = 0; editing = Setting.NONE; focusSearch(); }
            return true;
        }
        if (open && category == 0 && searchFocused && !event.isEscape()) return searchKey(action, event);
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
        if (category == 0 && inside(x, y, 116, 51, WIDTH - 128, 32)) {
            bindingBridgeKey = false; bindingFreeCamKey = false; focusSearch(); return;
        }
        searchFocused = false;
        for (int index = 0; index < 5; index++) {
            if (inside(x, y, 10, 48 + index * 30, 92, 26)) {
                bindingBridgeKey = false; bindingFreeCamKey = false;
                category = index;
                if (category == 0) focusSearch();
                editing = Setting.NONE;
                error = "";
                return;
            }
        }
        if (visible(Tool.BRIDGE) && inside(x, y, 308, toolTop(Tool.BRIDGE) + 24, 130, 20)) {
            bindingBridgeKey = true; bindingFreeCamKey = false;
            editing = Setting.NONE;
            error = "";
            return;
        }
        if (visible(Tool.FREECAM) && inside(x, y, 308, toolTop(Tool.FREECAM) + 24, 130, 20)) {
            bindingFreeCamKey = true; bindingBridgeKey = false; editing = Setting.NONE; error = ""; return;
        }
        bindingBridgeKey = false; bindingFreeCamKey = false;
        if (category == 4) {
            for (OreType ore : OreType.values()) {
                int left = 116 + (ore.ordinal() % 3) * 112;
                int top = 198 + (ore.ordinal() / 3) * 34;
                if (inside(x, y, left, top, 108, 30)) {
                    try {
                        XraySettings.setOreEnabled(ore, !XraySettings.oreEnabled(ore));
                        error = "";
                    } catch (IOException failure) { error = "Could not save settings."; }
                    return;
                }
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

    public static boolean capturesMovement() {
        return open && (searchFocused || bindingBridgeKey || bindingFreeCamKey);
    }

    private static void focusSearch() {
        searchFocused = true; searchSelected = false;
        var game = Minecraft.getInstance();
        for (var key : new net.minecraft.client.KeyMapping[]{game.options.keyUp, game.options.keyDown,
                game.options.keyLeft, game.options.keyRight, game.options.keyJump, game.options.keyShift, game.options.keySprint}) key.setDown(false);
    }

    private static boolean searchKey(int action, KeyEvent event) {
        if (action == InputConstants.RELEASE) return true;
        if (event.isConfirmation() || event.key() == InputConstants.KEY_TAB) { searchFocused = false; return true; }
        if (event.hasControlDown()) {
            if (event.key() == InputConstants.KEY_A) searchSelected = true;
            else if (event.key() == InputConstants.KEY_V) {
                query = ModuleSearch.append(searchSelected ? "" : query, Minecraft.getInstance().keyboardHandler.getClipboard());
                searchSelected = false; searchOffset = 0;
            }
            return true;
        }
        if (event.key() == InputConstants.KEY_BACKSPACE) {
            query = searchSelected ? "" : ModuleSearch.backspace(query); searchSelected = false; searchOffset = 0; return true;
        }
        int code = event.keycode();
        if (!event.hasAltDown() && Character.isValidCodePoint(code) && !Character.isISOControl(code)) {
            query = ModuleSearch.append(searchSelected ? "" : query, new String(Character.toChars(code)));
            searchSelected = false; searchOffset = 0;
        }
        return true;
    }

    private static List<Tool> searchResults() {
        List<Tool> result = new ArrayList<>();
        for (Tool tool : Tool.values()) if (ModuleSearch.matches(tool.title, tool.description, query)) result.add(tool);
        return result;
    }

    public static void scroll(double amount) {
        if (category == 0 && amount != 0) searchOffset = Math.clamp(searchOffset + (amount < 0 ? 1 : -1), 0, Math.max(0, searchResults().size() - SEARCH_ROWS));
    }

    private static void drawSearch(GuiGraphicsExtractor graphics) {
        var game = Minecraft.getInstance();
        graphics.nextStratum();
        MenuPaint.rounded(graphics, 116, 51, WIDTH - 128, 32, 8, searchFocused ? 0xFFAE8DE2 : 0xFF48405D);
        MenuPaint.rounded(graphics, 117, 52, WIDTH - 130, 30, 7, 0xFF211E36);
        if (searchSelected && !query.isEmpty()) MenuPaint.rounded(graphics, 126, 59, Math.min(300,game.font.width(query)+3), 15, 3, 0xFF51406D);
        String displayed = query.isEmpty() && !searchFocused ? "Search modules..." : query + (searchFocused ? "_" : "");
        if (query.isEmpty() && searchFocused) displayed = "Search modules... _";
        graphics.text(game.font, game.font.plainSubstrByWidth(displayed, 304), 127, 63, query.isEmpty() ? MUTED : TEXT);
        List<Tool> result = searchResults();
        if (result.isEmpty()) {
            graphics.centeredText(game.font, "No modules found", 282, 166, TEXT);
            graphics.centeredText(game.font, "Try another name or press Ctrl+A to replace.", 282, 185, MUTED);
        }
        if (error.isEmpty()) graphics.text(game.font, result.size() + (result.size() == 1 ? " module" : " modules") + (result.size() > SEARCH_ROWS ? "  /  Scroll for more" : ""), 127, HEIGHT - 19, MUTED);
        if (result.size() > SEARCH_ROWS) {
            MenuPaint.rounded(graphics, 452, 99, 2, 272, 1, 0xFF353047);
            int thumb = 272 * SEARCH_ROWS / result.size();
            int top = 99 + (272 - thumb) * searchOffset / (result.size() - SEARCH_ROWS);
            MenuPaint.rounded(graphics, 452, top, 2, thumb, 1, ACCENT);
        }
    }

    private static boolean visible(Tool tool) {
        if (category == 0) {
            int index = searchResults().indexOf(tool);
            return index >= searchOffset && index < searchOffset + SEARCH_ROWS;
        }
        return (category == 1 && (tool == Tool.SPEED || tool == Tool.GUIMOVE || tool == Tool.BRIDGE || tool == Tool.FREECAM))
            || (category == 2 && tool == Tool.PLAYERS) || (category == 3 && tool == Tool.HIT)
            || (category == 4 && tool == Tool.XRAY);
    }

    private static int toolTop(Tool tool) {
        if (category == 0) {
            return 99 + (searchResults().indexOf(tool) - searchOffset) * 56;
        }
        return tool == Tool.BRIDGE ? 233 : tool == Tool.GUIMOVE ? 177 : tool == Tool.FREECAM ? 289 : 51;
    }

    private static int settingTop(Setting setting) {
        return switch (setting) {
            case SPEED -> category == 1 ? 115 : -1;
            case RANGE -> category == 2 ? 115 : -1;
            case NAMES -> category == 2 ? 177 : -1;
            case XRAY_RANGE -> category == 4 ? 115 : -1;
            case NONE -> -1;
        };
    }

    private static boolean toolEnabled(Tool tool) {
        return switch (tool) {
            case FREECAM -> FreeCam.active();
            case XRAY -> XraySettings.enabled();
            case SPEED -> speedEnabled;
            case GUIMOVE -> PlayerDisplaySettings.guiMoveEnabled();
            case PLAYERS -> PlayerDisplaySettings.enabled();
            case BRIDGE -> PlayerDisplaySettings.autoBridgeEnabled();
            case HIT -> PlayerDisplaySettings.autoHitEnabled();
        };
    }

    private static void toggleTool(Tool tool) {
        try {
            switch (tool) {
                case FREECAM -> FreeCam.toggle();
                case XRAY -> XraySettings.setEnabled(!XraySettings.enabled());
                case HIT -> PlayerDisplaySettings.setAutoHitEnabled(!PlayerDisplaySettings.autoHitEnabled());
                case BRIDGE -> PlayerDisplaySettings.setAutoBridgeEnabled(!PlayerDisplaySettings.autoBridgeEnabled());
                case SPEED -> speedEnabled = !speedEnabled;
                case GUIMOVE -> PlayerDisplaySettings.setGuiMoveEnabled(
                    !PlayerDisplaySettings.guiMoveEnabled());
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
            int current = setting == Setting.RANGE ? PlayerDisplaySettings.range()
                : setting == Setting.XRAY_RANGE ? XraySettings.range() : PlayerDisplaySettings.nameLimit();
            long next = (long) current + direction * (setting == Setting.NAMES ? 1 : 25);
            long minimum = setting == Setting.NAMES ? 0 : 1;
            long maximum = setting == Setting.XRAY_RANGE ? XraySettings.MAX_RANGE : Integer.MAX_VALUE;
            input = Long.toString(Math.clamp(next, minimum, maximum));
        }
        saveInput();
    }

    private static void saveInput() {
        try {
            switch (editing) {
                case SPEED -> PlayerDisplaySettings.setSpeedMultiplier(Double.parseDouble(input));
                case XRAY_RANGE -> XraySettings.setRange(Integer.parseInt(input));
                case RANGE -> PlayerDisplaySettings.setRange(Integer.parseInt(input));
                case NAMES -> PlayerDisplaySettings.setNameLimit(Integer.parseInt(input));
                case NONE -> {
                    return;
                }
            }
            editing = Setting.NONE;
            error = "";
        } catch (IllegalArgumentException invalid) {
            error = editing == Setting.XRAY_RANGE ? "Enter a whole number from 1 to 5000."
                : editing == Setting.SPEED ? "Enter a multiplier of 1 or more."
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
        long now = System.nanoTime();
        float elapsed = lastFrame == 0 ? 0.016F : Math.min(0.1F, (now - lastFrame) / 1_000_000_000F);
        lastFrame = now;
        smoothing = 1F - (float) Math.exp(-elapsed / 0.075F);
        graphics.nextStratum();
        graphics.blurBeforeThisStratum();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0x4C080B17);
        graphics.pose().pushMatrix();
        graphics.pose().translate(layout.x, layout.y);
        graphics.pose().scale(layout.scale);
        for (int spread = 10; spread >= 2; spread -= 2) {
            MenuPaint.rounded(graphics, -spread, -spread + 5, WIDTH + spread * 2,
                HEIGHT + spread * 2, 14 + spread, 0x0C02020C);
        }
        MenuPaint.rounded(graphics, 0, 0, WIDTH, HEIGHT, 12, 0xFF51496F);
        MenuPaint.rounded(graphics, 1, 1, WIDTH - 2, HEIGHT - 2, 11, 0xFC1C1B32);
        MenuPaint.rounded(graphics, 2, 2, 106, HEIGHT - 4, 10, 0xFF19192F);
        graphics.fill(105, 35, 106, HEIGHT - 15, 0x283E3B5C);
        graphics.fill(119, 39, WIDTH - 15, 40, 0x484A456B);
        graphics.text(game.font, "F A S T E R", 15, 12, TEXT);
        graphics.text(game.font, "CONTROL PANEL", 15, 25, 0xFF6F6B91);
        String[] headings = {"Search", "Movement", "Players", "Combat", "Xray"};
        String[] subtitles = {"Find a module and make it yours.", "Fine-tune your movement.",
            "Keep everyone in sight.", "Ready when you are.", "Find the ores you need."};
        graphics.text(game.font, headings[category], 127, 11, TEXT);
        graphics.text(game.font, subtitles[category], 127, 25, MUTED);
        int enabledCount = 0;
        for (Tool tool : Tool.values()) if (toolEnabled(tool)) enabledCount++;
        MenuPaint.rounded(graphics, WIDTH - 118, 10, 75, 20, 10, 0xFF2C2948);
        MenuPaint.circle(graphics, WIDTH - 108, 20, 2, 0xFF79D8C5);
        graphics.text(game.font, enabledCount + " active", WIDTH - 101, 16, 0xFFD2C7EB);
        boolean closeHovered = inside(mouseX, mouseY, WIDTH - 32, 9, 22, 20);
        MenuPaint.rounded(graphics, WIDTH - 32, 9, 22, 22, 7,
            closeHovered ? 0xFF4C375B : 0xFF29263F);
        for (int step = 0; step < 7; step++) {
            graphics.fill(WIDTH - 25 + step, 16 + step, WIDTH - 24 + step, 17 + step, MUTED);
            graphics.fill(WIDTH - 19 - step, 16 + step, WIDTH - 18 - step, 17 + step, MUTED);
        }
        String[] categories = {"Search", "Movement", "Players", "Combat", "Xray"};
        for (int index = 0; index < categories.length; index++) {
            int top = 48 + index * 30;
            boolean selected = category == index;
            boolean hovered = inside(mouseX, mouseY, 10, top, 92, 26);
            if (selected || hovered) {
                MenuPaint.rounded(graphics, 10, top, 92, 26, 7, selected ? 0xFF302949 : 0xFF222139);
                if (selected) MenuPaint.rounded(graphics, 11, top + 7, 2, 12, 1, 0xFFC6A3FF);
            }
            drawIcon(graphics, index, 19, top + 8, selected ? ACCENT : 0xFF74728E);
            graphics.text(game.font, categories[index], 36, top + 9, selected ? TEXT : MUTED);
        }
        MenuPaint.rounded(graphics, 11, HEIGHT - 107, 85, 55, 8, 0xFF242139);
        MenuPaint.circle(graphics, 23, HEIGHT - 93, 3, 0xFF8F7BD5);
        graphics.text(game.font, "YOUR SETUP", 33, HEIGHT - 96, 0xFFC5B5E7);
        graphics.text(game.font, Tool.values().length + " modules", 20, HEIGHT - 79, TEXT);
        graphics.text(game.font, "Quick access", 20, HEIGHT - 65, 0xFF77738E);
        graphics.text(game.font, searchFocused ? "Type to search" : "WASD to move", 15, HEIGHT - 31, 0xFF77738E);
        graphics.text(game.font, searchFocused ? "Esc to close" : "P / Esc to close", 15, HEIGHT - 18, 0xFF77738E);
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

        if (category == 0) drawSearch(graphics);
        if (category == 4) drawOreFilters(graphics, mouseX, mouseY);
        if (!error.isEmpty()) {
            graphics.text(game.font, error, 127, HEIGHT - 19, 0xFFFFB6A3);
        }
        graphics.pose().popMatrix();
        MenuCursor.render(graphics);
    }

    private static void drawModule(GuiGraphicsExtractor graphics, Tool tool,
            int top, double mouseX, double mouseY) {
        Minecraft game = Minecraft.getInstance();
        graphics.nextStratum();
        boolean enabled = toolEnabled(tool);
        boolean hovered = inside(mouseX, mouseY, 116, top, WIDTH - 128, 48);
        float value = TOGGLES[tool.ordinal()];
        value += ((enabled ? 1F : 0F) - value) * smoothing;
        TOGGLES[tool.ordinal()] = value;
        MenuPaint.rounded(graphics, 117, top + 2, WIDTH - 128, 48, 9, 0x48060512);
        MenuPaint.rounded(graphics, 116, top, WIDTH - 128, 48, 9,
            hovered ? 0xFF625578 : enabled ? 0xFF4D4366 : 0xFF37334F);
        MenuPaint.rounded(graphics, 117, top + 1, WIDTH - 130, 46, 8,
            hovered ? 0xFF2E2949 : 0xFF25223D);
        MenuPaint.rounded(graphics, 125, top + 10, 28, 28, 7,
            MenuPaint.mix(0xFF302B46, 0xFF49355F, value));
        drawIcon(graphics, tool.icon, 133, top + 18, enabled ? ACCENT : 0xFF89839E);
        graphics.text(game.font, tool.title, 163, top + 10, TEXT);
        String description = tool == Tool.SPEED
            ? "Walk and sprint " + multiplierText() + "x"
            : tool == Tool.BRIDGE ? AutoBridge.status() : tool.description;
        graphics.text(game.font, description, 163, top + 28, MUTED);
        if (tool == Tool.BRIDGE || tool == Tool.FREECAM) {
            boolean binding = tool == Tool.BRIDGE ? bindingBridgeKey : bindingFreeCamKey;
            int boundKey = tool == Tool.BRIDGE ? PlayerDisplaySettings.autoBridgeKey() : FreeCamSettings.key();
            MenuPaint.rounded(graphics, 308, top + 24, 130, 20, 5, binding ? 0xFF4C3A65 : 0xFF322B49);
            String key = InputConstants.Type.KEYBOARD.getOrCreate(boundKey).getDisplayName().getString();
            if (key == null) key = "Key " + boundKey;
            String label = binding ? "Press a key..." : (tool == Tool.BRIDGE ? "Hold: " : "Toggle: ") + key.toUpperCase(java.util.Locale.ROOT);
            graphics.text(game.font, game.font.plainSubstrByWidth(label, 122), 312, top + 30, ACCENT);
        }
        graphics.text(game.font, enabled ? "ON" : "OFF", WIDTH - 80, top + 12,
            enabled ? ACCENT : 0xFF77718F);
        MenuPaint.rounded(graphics, WIDTH - 46, top + 10, 28, 14, 7,
            MenuPaint.mix(0xFF49415C, 0xFF9C7CD6, value));
        MenuPaint.circle(graphics, WIDTH - 39 + value * 14, top + 17, 4.5F, 0xFFF3EDFF);

    }

    private static void drawSetting(GuiGraphicsExtractor graphics, Setting setting,
            int top, double mouseX, double mouseY) {
        Minecraft game = Minecraft.getInstance();
        graphics.nextStratum();
        MenuPaint.rounded(graphics, 116, top - 4, WIDTH - 128, 56, 8, 0xFF373049);
        MenuPaint.rounded(graphics, 117, top - 3, WIDTH - 130, 54, 7, 0xFF211E36);
        String label = switch (setting) {
            case SPEED -> "Speed multiplier";
            case XRAY_RANGE -> "Xray radius (blocks)";
            case RANGE -> "ESP range (M)";
            case NAMES -> "Name limit";
            case NONE -> "";
        };
        String value = switch (setting) {
            case SPEED -> multiplierText() + "x";
            case XRAY_RANGE -> Integer.toString(XraySettings.range());
            case RANGE -> Integer.toString(PlayerDisplaySettings.range());
            case NAMES -> Integer.toString(PlayerDisplaySettings.nameLimit());
            case NONE -> "";
        };
        String hint = setting == Setting.XRAY_RANGE ? "Type any number from 1 to 5000."
            : setting == Setting.NAMES ? "Hide names above this player count."
            : "Click the number to type a value.";
        graphics.text(game.font, label, 127, top + 8, TEXT);
        drawAdjustment(graphics, 303, top, false, mouseX, mouseY);
        drawAdjustment(graphics, 425, top, true, mouseX, mouseY);
        MenuPaint.rounded(graphics, 333, top, 85, 24, 6, editing == setting ? 0xFFAD8EE4 : 0xFF4C425F);
        MenuPaint.rounded(graphics, 334, top + 1, 83, 22, 5, 0xFF221D36);
        String displayed = editing == setting ? input + "_" : value;
        displayed = game.font.plainSubstrByWidth(displayed, 77);
        graphics.centeredText(game.font, displayed, 375, top + 8, TEXT);
        graphics.text(game.font, editing == setting ? "Type a value, then Enter." : hint,
            127, top + 31, MUTED);
    }

    private static void drawOreFilters(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        Minecraft game = Minecraft.getInstance();
        graphics.nextStratum();
        graphics.text(game.font, "ORE TYPES", 127, 181, MUTED);
        for (OreType ore : OreType.values()) {
            int left = 116 + (ore.ordinal() % 3) * 112;
            int top = 198 + (ore.ordinal() / 3) * 34;
            boolean enabled = XraySettings.oreEnabled(ore);
            boolean hovered = inside(mouseX, mouseY, left, top, 108, 30);
            MenuPaint.rounded(graphics, left, top, 108, 30, 7,
                hovered ? 0xFF514363 : enabled ? 0xFF39304F : 0xFF29253B);
            MenuPaint.circle(graphics, left + 11, top + 15, 3, enabled ? ore.color : 0xFF666075);
            graphics.text(game.font, ore.label, left + 20, top + 11, enabled ? TEXT : MUTED);
            graphics.fill(left + 98, top + 10, left + 101, top + 20, enabled ? ore.color : 0xFF524960);
        }
        graphics.text(game.font, "Netherite = ancient debris. Copper excluded.", 127, 311, MUTED);
        graphics.text(game.font, "Radius in all directions; loaded chunks only.", 127, 329, MUTED);
    }

    private static String multiplierText() {
        return BigDecimal.valueOf(PlayerDisplaySettings.speedMultiplier())
            .stripTrailingZeros().toPlainString();
    }

    private static void drawAdjustment(GuiGraphicsExtractor graphics,
            int left, int top, boolean plus, double mouseX, double mouseY) {
        MenuPaint.rounded(graphics, left, top, 23, 24, 6,
            inside(mouseX, mouseY, left, top, 23, 24) ? 0xFF534168 : 0xFF342C4B);
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
        } else if (icon == 4) {
            graphics.fill(x, y, x + 10, y + 1, color);
            graphics.fill(x, y + 9, x + 10, y + 10, color);
            graphics.fill(x, y, x + 1, y + 10, color);
            graphics.fill(x + 9, y, x + 10, y + 10, color);
            graphics.fill(x + 3, y + 3, x + 6, y + 5, color);
            graphics.fill(x + 5, y + 6, x + 7, y + 8, color);
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
        GUIMOVE("GuiMove", "Move with any screen open", 3),
        PLAYERS("Player ESP", "Boxes, tracers + distances", 2),
        BRIDGE("Auto Bridge", "Place underfoot", 0),
        HIT("Auto Hit", "Crosshair + weapon cooldown", 2),
        XRAY("Xray", "Color-coded ore outlines", 4),
        FREECAM("Free Cam", "Fly the camera freely", 2);

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
        NONE, SPEED, RANGE, NAMES, XRAY_RANGE
    }

    private record Layout(float x, float y, float scale) {
    }
}
