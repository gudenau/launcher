package net.gudenau.launcher.impl.account;

import com.google.gson.JsonObject;
import net.gudenau.launcher.api.account.Account;
import net.gudenau.launcher.api.account.AccountManager;
import net.gudenau.launcher.api.account.AccountProvider;
import net.gudenau.launcher.api.util.SharedLock;
import net.gudenau.launcher.impl.profile.Profile;
import net.gudenau.launcher.impl.profile.ProfileManager;
import net.gudenau.launcher.impl.util.AutoSaver;
import net.gudenau.launcher.impl.util.MiscUtil;
import net.gudenau.launcher.plugin.PluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AccountManagerImpl implements AccountManager {
    public static AccountManagerImpl INSTANCE;
    
    public static void init() {
        new AccountManagerImpl();
    }
    
    private final SharedLock profilesLock = new SharedLock();
    private final Map<Profile, List<Account>> profiles = new HashMap<>();
    private final Map<UUID, AccountProviderWrapper> accountProviders;
    
    private AccountManagerImpl() {
        synchronized (AccountManagerImpl.class) {
            if (INSTANCE != null) {
                throw new IllegalStateException("Only one instance of AccountManagerImpl is allowed");
            }
            INSTANCE = this;
        }
    
        accountProviders = PluginLoader.services(AccountProvider.class)
            .map(AccountProviderWrapper::new)
            .collect(Collectors.toUnmodifiableMap(
                AccountProvider::id,
                Function.identity()
            ));
        
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load account information", e);
        }
        AutoSaver.registerSaver(this::save);
    }
    
    private void load() throws IOException {
        for (var profile : ProfileManager.profiles()) {
            var accountJson = profile.path("accounts.json");
            if (!Files.exists(accountJson)) {
                continue;
            }
            
            var accountInfo = MiscUtil.readJsonArray(accountJson);
            var accounts = new ArrayList<Account>();
            for (var element : accountInfo) {
                if (!(element instanceof JsonObject accountObject)) {
                    throw new IOException("Failed to parse account JSON: expected an object");
                }
                
                var providerId = UUID.fromString(accountObject.getAsJsonPrimitive("provider").getAsString());
                var providerName = accountObject.getAsJsonPrimitive("provider_name").getAsString();
                var provider = provider(providerId)
                    .orElseThrow(() -> new IOException("Failed to find provider " + providerId + " (" + providerName + ')'));
                var account = provider.deserialize(accountObject.getAsJsonObject("data"));
                accounts.add(account);
            }
        }
    }
    
    @Override
    @NotNull
    public Optional<AccountProvider> provider(@NotNull UUID id) {
        return Optional.ofNullable(accountProviders.get(id));
    }
    
    private void save() throws IOException {
    
    }
    
    @NotNull
    public List<Account> profileAccounts() {
        return List.of();
    }
}
