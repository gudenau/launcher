package net.gudenau.launcher.api.account;

import net.gudenau.launcher.impl.account.AccountManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * The account manager. Allows user accounts to be managed.
 */
public sealed interface AccountManager permits AccountManagerImpl {
    /**
     * Gets the {@link AccountManager} singleton.
     *
     * @return The {@link AccountManager} singleton
     */
    @NotNull
    static AccountManager instance() {
        return AccountManagerImpl.INSTANCE;
    }
    
    /**
     * Gets the {@link AccountProvider} for a given account {@link UUID}, if present.
     *
     * @param id The account {@link UUID}
     * @return The {@link AccountProvider} or empty
     */
    @NotNull Optional<AccountProvider> provider(@NotNull UUID id);
}
