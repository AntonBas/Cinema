package ua.lviv.bas.cinema.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.util.Arrays;

public class V22__CleanupProdTestAccounts extends BaseJavaMigration {

    private static final String[] TEST_ACCOUNT_EMAILS = {
            "admin@test.com", "cashier@test.com", "manager@test.com", "user@test.com"
    };

    @Override
    public void migrate(Context context) throws Exception {
        if (!isProdProfileActive()) {
            return;
        }

        String sql = "DELETE FROM users WHERE email = ANY (?)";
        try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
            statement.setArray(1, context.getConnection().createArrayOf("varchar", TEST_ACCOUNT_EMAILS));
            statement.executeUpdate();
        }
    }

    private boolean isProdProfileActive() {
        String activeProfiles = System.getenv("SPRING_PROFILES_ACTIVE");
        return activeProfiles != null && Arrays.asList(activeProfiles.split(",")).contains("prod");
    }
}
