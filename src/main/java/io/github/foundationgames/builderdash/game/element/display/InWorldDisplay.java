package io.github.foundationgames.builderdash.game.element.display;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.TemplateRegion;

public class InWorldDisplay extends ElementHolder {
    public final double sizeX;
    public final double sizeY;

    private Content currentContent = null;

    public InWorldDisplay(Vec3d origin, double sizeX, double sizeY) {
        this.currentPos = origin;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public static InWorldDisplay of(TemplateRegion region) {
        var b = region.getBounds();
        double sizeX = b.size().getX();
        double sizeY = b.size().getY();
        return new InWorldDisplay(Vec3d.of(b.min()), sizeX, sizeY);
    }

    public InWorldDisplay offsetCopy(Vec3i offset) {
        return new InWorldDisplay(this.currentPos.add(offset.getX(), offset.getY(), offset.getZ()),
                this.sizeX, this.sizeY);
    }

    public static InWorldDisplay[] offset(Vec3i offset, InWorldDisplay[] displays) {
        var nds = new InWorldDisplay[displays.length];
        for (int i = 0; i < displays.length; i++) {
            nds[i] = displays[i].offsetCopy(offset);
        }

        return nds;
    }

    public void setContent(@Nullable Content content) {
        if (this.currentContent != null) {
            this.currentContent.destroy(this);
        }

        this.currentContent = content;

        if (content != null) {
            content.init(this);
        }
    }

    public void clear() {
        this.setContent(null);
    }

    public static abstract class Content {
        protected abstract void init(InWorldDisplay display);

        protected abstract void destroy(InWorldDisplay display);
    }
}
