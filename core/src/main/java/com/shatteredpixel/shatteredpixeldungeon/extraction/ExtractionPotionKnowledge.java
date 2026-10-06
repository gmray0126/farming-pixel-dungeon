/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import static com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionGrowth.Stat.*;

/** One independently purchased knowledge node per randomized potion family. */
public final class ExtractionPotionKnowledge {
    private ExtractionPotionKnowledge(){}
    public static final String[] IDS={"know_healing","know_strength","know_experience","know_mindvision","know_levitation","know_invisibility","know_haste","know_purity","know_frost","know_flame","know_toxic","know_paralytic"};
    public static final String[] NAMES={"회복 물약","힘 물약","경험 물약","심안 물약","부유 물약","투명 물약","신속 물약","정화 물약","서리 물약","화염 물약","유독 가스 물약","마비 가스 물약"};
    public static final ExtractionGrowth.Stat[] STATS={KNOW_HEALING,KNOW_STRENGTH,KNOW_EXPERIENCE,KNOW_MINDVISION,KNOW_LEVITATION,KNOW_INVISIBILITY,KNOW_HASTE,KNOW_PURITY,KNOW_FROST,KNOW_FLAME,KNOW_TOXIC,KNOW_PARALYTIC};
    private static final Class[] TYPES={PotionOfHealing.class,PotionOfStrength.class,PotionOfExperience.class,PotionOfMindVision.class,PotionOfLevitation.class,PotionOfInvisibility.class,PotionOfHaste.class,PotionOfPurity.class,PotionOfFrost.class,PotionOfLiquidFlame.class,PotionOfToxicGas.class,PotionOfParalyticGas.class};
    @SuppressWarnings("unchecked")
    public static void apply(ExtractionProfile profile){
        for(int i=0;i<IDS.length;i++)if(profile.nodes.contains(IDS[i]))Potion.knowType(TYPES[i]);
    }
}
