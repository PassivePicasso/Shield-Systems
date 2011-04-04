package me.passivepicasso.shieldsystems;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListener;

public class ShieldSystemsServerListener extends ServerListener {

    private final ShieldSystems plugin;

    public ShieldSystemsServerListener( ShieldSystems plugin ) {
        super();
        this.plugin = plugin;
    }

    @Override
    public void onPluginDisable( PluginDisableEvent event ) {
        for (ShieldProjector sp : plugin.playerListener.getProjectors().values()) {
            sp.deactivateShield();
        }
    }

    @Override
    public void onPluginEnable( PluginEnableEvent event ) {

    }

    @Override
    public void onServerCommand( ServerCommandEvent event ) {
        // TODO Auto-generated method stub
        super.onServerCommand(event);
    }

}
