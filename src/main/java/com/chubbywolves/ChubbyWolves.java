package com.chubbywolves;

import com.mojang.math.Transformation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chubby Wolves
 * -------------
 * Tamed wolves render bigger/rounder (see WolfRendererMixin, client-side) and
 * get a small floating hat that rides above their head (handled here, server-side).
 *
 * The hat is a plain ItemDisplay entity, not a custom render layer. That's a
 * deliberate choice: Minecraft's entity-renderer internals were significantly
 * reworked around the time 1.21.11 shipped, and display entities are a stable,
 * well-documented, version-safe way to stick a floating item on something.
 */
public class ChubbyWolves implements ModInitializer {
	public static final String MOD_ID = "chubbywolves";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** The "hat". Change this to any item you like — a carved pumpkin is just a fun default. */
	private static ItemStack hatItem() {
		return new ItemStack(Items.CARVED_PUMPKIN);
	}

	/** How big the hat renders relative to a normal held/dropped item. */
	private static final float HAT_SCALE = 0.55f;

	/** How far above the wolf's head (in blocks) the hat floats. */
	private static final double HAT_HEIGHT_OFFSET = 0.15;

	// Tracks wolf UUID -> hat entity UUID. In-memory only: hats respawn automatically
	// after a server restart, so nothing needs to be saved to disk.
	private static final Map<UUID, UUID> WOLF_HATS = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Chubby Wolves loaded — tamed wolves are about to get chonky and festive.");
		ServerTickEvents.END_WORLD_TICK.register(ChubbyWolves::onWorldTick);
	}

	private static void onWorldTick(ServerLevel level) {
		// Clean up hats whose wolf is gone or no longer tamed.
		WOLF_HATS.entrySet().removeIf(entry -> {
			Entity wolfEntity = level.getEntity(entry.getKey());
			boolean stillValid = wolfEntity instanceof Wolf wolf && wolf.isAlive() && wolf.isTame();
			if (!stillValid) {
				Entity hat = level.getEntity(entry.getValue());
				if (hat != null) {
					hat.discard();
				}
			}
			return !stillValid;
		});

		// Make sure every tamed wolf has a hat, and keep it glued to their head.
		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof Wolf wolf) || !wolf.isTame() || !wolf.isAlive()) {
				continue;
			}

			UUID hatId = WOLF_HATS.get(wolf.getUUID());
			Entity hatEntity = hatId != null ? level.getEntity(hatId) : null;

			if (!(hatEntity instanceof Display.ItemDisplay hat) || !hat.isAlive()) {
				hat = spawnHat(level, wolf);
				WOLF_HATS.put(wolf.getUUID(), hat.getUUID());
			}

			hat.setPos(wolf.getX(), wolf.getY() + wolf.getBbHeight() + HAT_HEIGHT_OFFSET, wolf.getZ());
			hat.setYRot(wolf.getYRot());
			hat.setYHeadRot(wolf.getYHeadRot());
			hat.yRotO = hat.getYRot();
		}
	}

	private static Display.ItemDisplay spawnHat(ServerLevel level, Wolf wolf) {
		Display.ItemDisplay hat = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
		hat.setItemStack(hatItem());
		hat.setNoGravity(true);
		hat.setInvulnerable(true);
		hat.setTransformation(new Transformation(
				new Vector3f(0f, 0f, 0f),
				new Quaternionf(),
				new Vector3f(HAT_SCALE, HAT_SCALE, HAT_SCALE),
				new Quaternionf()
		));
		hat.setPos(wolf.getX(), wolf.getY() + wolf.getBbHeight() + HAT_HEIGHT_OFFSET, wolf.getZ());
		level.addFreshEntity(hat);
		return hat;
	}
}
