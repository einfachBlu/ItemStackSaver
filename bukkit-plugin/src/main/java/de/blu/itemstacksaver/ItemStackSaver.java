package de.blu.itemstacksaver;

import com.google.inject.Inject;
import de.blu.database.DatabaseAPI;
import de.blu.database.storage.cassandra.CassandraConnection;
import de.blu.database.storage.redis.RedisConnection;
import de.blu.itemstacksaver.event.ItemStackSaveEvent;
import de.blu.itemstacksaver.util.ItemSerialization;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Singleton
public final class ItemStackSaver {

  @Getter private static ItemStackSaver instance;

  @Inject private ExecutorService executorService;
  private RedisConnection redisConnection = DatabaseAPI.getInstance().getRedisConnection();
  private CassandraConnection cassandraConnection = DatabaseAPI.getInstance().getCassandraConnection();

  public ItemStackSaver() {
    ItemStackSaver.instance = this;
  }

  public ItemStack[] getItemStacks(String key) {
    if (!DatabaseAPI.getInstance().getCassandraConfig().isEnabled()) {
      return null;
    }

    String itemStackString = "";
    if (DatabaseAPI.getInstance().getRedisConfig().isEnabled()
        && this.redisConnection.contains("itemstacksaver." + key)) {
      itemStackString = this.redisConnection.get("itemstacksaver." + key);
    } else {
      Map<Integer, Map<String, Object>> data =
          this.cassandraConnection.selectAll("itemstacks", "key", key);
      if (data.size() == 0) {
        return null;
      }

      Collection<Map<String, Object>> rows = data.values();
      Map<String, Object> row = rows.iterator().next();
      itemStackString = (String) row.get("items");
    }

    return ItemSerialization.fromBase64(itemStackString);
  }

  public void getItemStacksAsync(String key, Consumer<ItemStack[]> itemStacksCallback) {
    this.executorService.submit(
        () -> {
          itemStacksCallback.accept(this.getItemStacks(key));
        });
  }

  public void setItemStacks(String key, ItemStack[] itemStacks) {
    if (!DatabaseAPI.getInstance().getCassandraConfig().isEnabled()) {
      return;
    }

    ItemStackSaveEvent event = new ItemStackSaveEvent(key, itemStacks);
    Bukkit.getServer().getPluginManager().callEvent(event);
    itemStacks = event.getItemStacks();

    String itemStackString = ItemSerialization.toBase64(itemStacks);

    // Cache in Redis if enabled
    if (DatabaseAPI.getInstance().getRedisConfig().isEnabled()) {
      this.redisConnection.set(
          "itemstacksaver." + key, itemStackString, (int) TimeUnit.MINUTES.toSeconds(10));
    }

    // Save in Cassandra
    if (this.cassandraConnection.selectAll("itemstacks", "key", key).size() == 0) {
      // Insert
      cassandraConnection.insertInto(
          "itemstacks", new String[] {"key", "items"}, new Object[] {key, itemStackString});
    } else {
      // Update
      cassandraConnection.update(
          "itemstacks", new String[] {"items"}, new Object[] {itemStackString}, "key", key);
    }
  }

  public void setItemStacksAsync(String key, ItemStack[] itemStacks) {
    this.executorService.submit(
        () -> {
          this.setItemStacks(key, itemStacks);
        });
  }
}
