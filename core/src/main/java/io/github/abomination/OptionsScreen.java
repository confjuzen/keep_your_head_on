package io.github.abomination;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class OptionsScreen implements Screen {

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void resume() {
        // Resume any paused game activities if needed
    }

    @Override
    public void pause() {
        // Pause any ongoing game activities if needed
    }

    private final Main game;
    private final SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private GlyphLayout layout;
    private Texture background;

    private final float[] scales = new float[] {
        0.5f,
        0.75f,
        1f,
        1.25f,
        1.5f,
        2f,
    };
    private int scaleIndex = 2;
    private boolean fullscreen = false;

    private int selectedIndex = 0;

    private int baseWidth;
    private int baseHeight;
    private int lastWindowedWidth;
    private int lastWindowedHeight;
    private final Screen gameScreen;

    public OptionsScreen(Main game, Screen gameScreen) {
        this.gameScreen = gameScreen;
        this.game = game;
        this.batch = game.batch;
        font = FontFactory.loadKnewave(36, Color.WHITE);
        camera = new OrthographicCamera();
        viewport = new FitViewport(1200, 800, camera);
        camera.position.set(
            viewport.getWorldWidth() / 2f,
            viewport.getWorldHeight() / 2f,
            0f
        );
        camera.update();
        layout = new GlyphLayout();
        background = new Texture(Gdx.files.internal("blank_blk_bg.png"));
        baseWidth = 1200;
        baseHeight = 800;
        lastWindowedWidth = Gdx.graphics.getWidth();
        lastWindowedHeight = Gdx.graphics.getHeight();
        fullscreen = Gdx.graphics.isFullscreen();
        float currentScaleW = (float) Gdx.graphics.getWidth() / baseWidth;
        float currentScaleH = (float) Gdx.graphics.getHeight() / baseHeight;
        float currentScale = Math.min(currentScaleW, currentScaleH);
        int closest = 0;
        float bestDiff = Float.MAX_VALUE;
        for (int i = 0; i < scales.length; i++) {
            float d = Math.abs(scales[i] - currentScale);
            if (d < bestDiff) {
                bestDiff = d;
                closest = i;
            }
        }
        scaleIndex = closest;
    }

    @Override
    public void render(float delta) {
        handleInput();

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        float ww = viewport.getWorldWidth();
        float wh = viewport.getWorldHeight();
        batch.draw(background, 0, 0, ww, wh);
        float line = font.getLineHeight();
        String title = "Options";
        layout.setText(font, title);
        float titleX = (ww - layout.width) / 2f;
        float startY = wh / 2f + line * 1.5f;
        font.draw(batch, title, titleX, startY + line);

        String item1 =
            (selectedIndex == 0 ? "> " : "  ") +
            "Resolution Scale: " +
            formatScale(scales[scaleIndex]);
        String item2 =
            (selectedIndex == 1 ? "> " : "  ") +
            "Fullscreen: " +
            (fullscreen ? "On" : "Off");
        String item3 = (selectedIndex == 2 ? "> " : "  ") + "Back";
        String item4 = (selectedIndex == 3 ? "> " : "  ") + "Restart";

        layout.setText(font, item1);
        float i1x = (ww - layout.width) / 2f;
        float i1y = startY;
        font.draw(batch, item1, i1x, i1y);

        layout.setText(font, item2);
        float i2x = (ww - layout.width) / 2f;
        float i2y = startY - (line * 1.1f);
        font.draw(batch, item2, i2x, i2y);

        layout.setText(font, item3);
        float i3x = (ww - layout.width) / 2f;
        float i3y = startY - (line * 2.2f);
        font.draw(batch, item3, i3x, i3y);

        layout.setText(font, item4);
        float i4x = (ww - layout.width) / 2f;
        float i4y = startY - (line * 3.3f);
        font.draw(batch, item4, i4x, i4y);
        batch.end();
    }

    private String formatScale(float s) {
        return ((int) (s * 100)) + "%";
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex + 4 - 1) % 4;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % 4;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (selectedIndex == 0) {
                scaleIndex = (scaleIndex + scales.length - 1) % scales.length;
                applyWindowedScaleIfNotFullscreen();
            } else if (selectedIndex == 1) {
                toggleFullscreen();
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (selectedIndex == 0) {
                scaleIndex = (scaleIndex + 1) % scales.length;
                applyWindowedScaleIfNotFullscreen();
            } else if (selectedIndex == 1) {
                toggleFullscreen();
            }
        } else if (
            Gdx.input.isKeyJustPressed(Input.Keys.LEFT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
        ) {
            if (selectedIndex == 0) {
                if (
                    Gdx.input.isKeyJustPressed(Input.Keys.LEFT) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                ) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                        scaleIndex =
                            (scaleIndex + scales.length - 1) % scales.length;
                    } else {
                        scaleIndex = (scaleIndex + 1) % scales.length;
                    }
                    applyWindowedScaleIfNotFullscreen();
                }
            } else if (selectedIndex == 1) {
                toggleFullscreen();
            } else if (selectedIndex == 2) {
                game.setScreen(gameScreen);
                gameScreen.resume();
            } else if (selectedIndex == 3) {
                game.setScreen(new TitleScreen(game));
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(gameScreen);
            gameScreen.resume();
        }

        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touch);
            float ww = viewport.getWorldWidth();
            float wh = viewport.getWorldHeight();
            float line = font.getLineHeight();
            float listStartY = wh / 2f + line * 1.5f;
            String[] items = new String[] {
                "> " + "Resolution Scale: " + formatScale(scales[scaleIndex]),
                "> " + "Fullscreen: " + (fullscreen ? "On" : "Off"),
                "> " + "Back",
                "> " + "Restart",
            };
            for (int i = 0; i < 4; i++) {
                String label =
                    items[i].replaceFirst(
                            "> ",
                            (selectedIndex == i ? "> " : "  ")
                        );
                layout.setText(font, label);
                float itemY = listStartY - i * (line * 1.1f);
                float itemX = (ww - layout.width) / 2f;
                float itemTop = itemY + line * 0.3f;
                float itemBottom = itemY - line;
                if (
                    touch.x >= itemX &&
                    touch.x <= itemX + layout.width &&
                    touch.y <= itemTop &&
                    touch.y >= itemBottom
                ) {
                    selectedIndex = i;
                    if (i == 0) {
                        applyWindowedScaleIfNotFullscreen();
                    } else if (i == 1) {
                        toggleFullscreen();
                    } else if (i == 2) {
                        game.setScreen(gameScreen);
                        gameScreen.resume();
                    } else if (i == 3) {
                        game.setScreen(new TitleScreen(game));
                    }
                    break;
                }
            }
        }
    }

    private void applyWindowedScaleIfNotFullscreen() {
        if (fullscreen) return;
        int w = Math.max(320, Math.round(baseWidth * scales[scaleIndex]));
        int h = Math.max(180, Math.round(baseHeight * scales[scaleIndex]));
        lastWindowedWidth = w;
        lastWindowedHeight = h;
        Gdx.graphics.setWindowedMode(w, h);
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            if (fullscreen) {
                DisplayMode mode = Gdx.graphics.getDisplayMode();
                if (mode != null) {
                    Gdx.graphics.setFullscreenMode(mode);
                } else {
                    fullscreen = false;
                }
            } else {
                int w = lastWindowedWidth > 0 ? lastWindowedWidth : baseWidth;
                int h = lastWindowedHeight > 0
                    ? lastWindowedHeight
                    : baseHeight;
                Gdx.graphics.setWindowedMode(w, h);
            }
        }
    }

    @Override
    public void dispose() {
        font.dispose();
        if (background != null) {
            background.dispose();
        }
    }
}
