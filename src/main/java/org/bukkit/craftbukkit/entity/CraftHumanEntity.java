
package org.bukkit.craftbukkit.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.EntityHuman;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionRemovedExecutor;

public class CraftHumanEntity extends CraftLivingEntity implements HumanEntity {
    private CraftInventoryPlayer inventory;
    private final List<PermissionAttachment> attachments = new LinkedList<PermissionAttachment>();
    private final Map<String, Boolean> permissions = new HashMap<String, Boolean>();
    private boolean dirtyPermissions = true;

    public CraftHumanEntity(final CraftServer server, final EntityHuman entity) {
        super(server, entity);
        this.inventory = new CraftInventoryPlayer(entity.inventory);
    }

    public String getName() {
        return getHandle().name;
    }

    @Override
    public EntityHuman getHandle() {
        return (EntityHuman) entity;
    }

    public void setHandle(final EntityHuman entity) {
        super.setHandle((EntityHuman) entity);
        this.entity = entity;
        this.inventory = new CraftInventoryPlayer(entity.inventory);
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    public ItemStack getItemInHand() {
        return getInventory().getItemInHand();
    }

    public void setItemInHand(ItemStack item) {
        getInventory().setItemInHand(item);
    }

    @Override
    public String toString() {
        return "CraftHumanEntity{" + "id=" + getEntityId() + "name=" + getName() + '}';
    }

    public boolean isSleeping() {
        return getHandle().sleeping;
    }

    public int getSleepTicks() {
        return getHandle().sleepTicks;
    }

    public boolean isPermissionSet(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Permission name cannot be null");
        }

        calculatePermissions();

        return permissions.containsKey(name.toLowerCase());
    }

    public boolean isPermissionSet(Permission perm) {
        if (perm == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        return isPermissionSet(perm.getName());
    }

    public boolean hasPermission(String inName) {
        if (inName == null) {
            throw new IllegalArgumentException("Permission name cannot be null");
        }

        calculatePermissions();

        String name = inName.toLowerCase();

        if (!isPermissionSet(name)) {
            return permissions.get(name);
        } else {
            Permission perm = getServer().getPluginManager().getPermission(name);

            if (perm != null) {
                return perm.getDefault();
            } else {
                return false;
            }
        }
    }

    public boolean hasPermission(Permission perm) {
        if (perm == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        calculatePermissions();

        String name = perm.getName().toLowerCase();

        if (!isPermissionSet(name)) {
            return permissions.get(name);
        } else if (perm != null) {
            return perm.getDefault();
        } else {
            return false;
        }
    }

    public PermissionAttachment addAttachment(String name, boolean value) {
        if (name == null) {
            throw new IllegalArgumentException("Permission name cannot be null");
        }

        PermissionAttachment result = addAttachment();
        result.setPermission(name, value);

        recalculatePermissions();

        return result;
    }

    public PermissionAttachment addAttachment() {
        PermissionAttachment result = new PermissionAttachment(this);

        attachments.add(result);
        recalculatePermissions();

        return result;
    }

    public void removeAttachment(PermissionAttachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment cannot be null");
        }

        if (attachments.contains(attachment)) {
            attachments.remove(attachment);
            PermissionRemovedExecutor ex = attachment.getRemovalCallback();
            
            if (ex != null) {
                ex.attachmentRemoved(attachment);
            }
        } else {
            throw new IllegalArgumentException("Given attachment is not part of Permissible object " + this);
        }
    }

    public void recalculatePermissions() {
        dirtyPermissions = true;
    }

    private synchronized void calculatePermissions() {
        if (dirtyPermissions) {
            permissions.clear();

            for (PermissionAttachment attachment : attachments) {
                permissions.putAll(attachment.getPermissions());
            }

            dirtyPermissions = false;
        }
    }
}
