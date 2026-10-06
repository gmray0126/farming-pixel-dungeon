/* Extraction fork © 2026. GPL-3.0-or-later; see LICENSE.txt and original credits. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/** Separate permanent state. Run equipment is escrowed before starting a raid. */
public final class ExtractionProfile {
    public static final String FILE = "extraction-profile.dat";
    private static ExtractionProfile instance;
    public final ArrayList<Item> stash = new ArrayList<>();
    public final ArrayList<Item> prepared = new ArrayList<>();
    private final ArrayList<Item> escrow = new ArrayList<>();
    public final HashSet<String> nodes = new HashSet<>();
    public int gold = 100, xp = 0, points = 3, raidID = 0, nextRaid = 1, raidXP = 0;
    public boolean active = false;
    public String result = "";
    public static final String[] IDS = {"power", "edge", "master", "vital", "guard", "iron", "pack", "porter", "strength"};
    public static final String[] NAMES = {"무기 숙련", "예리한 칼날", "전투의 기억", "튼튼한 몸", "방어 훈련", "철벽", "짐 정리", "원정 준비", "완력"};
    public static final String[] DESCS = {"공격 피해 +1", "공격 피해 추가 +1", "공격 피해 추가 +2", "출격 최대 체력 +6", "방어 피해 감소 +1", "방어 피해 감소 추가 +1", "가방 용량 +2", "가방 용량 추가 +2", "출격 힘 +2"};
    public static final int[] COSTS = {1, 1, 2, 1, 1, 2, 1, 1, 2};
    public static final int[] PARENTS = {-1, 0, 1, -1, 3, 4, -1, 6, 7};

    public static synchronized ExtractionProfile get() {
        if (instance == null) {
            ExtractionProfile p = new ExtractionProfile();
            if (FileUtils.fileExists(FILE)) {
                try { p.restore(FileUtils.bundleFromFile(FILE)); }
                catch (IOException e) { throw new IllegalStateException("영구 저장을 읽지 못했습니다. 원본 파일을 보존했습니다.", e); }
            } else {
                p.stash.add(new WornShortsword().identify());
                p.stash.add(new ClothArmor().identify());
                for (int i=0; i<3; i++) p.stash.add(new SupplyHealingPotion().identify(false));
                try { p.save(); } catch (IOException e) { throw new IllegalStateException(e); }
            }
            instance = p;
        }
        return instance;
    }
    private Bundle bundle() {
        Bundle b = new Bundle(); b.put("schema", 1); b.put("stash", stash); b.put("prepared", prepared); b.put("escrow", escrow);
        b.put("nodes", nodes.toArray(new String[0])); b.put("gold", gold); b.put("xp", xp); b.put("points", points);
        b.put("active", active); b.put("raid", raidID); b.put("next", nextRaid); b.put("raid_xp", raidXP); b.put("result", result);
        return b;
    }
    private void restore(Bundle b) {
        if (b.getInt("schema") != 1) throw new IllegalStateException("지원하지 않는 영구 저장 형식입니다.");
        stash.clear(); prepared.clear(); escrow.clear(); nodes.clear();
        for (Bundlable i : b.getCollection("stash")) if (i instanceof Item) stash.add((Item)i);
        for (Bundlable i : b.getCollection("prepared")) if (i instanceof Item) prepared.add((Item)i);
        for (Bundlable i : b.getCollection("escrow")) if (i instanceof Item) escrow.add((Item)i);
        for (String n : b.getStringArray("nodes")) nodes.add(n);
        gold=b.getInt("gold"); xp=b.getInt("xp"); points=b.getInt("points"); active=b.getBoolean("active");
        raidID=b.getInt("raid"); nextRaid=b.getInt("next"); raidXP=b.getInt("raid_xp"); result=b.getString("result");
    }
    private void save() throws IOException { FileUtils.bundleToFile(FILE, bundle()); }
    private synchronized void change(Runnable mutation) {
        Bundle before = bundle();
        try { mutation.run(); save(); }
        catch (IOException e) { restore(before); throw new IllegalStateException("저장 실패. 장비 이동을 취소했습니다.", e); }
        catch (RuntimeException e) { restore(before); throw e; }
    }
    public void prepare(final Item i, final boolean take) {
        if (active) throw new IllegalStateException("진행 중인 원정이 있습니다.");
        if (take && prepared.size() >= 12) throw new IllegalStateException("준비 물품은 최대 12개입니다.");
        change(() -> { if (take && stash.remove(i)) prepared.add(i); else if (!take && prepared.remove(i)) stash.add(i); });
    }
    public void begin() {
        if (active) return; // retry the same escrow if first map creation was interrupted
        change(() -> { active=true; raidID=nextRaid++; raidXP=0; escrow.addAll(prepared); prepared.clear(); result=""; });
    }
    public void initialize(Hero h) {
        h.belongings.clear();
        // Reconstruct escrow items so equipped mutations cannot alter the persistent escrow.
        Bundle copy = new Bundle(); copy.put("items", escrow);
        for (Bundlable value : copy.getCollection("items")) {
            Item i=(Item)value;
            if (i instanceof KindOfWeapon && h.belongings.weapon == null) { h.belongings.weapon=(KindOfWeapon)i; h.belongings.weapon.activate(h); }
            else if (i instanceof Armor && h.belongings.armor == null) { h.belongings.armor=(Armor)i; h.belongings.armor.activate(h); }
            else if (!i.collect(h.belongings.backpack)) throw new IllegalStateException("준비 물품이 가방에 들어가지 않습니다.");
        }
        h.HTBoost += nodes.contains("vital") ? 6 : 0; h.updateHT(true); h.HP=h.HT;
        if (nodes.contains("strength")) h.STR += 2;
    }
    public int attackBonus() { return (nodes.contains("power")?1:0)+(nodes.contains("edge")?1:0)+(nodes.contains("master")?2:0); }
    public int defenseBonus() { return (nodes.contains("guard")?1:0)+(nodes.contains("iron")?1:0); }
    public int capacity() { return 12+(nodes.contains("pack")?2:0)+(nodes.contains("porter")?2:0); }
    public void learn(final int index) {
        if (active || index<0 || index>=IDS.length || nodes.contains(IDS[index])) return;
        int parent=PARENTS[index];
        if (parent>=0 && !nodes.contains(IDS[parent])) throw new IllegalStateException("선행 노드를 먼저 배워 주세요.");
        if (points<COSTS[index]) throw new IllegalStateException("성장 포인트가 부족합니다.");
        change(() -> { points-=COSTS[index]; nodes.add(IDS[index]); });
    }
    /** High-water credit prevents replaying an older run save from awarding the same XP twice. */
    public void credit(final int id, final int total) {
        if (!active || id!=raidID || total<=raidXP) return;
        change(() -> { int level=xp/25; xp+=total-raidXP; raidXP=total; points+=xp/25-level; });
    }
    public void settle(final int id, final boolean success) {
        if (!active || id!=raidID) return;
        change(() -> {
            if (success) {
                // Preserve only top-level inventory entries: Bag.iterator() also yields
                // nested contents, which would otherwise be deposited twice.
                com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings b = Dungeon.hero.belongings;
                Item[] worn = {b.weapon,b.armor,b.artifact,b.misc,b.ring,b.secondWep};
                for (Item i : worn) if (i != null) stash.add(i);
                stash.addAll(b.backpack.items);
                gold+=Dungeon.gold;
                int old=xp/25; xp+=10; points+=xp/25-old;
            }
            active=false; escrow.clear();
            result=success?"탈출 성공! 장비와 전리품을 창고에 보관했습니다.":"사망했습니다. 출격 물품은 잃었지만 창고와 성장 노드는 남았습니다.";
        });
        Dungeon.deleteGame(GamesInProgress.curSlot, true);
    }
    public void buyPotion() {
        if (active || gold<30) throw new IllegalStateException("골드가 부족하거나 원정 중입니다.");
        change(() -> { gold-=30; stash.add(new SupplyHealingPotion().identify(false)); });
    }
    public void sell(final Item i) {
        if (active || !stash.contains(i)) return;
        change(() -> { gold+=Math.max(0,i.value()); stash.remove(i); });
    }
}
