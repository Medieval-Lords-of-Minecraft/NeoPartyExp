package me.neoblade298.neopartyexp;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.Party;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;

import io.lumine.mythic.bukkit.events.MythicMobLootDropEvent;
import io.lumine.mythic.core.drops.Drop;
import io.lumine.mythic.core.drops.droppables.SkillAPIDrop;
import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neopartyexp.commands.CmdPartyFinder;
import net.md_5.bungee.api.ChatColor;

public class NeoPartyExp extends JavaPlugin implements org.bukkit.event.Listener {
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoPartyExp Enabled");
		getServer().getPluginManager().registerEvents(this, this);
		SubcommandManager mngr = new SubcommandManager("partyfinder", null, ChatColor.RED, this);
		mngr.register(new CmdPartyFinder("", "Look for suitable players to party with", null, SubcommandRunner.PLAYER_ONLY));
	}
	
	public void onDisable() {
	    org.bukkit.Bukkit.getServer().getLogger().info("NeoPartyExp Disabled");
	    super.onDisable();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobLoot(MythicMobLootDropEvent e) {
		if (!(e.getKiller() instanceof Player)) return;
		Player p = (Player) e.getKiller();
		// Get party
		String pName = Parties.getApi().getPartyPlayer(p.getUniqueId()).getPartyName();
		Party party = Parties.getApi().getParty(pName);
		
		if (party != null) {
			for (Drop drop : e.getDrops().getDrops()) {
				if (drop instanceof SkillAPIDrop) {
					// Get exp amount
					SkillAPIDrop sdrop = (SkillAPIDrop) drop;
					double exp = sdrop.getAmount();
					e.getDrops().getLootTableIntangible().remove(SkillAPIDrop.class);
					
					
					// Figure out how many people we need to split it with
					HashSet<Player> getsExp = new HashSet<Player>();
					getsExp.add(p);
					if (party.getOnlineMembers(false).size() > 1) {
						for (Entity ent : p.getNearbyEntities(60, 60, 60)) {
							if (ent instanceof Player) {
								Player receive = (Player) ent;
								getsExp.add(receive);
							}
						}
					}
					
					// Give exp
					double amount = (exp * 1.5) / getsExp.size();
					for (Player receive : getsExp) {
						SkillAPI.getPlayerData(receive).giveExp(amount, ExpSource.MOB);
					}
				}
			}
		}
	}
	
}
