package net.gudenau.launcher.impl.profile;

import com.google.gson.*;
import net.gudenau.launcher.api.util.SharedLock;
import net.gudenau.launcher.impl.account.AccountManagerImpl;
import net.gudenau.launcher.impl.util.AutoSaver;
import net.gudenau.launcher.impl.util.MiscUtil;
import net.gudenau.launcher.impl.util.ThreadUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.gudenau.launcher.impl.util.MiscUtil.uuid;

public final class ProfileManager {
    private static final Path SAVE_PATH = MiscUtil.getPath("profiles.json");
    private static final SharedLock PROFILES_LOCK = new SharedLock();
    private static final List<Profile> PROFILES = new ArrayList<>();
    private static final Model MODEL = new Model();
    private static volatile boolean dirty = false;
    
    public static void init() {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load profiles", e);
        }
    
        AutoSaver.registerSaver(ProfileManager::save);
    }
    
    private static void save() throws IOException {
        if (!dirty) {
            return;
        }
        dirty = false;
        
        var json = PROFILES_LOCK.read(() -> {
            var array = new JsonArray();
            for (var profile : PROFILES) {
                var object = new JsonObject();
                object.addProperty("id", profile.id().toString());
                object.addProperty("name", profile.name());
                array.add(object);
            }
            return array;
        });
        
        MiscUtil.ensureParentsExist(SAVE_PATH);
        try (var writer = Files.newBufferedWriter(SAVE_PATH)) {
            writer.write(json.toString());
        }
    }
    
    private static void load() throws IOException {
        if (!Files.exists(SAVE_PATH)) {
            return;
        }
    
        JsonElement root;
        try (var reader = Files.newBufferedReader(SAVE_PATH)) {
            root = JsonParser.parseReader(reader);
        } catch (JsonParseException e) {
            throw new IOException("Failed to read profiles: invalid JSON", e);
        }
    
        if (!root.isJsonArray()) {
            throw new IOException("Failed to read profiles: not an array");
        }
    
        List<Profile> profiles = new ArrayList<>();
        for (var element : (JsonArray) root) {
            if (!element.isJsonObject()) {
                throw new IOException("Failed to read profiles: profile " + profiles.size() + " isn't an object");
            }
        
            var object = element.getAsJsonObject();
            var id = UUID.fromString(object.getAsJsonPrimitive("id").getAsString());
            var name = object.getAsJsonPrimitive("name").getAsString();
            profiles.add(new Profile(id, name));
        }
    
        PROFILES_LOCK.write(() -> {
            PROFILES.clear();
            PROFILES.addAll(profiles);
        });
    }
    
    @NotNull
    public static Optional<Profile> createProfile(@NotNull String name) {
        return PROFILES_LOCK.write(() -> {
            if(PROFILES.stream().anyMatch((profile) -> profile.name().equalsIgnoreCase(name))) {
                return Optional.empty();
            }
            var profile = new Profile(uuid(PROFILES), name);
            int index = PROFILES.size();
            PROFILES.add(profile);
            ThreadUtil.submitUi(() -> MODEL.fireIntervalAdded(profile, index, index));
            dirty = true;
            return Optional.of(profile);
        });
    }
    
    public static ComboBoxModel<Profile> model() {
        return MODEL;
    }
    
    public static List<Profile> profiles() {
        return PROFILES_LOCK.read(() -> List.copyOf(PROFILES));
    }
    
    private static class Model extends AbstractListModel<Profile> implements ComboBoxModel<Profile> {
        private Profile selected = null;
        
        @Override
        public void setSelectedItem(Object item) {
            if(item instanceof Profile profile) {
                selected = profile;
            }
        }
    
        @Override
        public Object getSelectedItem() {
            return PROFILES_LOCK.read(() -> {
                if (!PROFILES.isEmpty() && selected == null) {
                    return PROFILES.get(0);
                } else {
                    return null;
                }
            });
        }
    
        @Override
        public int getSize() {
            return PROFILES_LOCK.read(PROFILES::size);
        }
    
        @Override
        public Profile getElementAt(int index) {
            return PROFILES_LOCK.read(() -> PROFILES.get(index));
        }
    
        protected void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1);
        }
        
        protected void fireIntervalAdded(Object source, int index0, int index1) {
            super.fireIntervalAdded(source, index0, index1);
        }
        
        protected void fireIntervalRemoved(Object source, int index0, int index1) {
            super.fireIntervalRemoved(source, index0, index1);
        }
    }
}
