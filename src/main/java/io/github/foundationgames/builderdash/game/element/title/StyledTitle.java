package io.github.foundationgames.builderdash.game.element.title;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import io.github.foundationgames.builderdash.Builderdash;
import net.minecraft.world.entity.EntityTypes;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class StyledTitle {
    public static final char[] CHARS = {
            'ᴀ', 'ʙ', 'ᴄ', 'ᴅ', 'ᴇ', 'ꜰ', 'ɢ', 'ʜ', 'ɪ', 'ᴊ', 'ᴋ', 'ʟ', 'ᴍ', 'ɴ', 'ᴏ', 'ᴘ', 'ꞯ', 'ʀ', 'ꜱ', 'ᴛ', 'ᴜ', 'ᴠ', 'ᴡ', 'x', 'ʏ', 'ᴢ'
    };

    public final Vec3 pos;
    public final String topText;
    public final int topColor;
    public final String bottomText;
    public final int bottomColor;

    private final float scale;
    private final Quaternionf rotation;

    public StyledTitle(Vec3 pos, float scale, Quaternionf rotation, String topText, int topColor, String bottomText, int bottomColor) {
        this.pos = pos;
        this.topColor = topColor;
        this.topText = topText;
        this.bottomText = bottomText;
        this.rotation = rotation;
        this.scale = scale;
        this.bottomColor = bottomColor;
    }

    public static String stylizedAlpha(String s) {
        var sty = new StringBuilder();
        var src = s.toLowerCase(Locale.ROOT);

        for (char c : src.toCharArray()) {
            int i = c - 'a';
            if (i >= 0 && i < CHARS.length) {
                sty.append(CHARS[i]);
            }
        }

        return sty.toString();
    }

    private void addOutlineTexts(ServerLevel world, Matrix4f xfm, String stylized) {
        var text = Component.literal(stylized).withStyle(ChatFormatting.BLACK);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                var xf = new Matrix4f().set(xfm);
                xf.translate(x * 0.02f, y * 0.02f, -0.0002f);

                var el = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, world);
                el.setTransformation(new Transformation(xf));
                el.setText(text);
                el.setBackgroundColor(0);

                world.addFreshEntity(el);
                el.setPos(pos);
            }
        }
    }

    private void addHighlightText(ServerLevel world, Matrix4f xfm, String stylized, int color, float factor) {
        int r = color >> 16;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        r = 128 + (r / 2);
        g = 128 + (g / 2);
        b = 128 + (b / 2);
        final int c = (r << 16) | (g << 8) | b;

        var text = Component.literal(stylized).withStyle(s -> s.withColor(c));
        var xf = new Matrix4f().set(xfm);
        xf.translate(0, factor * 0.004f, -0.0001f);

        var el = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, world);
        el.setTransformation(new Transformation(xf));
        el.setText(text);
        el.setBackgroundColor(0);

        world.addFreshEntity(el);
        el.setPos(pos);
    }

    private void addText(ServerLevel world, Matrix4f xfm, String stylized, int color) {
        var text = Component.literal(stylized).withStyle(s -> s.withColor(color));
        var el = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, world);
        el.setTransformation(new Transformation(xfm));
        el.setText(text);
        el.setBackgroundColor(0);

        world.addFreshEntity(el);
        el.setPos(pos);
    }

    public void spawn(ServerLevel world) {
        var topS = stylizedAlpha(this.topText);
        var topXfm = new Matrix4f().set(this.rotation).rotate(Axis.XP.rotationDegrees(-30)).scale(scale);
        this.addOutlineTexts(world, topXfm, topS);
        this.addHighlightText(world, topXfm, topS, topColor, -1);
        this.addText(world, topXfm, topS, topColor);

        var botS = stylizedAlpha(this.bottomText);
        var botXfm = new Matrix4f().set(this.rotation).scale(scale);
        botXfm.translate(0, -0.1f, -0.08f);
        botXfm.scale(0.75f);
        botXfm.rotate(Axis.XP.rotationDegrees(30));
        this.addOutlineTexts(world, botXfm, botS);
        this.addHighlightText(world, botXfm, botS, bottomColor, 1);
        this.addText(world, botXfm, botS, bottomColor);
    }

    public static StyledTitle forMinigame(Vec3 pos, float scale, Quaternionf rot, String gameName, int color) {
        return new StyledTitle(pos, scale, rot, Builderdash.ID, 0x4e96ed, gameName, color);
    }
}
