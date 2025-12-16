package io.github.abomination;

import com.badlogic.gdx.Gdx;
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

public class TitleScreen implements Screen {

    private final Main game;
    private Texture background;
    private Texture logo;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private GlyphLayout layout;
    private int selectedIndex = 0;
    private final String[] options = { "Start", "Options", "Quit" };

    public TitleScreen(Main game) {
        this.game = game;
        background = new Texture("title_background.png");
        logo = new Texture("logo.png");
        font = FontFactory.loadKnewave(48, Color.WHITE);
        camera = new OrthographicCamera();
        viewport = new FitViewport(1200, 800, camera);
        camera.position.set(
                viewport.getWorldWidth() / 2f,
                viewport.getWorldHeight() / 2f,
                0f);
        camera.update();
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();

        game.batch.begin();
        float ww = viewport.getWorldWidth();
        float wh = viewport.getWorldHeight();
        float aspectRatio = (float) background.getWidth() / background.getHeight();
        float targetWidth = Math.min(ww, wh * aspectRatio);
        float targetHeight = targetWidth / aspectRatio;
        float x = (ww - targetWidth) / 2f;
        float y = (wh - targetHeight) / 2f;
        game.batch.draw(background, x, y, targetWidth, targetHeight);
        float line = font.getLineHeight();

        // Draw logo with shadow
        float logoAspectRatio = (float) logo.getWidth() / logo.getHeight();
        float logoWidth = ww * 0.6f; // 60% of screen width
        float logoHeight = logoWidth / logoAspectRatio;
        float logoX = (ww - logoWidth) / 2f;
        float logoY = wh / 2f + line;

        // Draw shadow (slightly offset and semi-transparent)
        game.batch.setColor(0, 0, 0, 0.4f);
        game.batch.draw(logo, logoX + 5, logoY - 5, logoWidth, logoHeight);
        game.batch.setColor(1, 1, 1, 1); // Reset color to default

        // Draw the actual logo
        game.batch.draw(logo, logoX, logoY, logoWidth, logoHeight);

        // Draw text options using LevelSelect-style menu
        float optionSpacing = line * 1.2f;
        float firstY = wh / 2f - line * 1.25f;
        float[] optionX = new float[options.length];
        float[] optionY = new float[options.length];

        for (int i = 0; i < options.length; i++) {
            String base = options[i];
            String label = (i == selectedIndex)
                ? "> " + base + " <"
                : "  " + base + "  ";
            layout.setText(font, label);
            float optX = (ww - layout.width) / 2f;
            float optY = firstY - i * optionSpacing;
            optionX[i] = optX;
            optionY[i] = optY;
            font.draw(game.batch, label, optX, optY);
        }
        game.batch.end();
        // Convertir la position du clic/tap en coordonnées du monde (viewport)
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(
                    Gdx.input.getX(),
                    Gdx.input.getY(),
                    0);
            camera.unproject(touchPos);

            for (int i = 0; i < options.length; i++) {
                String base = options[i];
                String label = (i == selectedIndex)
                    ? "> " + base + " <"
                    : "  " + base + "  ";
                layout.setText(font, label);
                float optX = (ww - layout.width) / 2f;
                float optY = firstY - i * optionSpacing;
                if (touchPos.x >= optX &&
                        touchPos.x <= optX + layout.width &&
                        touchPos.y >= optY - layout.height &&
                        touchPos.y <= optY) {
                    selectedIndex = i;
                    // Activate the selected option
                    if (selectedIndex == 0) {
                        game.setScreen(new ClassSelectScreen(game));
                        dispose();
                    } else if (selectedIndex == 1) {
                        game.setScreen(new OptionsScreen(game, this));
                    } else if (selectedIndex == 2) {
                        Gdx.app.exit();
                    }
                    break;
                }
            }
        }
        // if (
        // Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
        // Gdx.input.justTouched()
        // ) {
        // game.setScreen(new ClassSelectScreen(game));
        // dispose();
        // } else if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
        // game.setScreen(new OptionsScreen(game, this));
        // }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selectedIndex == 0) {
                game.setScreen(new ClassSelectScreen(game));
                dispose();
            } else if (selectedIndex == 1) {
                game.setScreen(new OptionsScreen(game, this));
            } else if (selectedIndex == 2) {
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        background.dispose();
        logo.dispose();
        font.dispose();
    }
}
