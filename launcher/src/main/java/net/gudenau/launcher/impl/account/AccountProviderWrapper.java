package net.gudenau.launcher.impl.account;

import com.google.gson.JsonObject;
import net.gudenau.launcher.api.account.Account;
import net.gudenau.launcher.api.account.AccountProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AccountProviderWrapper implements AccountProvider {
    private final AccountProvider provider;
    private final UUID id;
    
    AccountProviderWrapper(AccountProvider provider) {
        this.provider = provider;
        id = provider.id();
    }
    
    @Override
    public @NotNull List<Field> fields() {
        return provider.fields();
    }
    
    @Override
    public @NotNull Account login(@NotNull Map<Field, Object> fields) throws IOException {
        return provider.login(fields);
    }
    
    @Override
    public @NotNull Account deserialize(@NotNull JsonObject object) {
        return provider.deserialize(object);
    }
    
    @Override
    public @NotNull UUID id() {
        return id;
    }
}
