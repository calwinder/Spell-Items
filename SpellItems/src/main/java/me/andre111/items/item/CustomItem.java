package me.andre111.items.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.andre111.items.CooldownManager;
import me.andre111.items.ManaManager;
import me.andre111.items.SpellItems;
import me.andre111.items.StatManager;
import me.andre111.items.iface.IUpCounter;
import me.andre111.items.utils.Attributes;
import me.andre111.items.utils.Attributes.Attribute;
import me.andre111.items.utils.Attributes.AttributeType;
import me.andre111.items.utils.EntityHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItem implements IUpCounter {
	private String internalName;

	private Material material;
	private int damage;
	private String name;
	private ArrayList<String> lore = new ArrayList<String>();
	private boolean use;
	private boolean ignoreDamage;
	private boolean allowPlace;

	private String bookauthor;
	private List<String> bookpages = new ArrayList<String>();

	private boolean hasCounter;
	private int counterMax;
	private int counterStep;
	private boolean counterOverridable;
	private boolean counterInterruptMove;
	private boolean counterInterruptDamage;
	private boolean counterInterruptItem;

	private int cooldownR;
	private int manaCostR;
	
	private int cooldownL;
	private int manaCostL;

	private int cooldownEat;
	private int manaCostEat;

	
	private String luaR;
	private String luaL;
	private String luaEat;

	//actions:
	//0 = leftclick
	//1 = rigthclick
	//2 = eat
	public void cast(int actions, Player player, Location loc, Block block, Entity target, boolean isCounter) {
		//TODO - isHasCounter Counter und Effekte wieder einbeuen
		if(isHasCounter() && !isCounter) {
			if(player!=null && !cooldownManaCheck(actions, player, true))
				StatManager.setCounter(player.getUniqueId(), this, player.getUniqueId()+"::"+actions);
		} else {
			String luaTemp = luaR;
			if(actions==0) luaTemp = luaL;
			if(actions==2) luaTemp = luaEat;

			if(!luaTemp.equals("")) {
				if(player!=null && cooldownManaCheck(actions, player, false)) return;
				putOnCoolDown(actions, player);

				if(!SpellItems.luacontroller.castFunction(luaTemp, player, target, block, loc, -1, -1)) {
					resetCoolDown(actions, player);
				}
			}
		}
	}

	//is the item currently on cooldown
	private boolean cooldownManaCheck(int actions, Player player, boolean onlyCheck) {
		//cooldown
		int cd = CooldownManager.getCustomCooldown(player.getUniqueId(), getCooldownName(actions));
		if(cd>0) {
			//player.sendMessage(ConfigManager.getLanguage().getString("string_wait", "You have to wait -0- Seconds!").replace("-0-", ""+cd));
			player.sendMessage("You have to wait -0- Seconds!".replace("-0-", ""+cd));

			return true;
		}

		//mana
		int cost = getManaCostR();
		if(actions==0) cost = getManaCostL();
		if(actions==2) cost = getManaCostEat();

		if(cost>0) {
			if(ManaManager.getMana(player.getUniqueId())<cost) {
				//player.sendMessage(ConfigManager.getLanguage().getString("string_needmana", "You need -0- Mana!").replace("-0-", ""+cost));
				player.sendMessage("You need -0- Mana!".replace("-0-", ""+cost));
				return true;
			}

			if(!onlyCheck) ManaManager.substractMana(player.getUniqueId(), cost);
		}

		//substract items
		if(isUse() && !onlyCheck) {
			ItemStack item = player.getItemInHand();
			if(item.getAmount()-1==0) 
				item.setType(Material.AIR);
			else
				item.setAmount(item.getAmount()-1);

			player.setItemInHand(item);
		}

		//everything ok
		return false;
	}

	private void putOnCoolDown(int action, Player player) {
		int time = cooldownR;
		if(action==0) time = cooldownL;
		if(action==2) time = cooldownEat;

		if(time>0) CooldownManager.setCustomCooldown(player.getUniqueId(), getCooldownName(action), time);
	}

	private String getCooldownName(int actions) {
		return "citem_"+name+"_"+actions;
	}

	public void resetCoolDown(int action, Player player) {
		CooldownManager.resetCustomCooldown(player.getUniqueId(), getCooldownName(action));
	}

	public ItemStack getItemStack() {
		ItemStack it = new ItemStack(material, 1, (short) damage);
		ItemMeta im = it.getItemMeta();

		im.setDisplayName(name);
		im.setLore(lore);

		if(im instanceof BookMeta) {
			BookMeta bm = (BookMeta) im;

			if(!bookauthor.equals("")) {
				bm.setAuthor(bookauthor);
			}
			bm.setPages(bookpages);
		}

		it.setItemMeta(im);

		Attribute att = Attribute.newBuilder().uuid(SpellItems.itemUUID).name("si_customitem_"+getInternalName()).amount(0).type(AttributeType.GENERIC_ATTACK_DAMAGE).build();
		
		Attributes attributes = new Attributes(it);
		attributes.add(att);
		
		return attributes.getStack();
	}
	public boolean isThisItem(ItemStack it) {
		if(it.getType()!=material) return false;
		if(!ignoreDamage && it.getDurability()!=damage) return false;

		Attributes attributes = new Attributes(it);
		for(Attribute att : attributes.values()) {
			if(att.getUUID().equals(SpellItems.itemUUID)) {
				if(!att.getName().startsWith("si_customitem_")) return false;
				if(!att.getName().replace("si_customitem_", "").equals(getInternalName())) return false;
				
				return true;
			}
		}

		return false;
	}

	public String getInternalName() {
		return internalName;
	}
	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}
	public void setMaterial(Material mat) {
		this.material = mat;
	}
	public void setDamage(int damage) {
		this.damage = damage;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void addLore(String nlore) {
		lore.add(nlore);
	}
	public boolean isUse() {
		return use;
	}
	public void setUse(boolean use) {
		this.use = use;
	}
	public boolean isIgnoreDamage() {
		return ignoreDamage;
	}
	public void setIgnoreDamage(boolean ignoreDamage) {
		this.ignoreDamage = ignoreDamage;
	}
	public boolean isAllowPlace() {
		return allowPlace;
	}
	public void setAllowPlace(boolean allowPlace) {
		this.allowPlace = allowPlace;
	}
	public String getBookauthor() {
		return bookauthor;
	}
	public void setBookauthor(String bookauthor) {
		this.bookauthor = bookauthor;
	}
	public List<String> getBookpages() {
		return bookpages;
	}
	public void setBookpages(List<String> bookpages) {
		this.bookpages = bookpages;
	}
	public boolean isHasCounter() {
		return hasCounter;
	}
	public void setHasCounter(boolean hasCounter) {
		this.hasCounter = hasCounter;
	}
	public int getCounterMax() {
		return counterMax;
	}
	public void setCounterMax(int counterMax) {
		this.counterMax = counterMax;
	}
	public int getCounterStep() {
		return counterStep;
	}
	public void setCounterStep(int counterStep) {
		this.counterStep = counterStep;
	}
	public boolean isCounterOverridable() {
		return counterOverridable;
	}
	public void setCounterOverridable(boolean counterOverridable) {
		this.counterOverridable = counterOverridable;
	}
	public boolean isCounterInterruptMove() {
		return counterInterruptMove;
	}
	public void setCounterInterruptMove(boolean counterInterruptMove) {
		this.counterInterruptMove = counterInterruptMove;
	}
	public boolean isCounterInterruptDamage() {
		return counterInterruptDamage;
	}
	public void setCounterInterruptDamage(boolean counterInterruptDamage) {
		this.counterInterruptDamage = counterInterruptDamage;
	}
	public boolean isCounterInterruptItem() {
		return counterInterruptItem;
	}
	public void setCounterInterruptItem(boolean counterInterruptItem) {
		this.counterInterruptItem = counterInterruptItem;
	}
	public int getCooldownR() {
		return cooldownR;
	}
	public void setCooldownR(int cooldownR) {
		this.cooldownR = cooldownR;
	}
	public int getManaCostR() {
		return manaCostR;
	}
	public void setManaCostR(int manaCostR) {
		this.manaCostR = manaCostR;
	}
	public int getCooldownL() {
		return cooldownL;
	}
	public void setCooldownL(int cooldownL) {
		this.cooldownL = cooldownL;
	}
	public int getManaCostL() {
		return manaCostL;
	}
	public void setManaCostL(int manaCostL) {
		this.manaCostL = manaCostL;
	}
	public int getCooldownEat() {
		return cooldownEat;
	}
	public void setCooldownEat(int cooldownEat) {
		this.cooldownEat = cooldownEat;
	}
	public int getManaCostEat() {
		return manaCostEat;
	}
	public void setManaCostEat(int manaCostEat) {
		this.manaCostEat = manaCostEat;
	}

	public String getLuaR() {
		return luaR;
	}
	public void setLuaR(String luaR) {
		this.luaR = luaR;
	}
	public String getLuaL() {
		return luaL;
	}
	public void setLuaL(String luaL) {
		this.luaL = luaL;
	}
	public String getLuaEat() {
		return luaEat;
	}
	public void setLuaEat(String luaEat) {
		this.luaEat = luaEat;
	}

	//Upcounter methods and fields
	@Override
	public int countUPgetMax() {
		return counterMax;
	}
	@Override
	public int countUPperSecond() {
		return counterStep;
	}
	@Override
	public boolean countUPOverridable() {
		return counterOverridable;
	}
	@Override
	public boolean countUPinterruptMove() {
		return counterInterruptMove;
	}
	@Override
	public boolean countUPinterruptDamage() {
		return counterInterruptDamage;
	}
	@Override
	public boolean countUPinterruptItemChange() {
		return counterInterruptItem;
	}
	@Override
	public void countUPincrease(String vars) {
		String[] split = vars.split("::");

		Player player = EntityHandler.getPlayerFromUUID(UUID.fromString(split[0]));
		//int action = Integer.parseInt(split[1]);

		if(player!=null) {
			//TODO - reimplement effects for countUP or maybe change countUP to lua
			//createEffects(player.getLocation(), action, "CounterStep");
		}
	}
	@Override
	public void countUPinterrupt(String vars) {
		String[] split = vars.split("::");

		Player player = EntityHandler.getPlayerFromUUID(UUID.fromString(split[0]));
		//int action = Integer.parseInt(split[1]);

		if(player!=null) {
			//TODO - reimplement effects for countUP or maybe change countUP to lua
			//createEffects(player.getLocation(), action, "CounterInterrupt");
		}
	}
	@Override
	public void countUPfinish(String vars) {
		String[] split = vars.split("::");

		Player player = EntityHandler.getPlayerFromUUID(UUID.fromString(split[0]));
		int action = Integer.parseInt(split[1]);

		if(player!=null) {
			//TODO - reimplement effects for countUP or maybe change countUP to lua
			//createEffects(player.getLocation(), action, "CounterFinish");

			//castUse(action, player, null, null, null);

			cast(action, player, null, null, null, true);
		}
	}
}
