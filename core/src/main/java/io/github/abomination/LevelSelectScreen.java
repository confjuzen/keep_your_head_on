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
 
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LevelSelectScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private Texture background;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private GlyphLayout layout;
    private int selectedOption = 0;
    private final String[] options = {
            "Shop",
            "Door 1",
            "Door 2",
            "Edit Mob 1",
            "Edit Mob 2",
            "Edit Mob 3",
    };
    private Mob mob1, mob2, mob3;

    public LevelSelectScreen(Main game) {
        this.game = game;
        this.batch = game.batch;

        camera = new OrthographicCamera();
        viewport = new FitViewport(1200, 800, camera);
        camera.position.set(
                viewport.getWorldWidth() / 2f,
                viewport.getWorldHeight() / 2f,
                0f);
        camera.update();

        if (Gdx.files.internal("level_select.png").exists()) {
            background = new Texture("level_select.png");
        }

        font = FontFactory.loadKnewave(40, Color.WHITE);

        layout = new GlyphLayout();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pause();
            game.setScreen(new OptionsScreen(game, this));
        }
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f;
        float optionSpacing = 50f;

        batch.begin();

        if (background != null) {
            batch.draw(
                    background,
                    0,
                    0,
                    viewport.getWorldWidth(),
                    viewport.getWorldHeight());
        }

        GameState gameState = game.getGameState();
        mob1 = gameState.getMob1();
        mob2 = gameState.getMob2();
        mob3 = gameState.getMob3();

        float originalLane1 = mob1 != null ? mob1.lanePosition : 0;
        float originalLane2 = mob2 != null ? mob2.lanePosition : 0;
        float originalLane3 = mob3 != null ? mob3.lanePosition : 0;
        float originalHight1 = mob1 != null ? mob1.laneHight : 0;
        float originalHight2 = mob2 != null ? mob2.laneHight : 0;
        float originalHight3 = mob3 != null ? mob3.laneHight : 0;

        float mobY = viewport.getWorldHeight() * 0.65f;
        float mobSpacing = viewport.getWorldWidth() / 4f;

        if (mob1 != null) {
            mob1.lanePosition = mobSpacing - 100;
            mob1.laneHight = mobY - 50;
            mob1.render(batch);
        }

        if (mob2 != null) {
            mob2.lanePosition = mobSpacing * 2 - 100;
            mob2.laneHight = mobY - 50;
            mob2.render(batch);
        }

        if (mob3 != null) {
            mob3.lanePosition = mobSpacing * 3 - 100;
            mob3.laneHight = mobY - 50;
            mob3.render(batch);
        }

        if (mob1 != null) {
            mob1.lanePosition = originalLane1;
            mob1.laneHight = originalHight1;
        }
        if (mob2 != null) {
            mob2.lanePosition = originalLane2;
            mob2.laneHight = originalHight2;
        }
        if (mob3 != null) {
            mob3.lanePosition = originalLane3;
            mob3.laneHight = originalHight3;
        }

        for (int i = 0; i < options.length; i++) {
            String option = (i == selectedOption)
                    ? "> " + options[i] + " <"
                    : "  " + options[i] + "  ";
            layout.setText(font, option);
            float x = centerX - layout.width / 2;
            float y = centerY + (options.length / 2f - i - 0.5f) * optionSpacing;
            font.draw(batch, option, x, y);
        }

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = (selectedOption - 1 + options.length) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            switch (selectedOption) {
                case 0: // Shop
                    game.setScreen(new ShopScreen(game, this));
                    break;
                case 1: // easy (win less money)
                    game.getGameState().setEnemyRateMultiplier(1.1f); // Door 1: +10%
                    game.getGameState().nextLevel();
                    game.setScreen(new GameScreen(game));
                    dispose(); // Only dispose when going to game screen
                    break;
                case 2: // difficult (win more money)
                    game.getGameState().setEnemyRateMultiplier(1.2f); // Door 2: +20%
                    game.getGameState().nextLevel();
                    game.setScreen(new GameScreen(game));
                    dispose(); // Only dispose when going to game screen
                    break;
                case 3: // Edit Mob 1
                case 4: // Edit Mob 2
                case 5: { // Edit Mob 3
                    int mobIndex = selectedOption - 3; // Convert to 0-2 index
                    game
                            .getGameState()
                            .setCurrentScreen(
                                    GameState.GameScreen.CHARACTER_EDITOR);
                    game.setScreen(new CharacterEditorScreen(game, mobIndex));
                    break;
                }
            }
        }
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
        if (background != null)
            background.dispose();
        if (font != null)
            font.dispose();
    }
}
