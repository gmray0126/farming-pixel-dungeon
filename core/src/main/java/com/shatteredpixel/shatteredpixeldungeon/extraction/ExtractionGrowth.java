/* Farming Pixel Dungeon fork. GPL-3.0-or-later; original credits retained. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import java.util.*;

/** Permanent skill graph. Stable legacy identifiers keep existing saves compatible. */
public final class ExtractionGrowth {
    private ExtractionGrowth() {}
    public static final int MAGIC = 11, FIST = 9;
    public static final String[] BRANCHES = {"전투","생존","탐사","검","대검","단검·쌍검","창","도끼","둔기·방패","격투","활·투척","마도","정찰","야전 치료","식량 활용","잠행","기세","위기 대응","물약 지식","수호 기술","비전 기술","그림자 기술","자연 기술","기동 기술","기도 기술"};
    public enum Stat {
        KNOW_HEALING("회복 물약 식별",false), KNOW_STRENGTH("힘 물약 식별",false), KNOW_EXPERIENCE("경험 물약 식별",false),
        KNOW_MINDVISION("심안 물약 식별",false), KNOW_LEVITATION("부유 물약 식별",false), KNOW_INVISIBILITY("투명 물약 식별",false),
        KNOW_HASTE("신속 물약 식별",false), KNOW_PURITY("정화 물약 식별",false), KNOW_FROST("서리 물약 식별",false),
        KNOW_FLAME("화염 물약 식별",false), KNOW_TOXIC("유독 가스 물약 식별",false), KNOW_PARALYTIC("마비 가스 물약 식별",false),
        DAMAGE("공격 피해", false), DAMAGE_PERCENT("공격 피해", true),
        ACCURACY("명중", true), ATTACK_SPEED("공격 속도", true),
        CRIT_CHANCE("치명타 확률", true), CRIT_POWER("치명타 배율", true),
        PIERCE("방어 관통", false), REACH("근접 사거리", false),
        HEALTH("출격 최대 체력", false), DEFENSE("피해 감소", false),
        EVASION("회피", true), MOVE_SPEED("이동 속도", true),
        CAPACITY("가방 칸", false), STRENGTH("출격 힘", false), GOLD("탈출 전리품 골드", true),
        WAND_DAMAGE("마법 막대 직접 피해", false), WAND_POWER("마법 막대 직접 피해", true), WAND_CHARGE("마법 막대 충전 속도", true),
        VISION("시야 거리",false), SEARCH_RANGE("탐색 반경",false), SEARCH_CHANCE("자동 탐색 발견 확률",true), SEARCH_SPEED("수동 탐색 시간 감소",true),
        FLOOR_AWARENESS("새 층 첫 진입 시 물품 감지 지속 턴",false), FLOOR_MINDVISION("새 층 첫 진입 시 층 내 적 감지 지속 턴",false), FLOOR_FORESIGHT("새 층 첫 진입 시 비밀 탐색 지속 턴",false),
        POTION_HEAL("회복 물약 회복량",true), POTION_SHIELD("회복 물약 사용 시 보호막",false), POTION_RECHARGE("회복 물약 사용 시 마법 충전 지속 턴",false), POTION_HASTE("회복 물약 사용 시 가속 지속 턴",false),
        FLOOR_HEAL("새 층 첫 진입 시 체력 회복",false), FLOOR_SHIELD("새 층 첫 진입 시 보호막",false), SECOND_WIND("원정당 한 번 치명상 방지 시 회복할 최대 체력 비율",true),
        FOOD_VALUE("음식 포만감",true), FOOD_HEAL("식사 시 체력 회복",false), FOOD_SHIELD("식사 시 보호막",false), FOOD_HASTE("식사 시 가속 지속 턴",false), FOOD_RECHARGE("식사 시 마법 충전 지속 턴",false), FOOD_INVIS("식사 시 투명화 지속 턴",false), EAT_SPEED("식사 시간 감소",true), HUNGER_SLOW("허기 증가량 감소",true),
        STEALTH("은밀성",false), SURPRISE_DAMAGE("기습 피해",true), SURPRISE_RECHARGE("기습 시 마법 충전 지속 턴",false), TRAP_REDUCTION("함정 직접 피해 감소",true), FALL_REDUCTION("추락 직접 피해 감소",true), FLOOR_INVIS("새 층 첫 진입 시 투명화 지속 턴",false),
        KILL_HEAL("주변 적 처치 시 체력 회복",false), KILL_SHIELD("주변 적 처치 시 보호막",false), KILL_HASTE("주변 적 처치 시 가속 지속 턴",false), KILL_RECHARGE("주변 적 처치 시 마법 충전 지속 턴",false), LOW_HP_EVASION("체력 35% 이하 회피",true), LOW_HP_DAMAGE("체력 35% 이하 공격 피해",true), LOW_HP_SPEED("체력 35% 이하 이동 속도",true),
        DOT_REDUCTION("독·화상·출혈·산성 피해 감소",true), POISON_REDUCTION("독 피해 감소",true), BURN_REDUCTION("화상 피해 감소",true), BLEED_REDUCTION("출혈 피해 감소",true), BOSS_REDUCTION("보스 직접 피해 감소",true), RANGED_REDUCTION("멀리 있는 적의 직접 피해 감소",true), PRAYER_CAPACITY("기도 최대 충전",false);
        public final String label; public final boolean percent;
        Stat(String label, boolean percent) { this.label=label; this.percent=percent; }
    }
    public static final class Node {
        public final String id, name; public final int branch, row, col, cost;
        private final String[] required;
        public int[] parents;
        public int[] alternatives=new int[0];
        public com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent;
        public int talentRank; public String skill,utilityDescription;
        public final EnumMap<Stat,Float> effects=new EnumMap<>(Stat.class);
        Node(String id,String name,int branch,int row,int col,int cost,String[] required,Object... effects) {
            this.id=id;this.name=name;this.branch=branch;this.row=row;this.col=col;this.cost=cost;this.required=required;
            for(int i=0;i<effects.length;i+=2)this.effects.put((Stat)effects[i],(Float)effects[i+1]);
        }
        public String description() {
            if(utilityDescription!=null)return utilityDescription;
            if(branch==18)return name+"을 습득하면 매 원정 시작부터 해당 물약의 정체를 압니다. 색이 달라져도 효과가 유지됩니다.\n\n대응하는 특수 물약도 함께 식별됩니다. 다른 종류의 물약은 직접 식별해야 합니다.";
            StringBuilder text=new StringBuilder();
            for(Map.Entry<Stat,Float> e:effects.entrySet()){
                if(text.length()>0)text.append("\n");
                text.append(e.getKey().label).append(" +").append(Math.round(e.getValue()));
                if(e.getKey().percent)text.append('%');
            }
            if(branch>=3&&branch<12)text.append("\n\n").append(BRANCHES[branch]).append(" 계통의 무기를 사용할 때 적용됩니다.");
            if(branch>=12)text.append("\n\n무기 종류와 관계없이 적용됩니다. 보호막은 누적되지 않으며, 새 층 효과는 원정 중 해당 층의 첫 방문에만 발동합니다.");
            if(branch==MAGIC)text.append(" 마법 막대 충전은 보유한 막대에 적용됩니다.");
            if(effects.containsKey(Stat.SECOND_WIND))text.append("\n두 생존 최종 특성은 원정당 한 번의 발동 횟수를 공유합니다.");
            if(effects.containsKey(Stat.CRIT_CHANCE)||effects.containsKey(Stat.CRIT_POWER))text.append("\n치명타의 기본 피해 배율은 150%입니다.");
            if(effects.containsKey(Stat.STRENGTH))text.append("\n\n영구 힘 보너스입니다. 사망·탈출 후에도 유지되며 매 새 출격에 적용됩니다. 힘 물약으로 얻은 힘은 해당 원정에서만 유지됩니다.");
            if(alternatives.length>0)text.append("\n\n연결된 다른 계통의 노드에서도 진입할 수 있습니다.");
            if(parents.length>1)text.append("\n\n양쪽 갈래의 선행 노드를 모두 습득해야 합니다.");
            return text.toString();
        }
        public String summary() {
            if(utilityDescription!=null)return skill!=null?"사용 스킬":talent!=null?talentRank+"단계":effects.isEmpty()?"고유 능력":"기도 +2";
            if(branch==18)return "미리 식별";
            Stat s=effects.keySet().iterator().next();
            String label;
            switch(s){
                case DAMAGE: case DAMAGE_PERCENT:label="피해";break;
                case ACCURACY:label="명중";break;
                case ATTACK_SPEED:label="공속";break;
                case CRIT_CHANCE:label="치명률";break;
                case CRIT_POWER:label="치명 배율";break;
                case PIERCE:label="관통";break;
                case REACH:label="사거리";break;
                case HEALTH:label="HP";break;
                case DEFENSE:label="방어";break;
                case EVASION:label="회피";break;
                case MOVE_SPEED:label="이속";break;
                case CAPACITY:label="가방";break;
                case STRENGTH:label="힘";break;
                case GOLD:label="골드";break;
                case WAND_CHARGE:label="충전";break;
                default:label="마법";
            }
            return label+" +"+Math.round(effects.get(s))+(s.percent?"%":"");
        }
    }
    public static final Node[] NODES;
    public static final int[][] BRANCH_NODES;
    public static final String[] IDS,NAMES,DESCS;
    public static final int[] COSTS,PARENTS;
    static {
        ArrayList<Node> list=new ArrayList<>();
        add(list, "power", "무기 숙련", 0, 0, 1, 1, new String[]{}, Stat.DAMAGE, 1f);
        add(list, "edge", "예리한 칼날", 0, 1, 0, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "master", "전투의 기억", 0, 2, 0, 2, new String[]{"edge"}, Stat.DAMAGE, 2f);
        add(list, "vital", "튼튼한 몸", 1, 0, 1, 1, new String[]{}, Stat.HEALTH, 6f);
        add(list, "guard", "방어 훈련", 1, 1, 0, 1, new String[]{"vital"}, Stat.DEFENSE, 1f);
        add(list, "iron", "철벽", 1, 2, 0, 2, new String[]{"guard"}, Stat.DEFENSE, 1f);
        add(list, "pack", "짐 정리", 2, 0, 1, 1, new String[]{}, Stat.CAPACITY, 2f);
        add(list, "porter", "원정 준비", 2, 1, 0, 1, new String[]{"pack"}, Stat.CAPACITY, 2f);
        add(list, "strength", "완력", 2, 2, 0, 2, new String[]{"porter"}, Stat.STRENGTH, 2f);
        add(list, "combat_left_end", "강타", 0, 3, 0, 3, new String[]{"master"}, Stat.DAMAGE, 2f);
        add(list, "combat_right_1", "정밀 조준", 0, 1, 2, 1, new String[]{"power"}, Stat.ACCURACY, 10f);
        add(list, "combat_right_2", "집중", 0, 2, 2, 2, new String[]{"combat_right_1"}, Stat.ACCURACY, 10f);
        add(list, "combat_right_end", "급소 포착", 0, 3, 2, 3, new String[]{"combat_right_2"}, Stat.CRIT_CHANCE, 5f);
        add(list, "combat_merge", "노련한 전투", 0, 4, 1, 4, new String[]{"combat_left_end", "combat_right_end"}, Stat.DAMAGE_PERCENT, 10f);
        add(list, "combat_cap", "전장의 달인", 0, 5, 1, 5, new String[]{"combat_merge"}, Stat.DAMAGE_PERCENT, 10f, Stat.CRIT_CHANCE, 10f);
        add(list, "survival_left_end", "중갑 훈련", 1, 3, 0, 3, new String[]{"iron"}, Stat.DEFENSE, 2f);
        add(list, "survival_right_1", "생명력", 1, 1, 2, 1, new String[]{"vital"}, Stat.HEALTH, 6f);
        add(list, "survival_right_2", "회피 훈련", 1, 2, 2, 2, new String[]{"survival_right_1"}, Stat.EVASION, 10f);
        add(list, "survival_right_end", "끈질긴 생존", 1, 3, 2, 3, new String[]{"survival_right_2"}, Stat.HEALTH, 8f, Stat.EVASION, 10f);
        add(list, "survival_merge", "불굴", 1, 4, 1, 4, new String[]{"survival_left_end", "survival_right_end"}, Stat.HEALTH, 10f, Stat.DEFENSE, 2f);
        add(list, "survival_cap", "생존의 달인", 1, 5, 1, 5, new String[]{"survival_merge"}, Stat.HEALTH, 12f, Stat.DEFENSE, 3f);
        add(list, "explore_left_end", "괴력", 2, 3, 0, 3, new String[]{"strength"}, Stat.STRENGTH, 1f);
        add(list, "explore_right_1", "가벼운 발", 2, 1, 2, 1, new String[]{"pack"}, Stat.MOVE_SPEED, 5f);
        add(list, "explore_right_2", "수집가", 2, 2, 2, 2, new String[]{"explore_right_1"}, Stat.GOLD, 10f);
        add(list, "explore_right_end", "능숙한 이동", 2, 3, 2, 3, new String[]{"explore_right_2"}, Stat.MOVE_SPEED, 5f, Stat.GOLD, 10f);
        add(list, "explore_merge", "인양 전문가", 2, 4, 1, 4, new String[]{"explore_left_end", "explore_right_end"}, Stat.CAPACITY, 2f, Stat.GOLD, 10f);
        add(list, "explore_cap", "탐사의 달인", 2, 5, 1, 5, new String[]{"explore_merge"}, Stat.CAPACITY, 2f, Stat.STRENGTH, 1f, Stat.GOLD, 10f);
        add(list, "sword_0", "검술 입문", 3, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "sword_1", "칼날 연마", 3, 1, 0, 1, new String[]{"sword_0"}, Stat.DAMAGE_PERCENT, 8f);
        add(list, "sword_2", "베어 넘기기", 3, 2, 0, 2, new String[]{"sword_1"}, Stat.DAMAGE, 1f);
        add(list, "sword_3", "급소 베기", 3, 3, 0, 3, new String[]{"sword_2"}, Stat.CRIT_CHANCE, 7f);
        add(list, "sword_4", "검의 조준", 3, 1, 2, 1, new String[]{"sword_0"}, Stat.ACCURACY, 10f);
        add(list, "sword_5", "연속 베기", 3, 2, 2, 2, new String[]{"sword_4"}, Stat.ATTACK_SPEED, 8f);
        add(list, "sword_6", "검으로 방어", 3, 3, 2, 3, new String[]{"sword_5"}, Stat.DEFENSE, 1f);
        add(list, "sword_7", "검술 완성", 3, 4, 1, 4, new String[]{"sword_3", "sword_6"}, Stat.DAMAGE_PERCENT, 10f, Stat.ATTACK_SPEED, 5f);
        add(list, "sword_8", "검의 달인", 3, 5, 1, 5, new String[]{"sword_7"}, Stat.DAMAGE_PERCENT, 15f, Stat.CRIT_CHANCE, 8f);
        add(list, "greatsword_0", "대검 입문", 4, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 2f);
        add(list, "greatsword_1", "묵직한 날", 4, 1, 0, 1, new String[]{"greatsword_0"}, Stat.DAMAGE_PERCENT, 12f);
        add(list, "greatsword_2", "양손 강타", 4, 2, 0, 2, new String[]{"greatsword_1"}, Stat.DAMAGE, 3f);
        add(list, "greatsword_3", "일격 필살", 4, 3, 0, 3, new String[]{"greatsword_2"}, Stat.CRIT_CHANCE, 12f);
        add(list, "greatsword_4", "흔들림 없이", 4, 1, 2, 1, new String[]{"greatsword_0"}, Stat.ACCURACY, 10f);
        add(list, "greatsword_5", "견고한 자세", 4, 2, 2, 2, new String[]{"greatsword_4"}, Stat.DEFENSE, 1f);
        add(list, "greatsword_6", "중량 방어", 4, 3, 2, 3, new String[]{"greatsword_5"}, Stat.DEFENSE, 2f);
        add(list, "greatsword_7", "중검 완성", 4, 4, 1, 4, new String[]{"greatsword_3", "greatsword_6"}, Stat.DAMAGE_PERCENT, 10f, Stat.PIERCE, 1f);
        add(list, "greatsword_8", "대검의 달인", 4, 5, 1, 5, new String[]{"greatsword_7"}, Stat.DAMAGE_PERCENT, 20f, Stat.PIERCE, 2f);
        add(list, "dagger_0", "단검 입문", 5, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "dagger_1", "급소 찾기", 5, 1, 0, 1, new String[]{"dagger_0"}, Stat.CRIT_CHANCE, 6f);
        add(list, "dagger_2", "깊은 상처", 5, 2, 0, 2, new String[]{"dagger_1"}, Stat.CRIT_POWER, 25f);
        add(list, "dagger_3", "치명적 찌름", 5, 3, 0, 3, new String[]{"dagger_2"}, Stat.CRIT_CHANCE, 8f);
        add(list, "dagger_4", "정밀한 찌름", 5, 1, 2, 1, new String[]{"dagger_0"}, Stat.ACCURACY, 12f);
        add(list, "dagger_5", "쌍검 연타", 5, 2, 2, 2, new String[]{"dagger_4"}, Stat.ATTACK_SPEED, 12f);
        add(list, "dagger_6", "유연한 몸", 5, 3, 2, 3, new String[]{"dagger_5"}, Stat.EVASION, 8f);
        add(list, "dagger_7", "암살의 기술", 5, 4, 1, 4, new String[]{"dagger_3", "dagger_6"}, Stat.ATTACK_SPEED, 8f, Stat.CRIT_CHANCE, 5f);
        add(list, "dagger_8", "단검의 달인", 5, 5, 1, 5, new String[]{"dagger_7"}, Stat.CRIT_CHANCE, 10f, Stat.CRIT_POWER, 25f);
        add(list, "spear_0", "창술 입문", 6, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "spear_1", "창끝 연마", 6, 1, 0, 1, new String[]{"spear_0"}, Stat.DAMAGE_PERCENT, 8f);
        add(list, "spear_2", "틈새 찌름", 6, 2, 0, 2, new String[]{"spear_1"}, Stat.PIERCE, 1f);
        add(list, "spear_3", "긴 사거리", 6, 3, 0, 3, new String[]{"spear_2"}, Stat.REACH, 1f);
        add(list, "spear_4", "창의 조준", 6, 1, 2, 1, new String[]{"spear_0"}, Stat.ACCURACY, 12f);
        add(list, "spear_5", "간격 유지", 6, 2, 2, 2, new String[]{"spear_4"}, Stat.DEFENSE, 1f);
        add(list, "spear_6", "빠른 찌름", 6, 3, 2, 3, new String[]{"spear_5"}, Stat.ATTACK_SPEED, 8f);
        add(list, "spear_7", "창술 완성", 6, 4, 1, 4, new String[]{"spear_3", "spear_6"}, Stat.PIERCE, 1f, Stat.DAMAGE_PERCENT, 10f);
        add(list, "spear_8", "창의 달인", 6, 5, 1, 5, new String[]{"spear_7"}, Stat.REACH, 1f, Stat.CRIT_CHANCE, 8f);
        add(list, "axe_0", "도끼 입문", 7, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "axe_1", "날 세우기", 7, 1, 0, 1, new String[]{"axe_0"}, Stat.DAMAGE_PERCENT, 10f);
        add(list, "axe_2", "힘찬 찍기", 7, 2, 0, 2, new String[]{"axe_1"}, Stat.DAMAGE, 2f);
        add(list, "axe_3", "급소 쪼개기", 7, 3, 0, 3, new String[]{"axe_2"}, Stat.CRIT_CHANCE, 10f);
        add(list, "axe_4", "도끼 조준", 7, 1, 2, 1, new String[]{"axe_0"}, Stat.ACCURACY, 8f);
        add(list, "axe_5", "갑옷 가르기", 7, 2, 2, 2, new String[]{"axe_4"}, Stat.PIERCE, 1f);
        add(list, "axe_6", "빠른 휘두름", 7, 3, 2, 3, new String[]{"axe_5"}, Stat.ATTACK_SPEED, 8f);
        add(list, "axe_7", "도끼술 완성", 7, 4, 1, 4, new String[]{"axe_3", "axe_6"}, Stat.PIERCE, 1f, Stat.DAMAGE_PERCENT, 10f);
        add(list, "axe_8", "도끼의 달인", 7, 5, 1, 5, new String[]{"axe_7"}, Stat.DAMAGE_PERCENT, 18f, Stat.CRIT_CHANCE, 8f);
        add(list, "blunt_0", "둔기 입문", 8, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "blunt_1", "묵직한 타격", 8, 1, 0, 1, new String[]{"blunt_0"}, Stat.DAMAGE_PERCENT, 10f);
        add(list, "blunt_2", "갑옷 깨기", 8, 2, 0, 2, new String[]{"blunt_1"}, Stat.PIERCE, 1f);
        add(list, "blunt_3", "분쇄", 8, 3, 0, 3, new String[]{"blunt_2"}, Stat.PIERCE, 2f);
        add(list, "blunt_4", "방패 자세", 8, 1, 2, 1, new String[]{"blunt_0"}, Stat.DEFENSE, 1f);
        add(list, "blunt_5", "견고한 방패", 8, 2, 2, 2, new String[]{"blunt_4"}, Stat.DEFENSE, 1f);
        add(list, "blunt_6", "방어 기동", 8, 3, 2, 3, new String[]{"blunt_5"}, Stat.EVASION, 8f);
        add(list, "blunt_7", "공수 일체", 8, 4, 1, 4, new String[]{"blunt_3", "blunt_6"}, Stat.DEFENSE, 2f, Stat.DAMAGE_PERCENT, 8f);
        add(list, "blunt_8", "둔기의 달인", 8, 5, 1, 5, new String[]{"blunt_7"}, Stat.DEFENSE, 2f, Stat.PIERCE, 1f);
        add(list, "fist_0", "격투 입문", 9, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "fist_1", "빠른 주먹", 9, 1, 0, 1, new String[]{"fist_0"}, Stat.ATTACK_SPEED, 8f);
        add(list, "fist_2", "연속 타격", 9, 2, 0, 2, new String[]{"fist_1"}, Stat.ATTACK_SPEED, 10f);
        add(list, "fist_3", "급소 타격", 9, 3, 0, 3, new String[]{"fist_2"}, Stat.CRIT_CHANCE, 8f);
        add(list, "fist_4", "몸놀림", 9, 1, 2, 1, new String[]{"fist_0"}, Stat.EVASION, 8f);
        add(list, "fist_5", "단단한 몸", 9, 2, 2, 2, new String[]{"fist_4"}, Stat.DEFENSE, 1f);
        add(list, "fist_6", "정밀 타격", 9, 3, 2, 3, new String[]{"fist_5"}, Stat.ACCURACY, 10f);
        add(list, "fist_7", "격투 완성", 9, 4, 1, 4, new String[]{"fist_3", "fist_6"}, Stat.DAMAGE_PERCENT, 12f, Stat.EVASION, 8f);
        add(list, "fist_8", "격투의 달인", 9, 5, 1, 5, new String[]{"fist_7"}, Stat.CRIT_CHANCE, 10f, Stat.ATTACK_SPEED, 10f);
        add(list, "ranged_0", "사격 입문", 10, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f);
        add(list, "ranged_1", "탄도 이해", 10, 1, 0, 1, new String[]{"ranged_0"}, Stat.DAMAGE_PERCENT, 10f);
        add(list, "ranged_2", "급소 사격", 10, 2, 0, 2, new String[]{"ranged_1"}, Stat.CRIT_CHANCE, 8f);
        add(list, "ranged_3", "관통 사격", 10, 3, 0, 3, new String[]{"ranged_2"}, Stat.PIERCE, 1f);
        add(list, "ranged_4", "조준 훈련", 10, 1, 2, 1, new String[]{"ranged_0"}, Stat.ACCURACY, 10f);
        add(list, "ranged_5", "정밀 사격", 10, 2, 2, 2, new String[]{"ranged_4"}, Stat.ACCURACY, 10f);
        add(list, "ranged_6", "속사", 10, 3, 2, 3, new String[]{"ranged_5"}, Stat.ATTACK_SPEED, 10f);
        add(list, "ranged_7", "사격술 완성", 10, 4, 1, 4, new String[]{"ranged_3", "ranged_6"}, Stat.DAMAGE_PERCENT, 12f, Stat.ATTACK_SPEED, 8f);
        add(list, "ranged_8", "원거리 달인", 10, 5, 1, 5, new String[]{"ranged_7"}, Stat.DAMAGE_PERCENT, 15f, Stat.CRIT_CHANCE, 10f);
        add(list, "magic_0", "마도 입문", 11, 0, 1, 1, new String[]{"power"}, Stat.DAMAGE, 1f, Stat.WAND_DAMAGE, 1f);
        add(list, "magic_1", "마력 집중", 11, 1, 0, 1, new String[]{"magic_0"}, Stat.WAND_POWER, 10f, Stat.DAMAGE_PERCENT, 8f);
        add(list, "magic_2", "응축된 마력", 11, 2, 0, 2, new String[]{"magic_1"}, Stat.WAND_DAMAGE, 2f, Stat.DAMAGE, 1f);
        add(list, "magic_3", "마력 폭발", 11, 3, 0, 3, new String[]{"magic_2"}, Stat.WAND_POWER, 15f, Stat.CRIT_CHANCE, 8f);
        add(list, "magic_4", "충전 훈련", 11, 1, 2, 1, new String[]{"magic_0"}, Stat.WAND_CHARGE, 10f);
        add(list, "magic_5", "마력 순환", 11, 2, 2, 2, new String[]{"magic_4"}, Stat.WAND_CHARGE, 15f);
        add(list, "magic_6", "고속 충전", 11, 3, 2, 3, new String[]{"magic_5"}, Stat.WAND_CHARGE, 20f);
        add(list, "magic_7", "마도 완성", 11, 4, 1, 4, new String[]{"magic_3", "magic_6"}, Stat.WAND_POWER, 10f, Stat.WAND_CHARGE, 10f, Stat.ATTACK_SPEED, 8f);
        add(list, "magic_8", "마도의 달인", 11, 5, 1, 5, new String[]{"magic_7"}, Stat.WAND_DAMAGE, 2f, Stat.WAND_POWER, 20f, Stat.DAMAGE_PERCENT, 15f);
        add(list, "scout_0", "등불의 눈", 12, 0, 1, 1, new String[]{"pack"}, Stat.VISION, 1.0f);
        add(list, "scout_1", "넓은 수색", 12, 1, 0, 1, new String[]{"scout_0"}, Stat.SEARCH_RANGE, 1.0f);
        add(list, "scout_2", "수상한 흔적", 12, 2, 0, 2, new String[]{"scout_1"}, Stat.SEARCH_CHANCE, 20.0f);
        add(list, "scout_3", "노련한 수색", 12, 3, 0, 3, new String[]{"scout_2"}, Stat.SEARCH_CHANCE, 25.0f, Stat.SEARCH_SPEED, 50.0f);
        add(list, "scout_4", "보물의 기척", 12, 1, 2, 1, new String[]{"scout_0"}, Stat.FLOOR_AWARENESS, 8.0f);
        add(list, "scout_5", "생명의 기척", 12, 2, 2, 2, new String[]{"scout_4"}, Stat.FLOOR_MINDVISION, 5.0f);
        add(list, "scout_6", "위험 예감", 12, 3, 2, 3, new String[]{"scout_5"}, Stat.FLOOR_FORESIGHT, 8.0f);
        add(list, "scout_7", "길잡이", 12, 4, 1, 4, new String[]{"scout_3", "scout_6"}, Stat.VISION, 1.0f, Stat.SEARCH_RANGE, 1.0f);
        add(list, "scout_8", "심층 정찰", 12, 5, 1, 5, new String[]{"scout_7"}, Stat.FLOOR_AWARENESS, 12.0f, Stat.FLOOR_MINDVISION, 10.0f, Stat.FLOOR_FORESIGHT, 8.0f);
        add(list, "medic_0", "응급 약제", 13, 0, 1, 1, new String[]{"vital"}, Stat.POTION_HEAL, 15.0f);
        add(list, "medic_1", "보호 처방", 13, 1, 0, 1, new String[]{"medic_0"}, Stat.POTION_SHIELD, 5.0f);
        add(list, "medic_2", "마력 처방", 13, 2, 0, 2, new String[]{"medic_1"}, Stat.POTION_RECHARGE, 4.0f);
        add(list, "medic_3", "신속 처방", 13, 3, 0, 3, new String[]{"medic_2"}, Stat.POTION_HASTE, 3.0f);
        add(list, "medic_4", "숨 돌리기", 13, 1, 2, 1, new String[]{"medic_0"}, Stat.FLOOR_HEAL, 3.0f);
        add(list, "medic_5", "회복 거점", 13, 2, 2, 2, new String[]{"medic_4"}, Stat.FLOOR_HEAL, 3.0f);
        add(list, "medic_6", "정비의 장막", 13, 3, 2, 3, new String[]{"medic_5"}, Stat.FLOOR_SHIELD, 6.0f);
        add(list, "medic_7", "야전 의무관", 13, 4, 1, 4, new String[]{"medic_3", "medic_6"}, Stat.POTION_HEAL, 20.0f, Stat.FLOOR_HEAL, 2.0f);
        add(list, "medic_8", "마지막 처방", 13, 5, 1, 5, new String[]{"medic_7"}, Stat.SECOND_WIND, 25.0f, Stat.POTION_SHIELD, 5.0f);
        add(list, "food_0", "알뜰한 식량", 14, 0, 1, 1, new String[]{"pack"}, Stat.FOOD_VALUE, 15.0f);
        add(list, "food_1", "치유의 식사", 14, 1, 0, 1, new String[]{"food_0"}, Stat.FOOD_HEAL, 3.0f);
        add(list, "food_2", "든든한 한 끼", 14, 2, 0, 2, new String[]{"food_1"}, Stat.FOOD_SHIELD, 5.0f);
        add(list, "food_3", "활력 식사", 14, 3, 0, 3, new String[]{"food_2"}, Stat.FOOD_HASTE, 3.0f);
        add(list, "food_4", "절약 대사", 14, 1, 2, 1, new String[]{"food_0"}, Stat.HUNGER_SLOW, 10.0f);
        add(list, "food_5", "빠른 식사", 14, 2, 2, 2, new String[]{"food_4"}, Stat.EAT_SPEED, 50.0f);
        add(list, "food_6", "마력의 식사", 14, 3, 2, 3, new String[]{"food_5"}, Stat.FOOD_RECHARGE, 8.0f);
        add(list, "food_7", "원정 요리사", 14, 4, 1, 4, new String[]{"food_3", "food_6"}, Stat.FOOD_VALUE, 20.0f, Stat.FOOD_HEAL, 3.0f);
        add(list, "food_8", "조용한 만찬", 14, 5, 1, 5, new String[]{"food_7"}, Stat.FOOD_INVIS, 5.0f, Stat.HUNGER_SLOW, 10.0f);
        add(list, "stealth_0", "조용한 발걸음", 15, 0, 1, 1, new String[]{"explore_right_1"}, Stat.STEALTH, 1.0f);
        add(list, "stealth_1", "빈틈 노리기", 15, 1, 0, 1, new String[]{"stealth_0"}, Stat.SURPRISE_DAMAGE, 15.0f);
        add(list, "stealth_2", "어둠의 일격", 15, 2, 0, 2, new String[]{"stealth_1"}, Stat.SURPRISE_DAMAGE, 15.0f);
        add(list, "stealth_3", "마력 탈취", 15, 3, 0, 3, new String[]{"stealth_2"}, Stat.SURPRISE_RECHARGE, 3.0f);
        add(list, "stealth_4", "함정 대비", 15, 1, 2, 1, new String[]{"stealth_0"}, Stat.TRAP_REDUCTION, 20.0f);
        add(list, "stealth_5", "낙법", 15, 2, 2, 2, new String[]{"stealth_4"}, Stat.FALL_REDUCTION, 35.0f);
        add(list, "stealth_6", "은밀한 진입", 15, 3, 2, 3, new String[]{"stealth_5"}, Stat.FLOOR_INVIS, 3.0f);
        add(list, "stealth_7", "그림자 보행", 15, 4, 1, 4, new String[]{"stealth_3", "stealth_6"}, Stat.STEALTH, 1.0f, Stat.TRAP_REDUCTION, 15.0f);
        add(list, "stealth_8", "그림자 원정자", 15, 5, 1, 5, new String[]{"stealth_7"}, Stat.SURPRISE_DAMAGE, 20.0f, Stat.FLOOR_INVIS, 5.0f);
        add(list, "momentum_0", "전투 회복", 16, 0, 1, 1, new String[]{"power"}, Stat.KILL_HEAL, 1.0f);
        add(list, "momentum_1", "승리의 장막", 16, 1, 0, 1, new String[]{"momentum_0"}, Stat.KILL_SHIELD, 3.0f);
        add(list, "momentum_2", "추격 본능", 16, 2, 0, 2, new String[]{"momentum_1"}, Stat.KILL_HASTE, 2.0f);
        add(list, "momentum_3", "마력 수확", 16, 3, 0, 3, new String[]{"momentum_2"}, Stat.KILL_RECHARGE, 3.0f);
        add(list, "momentum_4", "위기의 몸놀림", 16, 1, 2, 1, new String[]{"momentum_0"}, Stat.LOW_HP_EVASION, 15.0f);
        add(list, "momentum_5", "궁지의 일격", 16, 2, 2, 2, new String[]{"momentum_4"}, Stat.LOW_HP_DAMAGE, 15.0f);
        add(list, "momentum_6", "탈출 본능", 16, 3, 2, 3, new String[]{"momentum_5"}, Stat.LOW_HP_SPEED, 15.0f);
        add(list, "momentum_7", "끊기지 않는 기세", 16, 4, 1, 4, new String[]{"momentum_3", "momentum_6"}, Stat.KILL_HEAL, 1.0f, Stat.KILL_SHIELD, 2.0f);
        add(list, "momentum_8", "역전의 기세", 16, 5, 1, 5, new String[]{"momentum_7"}, Stat.KILL_HASTE, 2.0f, Stat.LOW_HP_DAMAGE, 15.0f);
        add(list, "ward_0", "환경 적응", 17, 0, 1, 1, new String[]{"vital"}, Stat.DOT_REDUCTION, 10.0f);
        add(list, "ward_1", "독 내성", 17, 1, 0, 1, new String[]{"ward_0"}, Stat.POISON_REDUCTION, 20.0f);
        add(list, "ward_2", "화상 내성", 17, 2, 0, 2, new String[]{"ward_1"}, Stat.BURN_REDUCTION, 25.0f);
        add(list, "ward_3", "출혈 억제", 17, 3, 0, 3, new String[]{"ward_2"}, Stat.BLEED_REDUCTION, 25.0f);
        add(list, "ward_4", "보스 대비", 17, 1, 2, 1, new String[]{"ward_0"}, Stat.BOSS_REDUCTION, 10.0f);
        add(list, "ward_5", "원거리 대비", 17, 2, 2, 2, new String[]{"ward_4"}, Stat.RANGED_REDUCTION, 15.0f);
        add(list, "ward_6", "안전 진입", 17, 3, 2, 3, new String[]{"ward_5"}, Stat.FLOOR_SHIELD, 6.0f);
        add(list, "ward_7", "원정 방호", 17, 4, 1, 4, new String[]{"ward_3", "ward_6"}, Stat.DOT_REDUCTION, 10.0f, Stat.BOSS_REDUCTION, 5.0f);
        add(list, "ward_8", "생존 안전망", 17, 5, 1, 5, new String[]{"ward_7"}, Stat.SECOND_WIND, 10.0f, Stat.FLOOR_SHIELD, 8.0f);
        for(int i=0;i<ExtractionPotionKnowledge.IDS.length;i++)
            add(list,ExtractionPotionKnowledge.IDS[i],ExtractionPotionKnowledge.NAMES[i]+" 지식",18,i,1,1,new String[]{"pack"},ExtractionPotionKnowledge.STATS[i],1f);
        // Common strength milestones: any common specialization can unlock them.
        // Append stable IDs so old saves and existing node indices remain compatible.
        add(list,"strength_early","기초 근력",0,6,0,2,new String[]{"edge"},Stat.STRENGTH,1f);
        add(list,"strength_mid","신체 단련",1,6,2,3,new String[]{"survival_left_end"},Stat.STRENGTH,1f);
        add(list,"strength_advanced","중량 적응",2,6,0,4,new String[]{"explore_merge"},Stat.STRENGTH,1f);
        add(list,"strength_master","완력 완성",0,6,2,5,new String[]{"combat_cap"},Stat.STRENGTH,3f);
        ExtractionClassUtilities.append(list);
        NODES=list.toArray(new Node[0]);
        IDS=new String[NODES.length];NAMES=new String[NODES.length];DESCS=new String[NODES.length];
        COSTS=new int[NODES.length];PARENTS=new int[NODES.length];
        BRANCH_NODES=new int[BRANCHES.length][];int[] count=new int[BRANCHES.length];
        for(Node n:NODES)count[n.branch]++;
        for(int b=0;b<count.length;b++)BRANCH_NODES[b]=new int[count[b]];
        Arrays.fill(count,0);
        HashMap<String,Integer> index=new HashMap<>();
        for(int i=0;i<NODES.length;i++){
            Node n=NODES[i];if(index.put(n.id,i)!=null)throw new IllegalStateException("Duplicate node: "+n.id);
            IDS[i]=n.id;NAMES[i]=n.name;COSTS[i]=n.cost;
            BRANCH_NODES[n.branch][count[n.branch]++]=i;
        }
        for(int i=0;i<NODES.length;i++){
            Node n=NODES[i];n.parents=new int[n.required.length];
            for(int j=0;j<n.parents.length;j++)n.parents[j]=index.get(n.required[j]);
            PARENTS[i]=n.parents.length==0?-1:n.parents[0];
        }
        String[] order={"sword","greatsword","axe","blunt","spear","ranged","dagger","fist","magic"};
        for(int i=0;i<order.length;i++){
            String a=order[i]+"_2",b=order[(i+1)%order.length]+"_5";
            alternative(a,b);alternative(b,a);
        }
        alternative("blunt_0","guard");
        alternative("ranged_0","pack");
        alternative("fist_0","strength");
        String[] utility={"scout","medic","food","stealth","momentum","ward"};
        for(int i=0;i<utility.length;i++){
            String a=utility[i]+"_2",b=utility[(i+1)%utility.length]+"_5";
            alternative(a,b);alternative(b,a);
        }
        alternative("scout_0","ranged_4");alternative("medic_0","magic_4");
        alternative("food_0","strength");alternative("stealth_0","dagger_4");
        alternative("momentum_0","fist_4");alternative("ward_0","blunt_4");
        alternative("strength_early","guard");alternative("strength_early","porter");
        alternative("strength_mid","combat_left_end");alternative("strength_mid","explore_left_end");
        alternative("strength_advanced","combat_merge");alternative("strength_advanced","survival_merge");
        alternative("strength_master","survival_cap");alternative("strength_master","explore_cap");
        for(int i=0;i<NODES.length;i++)DESCS[i]=NODES[i].description();
    }
    private static void alternative(String target,String source){
        Node node=NODES[index(target)];
        node.alternatives=Arrays.copyOf(node.alternatives,node.alternatives.length+1);
        node.alternatives[node.alternatives.length-1]=index(source);
    }
    private static void add(ArrayList<Node> list,String id,String name,int b,int row,int col,int cost,String[] p,Object... effects){
        list.add(new Node(id,name,b,row,col,cost,p,effects));
    }
    public static int index(String id) {
        for(int i=0;i<NODES.length;i++)if(NODES[i].id.equals(id))return i;
        throw new IllegalArgumentException(id);
    }
    public static boolean unlocked(Node node,Set<String> learned){
        boolean primary=true;
        for(int p:node.parents)if(!learned.contains(NODES[p].id))primary=false;
        if(primary)return true;
        for(int p:node.alternatives)if(learned.contains(NODES[p].id))return true;
        return false;
    }
    public static int family(KindOfWeapon weapon){
        if(weapon==null||weapon instanceof Gloves||weapon instanceof Gauntlet)return FIST;
        if(weapon instanceof MagesStaff)return MAGIC;
        if(weapon instanceof MissileWeapon||weapon instanceof SpiritBow||weapon instanceof Crossbow)return 10;
        if(weapon instanceof Greatsword)return 4;
        if(weapon instanceof Dagger||weapon instanceof Dirk||weapon instanceof AssassinsBlade||weapon instanceof Sai)return 5;
        if(weapon instanceof Spear||weapon instanceof Glaive||weapon instanceof Whip)return 6;
        if(weapon instanceof HandAxe||weapon instanceof BattleAxe||weapon instanceof Greataxe||weapon instanceof Sickle||weapon instanceof WarScythe)return 7;
        if(weapon instanceof Cudgel||weapon instanceof Mace||weapon instanceof WarHammer||weapon instanceof Flail||weapon instanceof Quarterstaff||weapon instanceof RoundShield||weapon instanceof Greatshield)return 8;
        if(weapon instanceof WornShortsword||weapon instanceof Shortsword||weapon instanceof Sword||weapon instanceof Longsword||weapon instanceof Scimitar||weapon instanceof Katana||weapon instanceof Rapier||weapon instanceof RunicBlade)return 3;
        return -1; // Other special weapons receive common growth only.
    }
}
