package org.mik.sellguiplus.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.model.PendingSignAction;
import org.mik.sellguiplus.model.SignInputSession;
import org.mik.sellguiplus.util.TextUtil;

public class SignInputManager {

    private final JavaPlugin plugin;
    private final Map<UUID, SignInputSession> sessions = new ConcurrentHashMap<>();
    private static final String PLACEHOLDER_LINE1 = "Type item name";
    private static final String PLACEHOLDER_LINE2 = "to search";

    public SignInputManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openSearch(Player player, int page) {
        Location location = findFreeLocation(player);
        if (location == null) {
            TextUtil.sendMessage(player, plugin, "messages.admin-search-failed");
            return;
        }
        Block block = location.getBlock();
        BlockData original = block.getBlockData();
        block.setType(Material.OAK_SIGN, false);
        Sign sign = (Sign) block.getState();
        sign.setLine(0, TextUtil.color("&7" + PLACEHOLDER_LINE1));
        sign.setLine(1, TextUtil.color("&7" + PLACEHOLDER_LINE2));
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update(false, false);

        SignInputSession session = new SignInputSession(location, original, new PendingSignAction(PendingSignAction.Type.SEARCH, page));
        sessions.put(player.getUniqueId(), session);
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean opened = openSignEditor(player, sign, location);
            if (!opened) {
                TextUtil.sendMessage(player, plugin, "messages.admin-search-failed");
            } else {
                TextUtil.sendMessage(player, plugin, "messages.admin-search-prompt");
            }
        });
    }

    public SignInputSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public void clear(UUID uuid) {
        SignInputSession session = sessions.remove(uuid);
        if (session == null) {
            return;
        }
        restore(session);
    }

    public void complete(UUID uuid) {
        SignInputSession session = sessions.remove(uuid);
        if (session == null) {
            return;
        }
        restore(session);
    }

    private void restore(SignInputSession session) {
        Block block = session.getLocation().getBlock();
        block.setBlockData(session.getOriginalData(), false);
    }

    private Location findFreeLocation(Player player) {
        Location base = player.getLocation();
        Location loc = base.clone();
        if (isReplaceable(loc.getBlock())) {
            return loc;
        }
        Location above = base.clone().add(0, 1, 0);
        if (isReplaceable(above.getBlock())) {
            return above;
        }
        Location aboveTwo = base.clone().add(0, 2, 0);
        if (isReplaceable(aboveTwo.getBlock())) {
            return aboveTwo;
        }
        return null;
    }

    private boolean isReplaceable(Block block) {
        return block.getType() == Material.AIR || block.isEmpty() || block.isLiquid();
    }

    public String extractInput(String[] lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String plain = org.bukkit.ChatColor.stripColor(trimmed).trim();
            if (plain.equalsIgnoreCase(PLACEHOLDER_LINE1) || plain.equalsIgnoreCase(PLACEHOLDER_LINE2)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(plain);
        }
        return builder.toString().trim();
    }

    private boolean openSignEditor(Player player, Sign sign, Location location) {
        try {
            java.lang.reflect.Method method = player.getClass().getMethod("openSign", Sign.class);
            method.invoke(player, sign);
            return true;
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Method method = player.getClass().getMethod("openSign", org.bukkit.block.Block.class);
            method.invoke(player, location.getBlock());
            return true;
        } catch (Exception ignored) {
        }
        return sendSignEditorPacket(player, location);
    }

    private boolean sendSignEditorPacket(Player player, Location location) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Object blockPos = createBlockPos(version, location);
            if (blockPos == null) {
                return false;
            }
            Object packet = createOpenSignPacket(version, blockPos);
            if (packet == null) {
                return false;
            }
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = getConnection(handle);
            if (connection == null) {
                return false;
            }
            java.lang.reflect.Method sendMethod = getSendPacketMethod(connection);
            if (sendMethod == null) {
                return false;
            }
            sendMethod.invoke(connection, packet);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Object createBlockPos(String version, Location location) {
        try {
            Class<?> blockPosClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
            return blockPosClass.getConstructor(int.class, int.class, int.class)
                .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } catch (Exception ignored) {
        }
        try {
            Class<?> blockPosClass = Class.forName("net.minecraft.core.BlockPosition");
            return blockPosClass.getConstructor(int.class, int.class, int.class)
                .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } catch (Exception ignored) {
        }
        return null;
    }

    private Object createOpenSignPacket(String version, Object blockPos) {
        try {
            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutOpenSignEditor");
            return packetClass.getConstructor(blockPos.getClass()).newInstance(blockPos);
        } catch (Exception ignored) {
        }
        String[] candidates = new String[] {
            "net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor",
            "net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket"
        };
        for (String name : candidates) {
            try {
                Class<?> packetClass = Class.forName(name);
                for (java.lang.reflect.Constructor<?> ctor : packetClass.getConstructors()) {
                    Class<?>[] params = ctor.getParameterTypes();
                    if (params.length == 1 && params[0].isAssignableFrom(blockPos.getClass())) {
                        return ctor.newInstance(blockPos);
                    }
                    if (params.length == 2 && params[0].isAssignableFrom(blockPos.getClass())) {
                        return ctor.newInstance(blockPos, true);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Object getConnection(Object handle) {
        for (java.lang.reflect.Field field : handle.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(handle);
                if (value == null) {
                    continue;
                }
                String name = value.getClass().getName();
                if (name.endsWith("PlayerConnection") || name.endsWith("ServerGamePacketListenerImpl")) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private java.lang.reflect.Method getSendPacketMethod(Object connection) {
        for (java.lang.reflect.Method method : connection.getClass().getMethods()) {
            if (method.getName().equals("sendPacket") && method.getParameterTypes().length == 1) {
                return method;
            }
            if (method.getName().equals("send") && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return null;
    }
}
