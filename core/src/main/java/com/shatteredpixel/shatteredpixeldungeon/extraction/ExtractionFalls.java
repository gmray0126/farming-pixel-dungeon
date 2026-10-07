/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Random;
import java.util.ArrayList;

/** Falls relocate within the current connected area; locked rooms stay locked. */
public final class ExtractionFalls {
    public static int landingCell(Level level, int origin) {
        boolean[] reachable = reachable(level, origin, true);
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell=0; cell<level.length(); cell++) {
            if (reachable[cell] && cell!=origin && safe(level,cell) && ordinaryRoom(level,cell)) cells.add(cell);
        }
        if (!cells.isEmpty()) return Random.element(cells);
        // An isolated special room may have no ordinary ground, but must still have a landing.
        for (int cell=0; cell<level.length(); cell++) {
            if (reachable[cell] && cell!=origin && safe(level,cell)) cells.add(cell);
        }
        if (!cells.isEmpty()) return Random.element(cells);
        return safe(level,origin) ? origin : -1;
    }

    private static boolean safe(Level level,int cell) {
        return cell>=0 && cell<level.length() && level.passable[cell] && !level.solid[cell]
                && !level.pit[cell] && level.traps.get(cell)==null && level.findMob(cell)==null;
    }

    private static boolean ordinaryRoom(Level level,int cell) {
        if (!(level instanceof RegularLevel)) return true;
        Room room=((RegularLevel)level).room(cell);
        return !(room instanceof SpecialRoom) && !(room instanceof SecretRoom);
    }

    /** Includes chasms for landing reachability, never walls or locked doors. */
    private static boolean[] reachable(Level level,int origin,boolean crossPits) {
        boolean[] reached=new boolean[level.length()];
        if (origin<0 || origin>=reached.length) return reached;
        int[] queue=new int[reached.length]; int head=0,tail=0;
        queue[tail++]=origin; reached[origin]=true;
        while(head<tail) {
            int cell=queue[head++],x=cell%level.width(),y=cell/level.width();
            for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++) {
                if(dx==0 && dy==0 || x+dx<0 || x+dx>=level.width() || y+dy<0 || y+dy>=level.height()) continue;
                int next=(y+dy)*level.width()+x+dx;
                if(!reached[next] && (level.passable[next] || crossPits && level.pit[next])) {
                    reached[next]=true; queue[tail++]=next;
                }
            }
        }
        return reached;
    }

    /** Move an existing pit-room key; no new key is minted on repeated loads. */
    public static void movePitKeys(RegularLevel level) {
        if(!ExtractionDifficulty.active()) return;
        boolean[] reached=reachable(level,level.entrance(),false);
        ArrayList<Integer> destinations=new ArrayList<>();
        for(int cell=0;cell<level.length();cell++) {
            Heap heap=level.heaps.get(cell);
            if(reached[cell] && safe(level,cell) && ordinaryRoom(level,cell)
                    && (heap==null || heap.type==Heap.Type.HEAP)) destinations.add(cell);
        }
        if(destinations.isEmpty()) return;
        for(Heap heap:level.heaps.valueList()) {
            if(!(((RegularLevel)level).room(heap.pos) instanceof PitRoom)) continue;
            for(Item item:new ArrayList<>(heap.items)) {
                if(item instanceof CrystalKey) {
                    int destination=Random.element(destinations);
                    level.drop(item,destination);
                    heap.items.remove(item);
                }
            }
            if(heap.items.isEmpty()) level.heaps.remove(heap.pos);
        }
    }
    private ExtractionFalls() {}
}
