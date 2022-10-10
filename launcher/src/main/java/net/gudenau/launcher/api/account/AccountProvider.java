package net.gudenau.launcher.api.account;

import com.google.gson.JsonObject;
import net.gudenau.launcher.api.util.Identified;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The interface to implement if your plugin provides user accounts.
 */
public interface AccountProvider extends Identified {
    /**
     * The different types of fields that can be used when logging into a new account.
     */
    enum FieldType {
        /**
         * A username field, it will always be a {@link String}.
         */
        USERNAME,
        /**
         * A password field, it will always be a char[].
         */
        PASSWORD,
        /**
         * An email field, it will always be a {@link String}.
         */
        EMAIL,
        /**
         * A generic field, it will always be a {@link String}.
         */
        OTHER,
    }
    
    /**
     * A field to provide the UI for new account creation.
     *
     * @param name The name of the field
     * @param type The type of the field
     * @param hint The hint for the field
     * @param required Is the field required to attempt a login
     */
    record Field(
        @NotNull String name,
        @NotNull FieldType type,
        @Nullable String hint,
        boolean required
    ) {}
    
    /**
     * Gets a list of {@link Field}s required to create a new account instance.
     *
     * @return The {@link List} of fields
     */
    @NotNull List<Field> fields();
    
    /**
     * Logs into a new account instance with the requested {@link Field}s.
     *
     * @param fields The fields use to log in
     * @return The new account
     * @throws IOException If an error occurred when attempting to log in
     */
    @NotNull Account login(@NotNull Map<Field, Object> fields) throws IOException;
    
    /**
     * Creates an {@link Account} instance from the provided {@link JsonObject JSON}. This should not perform network
     * operations.
     *
     * @param object The {@link JsonObject JSON} to deserialize
     * @return The deserialized {@link Account} instance
     */
    @NotNull Account deserialize(@NotNull JsonObject object);
}
