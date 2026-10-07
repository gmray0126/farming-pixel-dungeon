/* Extraction fork © 2026. GPL-3.0-or-later; see LICENSE.txt and original credits. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
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
    public static final int MAX_GROWTH_LEVEL=100, MAX_GROWTH_XP=2475, MAX_GROWTH_POINTS=300, PRESET_SLOTS=3;
    private static ExtractionProfile instance;
    private int growthPointRate=3;
    private final String[][] presets=new String[PRESET_SLOTS][];
    public final ArrayList<Item> stash = new ArrayList<>();
    public final ArrayList<Item> prepared = new ArrayList<>();
    private final ArrayList<Item> escrow = new ArrayList<>();
    public final HashSet<String> nodes = new HashSet<>();
    public int gold = 100, xp = 0, points = 3, raidID = 0, nextRaid = 1, raidXP = 0;
    public boolean active = false;
    public boolean debugEnabled=false;
    public int selectedChapter=1, selectedDifficulty=1, raidChapter=1, raidDifficulty=1;
    public int raidRules=1; // Missing in old saves: preserve an ongoing staged raid.
    public final int[] unlockedDifficulty={1,0,0,0,0};
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
                for (int i=0; i<3; i++) p.stash.add(new SupplyHealingPotion().identify(false));
                try { p.save(); } catch (IOException e) { throw new IllegalStateException(e); }
            }
            p.migrateGrowthCap();
            instance = p;
        }
        return instance;
    }
    private Bundle bundle() {
        Bundle b = new Bundle(); b.put("schema", 1); b.put("stash", stash); b.put("prepared", prepared); b.put("escrow", escrow);
        b.put("nodes", nodes.toArray(new String[0])); b.put("gold", gold); b.put("xp", xp); b.put("points", points);
        b.put("active", active); b.put("raid", raidID); b.put("next", nextRaid); b.put("raid_xp", raidXP); b.put("result", result);
        b.put("chapter",selectedChapter);b.put("difficulty",selectedDifficulty);b.put("raid_chapter",raidChapter);b.put("raid_difficulty",raidDifficulty);
        b.put("difficulty_unlocks",unlockedDifficulty);
        b.put("raid_rules",raidRules);
        b.put("debug_enabled",debugEnabled);
        b.put("growth_point_rate",growthPointRate);
        for(int i=0;i<PRESET_SLOTS;i++)if(presets[i]!=null)b.put("growth_preset_"+i,presets[i]);
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
        selectedChapter=b.contains("chapter")?b.getInt("chapter"):1;
        selectedDifficulty=b.contains("difficulty")?b.getInt("difficulty"):1;
        raidChapter=b.contains("raid_chapter")?b.getInt("raid_chapter"):1;
        raidDifficulty=b.contains("raid_difficulty")?b.getInt("raid_difficulty"):1;
        raidRules=b.contains("raid_rules")?b.getInt("raid_rules"):1;
        debugEnabled=b.getBoolean("debug_enabled");
        growthPointRate=b.contains("growth_point_rate")?b.getInt("growth_point_rate"):1;
        for(int i=0;i<PRESET_SLOTS;i++)presets[i]=b.contains("growth_preset_"+i)?b.getStringArray("growth_preset_"+i):null;
        int[] unlocked=b.contains("difficulty_unlocks")?b.getIntArray("difficulty_unlocks"):new int[0];
        java.util.Arrays.fill(unlockedDifficulty,0);unlockedDifficulty[0]=1;
        for(int n=0;n<Math.min(unlockedDifficulty.length,unlocked.length);n++)unlockedDifficulty[n]=Math.max(n==0?1:0,Math.min(10,unlocked[n]));
    }
    private void save() throws IOException { FileUtils.bundleToFile(FILE, bundle()); }
    public void setDebugEnabled(boolean enabled){change(()->debugEnabled=enabled);}
    public void debugResources(int addedGold,int addedXP,int addedPoints){
        requireDebug();
        if(addedGold<0||addedXP<0||addedPoints<0)throw new IllegalArgumentException("지급량은 음수일 수 없습니다.");
        change(()->{gold=Math.addExact(gold,addedGold);awardGrowthXP(addedXP);points=(int)Math.min(Math.max(0,MAX_GROWTH_POINTS-spentPoints()),(long)points+addedPoints);});
    }
    public void debugUnlockPrison(){requireDebug();change(()->unlockedDifficulty[1]=Math.max(1,unlockedDifficulty[1]));}
    public void debugUnlockChapters(){requireDebug();change(()->java.util.Arrays.fill(unlockedDifficulty,1));}
    public void debugStore(Item item){
        requireDebug();if(active)throw new IllegalStateException("원정 중에는 원정 가방으로 지급하세요.");
        change(()->stash.add(item));
    }
    public void requireDebug(){if(!debugEnabled)throw new IllegalStateException("디버그 메뉴를 먼저 활성화하세요.");}
    private synchronized void change(Runnable mutation) {
        Bundle before = bundle();
        try { mutation.run(); save(); }
        catch (IOException e) { restore(before); throw new IllegalStateException("저장 실패. 변경 사항을 취소했습니다.", e); }
        catch (RuntimeException e) { restore(before); throw e; }
    }
    public void prepare(final Item i, final boolean take) {
        if (active) throw new IllegalStateException("진행 중인 원정이 있습니다.");
        if (take && prepared.size() >= capacity()) throw new IllegalStateException("출격 가방이 가득 찼습니다.");
        change(() -> { if (take && stash.remove(i)) prepared.add(i); else if (!take && prepared.remove(i)) stash.add(i); });
    }
    public void begin() {
        if (active) return; // retry the same escrow if first map creation was interrupted
        if(!ExtractionDifficulty.validChapter(selectedChapter)||unlockedDifficulty[selectedChapter-1]==0)throw new IllegalStateException("아직 해금되지 않은 챕터입니다.");
        migrateGrowthCap();
        change(() -> { active=true; raidChapter=selectedChapter;raidDifficulty=ExtractionDifficulty.fixedStage(selectedChapter);raidRules=4;raidID=nextRaid++; raidXP=0; escrow.addAll(prepared); prepared.clear(); result=""; });
    }
    public void selectRaid(final int chapter){
        if(active)throw new IllegalStateException("원정 중에는 출격 지역을 바꿀 수 없습니다.");
        if(!ExtractionDifficulty.validChapter(chapter)||unlockedDifficulty[chapter-1]==0)throw new IllegalStateException("아직 해금되지 않은 챕터입니다.");
        change(()->{selectedChapter=chapter;selectedDifficulty=ExtractionDifficulty.fixedStage(chapter);});
    }
    public Item preparedWeapon() {
        for (Item i : prepared) if (i instanceof KindOfWeapon && !(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow)) return i;
        return null;
    }
    public Item preparedArmor() {
        for (Item i : prepared) if (i instanceof Armor) return i;
        return null;
    }
    public Item preparedPants(){for(Item item:prepared)if(item instanceof ExpeditionClothing.Pants)return item;return null;}
    public Item preparedBoots(){for(Item item:prepared)if(item instanceof ExpeditionClothing.Boots)return item;return null;}
    public boolean preparedEquipment(Item item){return item==preparedWeapon()||item==preparedArmor()||item==preparedPants()||item==preparedBoots()||preparedRelics().contains(item);}
    public ArrayList<Item> preparedRelics(){
        ArrayList<Item> result=new ArrayList<>();
        for(Item i:prepared)if(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact&&result.size()<2
                &&(result.isEmpty()||result.get(0).getClass()!=i.getClass()))result.add(i);
        return result;
    }
    /** Selected gear goes first, preserving the existing profile/escrow format. */
    public void selectEquipment(final Item i) {
        if (active) throw new IllegalStateException("원정 중에는 착용 장비를 바꿀 수 없습니다.");
        if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow) return;
        if (!prepared.contains(i) || !(i instanceof KindOfWeapon || i instanceof Armor || i instanceof ExpeditionClothing || i instanceof com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc)) return;
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
            if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow){if(!i.collect(h.belongings.backpack))throw new IllegalStateException("영혼의 활을 가방에 넣지 못했습니다.");}
            else if (i instanceof KindOfWeapon && h.belongings.weapon == null) { h.belongings.weapon=(KindOfWeapon)i; h.belongings.weapon.activate(h); }
            else if (i instanceof Armor && h.belongings.armor == null) { h.belongings.armor=(Armor)i; h.belongings.armor.activate(h); }
            else if(i instanceof ExpeditionClothing.Pants&&h.belongings.pants==null){h.belongings.pants=(ExpeditionClothing)i;}
            else if(i instanceof ExpeditionClothing.Boots&&h.belongings.boots==null){h.belongings.boots=(ExpeditionClothing)i;}
            else if(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact&&h.belongings.artifact==null){h.belongings.artifact=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)i;h.belongings.artifact.activate(h);}
            else if(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact&&h.belongings.misc==null&&h.belongings.artifact.getClass()!=i.getClass()){h.belongings.misc=(com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc)i;h.belongings.misc.activate(h);}
            else if(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring&&h.belongings.ring==null){h.belongings.ring=(com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)i;h.belongings.ring.activate(h);}
            else if (!i.collect(h.belongings.backpack)) throw new IllegalStateException("준비 물품이 가방에 들어가지 않습니다.");
        }
        if(h.belongings.weapon==null){
            h.belongings.weapon=(KindOfWeapon)new BasicExpeditionSword().identify(false);
            h.belongings.weapon.activate(h);
        }
        if(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class)==null
                && !new com.shatteredpixel.shatteredpixeldungeon.items.Waterskin().collect(h.belongings.backpack))
            throw new IllegalStateException("기본 물통을 가방에 넣지 못했습니다.");
        if(nodes.contains("ranged_0")&&h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.class)==null
                &&!new NodeSpiritBow().identify(false).collect(h.belongings.backpack))
            throw new IllegalStateException("기본 영혼의 활을 가방에 넣지 못했습니다.");
        ExtractionPotionKnowledge.apply(this);
        for(Item i:h.belongings)if(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)com.shatteredpixel.shatteredpixeldungeon.items.Generator.removeArtifact((Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact>)i.getClass());
        h.HTBoost += Math.round(bonus(ExtractionGrowth.Stat.HEALTH)); h.updateHT(true); h.HP=h.HT;
        h.STR += Math.round(bonus(ExtractionGrowth.Stat.STRENGTH));
    }
    public float bonus(ExtractionGrowth.Stat stat) { return bonus(stat, -1); }
    public float bonus(ExtractionGrowth.Stat stat, int family) {
        float result=0;
        for(ExtractionGrowth.Node n:ExtractionGrowth.NODES){
            if((n.branch<3||n.branch>=12||n.branch==family)&&nodes.contains(n.id)){ Float value=n.effects.get(stat);if(value!=null)result+=value; }
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
    public int spentPoints(){
        int used=0;for(ExtractionGrowth.Node n:ExtractionGrowth.NODES)if(nodes.contains(n.id))used=Math.addExact(used,n.cost);return used;
    }
    /** Full, atomic refund. Raid stats and granted skills cannot change during an expedition. */
    public int resetNodes(){
        if(active)throw new IllegalStateException("진행 중인 원정을 끝내거나 포기한 뒤 초기화할 수 있습니다.");
        int refunded=spentPoints();if(nodes.isEmpty())return 0;
        change(()->{points=(int)Math.min(MAX_GROWTH_POINTS,(long)points+refunded);nodes.clear();trimPrepared();});return refunded;
    }
    public void learn(final int index) {
        if (active || index<0 || index>=IDS.length || nodes.contains(IDS[index])) return;
        if (!unlocked(index)) throw new IllegalStateException("선행 경로나 연결된 계통의 노드를 먼저 배워 주세요.");
        if (spentPoints()+COSTS[index]>MAX_GROWTH_POINTS) throw new IllegalStateException("성장 배분 한도는 총 300 P입니다. 다른 노드를 초기화하거나 프리셋을 바꿔 주세요.");
        if (points<COSTS[index]) throw new IllegalStateException("성장 포인트가 부족합니다.");
        change(() -> { points-=COSTS[index]; nodes.add(IDS[index]); });
    }
    /** High-water credit prevents replaying an older run save from awarding the same XP twice. */
    public int growthLevel(){return 1+Math.min(MAX_GROWTH_XP,Math.max(0,xp))/25;}
    public int growthExperience(){return growthLevel()==MAX_GROWTH_LEVEL?0:Math.max(0,xp)%25;}
    public int growthExperienceRequired(){return growthLevel()==MAX_GROWTH_LEVEL?0:25;}
    public String growthDisplay(){return "성장 Lv. "+growthLevel()+" / 100 · "+(growthLevel()==MAX_GROWTH_LEVEL?"최대 레벨":"XP "+growthExperience()+"/25");}
    public int startingStrength(){return Hero.STARTING_STR+Math.round(bonus(ExtractionGrowth.Stat.STRENGTH));}
    public void credit(final int id, final int total) {
        if (!active || id!=raidID || total<=raidXP) return;
        change(() -> { awardGrowthXP((long)total-raidXP); raidXP=total; });
    }
    public void settle(final int id, final boolean success) {
        if (!active || id!=raidID) return;
        change(() -> {
            if (success) {
                if(raidChapter<ExtractionDifficulty.CHAPTER_COUNT)unlockedDifficulty[raidChapter]=Math.max(1,unlockedDifficulty[raidChapter]);
                // Preserve only top-level inventory entries: Bag.iterator() also yields
                // nested contents, which would otherwise be deposited twice.
                com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings b = Dungeon.hero.belongings;
                Item[] worn = {b.weapon,b.armor,b.artifact,b.misc,b.ring,b.secondWep,b.pants,b.boots};
                ArrayList<Item> loot=new ArrayList<>();
                for (Item i : worn) if (i != null) loot.add(i);
                loot.addAll(b.backpack.items);
                // Work on a copy so failed settlement never removes scrolls from the run save.
                Bundle copied=new Bundle();copied.put("loot",loot);loot.clear();
                for(Bundlable item:copied.getCollection("loot"))loot.add((Item)item);
                ExtractionShop.Redemption redeemed=ExtractionShop.redeemConsumables(loot);
                // Unmodified free swords are reissued next run instead of filling the stash.
                loot.removeIf(i -> i instanceof BasicExpeditionSword && i.level()==0 && ((BasicExpeditionSword)i).enchantment==null);
                loot.removeIf(i -> i instanceof com.shatteredpixel.shatteredpixeldungeon.items.Waterskin
                        && ((com.shatteredpixel.shatteredpixeldungeon.items.Waterskin)i).isEmpty());
                loot.removeIf(i -> i instanceof NodeSpiritBow && ((NodeSpiritBow)i).enchantment==null && ((NodeSpiritBow)i).augment==com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Augment.NONE);
                stash.addAll(loot);
                gold+=Math.round(Dungeon.gold*(1+bonus(ExtractionGrowth.Stat.GOLD)/100f)*ExtractionDifficulty.rewardMultiplier(raidChapter,raidDifficulty));
                gold+=redeemed.gold;
                awardGrowthXP(10+5*(raidDifficulty-1)+10*(raidChapter-1));
                result="탈출 성공! 장비와 전리품을 창고에 보관했습니다."
                        +(redeemed.potions+redeemed.scrolls>0?"\n포션 "+redeemed.potions+"개 · 스크롤 "+redeemed.scrolls+"장 정산 · +"+redeemed.gold+" G":"");
                if(raidChapter<ExtractionDifficulty.CHAPTER_COUNT)result+="\n"+(raidChapter+1)+"챕터 "+ExtractionDifficulty.chapterName(raidChapter+1)+" 출격 가능";
                else result+="\n5챕터 완주! 모든 지역에 다시 출격할 수 있습니다.";
            }
            active=false; escrow.clear();
            if(!success)result="사망했습니다. 출격 물품은 잃었지만 창고·성장 노드·획득한 성장 경험치는 유지됩니다.";
        });
        migrateGrowthCap();
        Dungeon.deleteGame(GamesInProgress.curSlot, true);
    }
    private void awardGrowthXP(long amount){
        int old=Math.min(MAX_GROWTH_XP,Math.max(0,xp));
        xp=(int)Math.min(MAX_GROWTH_XP,old+Math.max(0,amount));
        points=(int)Math.min(Math.max(0,MAX_GROWTH_POINTS-spentPoints()),(long)points+3*(xp/25-old/25));
    }
    private void trimPrepared(){while(prepared.size()>capacity())stash.add(prepared.remove(prepared.size()-1));}
    /** Preserve a running hero; over-budget legacy allocations are refunded at the hub. */
    private void migrateGrowthCap(){
        int used=spentPoints(), cappedXP=Math.min(MAX_GROWTH_XP,Math.max(0,xp));
        boolean refund=!active&&used>MAX_GROWTH_POINTS;
        int remaining=Math.max(0,MAX_GROWTH_POINTS-(refund?0:used));
        long backpay=growthPointRate<3?(long)(3-growthPointRate)*(cappedXP/25):0;
        int cappedPoints=refund?MAX_GROWTH_POINTS:(int)Math.min(remaining,Math.max(0L,(long)points+backpay));
        if(xp==cappedXP&&points==cappedPoints&&!refund&&growthPointRate==3)return;
        change(()->{xp=cappedXP;points=cappedPoints;growthPointRate=3;if(refund){nodes.clear();trimPrepared();result+="\n성장 한도 300 P 적용: 기존 배분을 초기화하고 포인트를 반환했습니다.";}});
    }
    private void checkPresetSlot(int slot){if(slot<0||slot>=PRESET_SLOTS)throw new IllegalArgumentException("없는 프리셋입니다.");}
    public boolean hasPreset(int slot){checkPresetSlot(slot);return presets[slot]!=null;}
    public int presetCost(int slot){checkPresetSlot(slot);if(presets[slot]==null)return 0;return allocationCost(new HashSet<>(java.util.Arrays.asList(presets[slot])));}
    private int allocationCost(java.util.Set<String> allocation){
        int cost=0;for(String id:allocation)cost=Math.addExact(cost,ExtractionGrowth.NODES[ExtractionGrowth.index(id)].cost);return cost;
    }
    public void savePreset(int slot){
        checkPresetSlot(slot);if(active)throw new IllegalStateException("프리셋은 거점에서 저장할 수 있습니다.");
        if(spentPoints()>MAX_GROWTH_POINTS)throw new IllegalStateException("배분 한도를 넘었습니다. 먼저 초기화해 주세요.");
        change(()->presets[slot]=nodes.toArray(new String[0]));
    }
    public void applyPreset(int slot){
        checkPresetSlot(slot);if(active)throw new IllegalStateException("원정을 끝내거나 포기한 뒤 프리셋을 바꿀 수 있습니다.");
        if(presets[slot]==null)throw new IllegalStateException("저장된 프리셋이 없습니다.");
        HashSet<String> target=new HashSet<>(java.util.Arrays.asList(presets[slot]));int cost=allocationCost(target);
        int budget=(int)Math.min(MAX_GROWTH_POINTS,(long)points+spentPoints());
        if(cost>budget)throw new IllegalStateException("프리셋에 "+cost+" P가 필요합니다. 현재 총 "+budget+" P입니다.");
        HashSet<String> valid=new HashSet<>();boolean progress;
        do{progress=false;for(String id:target)if(!valid.contains(id)&&ExtractionGrowth.unlocked(ExtractionGrowth.NODES[ExtractionGrowth.index(id)],valid)){valid.add(id);progress=true;}}while(progress);
        if(valid.size()!=target.size())throw new IllegalStateException("프리셋의 선행 노드가 부족합니다.");
        change(()->{nodes.clear();nodes.addAll(target);points=budget-cost;trimPrepared();});
    }
    public void buyPotion() {
        buy(0);
    }
    /** Abandon without loading a possibly broken run; only credited XP is retained. */
    public void abandon() {
        if(!active)return;
        change(()->{
            active=false;escrow.clear();raidXP=0;
            result="원정을 포기했습니다. 출격 물품과 전리품은 잃었지만 창고·성장 노드·획득한 성장 경험치는 유지됩니다.";
        });
        migrateGrowthCap();
        Dungeon.deleteGame(1,true);
        Dungeon.hero=null;Dungeon.level=null;
    }
    public void buy(final int offerIndex) {
        if(active)throw new IllegalStateException("원정 중에는 거점 상점을 이용할 수 없습니다.");
        if(offerIndex<0||offerIndex>=ExtractionShop.OFFERS.size())throw new IllegalArgumentException("없는 상품입니다.");
        ExtractionShop.Offer offer=ExtractionShop.OFFERS.get(offerIndex);
        if(!offer.available())throw new IllegalStateException("T3 이상 장비는 원정에서 파밍해야 합니다.");
        if(gold<offer.price)throw new IllegalStateException("골드가 부족합니다.");
        change(() -> { gold-=offer.price;stash.add(offer.item()); });
    }
    public void sell(final Item i) {
        sell(i,true);
    }
    public void sell(final Item i,final boolean all) {
        if(active)throw new IllegalStateException("원정 중에는 거점 상점을 이용할 수 없습니다.");
        if(!stash.contains(i))return;
        boolean one=!all&&ExtractionShop.canSellOne(i);
        Item sold=one?i.duplicate().quantity(1):i;
        final int price=ExtractionShop.salePrice(sold);
        if(price<=0)throw new IllegalStateException("이 물품은 판매할 수 없습니다.");
        change(() -> { gold+=price;if(one)i.quantity(i.quantity()-1);else stash.remove(i); });
    }
}
