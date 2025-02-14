package io.github.fisher2911.hmccosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
import io.github.fisher2911.hmccosmetics.dao.ArmorItemDAO;
import io.github.fisher2911.hmccosmetics.dao.CitizenDAO;
import io.github.fisher2911.hmccosmetics.dao.UserDAO;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.EntityIds;
import io.github.fisher2911.hmccosmetics.user.NPCUser;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Database {

    protected final HMCCosmetics plugin;
    private final Dao<UserDAO, UUID> userDao;
    private final Dao<CitizenDAO, Integer> citizenDao;
    private final Dao<ArmorItemDAO, String> armorItemDao;
    private final ConnectionSource dataSource;
    private final DatabaseType databaseType;

    public Database(
            final HMCCosmetics plugin,
            final ConnectionSource dataSource,
            final DatabaseType databaseType) throws SQLException {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.userDao = DaoManager.createDao(this.dataSource, UserDAO.class);
        this.citizenDao = DaoManager.createDao(this.dataSource, CitizenDAO.class);
        this.armorItemDao = DaoManager.createDao(this.dataSource, ArmorItemDAO.class);
        this.databaseType = databaseType;
    }

    public void load() {
        Threads.getInstance().execute(() -> new DatabaseConverter(this.plugin, this).convert());
    }

    protected void createTables() {
        try {
            TableUtils.createTableIfNotExists(this.dataSource, ArmorItemDAO.class);
            TableUtils.createTableIfNotExists(this.dataSource, UserDAO.class);
            TableUtils.createTableIfNotExists(this.dataSource, CitizenDAO.class);
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void loadUser(final Entity entity, final Consumer<User> onComplete) {
        final UUID uuid = entity.getUniqueId();
        final int armorStandId = getNextEntityId();
        final int balloonId = getNextEntityId();
        final int wardrobeViewerId = getNextEntityId();
        final Wardrobe wardrobe = this.createNewWardrobe(uuid);
        Threads.getInstance().execute(
                () -> {
                    try {
                        UserDAO user = this.userDao.queryForId(uuid);

                        if (user == null) {
                            user = this.userDao.createIfNotExists(new UserDAO(uuid));
                        }

                        final List<ArmorItemDAO> armorItems = this.armorItemDao.queryForEq("uuid", uuid.toString());

                        final User actualUser = user.toUser(
                                this.plugin.getCosmeticManager(),
                                new EntityIds(
                                        entity.getEntityId(),
                                        armorStandId,
                                        balloonId,
                                        wardrobeViewerId
                                ),
                                armorItems,
                                wardrobe
                        );
                        Bukkit.getScheduler().runTask(this.plugin,
                                () -> onComplete.accept(actualUser)
                        );

                    } catch (final SQLException exception) {
                        exception.printStackTrace();
                    }
                });
        onComplete.accept(new User(
                uuid,
                PlayerArmor.empty(),
                wardrobe,
                new EntityIds(entity.getEntityId(), armorStandId, balloonId, wardrobeViewerId)
        ));
    }

    public void loadNPCUser(final int id, final Entity entity, final Consumer<NPCUser> onComplete) {
        final int armorStandId = getNextEntityId();
        final int balloonId = getNextEntityId();
        final int wardrobeViewerId = getNextEntityId();
        Threads.getInstance().execute(
                () -> {
                    try {
                        CitizenDAO citizen = this.citizenDao.queryForId(id);

                        if (citizen == null) {
                            citizen = this.citizenDao.createIfNotExists(new CitizenDAO(id));
                        }

                        final List<ArmorItemDAO> armorItems = this.armorItemDao.queryForEq("uuid", String.valueOf(id));

                        final NPCUser actualUser = citizen.toUser(
                                this.plugin.getCosmeticManager(),
                                new EntityIds(
                                        entity.getEntityId(),
                                        armorStandId,
                                        balloonId,
                                        wardrobeViewerId
                                ),
                                armorItems
                        );

                        Bukkit.getScheduler().runTask(this.plugin,
                                () -> onComplete.accept(actualUser)
                        );

                    } catch (final SQLException exception) {
                        exception.printStackTrace();
                    }
                });

        onComplete.accept(new NPCUser(
                        id,
                        PlayerArmor.empty(),
                        new EntityIds(entity.getEntityId(), armorStandId, balloonId, wardrobeViewerId)
                )
        );
    }

    public void saveUser(final User user) {
        try {
            final UserDAO userDAO = new UserDAO(user.getId());
            this.userDao.createOrUpdate(userDAO);

            final String uuid = user.getId().toString();
            for (final ArmorItem armorItem : user.getPlayerArmor().getArmorItems()) {
                final ArmorItemDAO dao = ArmorItemDAO.fromArmorItem(armorItem);
                dao.setUuid(uuid);
                this.armorItemDao.createOrUpdate(dao);
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void saveNPCUser(final NPCUser user) {
        try {
            final CitizenDAO citizenDAO = new CitizenDAO(user.getId());
            this.citizenDao.createOrUpdate(citizenDAO);

            final String id = user.getId().toString();
            for (final ArmorItem armorItem : user.getPlayerArmor().getArmorItems()) {
                final ArmorItemDAO dao = ArmorItemDAO.fromArmorItem(armorItem);
                dao.setUuid(id);
                this.armorItemDao.createOrUpdate(dao);
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void saveAll() {
        for (final User user : this.plugin.getUserManager().getAll()) {
            this.saveUser(user);
        }
    }

    public void close() {
        try {
            this.dataSource.close();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    protected ConnectionSource getDataSource() {
        return dataSource;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public Dao<UserDAO, UUID> getUserDao() {
        return userDao;
    }

    public Dao<ArmorItemDAO, String> getArmorItemDao() {
        return armorItemDao;
    }

    public Wardrobe createNewWardrobe(final UUID ownerUUID) {
        return new Wardrobe(
                this.plugin,
                UUID.randomUUID(),
                ownerUUID,
                PlayerArmor.empty(),
                new EntityIds(
                        getNextEntityId(),
                        getNextEntityId(),
                        getNextEntityId(),
                        getNextEntityId()
                ),
                false
        );
    }

    public static int getNextEntityId() {
        return SpigotReflectionUtil.generateEntityId();
    }
}
