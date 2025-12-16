package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.abomination.body.BodyPartLoader;

public class Main extends Game {
    private GameState gameState;
    public SpriteBatch batch;

    @Override
    public void create() {
        this.gameState = new GameState();
        BodyPartLoader loader = new BodyPartLoader(Gdx.files.internal("body_parts/body_parts.json"));
        Mob.initialize(loader);

        batch = new SpriteBatch();
        if (!Gdx.graphics.isFullscreen()) {
            int w = Gdx.graphics.getWidth();
            int h = Gdx.graphics.getHeight();
            if (w < 1200 || h < 800) {
                Gdx.graphics.setWindowedMode(1200, 800);
            }
        }
        this.setScreen(new TitleScreen(this));
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        super.dispose();
    }

    public GameState getGameState() {
        return gameState;
    }

}
