package io.github.abomination;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;

public class LaneManager {
    private List<Lane> lanes;

    public LaneManager() {
        lanes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lanes.add(new Lane(i));
        }
    }

    public void updateLanes() {
        for (Lane lane : lanes) {
            lane.update();
        }
    }

    public List<Lane> getLanes() {
        return lanes;
    }

}
