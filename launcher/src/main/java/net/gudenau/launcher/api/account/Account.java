package net.gudenau.launcher.api.account;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;

/**
 * The representation of an account that is required to play a game or to use related services.
 */
public interface Account {
    /**
     * The username of this account.
     *
     * @return The account username
     */
    @NotNull String username();
    
    /**
     * Invalidates this account, forcing the user to sign in again to use it.
     *
     * @throws IOException on a networking failure
     */
    void invalidate() throws IOException;
    
    /**
     * Refreshes this account, ensuring any tokens are valid and getting fresh ones if required.
     *
     * @throws IOException on a networking failure
     */
    void refresh() throws IOException;
    
    /**
     * Serializes this account in to a JSON representation for saving.
     *
     * @return The serialized version of this account
     */
    @NotNull JsonObject serialize();
    
    /**
     * Returns the image for this account, if present.
     *
     * @return The account image
     *
     * @throws IOException On failure to load the image
     */
    @Nullable
    default Image icon() throws IOException {
        return null;
    }
}
