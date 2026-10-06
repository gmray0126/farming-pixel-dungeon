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
    public static final String[] BRANCHES = {"전투","생존","탐사","검","대검","단검·쌍검","창","도끼","둔기·방패","격투","활·투척","마도"};
    public enum Stat {
        DAMAGE("공격 피해", false), DAMAGE_PERCENT("공격 피해", true),
        ACCURACY("명중", true), ATTACK_SPEED("공격 속도", true),
        CRIT_CHANCE("치명타 확률", true), CRIT_POWER("치명타 배율", true),
        PIERCE("방어 관통", false), REACH("근접 사거리", false),
        HEALTH("출격 최대 체력", false), DEFENSE("피해 감소", false),
        EVASION("회피", true), MOVE_SPEED("이동 속도", true),
        CAPACITY("가방 칸", false), STRENGTH("출격 힘", false), GOLD("탈출 전리품 골드", true),
        WAND_DAMAGE("마법 막대 직접 피해", false), WAND_POWER("마법 막대 직접 피해", true), WAND_CHARGE("마법 막대 충전 속도", true);
        public final String label; public final boolean percent;
        Stat(String label, boolean percent) { this.label=label; this.percent=percent; }
    }
    public static final class Node {
        public final String id, name; public final int branch, row, col, cost;
        private final String[] required;
        public int[] parents;
        public int[] alternatives=new int[0];
        public final EnumMap<Stat,Float> effects=new EnumMap<>(Stat.class);
        Node(String id,String name,int branch,int row,int col,int cost,String[] required,Object... effects) {
            this.id=id;this.name=name;this.branch=branch;this.row=row;this.col=col;this.cost=cost;this.required=required;
            for(int i=0;i<effects.length;i+=2)this.effects.put((Stat)effects[i],(Float)effects[i+1]);
        }
        public String description() {
            StringBuilder text=new StringBuilder();
            for(Map.Entry<Stat,Float> e:effects.entrySet()){
                if(text.length()>0)text.append("\n");
                text.append(e.getKey().label).append(" +").append(Math.round(e.getValue()));
                if(e.getKey().percent)text.append('%');
            }
            if(branch>=3)text.append("\n\n").append(BRANCHES[branch]).append(" 계통의 무기를 사용할 때 적용됩니다.");
            if(branch==MAGIC)text.append(" 마법 막대 충전은 보유한 막대에 적용됩니다.");
            if(effects.containsKey(Stat.CRIT_CHANCE)||effects.containsKey(Stat.CRIT_POWER))text.append("\n치명타의 기본 피해 배율은 150%입니다.");
            if(alternatives.length>0)text.append("\n\n연결된 다른 계통의 노드에서도 진입할 수 있습니다.");
            if(parents.length>1)text.append("\n\n양쪽 갈래의 선행 노드를 모두 습득해야 합니다.");
            return text.toString();
        }
        public String summary() {
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
        NODES=list.toArray(new Node[0]);
        IDS=new String[NODES.length];NAMES=new String[NODES.length];DESCS=new String[NODES.length];
        COSTS=new int[NODES.length];PARENTS=new int[NODES.length];
        BRANCH_NODES=new int[BRANCHES.length][9];int[] count=new int[BRANCHES.length];
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
