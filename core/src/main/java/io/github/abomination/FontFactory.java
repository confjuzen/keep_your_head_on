package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.files.FileHandle;

public final class FontFactory {
    private FontFactory() {}

    public static BitmapFont loadKnewave(int size, Color color) {
        try {
            FileHandle fontFile = Gdx.files.internal("fonts/Knewave-Regular.ttf");
            if (fontFile.exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
                p.size = size;
                p.color = color;
                BitmapFont font = gen.generateFont(p);
                gen.dispose();
                return font;
            }
        } catch (Exception ignored) {}
        BitmapFont fallback = new BitmapFont();
        float scale = Math.max(1f, size / 18f);
        fallback.getData().setScale(scale);
        return fallback;
    }

    public static BitmapFont loadKnewave(int size, Color color, float borderWidth, Color borderColor) {
        try {
            FileHandle fontFile = Gdx.files.internal("fonts/Knewave-Regular.ttf");
            if (fontFile.exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
                p.size = size;
                p.color = color;
                p.borderWidth = borderWidth;
                p.borderColor = borderColor;
                BitmapFont font = gen.generateFont(p);
                gen.dispose();
                return font;
            }
        } catch (Exception ignored) {}
        BitmapFont fallback = loadKnewave(size, color);
        return fallback;
    }
}
