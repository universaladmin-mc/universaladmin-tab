package dev.universaladmin.tab;

import dev.universaladmin.core.UniversalAdmin;
import dev.universaladmin.core.UniversalAdminProvider;
import java.util.Optional;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * UniversalAdmin TAB: connects TAB (by NEZNAMY) to UniversalAdmin - the tab
 * list and name tags follow nicks, vanish and staff mode. Needs
 * UniversalAdmin 1.7 or newer (API 1.6: the {@link dev.universaladmin.status.Nicks}
 * service).
 */
public final class TabPlugin extends JavaPlugin {

    private static final String REQUIRED_API = "1.6";

    @Override
    public void onEnable() {
        Optional<UniversalAdmin> universalAdmin = UniversalAdminProvider.find();
        if (universalAdmin.isEmpty()) {
            getLogger().severe("UniversalAdmin is not running - UniversalAdminTAB needs it and disables itself.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (compare(runningApiVersion(), REQUIRED_API) < 0) {
            getLogger().severe("UniversalAdminTAB needs UniversalAdmin 1.7 or newer - please update it (/admin update), "
                    + "then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        universalAdmin.get().extensions().register(this, new TabModule(universalAdmin.get()));
        universalAdmin.get().extensions().updatesFrom(this, "universaladmin-mc/universaladmin-tab", "universaladmin-tab-");
    }

    /**
     * The API version of the UniversalAdmin that runs - read at runtime, as
     * the compiler copies the constant's value into this class otherwise.
     */
    private static String runningApiVersion() {
        try {
            return (String) UniversalAdminProvider.class.getField("API_VERSION").get(null);
        } catch (ReflectiveOperationException e) {
            return "0";
        }
    }

    /** Compares "major.minor" version strings numerically. */
    static int compare(String version, String required) {
        String[] have = version.split("\\.");
        String[] need = required.split("\\.");
        for (int i = 0; i < Math.max(have.length, need.length); i++) {
            int a = i < have.length ? Integer.parseInt(have[i]) : 0;
            int b = i < need.length ? Integer.parseInt(need[i]) : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }
}
