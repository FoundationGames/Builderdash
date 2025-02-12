package io.github.foundationgames.builderdash.game.element.display;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class GenericContent extends InWorldDisplay.Content {
    // WPB = line-width per block at scale = 1
    // Line Width = (WPB * display-width) / scale
    public static final int WIDTH_PER_BLOCK = 40;
    public static final int LINES_PER_BLOCK = 4;

    public final List<TextLine> fromTop;
    public final List<TextLine> fromBottom;
    public final List<GameProfile> playerHeads;
    public final float margin;

    private final List<VirtualElement> elems = new ArrayList<>();

    public GenericContent(List<TextLine> fromTop, List<TextLine> fromBottom, List<GameProfile> playerHeads, float margin) {
        this.fromTop = fromTop;
        this.fromBottom = fromBottom;
        this.playerHeads = playerHeads;
        this.margin = margin;
    }

    public static Builder builder(float margin) {
        return new Builder(margin);
    }

    public static Builder builder() {
        return builder(0.5f);
    }

    @Override
    protected void init(InWorldDisplay display) {
        float halfway = (float) (display.sizeX * 0.5);
        float offset = 0;

        float bottomMargin = this.playerHeads.isEmpty() ? 0 : 2.25f;
        for (var line : fromBottom) {
            float scale = line.scale();
            int lineWidth = (int) ((WIDTH_PER_BLOCK * (display.sizeX - (2 * this.margin))) / scale);
            float lineHeight = line.expectedLineCount() * (scale / LINES_PER_BLOCK);

            var text = new TextDisplayElement();
            text.setText(line.text());
            text.setBackground(0);
            text.setTranslation(new Vector3f(halfway, (this.margin + offset + bottomMargin), 0.05f));
            text.setScale(new Vector3f(scale));
            text.setLineWidth(lineWidth);

            display.addElement(text);
            this.elems.add(text);

            offset += lineHeight;
        }

        offset = 0;
        for (var line : fromTop) {
            float scale = line.scale();
            int lineWidth = (int) ((WIDTH_PER_BLOCK * (display.sizeX - (2 * this.margin))) / scale);
            float lineHeight = line.expectedLineCount() * (scale / LINES_PER_BLOCK);
            offset += lineHeight;

            var text = new TextDisplayElement();
            text.setText(line.text());
            text.setBackground(0);
            text.setTranslation(new Vector3f(halfway, ((float) display.sizeY - (this.margin + offset)), 0.05f));
            text.setScale(new Vector3f(scale));
            text.setLineWidth(lineWidth);

            display.addElement(text);
            this.elems.add(text);
        }

        float headSpacing = Math.min((float) ((display.sizeX - 2) / (this.playerHeads.size() + 1)), 3.5f);
        float headsTotalWidth = headSpacing * this.playerHeads.size();

        float x = (float) ((display.sizeX - headsTotalWidth) * 0.5);
        for (var profile : this.playerHeads) {
            var head = new ItemDisplayElement();
            var stack = Items.PLAYER_HEAD.getDefaultStack();
            stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));

            head.setItem(stack);
            head.setScale(new Vector3f(4f));
            head.setTranslation(new Vector3f(x + 2f, this.margin + 2f, -0.5f));
            head.setLeftRotation(RotationAxis.POSITIVE_Y.rotation(MathHelper.PI));

            display.addElement(head);
            this.elems.add(head);
            x += headSpacing;
        }
    }

    @Override
    protected void destroy(InWorldDisplay display) {
        for (var el : this.elems) {
            display.removeElement(el);
        }
    }

    public record TextLine(Text text, int expectedLineCount, float scale) {
    }

    public static class Builder {
        private final List<TextLine> fromTop = new ArrayList<>();
        private final List<TextLine> fromBottom = new ArrayList<>();
        private final List<GameProfile> playerHeads = new ArrayList<>();
        private final float margin;

        private Builder(float margin) {
            this.margin = margin;
        }

        public GenericContent build() {
            return new GenericContent(List.copyOf(fromTop), List.copyOf(fromBottom), List.copyOf(playerHeads), margin);
        }

        public Builder addTop(Text line, int linesLong, float scale) {
            this.fromTop.add(new TextLine(line, linesLong, scale));
            return this;
        }

        public Builder addTop(Text line, int linesLong) {
            return this.addTop(line, linesLong, 3);
        }

        public Builder addTop(Text line) {
            return this.addTop(line, 2, 3);
        }

        public Builder addBottom(Text line, int linesLong, float scale) {
            this.fromBottom.add(new TextLine(line, linesLong, scale));
            return this;
        }

        public Builder addBottom(Text line, int linesLong) {
            return this.addBottom(line, linesLong, 3);
        }

        public Builder addBottom(Text line) {
            return this.addBottom(line, 2, 3);
        }

        public Builder addPlayer(GameProfile player) {
            this.playerHeads.add(player);
            return this;
        }
    }
}
