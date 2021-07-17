package de.blu.itemstacksaver.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@AllArgsConstructor
public final class ItemStackSaveEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private String key;
  private ItemStack[] itemStacks;

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
