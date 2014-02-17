package me.andre111.items.item.spell;

import me.andre111.items.SpellItems;
import me.andre111.items.item.ItemSpell;
import me.andre111.items.item.SpellVariable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class ItemSetDamage extends ItemSpell {
	/*private int damage = 0;
	
	@Override
	public void setCastVar(int id, double var) {
		if(id==0) damage = (int) Math.round(var);
	}
	
	@Override
	public void setCastVar(int id, SpellVariable var) {
		if(id==0) damage = var.getAsInt();
	}
	
	@Override
	public boolean cast(Player player, Location loc, Player target, Block block) {
		if(player==null) return false;
		
		ItemStack it = player.getItemInHand();
		it.setDurability((short) damage);
		player.setItemInHand(it);
		
		return true;
	}*/
	
	@Override
	public Varargs invoke(Varargs args) {
		if(args.narg()>=2) {
			LuaValue playerN = args.arg(1);
			LuaValue damageN = args.arg(1);
			
			if(playerN.isstring() && damageN.isnumber()) {
				Player player = Bukkit.getPlayerExact(playerN.toString());
				int damage = damageN.toint();
				
				if(player!=null) {
					ItemStack it = player.getItemInHand();
					it.setDurability((short) damage);
					player.setItemInHand(it);
					
					return RETURN_TRUE;
				}
			}
		} else {
			SpellItems.log("Missing Argument for "+getClass().getCanonicalName());
		}
		
		return RETURN_FALSE;
	}
}
