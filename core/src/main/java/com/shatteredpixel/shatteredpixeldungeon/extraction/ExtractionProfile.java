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
    public static final String[] IDS=ExtractionGrowth.IDS, NAMES=ExtractionGrowth.NAMES, DESCS=ExtractionGrowth.DESCS;
    public static final int[] COSTS=ExtractionGrowth.COSTS, PARENTS=ExtractionGrowth.PARENTS;

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
        if (take && prepared.size() >= capacity()) throw new IllegalStateException("출격 가방이 가득 찼습니다.");
        change(() -> { if (take && stash.remove(i)) prepared.add(i); else if (!take && prepared.remove(i)) stash.add(i); });
    }
    public void begin() {
        if (active) return; // retry the same escrow if first map creation was interrupted
        change(() -> { active=true; raidID=nextRaid++; raidXP=0; escrow.addAll(prepared); prepared.clear(); result=""; });
    }
    public Item preparedWeapon() {
        for (Item i : prepared) if (i instanceof KindOfWeapon) return i;
        return null;
    }
    public Item preparedArmor() {
        for (Item i : prepared) if (i instanceof Armor) return i;
        return null;
    }
    /** Selected gear goes first, preserving the existing profile/escrow format. */
    public void selectEquipment(final Item i) {
        if (active) throw new IllegalStateException("원정 중에는 착용 장비를 바꿀 수 없습니다.");
        if (!prepared.contains(i) || !(i instanceof KindOfWeapon || i instanceof Armor)) return;
        change(() -> { prepared.remove(i); prepared.add(0, i); });
    }
    public void returnPrepared() {
        if (active) throw new IllegalStateException("원정 중에는 물품을 돌려놓을 수 없습니다.");
        if (prepared.isEmpty()) return;
        change(() -> { stash.addAll(prepared); prepared.clear(); });
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
        h.HTBoost += Math.round(bonus(ExtractionGrowth.Stat.HEALTH)); h.updateHT(true); h.HP=h.HT;
        h.STR += Math.round(bonus(ExtractionGrowth.Stat.STRENGTH));
    }
    public float bonus(ExtractionGrowth.Stat stat) { return bonus(stat, -1); }
    public float bonus(ExtractionGrowth.Stat stat, int family) {
        float result=0;
        for(ExtractionGrowth.Node n:ExtractionGrowth.NODES){
            if((n.branch<3||n.branch==family)&&nodes.contains(n.id)){ Float value=n.effects.get(stat);if(value!=null)result+=value; }
        }
        return result;
    }
    public int attackBonus() { return Math.round(bonus(ExtractionGrowth.Stat.DAMAGE)); }
    public int defenseBonus() { return Math.round(bonus(ExtractionGrowth.Stat.DEFENSE)); }
    public int defenseBonus(KindOfWeapon weapon) { return Math.round(bonus(ExtractionGrowth.Stat.DEFENSE,ExtractionGrowth.family(weapon))); }
    public int capacity() { return 12+Math.round(bonus(ExtractionGrowth.Stat.CAPACITY)); }
    public float accuracyMultiplier(KindOfWeapon weapon) { return 1+bonus(ExtractionGrowth.Stat.ACCURACY,ExtractionGrowth.family(weapon))/100f; }
    public float evasionMultiplier(KindOfWeapon weapon) { return 1+bonus(ExtractionGrowth.Stat.EVASION,ExtractionGrowth.family(weapon))/100f; }
    public float attackSpeedMultiplier(KindOfWeapon weapon) { return 1+bonus(ExtractionGrowth.Stat.ATTACK_SPEED,ExtractionGrowth.family(weapon))/100f; }
    public float moveSpeedMultiplier() { return 1+bonus(ExtractionGrowth.Stat.MOVE_SPEED)/100f; }
    public int armorPierce(KindOfWeapon weapon) { return Math.round(bonus(ExtractionGrowth.Stat.PIERCE,ExtractionGrowth.family(weapon))); }
    public int reachBonus(KindOfWeapon weapon) { return Math.round(bonus(ExtractionGrowth.Stat.REACH,ExtractionGrowth.family(weapon))); }
    public float wandChargeMultiplier() { return 1+bonus(ExtractionGrowth.Stat.WAND_CHARGE,ExtractionGrowth.MAGIC)/100f; }
    public int magicDamage(int damage) {
        return Math.round((damage+bonus(ExtractionGrowth.Stat.WAND_DAMAGE,ExtractionGrowth.MAGIC))*(1+bonus(ExtractionGrowth.Stat.WAND_POWER,ExtractionGrowth.MAGIC)/100f));
    }
    /** Deterministic roll parameter makes critical/weapon specialization rules testable. */
    public int physicalDamage(int damage,KindOfWeapon weapon,float criticalRoll) {
        int family=ExtractionGrowth.family(weapon);
        float value=(damage+bonus(ExtractionGrowth.Stat.DAMAGE,family))*(1+bonus(ExtractionGrowth.Stat.DAMAGE_PERCENT,family)/100f);
        float chance=Math.min(0.65f,bonus(ExtractionGrowth.Stat.CRIT_CHANCE,family)/100f);
        if(criticalRoll<chance)value*=1.5f+bonus(ExtractionGrowth.Stat.CRIT_POWER,family)/100f;
        return Math.max(0,Math.round(value));
    }
    public boolean unlocked(int index) { return ExtractionGrowth.unlocked(ExtractionGrowth.NODES[index],nodes); }
    public String prerequisites(int index) {
        StringBuilder result=new StringBuilder();
        for(int parent:ExtractionGrowth.NODES[index].parents){
            if(result.length()>0)result.append(" · ");
            result.append(NAMES[parent]).append(nodes.contains(IDS[parent])?" (습득)":" (필요)");
        }
                if(ExtractionGrowth.NODES[index].alternatives.length>0){
            result.append("\n또는 연결 경로: ");
            for(int p:ExtractionGrowth.NODES[index].alternatives)result.append(NAMES[p]).append(nodes.contains(IDS[p])?" (습득)":" (필요)").append(' ');
        }
        return result.length()==0?"없음":result.toString();
    }
    public void learn(final int index) {
        if (active || index<0 || index>=IDS.length || nodes.contains(IDS[index])) return;
        if (!unlocked(index)) throw new IllegalStateException("선행 경로나 연결된 계통의 노드를 먼저 배워 주세요.");
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
                gold+=Math.round(Dungeon.gold*(1+bonus(ExtractionGrowth.Stat.GOLD)/100f));
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
