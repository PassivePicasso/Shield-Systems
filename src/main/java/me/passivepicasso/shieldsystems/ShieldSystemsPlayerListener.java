package me.passivepicasso.shieldsystems;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handle events for all Player related events
 * 
 * @author PassivePicasso
 */
public class ShieldSystemsPlayerListener extends PlayerListener {
	private final ShieldSystems	plugin;

	HashMap<Player, Boolean>		constructingForcefield	= new HashMap<Player, Boolean>();

	public ShieldSystemsPlayerListener( ShieldSystems instance ) {
		this.plugin = instance;
	}

	public void addPlayer( Player player ) {
		this.constructingForcefield.put(player, false);
	}

	public boolean isPlayerConstructing( Player player ) {
		return constructingForcefield.get(player);
	}

	@Override
	public void onPlayerInteract( PlayerInteractEvent event ) {
		final ArrayList<DomeEmitter> domeEmitters = new ArrayList<DomeEmitter>();
		domeEmitters.addAll(plugin.blockListener.getEmitters());
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			Player player = event.getPlayer();
			ItemStack item = event.getItem();

			if (item.getType().equals(Material.STICK) && block.getType().equals(Material.DIRT)) {
				if (!this.constructingForcefield.get(player)) {
					this.constructingForcefield.put(player, true);
					player.sendMessage("Emitter Construction has begun.");
				} else {
					this.constructingForcefield.put(player, false);
					player.sendMessage("Emitter Construction has ended.");
				}
			}

			if (item.getType().equals(Material.STICK) && this.constructingForcefield.get(player)) {
				Sign sign = block.getRelative(1, 0, 0).getState() instanceof Sign ? (Sign) block.getRelative(1, 0, 0).getState()
						: block.getRelative(-1, 0, 0).getState() instanceof Sign ? (Sign) block.getRelative(-1, 0, 0).getState()
								: block.getRelative(0, 0, 1).getState() instanceof Sign ? (Sign) block.getRelative(0, 0, 1).getState()
										: block.getRelative(0, 0, -1).getState() instanceof Sign ? (Sign) block.getRelative(0, 0, -1).getState() : null;

				if (sign != null) {
					DomeEmitter emitter = new DomeEmitter(block, sign);
					if (domeEmitters.contains(emitter)) {
						emitter = domeEmitters.get(domeEmitters.indexOf(emitter));
						if (emitter.isActive()) {
							emitter.pulse();
						}
					}
				}
			}

			if (this.constructingForcefield.get(player)) {
				if (item.getType().equals(Material.WOOD_SWORD) && block.getType().equals(Material.SAND)) {
					Sign sign = block.getRelative(1, 0, 0).getState() instanceof Sign ? (Sign) block.getRelative(1, 0, 0).getState()
							: block.getRelative(-1, 0, 0).getState() instanceof Sign ? (Sign) block.getRelative(-1, 0, 0).getState()
									: block.getRelative(0, 0, 1).getState() instanceof Sign ? (Sign) block.getRelative(0, 0, 1).getState()
											: block.getRelative(0, 0, -1).getState() instanceof Sign ? (Sign) block.getRelative(0, 0, -1).getState() : null;

					if (sign != null) {
						DomeEmitter emitter = new DomeEmitter(block, sign);
						if (domeEmitters.contains(emitter)) {
							emitter = domeEmitters.get(domeEmitters.indexOf(emitter));
							if (emitter.isActive()) {
								emitter.deactivate();
							} else {
								emitter.activate();
							}
						} else {
							if (emitter.isActive()) {
								emitter.deactivate();
							} else {
								emitter.activate();
							}
							plugin.blockListener.addEmitter(emitter);
						}

					}
				}
			}
		}
	}

	@Override
	public void onPlayerJoin( PlayerEvent event ) {
		Player player = event.getPlayer();
		this.plugin.connectPlayer(player);
	}

	@Override
	public void onPlayerMove( PlayerMoveEvent event ) {
		Block b = event.getPlayer().getWorld().getBlockAt(event.getTo());
		ArrayList<DomeEmitter> emitters = new ArrayList<DomeEmitter>();
		emitters.addAll(plugin.blockListener.getEmitters());
		for (DomeEmitter de : emitters) {
			ArrayList<Block> fields = new ArrayList<Block>();
			fields.addAll(de.getLocalBlocks(b));
			if (fields.size() > 0) {
				double deltaZ = event.getFrom().getZ() - event.getTo().getZ();
				double deltaX = event.getFrom().getX() - event.getTo().getX();
				double deltaY = event.getFrom().getY() - event.getTo().getY();
				deltaY *= 2;
				deltaX *= 2;
				deltaZ *= 2;
				Player p = event.getPlayer();
				Location l = new Location(p.getWorld(), deltaX, event.getFrom().getX() + event.getFrom().getY() + deltaY, event.getFrom().getZ() + deltaZ);
				p.setVelocity(l.toVector());
				// p.teleport(l);
				// Vector v = event.getPlayer().getVelocity();
				// v.multiply(-10);
				// event.getPlayer().setVelocity(v);
			}
		}
	}

	@Override
	public void onPlayerQuit( PlayerEvent event ) {
		Player player = event.getPlayer();
		this.plugin.disconnectPlayer(player);
	}

	public void removePlayer( Player player ) {
		this.constructingForcefield.remove(player);
	}

}
