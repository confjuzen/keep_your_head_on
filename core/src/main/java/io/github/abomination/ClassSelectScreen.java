package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;

public class ClassSelectScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private BitmapFont font;
    private Texture background;
    private OrthographicCamera camera;
    private Viewport viewport;
    private GlyphLayout layout;

    private final PlayerClass[] options = PlayerClass.values();
    private int selectedIndex = 0;
    private Texture[] classHeadTextures;
    private int hoveredIndex = -1;

    public ClassSelectScreen(Main game) {
        this.game = game;
        this.batch = game.batch;
        font = FontFactory.loadKnewave(40, Color.WHITE);
        if (Gdx.files.internal("class_select.png").exists()) {
            background = new Texture("class_select.png");
        } else {
            background = new Texture("title_background.png");
        }
        camera = new OrthographicCamera();
        viewport = new FitViewport(1200, 800, camera);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0f);
        camera.update();
        layout = new GlyphLayout();

        classHeadTextures = new Texture[options.length];
        for (int i = 0; i < options.length; i++) {
            try {
                Mob mob = new Mob(0f, options[i]);
                if (mob.getHead() != null && mob.getHead().getTexture() != null) {
                    classHeadTextures[i] = mob.getHead().getTexture();
                }
            } catch (Exception e) {
                classHeadTextures[i] = null;
            }
        }
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
        float aspectRatio = (float) background.getWidth() / background.getHeight();
        float targetWidth = Math.min(ww, wh * aspectRatio);
        float targetHeight = targetWidth / aspectRatio;
        float x = (ww - targetWidth) / 2f;
        float y = (wh - targetHeight) / 2f;
        batch.draw(background, x, y, targetWidth, targetHeight);

        float centerX = ww / 2f;
        float centerY = wh * 0.46f;
        float radius = Math.min(ww, wh) * 0.40f;
        float baseIconSize = Math.min(ww, wh) * 0.20f;

        // First pass: determine which icon (if any) the mouse is hovering
        hoveredIndex = -1;
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(mouse);

        float angleStep = 360f / options.length;
        for (int i = 0; i < options.length; i++) {
            float angleDeg = 90f - i * angleStep;
            float angleRad = (float) Math.toRadians(angleDeg);
            float iconCenterX = centerX + (float) Math.cos(angleRad) * radius;
            float iconCenterY = centerY + (float) Math.sin(angleRad) * radius;

            float dx = mouse.x - iconCenterX;
            float dy = mouse.y - iconCenterY;
            float hitRadius = baseIconSize * 0.5f;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                hoveredIndex = i;
            }
        }

        int primaryIndex = (hoveredIndex >= 0) ? hoveredIndex : selectedIndex;

        // Second pass: render all icons, scaling only the primary one
        for (int i = 0; i < options.length; i++) {
            float angleDeg = 90f - i * angleStep;
            float angleRad = (float) Math.toRadians(angleDeg);
            float iconCenterX = centerX + (float) Math.cos(angleRad) * radius;
            float iconCenterY = centerY + (float) Math.sin(angleRad) * radius;

            float scale = (i == primaryIndex) ? 1.2f : 1f;

            float iconWidth = baseIconSize * scale;
            float iconHeight = baseIconSize * scale;

            Texture headTex = classHeadTextures != null && i < classHeadTextures.length
                    ? classHeadTextures[i]
                    : null;
            if (headTex != null) {
                batch.draw(headTex,
                        iconCenterX - iconWidth / 2f,
                        iconCenterY - iconHeight / 2f,
                        iconWidth,
                        iconHeight);
            } else {
                String name = options[i].name().substring(0, 1);
                layout.setText(font, name);
                font.draw(batch,
                        name,
                        iconCenterX - layout.width / 2f,
                        iconCenterY + layout.height / 2f);
            }
        }

        String selectedName = options[primaryIndex].name();
        layout.setText(font, selectedName);
        float nameX = (ww - layout.width) / 2f;
        float nameY = centerY + baseIconSize * 0.30f;
        font.draw(batch, selectedName, nameX, nameY);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedIndex = (selectedIndex + 1) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            GameState gameState = game.getGameState();
            gameState.setPlayerClass(options[selectedIndex]);
            gameState.initializeNewGame();
            game.setScreen(new LevelSelectScreen(game));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
            dispose();
        }

        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touch);
            float ww = viewport.getWorldWidth();
            float wh = viewport.getWorldHeight();
            float centerX = ww / 2f;
            float centerY = wh * 0.46f;
            float radius = Math.min(ww, wh) * 0.40f;
            float baseIconSize = Math.min(ww, wh) * 0.16f;
            float angleStep = 360f / options.length;

            for (int i = 0; i < options.length; i++) {
                float angleDeg = 90f - i * angleStep;
                float angleRad = (float) Math.toRadians(angleDeg);
                float iconCenterX = centerX + (float) Math.cos(angleRad) * radius;
                float iconCenterY = centerY + (float) Math.sin(angleRad) * radius;

                float dx = touch.x - iconCenterX;
                float dy = touch.y - iconCenterY;
                float hitRadius = baseIconSize * 0.5f;
                if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                    selectedIndex = i;
                    GameState gameState = game.getGameState();
                    gameState.setPlayerClass(options[selectedIndex]);
                    gameState.initializeNewGame();
                    game.setScreen(new LevelSelectScreen(game));
                    dispose();
                    break;
                }
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
        font.dispose();
        background.dispose();
    }
}
