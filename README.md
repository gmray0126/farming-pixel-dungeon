# 파밍픽셀던전 / Farming Pixel Dungeon

**Shattered Pixel Dungeon 모딩 작품입니다. AI를 사용했습니다.**

**An extraction and equipment-farming mod of Shattered Pixel Dungeon, developed with AI assistance.**

## 다운로드 / Downloads

[v0.58.0-farming — 개발 버전 / Development release](https://github.com/gmray0126/farming-pixel-dungeon/releases/tag/v0.58.0-farming)

- [Android APK](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.58.0-farming/Farming_Pixel_Dungeon_v0.58.0.apk)
- [Windows 64-bit ZIP](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.58.0-farming/Farming_Pixel_Dungeon_PC_v0.58.0_Windows.zip) — ZIP 전체를 압축 해제한 뒤 `FarmingPixelDungeon/FarmingPixelDungeon.exe`를 실행합니다. Extract the entire ZIP and run that executable; Java is bundled.

모딩 소스는 [`extraction` 브랜치](https://github.com/gmray0126/farming-pixel-dungeon/tree/extraction)에 있습니다. 기본 브랜치의 소개에서 아래 링크를 통해 실제 게임 소스로 이동할 수 있습니다.

The mod's game source is on the [`extraction` branch](https://github.com/gmray0126/farming-pixel-dungeon/tree/extraction). Use that branch when building or exploring the changes described here.

## 한국어 소개

파밍픽셀던전은 장비를 챙겨 원정을 떠나고, 보스를 쓰러뜨린 뒤 탈출하여 다음 원정을 준비하는 게임입니다. 거점의 창고에 장비를 보관하고 성장 노드를 선택하면서 반복 플레이로 캐릭터를 만들어 갑니다.

- **원정과 탈출:** 각 챕터는 독립된 5층 원정입니다. 보스를 처치하고 다음 계단을 이용하면 탈출합니다. 탈출에 성공하면 장비를 보관하고, 남은 모든 포션과 스크롤을 골드로 정산합니다.
- **사망 후에도 성장:** 사망하거나 원정을 포기하면 원정 물품과 전리품을 잃지만, 창고·성장 노드·획득한 성장 경험치는 유지됩니다.
- **직업 대신 성장 노드:** 무기 계열과 기존 직업·2차 직업의 특성 및 유틸리티를 노드로 선택합니다. 힘도 노드로 성장하며, 힘의 물약 효과는 해당 원정에서만 유지됩니다. 영혼의 활은 궁수 계열의 첫 노드를 통해 얻습니다.
- **레벨과 프리셋:** 성장 레벨은 최대 100이며 레벨마다 3포인트를 얻습니다. 레벨 자체는 능력치를 올리지 않습니다. 노드 초기화와 3개의 프리셋으로 구성을 바꿀 수 있습니다.
- **장비 파밍:** 무기·갑옷·바지·신발은 T1~T5로 구성됩니다. 갑옷의 방어 성능을 세 부위에 나누고, 문양은 세 부위 모두에 적용할 수 있습니다. 신발에는 이동 속도 효과도 있습니다. 힘이 부족해도 장착할 수 있지만 성능에 불이익을 받습니다.
- **유물과 상점:** 신규 유물에는 회복과 물약 효율, 탐욕과 받는 피해, 시간 가속과 둔화처럼 이점과 대가가 함께 있습니다. 상점에서는 물품을 사고팔고 T1~T5 장비를 구매할 수 있습니다. 반지·마법 지팡이·성장형 유물은 최대 +15이며, 티어 장비는 티어 × 3(최대 +15)까지 강화할 수 있습니다. 영혼의 활은 일반 강화 스크롤을 사용할 수 없습니다.
- **단계별 난이도:** 하수도에서 장비와 성장 포인트를 준비하고 더 강한 적이 있는 다음 챕터에 도전합니다. 챕터별 장비 드롭 상한이 적용됩니다.

## English overview

Farming Pixel Dungeon is built around repeated expeditions: prepare equipment at the hub, defeat a chapter boss, extract, and use the rewards to prepare for your next run. Your stash and a branching growth tree carry progression between expeditions.

- **Five-floor expeditions:** Each chapter is a separate run. After defeating its boss, take the next staircase to extract. Successful extraction stores your equipment and converts all remaining potions and scrolls into gold.
- **Progress through defeat:** Death or abandonment costs expedition supplies and loot. Your stash, learned growth nodes, and earned growth experience remain.
- **A classless growth tree:** Choose weapon paths, utility skills, and traits adapted from the original classes and subclasses. Permanent strength comes from nodes; strength potions last for the current expedition. The first archer node unlocks the Spirit Bow.
- **Levels and presets:** The growth level cap is 100, with three points awarded per level. Levels do not directly increase stats. Reset your nodes or switch between three saved presets to change your build.
- **More equipment slots:** Weapons, body armor, trousers, and boots span T1–T5. Defense is shared across the three armor pieces, which support armor glyphs. Boots also improve movement speed. Insufficient strength causes penalties rather than blocking equipment.
- **Artifacts and trading:** New artifacts pair benefits with drawbacks, such as healing versus potion efficiency, or acceleration followed by slowdown. Buy and sell supplies and T1–T5 equipment at the hub shop. Rings, magic wands, and growing artifacts cap at +15. Tiered equipment caps at tier × 3, up to +15. The Spirit Bow cannot use regular upgrade scrolls.
- **Chapter progression:** Farm the Sewers before taking on the substantially harder later chapters. Equipment drops follow the chapter's tier ceiling.

## v0.58.0 — 총 강화·속성탄·두 칸 구르기 / Gun upgrades, elemental shots, and two-cell roll

연금 권총은 강화 스크롤로 최대 +15까지 강화되고 투척의 반지 피해 보너스를 받습니다. 장착 사격과 가방·퀵슬롯 사격에 동일하게 적용됩니다. 속성탄은 투척 계열 물리 피해와 별도 마법 피해(기본 사격 피해 평균의 30%)를 함께 입힙니다. 구르기는 최대 두 칸으로 늘어나며 벽·적·함정·식물을 통과하지 않습니다. 0.1초 이동 연출과 휘릭 효과음을 추가했고 행동 턴 0·재사용 시간·탄약을 소비하는 구르기 장전은 유지합니다.

The pistol accepts upgrade scrolls up to permanent +15 and benefits from Ring of Sharpshooting in both shooting modes. Elemental rounds add a separate magic hit equal to 30% of average base shot damage. Tactical Roll moves up to two safe cells with a 0.1-second dash and whoosh; walls, enemies, traps, and plants block its path. Zero action-turn cost, cooldowns, and ammunition-based rolling reload remain.

## v0.57.1 — 총기 버튼 표시 수정 / Gun action label fix

연금 권총의 사격·장전 버튼에 표시되던 `NO TEXT FOUND`를 수정했습니다. 버튼은 ‘사격하기’·‘장전하기’로 표시됩니다.

Fixed missing Alchemy Pistol action labels. Shoot and reload now display their Korean button names instead of `NO TEXT FOUND`.

## v0.57.0 — 영혼의 활·앙크 수정 / Spirit Bow and Ankh fixes

영혼의 활이 영구 성장 레벨 5마다 +1, 최대 +15로 자동 강화되도록 수정했습니다. 레벨 75 이후 기본 피해는 더 증가하지 않으며 일반 강화 스크롤 제한은 유지합니다. 원정 사망 시 앙크를 먼저 확인해 축복 앙크의 즉시 부활과 일반 앙크의 원본 부활 절차가 작동하도록 수정했습니다. 부활 대기 중에는 사망 기록·원정 정산을 확정하지 않습니다.

Spirit Bows now scale with permanent growth level: +1 every five levels, capped at +15, with base damage capped at level 75. Upgrade scrolls remain unavailable. Expeditions process carried Ankhs before final death settlement, restoring blessed instant revival and ordinary item-preservation/lost-backpack resurrection. Pending resurrection does not finalize death records or settle the expedition.

## v0.56.1 — 마탄학자 계통 수정 / Arcane Ballistician path correction

총포술사 이름을 마탄학자로 바꾸고 트리를 마법사·궁수 사이로 옮겼습니다. 새 입문에는 마법사와 궁수 양쪽 첫 노드가 필요하며 기존 배분·프리셋은 유지합니다. APK와 윈도우 ZIP 모두 반영했습니다.

Renamed the gun hybrid path to Arcane Ballistician (마탄학자), moved it between Mage and Huntress, and connected both first nodes as unlock prerequisites. Existing allocations and presets are preserved. Available in both Android and Windows builds.

## v0.56.0 — 회피 상한·훈련장·마탄학자·검기 / Dodge cap, training, Gunner, and sword waves

실제 회피 확률을 최대 80%로 제한하고 회피 반지의 지수 중첩을 선형 합산으로 바꿨습니다. 로비 상단 훈련장에서 준비 장비의 복사본으로 일반·회피·중갑 허수아비를 공격하고 메뉴에서 피해 기록을 확인합니다. 훈련은 실제 원정 저장·장비·경험치를 변경하지 않습니다.

마탄학자의 연금 권총은 주무기로 장착해 적을 클릭하여 사격할 수 있습니다. 첫 입문 보상은 권총과 일반탄 12발입니다. 씨앗 1개 + 에너지 1 = 일반탄 6발, 일반탄 1발 + 대응 씨앗 1개 + 에너지 2 = 속성탄 1발입니다. 탄창·탄 소모·재장전 비용이 적용됩니다. 전술 구르기는 한 칸 즉시 이동하며 행동 턴을 소모하지 않고, 구르기 장전 퍽은 가진 탄약으로 재장전합니다. 재사용 시간은 기본 8턴이며 노드로 6턴·5턴까지 줄어듭니다.

마검사는 검·대검·마법사의 지팡이 평타에 3칸(노드로 4칸) 검기와 별도 마법 피해 15%(노드로 20%)를 적용합니다. 벽을 관통하지 않으며 평타와 주문의 교대 강화 연계를 유지합니다. 기존 혼합 노드 ID와 프리셋은 보존합니다.

Actual dodge chance is capped at 80%, with linear evasion-ring stacking. Native training uses copied equipment and real combat against three dummy types, without modifying real saves or progression. The Gunner's pistol can be equipped in the primary slot and uses crafted seed ammunition, limited magazines, and reload turns. Tactical Roll moves one cell instantly with no action-turn cost; Rolling Reload consumes existing rounds. Spellblade basic attacks gain ranged sword waves with separate, resistance-aware magic damage. Existing hybrid node IDs and presets remain intact.

## v0.55.0 — 메마른 장미 활성화 유지 / Permanent Dried Rose awakening

슬픈 유령 퀘스트 보상을 한 번 받으면 장미 활성화를 영구 저장합니다. 원정을 다시 시작하거나 감옥 등 다른 챕터로 출격해도 NPC를 다시 만날 필요가 없습니다. 강화·꽃잎·유령 장비·충전량은 유지하고 새 원정의 이전 소환 정보만 초기화합니다. 기존에 보유한 장미는 업데이트 시 한 번 복구합니다. 신규 프로필과 장미를 보유하지 않았던 기존 프로필은 처음 퀘스트를 완료해야 합니다.

Claim the Sad Ghost quest reward once to permanently awaken the Dried Rose across expeditions, deaths, restarts, and chapters. Preserve upgrades, petals, ghost equipment, and charge while clearing stale summon IDs only for fresh expeditions. Existing owned roses receive one compatibility recovery; new profiles and older profiles without an owned rose still require the first quest.

## v0.54.0 — 노드 배치와 전사 인장 / Atlas spacing and Warrior seal

629개 성장 노드의 바깥 테두리와 선택 영역을 고려해 겹치는 위치를 보정했습니다. 원형 갈래와 선행 조건을 유지하며 작은 지도와 전체 지도에서 같은 좌표를 사용합니다. 노드 ID·습득 상태·포인트·프리셋은 유지됩니다.

전사 첫 노드 **인내**를 처음 배울 때 부러진 인장을 창고에 하나 지급합니다. 준비 탭에서 챙긴 뒤 원정 가방에서 몸통 갑옷에 붙여 사용하세요. 기존 습득자에게도 누락 보상을 복구하며 이미 보관 중이거나 갑옷에 붙은 인장이 있으면 중복 지급하지 않습니다. 수령 기록을 저장하여 초기화·취소·재습득·프리셋 변경으로 반복 지급되지 않습니다.

The atlas now separates all 629 growth nodes while accounting for outer rings and selection space, preserving circular branches and prerequisites. Small and full maps share coordinates. Learned nodes, IDs, points, and presets remain unchanged.

The first Warrior node, **Endure**, grants one Broken Seal to the stash on first acquisition. Prepare it and affix it to body armor during an expedition. Existing owners receive a missing-reward recovery; already stored or affixed seals are not duplicated. Persistent reward history prevents repeated grants through refunds, resets, relearning, or presets.

## v0.53.0 — 수도사 에너지 합산 / Monk energy stacking

자유로운 영혼이 주무기·몸통 갑옷만 계산하던 문제를 수정하여 **바지와 신발도 각각 합산**합니다. 3단계에서 T1 네 부위를 착용하면 일반 적 처치당 기본 1 + 부위별 1씩 = **5 에너지**를 얻습니다. 강화 수치가 아닌 티어로 계산하고 최대 에너지는 기존 10을 유지합니다. 가방 속 물품·보조 무기는 제외합니다. 맨손이나 완력의 반지 격투 자세에서는 무기 보너스만 제외되며, 방어구 세 부위는 계속 적용됩니다. 에너지 설명창에 현재 배율을 표시합니다.

Unencumbered Spirit now adds bonuses from **pants and boots** alongside the primary melee weapon and body armor. Four tier-one pieces at rank three grant **5 energy** from a regular enemy (1 base plus 1 per slot). Uses item tiers, not upgrade levels; the existing raid cap of 10 remains. Backpack items and secondary weapons do not count. Unarmed attacks and active Brawler's Stance exclude only the weapon bonus. The energy tooltip shows the current multiplier.

## v0.52.0 — 자유로운 영혼 보상 수정 / Free Spirit reward fix

수도사 특성 **자유로운 영혼 3단계**의 천 갑옷·징 박힌 장갑 지급 누락을 수정했습니다. 처음 3단계를 습득하면 감정된 장비를 창고에 각각 하나씩 지급합니다. 징 박힌 장갑은 공격 속도가 빠른 T1 격투 무기입니다. 1·2단계에서는 장비를 지급하지 않습니다. 이미 3단계를 습득한 기존 프로필은 보상을 한 번 복구하며, 진행 중인 원정의 장비는 바꾸지 않습니다. 노드 취소·초기화·프리셋 변경으로 보상이 반복되지 않습니다.

Fixed missing **Unencumbered Spirit rank-three** equipment rewards. The first acquisition grants identified Cloth Armor and Gloves to the stash; Gloves are a fast tier-one fist weapon. Ranks one and two do not grant equipment. Existing rank-three owners receive one recovery without modifying their active raid. Refunds, resets, and preset changes cannot duplicate the reward.

## v0.51.0 — 사용자 스킨 / Player PNG skins

설정 → 화면 탭 → 캐릭터 스킨에서 원본과 같은 **256×128 투명 PNG**를 등록할 수 있습니다. 대기·걷기·공격, 장비 외형과 좌우 반전을 미리 확인하고 적용하세요. 재실행 후에도 유지되며 기본 외형으로 복구할 수 있습니다. 능력치와 직업은 바뀌지 않습니다.

Import an original-layout **256×128 transparent PNG** in Settings → Display → Character skin. Preview animations, equipment appearance rows, and mirroring before applying. Skins persist across launches and can be reset; gameplay stats and class stay the same.

[원본 사냥꾼 PNG / Original Huntress template](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.51.0-farming/Huntress_Original_256x128.png) · [양식 안내 / Frame guide](https://github.com/gmray0126/farming-pixel-dungeon/blob/extraction/SKIN_TEMPLATE_KO.md)

## v0.50.0 성장 노드 개별 취소 / Individual growth node refunds

성장 지도에서 배운 노드를 선택하고 습득 취소를 누릅니다. 선택한 노드를 요구하는 모든 배운 후속 노드도 함께 취소하며, 확인창에 전체 목록과 환급 포인트를 표시합니다. 관계없는 배분은 유지하고 취소 비용은 전액 반환합니다. 거점에서만 사용할 수 있으며, 줄어든 가방 한도를 넘는 준비 물품은 창고로 돌아갑니다. 저장한 프리셋은 유지됩니다.

Select a learned node in either growth map and choose Cancel learning. Confirmation lists every learned transitive dependent and the full refund. Unrelated allocations remain intact. Cancellation is available at the hub; prepared items exceeding reduced capacity return to the stash. Saved presets are preserved.

## v0.49.0 금고 입장 수정과 장비 복구 / Vault entry and stranded gear recovery

금고와 광산은 해당 챕터의 같은 층으로 왕복할 수 있습니다. 금고 입장 전에 이동 조건을 검사해 장비만 보관되고 이동이 막히는 오류를 방지합니다. 기존 저장에서 금고 밖인데 장비가 탈출 수정에 묶여 있다면 이어하기 때 복구하며, 오류 이후 얻은 물품과 재화도 보존합니다. 가방에 못 넣는 물품은 발밑에 놓습니다. 챕터와 보스 통과 조건은 유지됩니다.

Native vault and mine quests allow same-floor round trips. Vault entry validates the raid route before storing gear. Resuming an older expedition outside the vault recovers stranded equipment and currency from the Escape Crystal, preserves later loot, and places inventory overflow at the hero's feet. Chapter boundaries and boss completion requirements remain enforced.

## v0.48.0 기력 표시 / Skill energy display

기존 스킬 버튼 안에 기력 수치와 얇은 막대를 표시해 메뉴를 열지 않고 잔량을 확인합니다. 50 미만은 노란색, 20 미만은 붉은색입니다. 버튼 크기·위치와 퀵슬롯 배치는 유지하여 화면을 더 가리지 않습니다.

The existing Skills button displays current energy / 100 and a thin meter. Below 50 it turns amber, and below 20 red. Button size, placement, and quickslot layout are preserved, without an additional panel covering the dungeon.

## v0.47.0 창고 정리와 탈출 감정 / Stash organization and extraction identification

준비 탭의 창고 옆 정렬·합치기 버튼으로 같은 소모품을 합치고 종류별로 정렬합니다. 장비와 출격 준비 물품은 각각 보존합니다. 일반·비상탈출 때 보관하는 모든 장비를 감정하며, 반지는 다음 원정에도 종류 식별을 유지합니다. 저주는 제거하지 않습니다.

Sort/Merge beside the stash heading combines matching consumables and groups items by type. Equipment stays separate and prepared supplies are untouched. Both extraction routes identify deposited gear; ring types remain known in future expeditions. Identification does not remove curses.

## v0.46.0 범용 힘 노드 / Common strength nodes

영구 힘은 무기·직업 대신 범용 전투·생존·탐사와 유틸리티 계통에서 얻습니다. 선행 조건에도 무기·직업 노드가 없습니다. 총 10개가 각 +1이며 기본 힘 10에서 20까지 모든 경로를 배우려면 191 P가 필요합니다. 기존에 배운 힘과 포인트는 보존하며 강제로 초기화하지 않습니다.

Permanent strength comes from common growth and utility paths, with no weapon/class prerequisites. Ten +1 nodes require 191 total points across their paths to reach base strength 20. Existing learned strength and points are preserved without a forced reset.

## v0.45.0 지팡이 강화 / Staff upgrades

마탄의 지팡이를 포함한 마법사의 지팡이는 T1 근접무기 제한 대신 최대 +15까지 강화합니다. 융합된 마법 막대도 최대 +15로 동기화하며 상세창에 상한을 표시합니다.

Mage's Staff, including its Magic Missile form, upgrades to +15 rather than the tier-one melee cap. Bound wands synchronize up to +15, with the cap shown in details.

## v0.44.0 프리셋 저장 수정 / Preset save fix

빈 프리셋의 저장 버튼이 불러오기를 실행하던 오류를 수정했습니다. 성장 지도 → 프리셋에서 현재 배분 저장 / 프리셋 불러오기를 각각 선택하고 1~3번 칸을 고릅니다. 덮어쓰기는 확인 후 진행하며 불러오기에서 빈 칸은 비활성화합니다. 성공 메시지와 남은·사용한 포인트를 표시합니다.

Fixed empty-slot Save executing Load. Growth Map → Presets now offers separate Save current allocation and Load preset actions, followed by slots 1–3. Overwrites require confirmation; empty slots cannot be loaded. Success messages display remaining and spent points.

## v0.43.0 무기 분류 / Weapon classification

무기 상세창 맨 위에서 무기 종류와 적용 성장 계통을 확인합니다. 소검은 검 계통입니다. 맨손도 격투 판정으로 격투 노드 효과를 받으며, 해당 노드 설명에 적용 대상을 표시합니다. 난이도 버튼은 '하드'로 표시하고 레어 드롭 정보는 원정 설명에 안내합니다.

Weapon details show the type and matching growth family first. Shortswords use Sword growth. Bare hands receive Fist growth bonuses, explicitly noted on the Fist effect nodes. The difficulty button reads Hard; expedition descriptions explain Rare drops.

## v0.42.0 스킬 퀵슬롯과 가방 / Skill quickslots and bags

퀵슬롯은 총 12칸이며 하단 퀵 페이지 버튼으로 바꿉니다. 좁은 모바일 화면에서는 4칸씩 3페이지, 넓은 화면에서는 6칸씩 2페이지를 표시합니다. 빈 칸을 누르거나 퀵슬롯을 길게 눌러 스킬·아이템을 등록하거나 비웁니다. 스킬 메뉴의 퀵슬롯 등록에서도 배운 직업 기술·기도·수도승 기술·혼합 스킬을 선택합니다. 등록은 다음 원정에도 유지하고 충전·재사용 조건은 기존 규칙을 따릅니다.

씨앗·스크롤·물약·마법 가방은 탐사 계통의 각 해금 노드를 배워야 지급하며 상인은 판매하지 않습니다. 기존 무료 가방의 내용물은 보존합니다. 기본 배낭과 물통은 유지됩니다. 탐욕의 주머니는 전용 가방이 아닌 착용 유물로, 착용 후 자동 적용되며 클릭하면 효과와 상태를 확인합니다.

Twelve quickslots use a page button: four per page on narrow screens and six on wide screens. Tap an empty slot or long-press one to bind a skill or item, or clear it. The skill menu also offers registration for learned class abilities, prayers, individual Monk moves, and active hybrid skills. Skill bindings persist into future expeditions; original resource and cooldown rules remain.

Dedicated Seed, Scroll, Potion, and Magical bags require their new Exploration nodes and are not sold by dungeon merchants. Existing bag contents are preserved. The root backpack and waterskin remain. Greed Pouch is a passive equipped artifact, with a distinct icon and an effects/status action while equipped.

## v0.41.0 성장과 하드 / Growth and Hard mode

성장 노드는 표시된 모든 선행 조건을 요구합니다. 힘은 모두 +1인 10개 노드를 여러 계통 깊숙이 나누어 배치했습니다. 기존 배분은 한 번 초기화하여 포인트를 돌려주며, 진행 중인 원정은 종료한 뒤 환급합니다. 경험치와 보관 장비는 유지됩니다.

원정 탭에서 일반/하드를 선택합니다. 하드는 해당 챕터 일반 대비 적 체력 2.5배·공격 피해 2배에 추가 몬스터·정예·방어 관통을 적용합니다. 하드에서만 새 레어 장비가 나오며 보스는 해당 챕터 최대 티어 레어 무기 1개를 보장합니다. 레어는 해당 장비의 공격 피해 또는 물리 방어를 20% 높이고 강화 상한과 힘 요구치는 바꾸지 않습니다.

Growth nodes require every listed prerequisite. Ten +1 strength nodes are spread across deep paths. Existing allocations receive a one-time reset and point refund, deferred until the active expedition ends. Experience and stored gear remain.

Select Normal or Hard in the expedition tab. Hard adds 2.5× enemy health and 2× damage, more monsters, elites, and armor penetration. New Rare equipment drops only in Hard; each chapter boss guarantees one Rare weapon at its chapter tier ceiling. Rare quality adds 20% attack damage or physical defense without changing strength requirements or upgrade limits.

## 챕터 / Chapters

앞 챕터의 보스를 처치하고 탈출하면 다음 챕터가 열립니다. Defeat a chapter boss and extract to unlock the next chapter.

| 챕터 / Chapter | 층 / Floors | 보스 / Boss | 장비 드롭 상한 / Equipment tier cap |
| --- | --- | --- | --- |
| 하수도 / Sewers | 1–5 | 구 / Goo | T2 |
| 감옥 / Prison | 6–10 | 텐구 / Tengu | T3 |
| 동굴 / Caves | 11–15 | DM-300 | T4 |
| 드워프 도시 / Dwarven City | 16–20 | 드워프 제왕 / King of Dwarves | T5 |
| 악마의 전당 / Demon Halls | 21–25 | 요그제바 / Yog-Dzewa | T5 |

## 실행과 빌드 / Playing and building

현재 제공하는 플랫폼은 **Android와 Windows 64비트 PC**입니다. 개발 중인 모드이므로 오류 제보에는 게임 버전과 재현 방법을 함께 적어 주세요.

The currently provided builds target **Android and 64-bit Windows**. This mod is in development; please include the game version and reproduction steps when reporting a bug.

- **Android:** APK를 설치하여 실행합니다. Install the APK on your device.
- **Windows:** ZIP 전체를 압축 해제한 뒤 `FarmingPixelDungeon/FarmingPixelDungeon.exe`를 실행합니다. Java 런타임이 포함되어 있습니다. Extract the entire ZIP and run that executable; Java is bundled.
- **Builds:** [Android build workflow](https://github.com/gmray0126/farming-pixel-dungeon/actions/workflows/android-apk.yml) · [Windows build workflow](https://github.com/gmray0126/farming-pixel-dungeon/actions/workflows/desktop-pc.yml). Successful runs provide downloadable artifacts; GitHub may require sign-in.
- **Source builds:** [`extraction` branch](https://github.com/gmray0126/farming-pixel-dungeon/tree/extraction) · [Android guide](https://github.com/gmray0126/farming-pixel-dungeon/blob/extraction/docs/getting-started-android.md) · [Desktop guide](https://github.com/gmray0126/farming-pixel-dungeon/blob/extraction/docs/getting-started-desktop.md).

## 화면 표시 / Display

PC의 **설정 → 화면 설정 → 세로 모드 (PC)**에서 세로형 창과 모바일식 UI를 선택할 수 있습니다. 끄면 이전 창 크기로 돌아옵니다. 성장 지도는 검은 배경에 창 전체 높이로 표시하며, 상단 정보·버튼은 지도 위에 겹칩니다. 노드를 상단으로 옮겨도 정보 영역 경계에서 잘리지 않습니다. 긴 설명과 메시지는 창 안에서 줄바꿈되고 필요한 경우 스크롤됩니다.

Enable **세로 모드 (PC)** in the PC display settings for a portrait window and the mobile UI. Disabling it restores the previous window size. The growth map uses the full window height on a black background. Header controls overlay the map, so nodes remain visible when moved behind the header. Long descriptions and messages wrap within the window and scroll when needed.

## 내 원정 기록 / Expedition records

로비 오른쪽 위 **기록** 버튼에서 원작의 개인 플레이 기록을 확인합니다. 새 사망 원정부터 사망 원인과 도달 층·점수를 저장하며, 기록을 누르면 당시 능력치와 장비를 볼 수 있습니다. 기록창을 닫으면 로비로 돌아옵니다. 이전 버전의 미저장 사망은 복원할 수 없습니다.

Use **기록** at the top right of the hub to open the original personal records screen. New expedition deaths save their cause, floor, score, and character details. Select a record to inspect its stats and equipment; return to the hub when finished. Deaths not saved by earlier versions cannot be recovered.

## 로비 연금술과 보급 / Hub alchemy and supplies

로비 상단 여섯 번째 **연금술** 탭의 **연금술 열기** 버튼에서 재료를 최대 3개 넣고 기존 연금술 제작법으로 제작합니다. 에너지 1은 5 G로 구매하거나 창고 재료를 분해하여 얻습니다. 재료·에너지·완성품은 한 번에 저장되며 원정 중에는 이용할 수 없습니다. 장신구 촉매의 세 선택지는 저장됩니다.

Use **연금술 열기** in the sixth **연금술** hub tab to craft with up to three ingredients and the original alchemy recipes. Buy energy for 5 gold per unit or energize stash materials. Ingredients, energy, and outputs are saved together; hub crafting is unavailable during an active expedition. Catalyst choices persist when reopening the window.

식량은 층에서 자연 생성되며 몬스터 전리품과 상점에서도 얻을 수 있습니다. 출격 시 준비한 무기나 갑옷이 없으면 해당 보급 검·천 갑옷을 중복 없이 지급하고, 돌멩이 3개와 빈 물통(미준비 시)을 지급합니다. 피의 서약 유지 중에는 기존 명중 비용에 더해 5턴마다 체력 1을 소모하며 체력이 1이면 자동 해제됩니다.

Food spawns naturally on floors and is available from monster loot and the hub shop. Expeditions provide a fallback sword and cloth armor only when the corresponding gear is missing, plus three throwing stones and a waterskin when needed. Blood Oath additionally drains one HP every five turns while active and turns off automatically at one HP.

## 혼합 트리와 의뢰 / Hybrid trees and contracts

여섯 직업 계통 사이에 각각 15개 노드의 혼합 트리가 연결됩니다. 마검사·그림자술사·연금 사냥꾼·폭풍 유격수·성전사·혈기사로 총 90개 노드를 추가했습니다. 교대 공격, 표식 폭발, 속성 코팅, 거리 전환, 성력 소비, 체력과 출혈의 교환으로 전투 방식을 선택합니다. 원정 메뉴의 혼합 트리 기술에서 상태 확인과 활성 스킬을 사용합니다.

Six hybrid trees bridge the original class themes, adding 90 nodes: Spellblade, Shadowcaster, Alchemical Hunter, Storm Skirmisher, Crusader, and Blood Knight. Their mechanics cover physical/magic alternation, mark detonation, elemental coatings, ranged/melee transitions, faith spending, and health-for-bleeding trades. Check state and use active abilities through the expedition menu's hybrid skills.

로비 의뢰 게시판에서 최대 3개 의뢰를 받아 출격합니다. 챕터별 사냥·기록 회수·무기 납품 15개와 혼합 실전 6개가 있습니다. 원정 진행도는 탈출할 때만 저장되며 사망·포기하면 이번 원정의 진행도를 잃습니다. 납품 무기와 최초 완료 장비 보상은 직접 선택합니다. 하수도 연속 의뢰 보상은 전용 유물 ‘의뢰인의 인장’입니다.

Accept up to three of 21 hub contracts: 15 chapter-specific hunting, record-recovery, and weapon-delivery jobs, plus six hybrid trials. Run progress is banked only on extraction; death or abandonment loses the current run's progress. Select the weapon to donate and choose first-completion equipment rewards within the chapter's tier cap. Completing the Sewer contract chain awards the exclusive Commission Seal artifact.

## 제작자와 원작 / Credits

| 제작자 / Creator | 역할 / Role |
| --- | --- |
| [gmray0126](https://github.com/gmray0126) | 파밍픽셀던전 제작·기획·유지보수 / Farming Pixel Dungeon creator, design, and maintenance |
| [Evan Debenham / 00-Evan](https://github.com/00-Evan) | [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) 제작자 / Creator of Shattered Pixel Dungeon |
| [Oleg Dolya / Watabou](https://watabou.itch.io/) | 원작 Pixel Dungeon 제작자 / Creator of the original Pixel Dungeon |
| Shattered Pixel Dungeon 및 Pixel Dungeon 기여자 / contributors | 원작 코드·아트·음악·번역 등 / Upstream code, art, music, translations, and other contributions |

개발 과정에서 AI를 사용했습니다. 위 원작 제작자 표기는 기반 작품에 대한 크레딧이며, 이 모드의 개발 참여나 보증을 뜻하지 않습니다. 개별 원작 기여자에 대한 표기는 게임 내 크레딧과 기존 소스의 저작권 표시를 유지합니다.

AI assistance was used during development. Upstream credits acknowledge the works this mod is based on and do not imply participation in or endorsement of this mod. In-game credits and existing source notices preserve the individual upstream attributions.

## 라이선스 / License

원작 소스의 GNU GPL v3 또는 이후 버전 라이선스를 따릅니다. 기존 저작권 표시를 유지하며, 자세한 내용은 [LICENSE.txt](LICENSE.txt)와 소스 파일의 라이선스 표시를 확인해 주세요.

The source follows the upstream GNU GPL v3 or later license. Existing copyright notices are retained; see [LICENSE.txt](LICENSE.txt) and the notices in the source files.
