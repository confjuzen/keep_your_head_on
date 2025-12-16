package io.github.abomination.body;

import io.github.abomination.PlayerClass;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BodyPartLoader {
    private final Map<String, BodyPart> bodyParts = new HashMap<>();
    private final Map<BodyPartType, List<BodyPart>> partsByType = new HashMap<>();

    public BodyPartLoader(FileHandle file) {
        loadBodyParts(file);
    }

    private void loadBodyParts(FileHandle file) {
        JsonReader jsonReader = new JsonReader();
        JsonValue root = jsonReader.parse(file);

        for (JsonValue partEntry : root) {
            String id = partEntry.name;
            JsonValue partData = partEntry;

            // Convert the class string to title case to match the enum
            String className = partData.getString("class", "Human");
            className = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
            
            BodyPart part = new BodyPart(
                    id,
                    partData.getString("name"),
                    BodyPartType.valueOf(partData.getString("type")),
                    PlayerClass.valueOf(className),
                    partData.getInt("health", 0),
                    partData.getInt("damage", 0),
                    partData.getInt("speed", 0),
                    partData.getString("texture"));

            bodyParts.put(id, part);
            partsByType.computeIfAbsent(part.getType(), k -> new ArrayList<>()).add(part);
        }
    }

    public BodyPart getBodyPart(String id) {
        return bodyParts.get(id);
    }

    public List<BodyPart> getBodyPartsOfType(BodyPartType type) {
        return new ArrayList<>(partsByType.getOrDefault(type, new ArrayList<>()));
    }

    public BodyPart getRandomBodyPart(BodyPartType type) {
        return getRandomBodyPart(type, null);
    }
    
    public BodyPart getRandomBodyPart(BodyPartType type, PlayerClass playerClass) {
        List<BodyPart> parts = partsByType.getOrDefault(type, new ArrayList<>());
        if (parts.isEmpty()) {
            return null;
        }
        
        // If a specific player class is provided, filter parts by that class
        if (playerClass != null) {
            List<BodyPart> filteredParts = new ArrayList<>();
            for (BodyPart part : parts) {
                if (part.getPlayerClass() == playerClass) {
                    filteredParts.add(part);
                }
            }
            
            // If no parts match the class, fall back to any part
            if (!filteredParts.isEmpty()) {
                parts = filteredParts;
            }
        }
        
        return parts.get((int) (Math.random() * parts.size()));
    }
}
