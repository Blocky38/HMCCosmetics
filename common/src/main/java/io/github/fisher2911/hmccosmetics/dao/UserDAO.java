package io.github.fisher2911.hmccosmetics.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.EntityIds;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@DatabaseTable(tableName = "user")
public class UserDAO {

    @DatabaseField(id = true)
    private UUID uuid;

    public UserDAO() {
    }

    public UserDAO(final UUID uuid) {
        this.uuid = uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    @Nullable
    public User toUser(
            final CosmeticManager cosmeticManager,
            final EntityIds entityIds,
            final List<ArmorItemDAO> armorItems,
            final Wardrobe wardrobe
    ) {
        final PlayerArmor playerArmor = PlayerArmor.empty();

        for (final ArmorItemDAO armorItemDao : armorItems) {
            final ArmorItem armorItem = armorItemDao.toArmorItem(cosmeticManager);
            if (armorItem == null) {
                continue;
            }
            playerArmor.setItem(armorItem);
        }

        return new User(this.uuid, playerArmor, wardrobe, entityIds);
    }

    @Override
    public String toString() {
        return "UserDAO{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserDAO userDAO = (UserDAO) o;
        return Objects.equals(uuid, userDAO.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
