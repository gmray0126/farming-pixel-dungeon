/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

public class QuickSlot {

	/**
	 * Slots contain objects which are also in a player's inventory. The one exception to this is when quantity is 0,
	 * which can happen for a stackable item that has been 'used up', these are referred to as placeholders.
	 */

	// Twelve persistent slots; the toolbar displays one responsive page at a time.
	public static final int SIZE = 12;
    private int page;
    public static int pageSize(float width){return width>170?6:width>152?5:4;}
    public int pageCount(int size){return (SIZE+size-1)/size;}
    public int page(int size){page=Math.max(0,Math.min(page,pageCount(size)-1));return page;}
    public int firstSlot(int size){return page(size)*size;}
    public void nextPage(int size){page=(page(size)+1)%pageCount(size);}
    public void showSlot(int slot,int size){page=slot/size;}
	private Item[] slots = new Item[SIZE];


	//direct array interaction methods, everything should build from these methods.
	public void setSlot(int slot, Item item){
		if(item instanceof com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionSkillQuickslots.Shortcut)
            for(int i=0;i<SIZE;i++)if(item.isSimilar(slots[i]))slots[i]=null;
        clearItem(item); //we don't want to allow the same item in multiple slots.
		slots[slot] = item;
	}

	public void clearSlot(int slot){
		slots[slot] = null;
	}

	public void reset(){
		slots = new Item[SIZE];
        page=0;
	}

	public Item getItem(int slot){
		return slots[slot];
	}

	//utility methods, for easier use of the internal array.
	public int getSlot(Item item) {
		for (int i = 0; i < SIZE; i++) {
			if (getItem(i) == item) {
				return i;
			}
		}
		return -1;
	}

	public Boolean isPlaceholder(int slot){
		return getItem(slot) != null && getItem(slot).quantity() == 0;
	}

	public Boolean isNonePlaceholder(int slot){
		return getItem(slot) != null && getItem(slot).quantity() > 0;
	}

	public void clearItem(Item item){
		if (contains(item)) {
			clearSlot(getSlot(item));
		}
	}

	public boolean contains(Item item){
		return getSlot(item) != -1;
	}

	public void replacePlaceholder(Item item) {
		for (int i = 0; i < SIZE; i++) {
			if (isPlaceholder(i) && item.isSimilar(getItem(i))) {
				setSlot(i, item);
			}
		}
	}

	public void convertToPlaceholder(Item item){
		
		if (contains(item)) {
			Item placeholder = item.virtual();
			if (placeholder == null) return;
			
			for (int i = 0; i < SIZE; i++) {
				if (getItem(i) == item) setSlot(i, placeholder);
			}
		}
	}

	public Item randomNonePlaceholder(){

		ArrayList<Item> result = new ArrayList<>();
		for (int i = 0; i < SIZE; i ++) {
			if (getItem(i) != null && !isPlaceholder(i)) {
				result.add(getItem(i));
			}
		}
		return Random.element(result);
	}

	private final String PLACEHOLDERS = "placeholders";
	private final String PLACEMENTS = "placements";

	/**
	 * Placements array is used as order is preserved while bundling, but exact index is not, so if we
	 * bundle both the placeholders (which preserves their order) and an array telling us where the placeholders are,
	 * we can reconstruct them perfectly.
	 */

	public void storePlaceholders(Bundle bundle){
		ArrayList<Item> placeholders = new ArrayList<>(SIZE);
		boolean[] placements = new boolean[SIZE];

		for (int i = 0; i < SIZE; i++) {
			if (isPlaceholder(i)) {
				placeholders.add(getItem(i));
				placements[i] = true;
			}
		}
		bundle.put( PLACEHOLDERS, placeholders );
		bundle.put( PLACEMENTS, placements );
        bundle.put("quick_page",page);
        for(int i=0;i<SIZE;i++)if(slots[i] instanceof com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionSkillQuickslots.Shortcut)
            bundle.put("quick_skill_"+i,slots[i]);
	}

	public void restorePlaceholders(Bundle bundle){
		Collection<Bundlable> placeholders = bundle.getCollection(PLACEHOLDERS);
		boolean[] placements = bundle.getBooleanArray( PLACEMENTS );

		int i = 0;
		for (Bundlable item : placeholders){
			while (i<Math.min(SIZE,placements.length)&&!placements[i]){
				i++;
			}
			if(i>=Math.min(SIZE,placements.length))break;
            setSlot( i, (Item)item );
			i++;
		}

        page=Math.max(0,bundle.getInt("quick_page"));
        for(int slot=0;slot<SIZE;slot++)if(bundle.contains("quick_skill_"+slot)){
            Bundlable skill=bundle.get("quick_skill_"+slot);
            if(skill instanceof com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionSkillQuickslots.Shortcut)setSlot(slot,(Item)skill);
        }
    }

}
