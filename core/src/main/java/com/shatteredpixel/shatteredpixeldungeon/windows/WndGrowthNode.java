/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionGrowth;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.watabou.noosa.Game;

/** Shared node details and explicit cancellation confirmation for both growth maps. */
public final class WndGrowthNode {
    public static void open(int index,Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();
        boolean learned=p.nodes.contains(ExtractionProfile.IDS[index]);
        boolean canLearn=!learned&&!p.active&&p.unlocked(index)&&p.points>=ExtractionProfile.COSTS[index];
        boolean canCancel=learned&&!p.active;
        String state=learned?(p.active?"습득한 노드입니다. 원정을 끝내면 개별 취소할 수 있습니다.":"습득한 노드입니다. 취소하면 사용한 포인트를 돌려받습니다.")
                :p.active?"거점으로 돌아온 뒤 배울 수 있습니다.":"비용 "+ExtractionProfile.COSTS[index]+" P · 보유 "+p.points+" P";
        String[] choices=canLearn?new String[]{"습득 · "+ExtractionProfile.COSTS[index]+" P","닫기"}
                :canCancel?new String[]{"습득 취소","닫기"}:new String[]{"닫기"};
        Game.scene().add(new WndOptions(ExtractionProfile.NAMES[index],ExtractionProfile.DESCS[index]
                +"\n\n선행: "+p.prerequisites(index)+"\n\n"+state,choices){
            @Override protected void onSelect(int choice){
                if(choice!=0)return;
                if(canCancel){confirmCancellation(index,refreshed);return;}
                if(canLearn)try{
                    boolean awarded=p.monkEquipmentAwarded();
                    boolean sealAwarded=p.warriorSealAwarded();
                    p.learn(index);if(refreshed!=null)refreshed.run();
                    if(!awarded&&p.monkEquipmentAwarded())Game.scene().add(new WndMessage("자유로운 영혼 3단계 보상으로 천 갑옷과 징 박힌 장갑을 창고에 지급했습니다.\n준비 탭에서 출격 물품에 넣어 주세요.\n\n징 박힌 장갑은 T1 격투 무기입니다. 보상은 프로필당 한 번 지급됩니다."));
                    if(!sealAwarded&&p.warriorSealAwarded())Game.scene().add(new WndMessage("전사 입문 보상: 부러진 인장을 해금했습니다.\n처음 습득하면 창고에 하나 지급합니다. 이미 보관 중이거나 갑옷에 붙어 있다면 중복 지급하지 않습니다.\n\n준비 탭에서 챙긴 뒤 원정 가방에서 인장을 몸통 갑옷에 붙여 사용하세요. 초기화·재습득으로 반복 지급하지 않습니다."));
                }
                catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}
            }
        });
    }
    private static void confirmCancellation(int index,Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();
        ExtractionProfile.NodeRemoval plan=p.previewNodeRemoval(index);
        if(plan.indices.isEmpty())return;
        StringBuilder message=new StringBuilder("선택: ").append(ExtractionProfile.NAMES[index])
                .append("\n취소 ").append(plan.indices.size()).append("개 · 환급 ").append(plan.refund).append(" P")
                .append("\n남은 ").append(p.points).append(" P → ").append(p.points+plan.refund).append(" P\n\n");
        if(plan.indices.size()>1)message.append("주의: 이 노드를 요구하는 후속 노드도 모두 함께 취소됩니다. 다른 계통의 후속 노드도 포함됩니다.\n\n");
        else message.append("이 노드만 취소합니다.\n\n");
        message.append("취소할 노드:\n");
        for(int n:plan.indices){
            ExtractionGrowth.Node node=ExtractionGrowth.NODES[n];
            message.append("• ").append(node.name).append(" [").append(ExtractionGrowth.BRANCHES[node.branch])
                    .append("] · ").append(node.cost).append(" P\n");
        }
        message.append("\n줄어든 가방 한도를 넘는 준비 물품은 창고로 돌려놓습니다.");
        Game.scene().add(new WndOptions("노드 습득 취소",message.toString(),"취소 확정 · "+plan.refund+" P 반환","유지하기"){
            @Override protected void onSelect(int choice){if(choice==0)try{
                int refund=p.cancelNode(plan);
                if(refreshed!=null)refreshed.run();
                Game.scene().add(new WndMessage(plan.indices.size()+"개 노드를 취소하고 "+refund+" P를 돌려받았습니다.\n남은 "+p.points+" P · 사용한 "+p.spentPoints()+" P"));
            }catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}
        });
    }
    private WndGrowthNode(){}
}
