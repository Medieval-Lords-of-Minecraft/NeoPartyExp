package me.neoblade298.neopartyexp.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.Party;
import com.sucy.skill.SkillAPI;

import me.neoblade298.neocore.commands.CommandArguments;
import me.neoblade298.neocore.commands.Subcommand;
import me.neoblade298.neocore.commands.SubcommandRunner;
import me.neoblade298.neocore.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CmdPartyFinder implements Subcommand {
	private static final CommandArguments args = new CommandArguments();

	@Override
	public String getPermission() {
		return null;
	}

	@Override
	public SubcommandRunner getRunner() {
		return SubcommandRunner.PLAYER_ONLY;
	}

	@Override
	public String getKey() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Looks for suitable players to party with";
	}

	@Override
	public void run(CommandSender s, String[] args) {
		ComponentBuilder msg = null;
		Player p = (Player) s;
		if (SkillAPI.getPlayerData(p).getMainClass() == null) Util.msg(s, "&cFirst you need a class! /warp classes");
		int sLevel = SkillAPI.getPlayerData(p).getMainClass().getLevel();
		boolean senderInParty = Parties.getApi().isPlayerInParty(p.getUniqueId());
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (p == target || SkillAPI.getPlayerData(target).getMainClass() == null) continue;
			
			int tLevel = SkillAPI.getPlayerData(target).getMainClass().getLevel();
			int diff = Math.abs(sLevel - tLevel);
			if (diff > 10) continue;
			
			String clss = SkillAPI.getPlayerData(target).getMainClass().getData().getName();
			Party targetParty = Parties.getApi().getPartyOfPlayer(target.getUniqueId());
			boolean targetInParty = targetParty != null;
			String text = "§7- §fLv " + tLevel + " §6" + target.getName() + " §7[§e" + clss + "§7] | §cParty: " + (targetInParty ? targetParty.getName() : "N/A");
			text += " §a[Click me!]";
			if (msg == null) {
				msg = new ComponentBuilder(text);
			}
			else {
				msg.append(new TextComponent("\n" + text));
			}
			String click = createClickCommand(target, targetParty, senderInParty, targetInParty);
			if (click != null) msg.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, click));
			msg.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createHoverText(senderInParty, targetInParty)));
		}
		
		if (msg == null) {
			Util.msg(s, "&7No players found within 10 levels of you! Try again later.");
		}
		else {
			Util.msg(s, "&fClick any of the below players!");
			s.spigot().sendMessage(msg.create());
		}
	}

	@Override
	public CommandArguments getArgs() {
		return args;
	}
	
	private Text createHoverText(boolean senderInParty, boolean targetInParty) {
		if (senderInParty && targetInParty) {
			return new Text("§cYou're in a party! Leave your party to request to join them!");
		}
		else if (senderInParty && !targetInParty) {
			return new Text("§aClick to invite them to your party!");
		}
		else if (!senderInParty && targetInParty) {
			return new Text("§aClick to request to join their party!");
		}
		else {
			return new Text("§cYou're not in a party! First create a party with /party create [name]!");
		}
	}
	
	private String createClickCommand(Player target, Party targetParty, boolean senderInParty, boolean targetInParty) {
		if (senderInParty && targetInParty) {
			return null;
		}
		else if (senderInParty && !targetInParty) {
			return "/party invite " + target.getName();
		}
		else if (!senderInParty && targetInParty) {
			return "/party ask " + targetParty.getName();
		}
		else {
			return null;
		}
	}
}
