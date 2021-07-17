package de.blu.itemstacksaver;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.blu.database.DatabaseAPI;
import de.blu.database.data.TableColumn;
import de.blu.database.data.TableColumnType;
import de.blu.database.storage.cassandra.CassandraConnection;
import de.blu.database.storage.redis.RedisConnection;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public final class ItemStackSaverPlugin extends JavaPlugin {

  @Inject private CassandraConnection cassandraConnection;

  @Override
  public void onEnable() {
    Injector injector =
        Guice.createInjector(
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(JavaPlugin.class).toInstance(ItemStackSaverPlugin.this);
                bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
                bind(RedisConnection.class)
                    .toInstance(DatabaseAPI.getInstance().getRedisConnection());
                bind(CassandraConnection.class)
                    .toInstance(DatabaseAPI.getInstance().getCassandraConnection());
              }
            });

    injector.injectMembers(this);
    this.init(injector);
  }

  private void init(Injector injector) {
    injector.getInstance(ItemStackSaver.class);

    // Create Database Table
    CassandraConnection cassandraConnection = this.cassandraConnection;
    if (!DatabaseAPI.getInstance().getCassandraConfig().isEnabled()) {
      return;
    }

    List<TableColumn> columns =
        Arrays.asList(
            new TableColumn(TableColumnType.STRING, "key", true),
            new TableColumn(TableColumnType.STRING, "items", false));
    cassandraConnection.createTableIfNotExist("itemstacks", columns);
  }
}
