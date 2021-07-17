package de.blu.itemstacksaver.util;

import com.google.inject.Singleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Singleton
public final class ItemSerialization {

  /**
   * A method to serialize an {@link ItemStack} array to Base64 String.
   *
   * @param items to turn into a Base64 String.
   * @return Base64 string of the items.
   */
  public static String toBase64(ItemStack[] items) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

      // Write the size of the array
      dataOutput.writeInt(items.length);

      // Save every element in the list
      for (int i = 0; i < items.length; i++) {
        dataOutput.writeObject(items[i]);
      }

      // Serialize that array
      dataOutput.close();
      return Base64Coder.encodeLines(outputStream.toByteArray());
    } catch (Exception e) {
      new IllegalStateException("Unable to save item stacks.", e).printStackTrace();
    }

    return "";
  }

  /**
   * Gets an array of ItemStacks from Base64 string.
   *
   * @param data Base64 string to convert to ItemStack array.
   * @return ItemStack array created from the Base64 string.
   */
  public static ItemStack[] fromBase64(String data) {
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      ItemStack[] items = new ItemStack[dataInput.readInt()];

      // Read the serialized array
      for (int i = 0; i < items.length; i++) {
        items[i] = (ItemStack) dataInput.readObject();
      }

      dataInput.close();
      return items;
    } catch (ClassNotFoundException e) {
      new IOException("Unable to decode class type.", e).printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new ItemStack[0];
  }
}
