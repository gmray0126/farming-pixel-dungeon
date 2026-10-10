# 파밍픽셀던전 / Farming Pixel Dungeon

**Shattered Pixel Dungeon 모딩 작품입니다. AI를 사용했습니다.**

**An extraction and equipment-farming mod of Shattered Pixel Dungeon, developed with AI assistance.**

## 다운로드 / Downloads

[v0.48.0-farming — 개발 버전 / Development release](https://github.com/gmray0126/farming-pixel-dungeon/releases/tag/v0.48.0-farming)

- [Android APK](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.48.0-farming/Farming_Pixel_Dungeon_v0.48.0.apk)
- [Windows 64-bit ZIP](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.48.0-farming/Farming_Pixel_Dungeon_PC_v0.48.0_Windows.zip) — ZIP 전체를 압축 해제한 뒤 `FarmingPixelDungeon/FarmingPixelDungeon.exe`를 실행합니다. Extract the entire ZIP and run that executable; Java is bundled.

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

- **혼합 전투 트리:** 여섯 직업 계통 사이에 마검사·그림자술사·연금 사냥꾼·폭풍 유격수·성전사·혈기사의 90개 노드를 배치했습니다. 교대 공격, 표식 폭발, 약품 코팅, 거리 전환, 성력 소비, 피의 서약으로 전투 방식을 바꿉니다.
- **로비 의뢰:** 사냥·기록 회수·무기 납품과 혼합 실전 훈련의 21개 의뢰를 제공합니다. 최대 3개를 받아 출격하고 탈출하면 진행도를 저장합니다. 최초 완료 장비와 하수도 연속 의뢰 유물도 얻을 수 있습니다.

- **출격 보급:** 준비한 무기·갑옷이 없으면 해당 보급 검·천 갑옷을 중복 없이 지급합니다. 새 원정마다 돌멩이 3개와 미준비 시 빈 물통을 지급합니다. 식량은 층 자연 생성·몬스터 전리품·상점으로 얻습니다.

## English overview

Farming Pixel Dungeon is built around repeated expeditions: prepare equipment at the hub, defeat a chapter boss, extract, and use the rewards to prepare for your next run. Your stash and a branching growth tree carry progression between expeditions.

- **Five-floor expeditions:** Each chapter is a separate run. After defeating its boss, take the next staircase to extract. Successful extraction stores your equipment and converts all remaining potions and scrolls into gold.
- **Progress through defeat:** Death or abandonment costs expedition supplies and loot. Your stash, learned growth nodes, and earned growth experience remain.
- **A classless growth tree:** Choose weapon paths, utility skills, and traits adapted from the original classes and subclasses. Permanent strength comes from nodes; strength potions last for the current expedition. The first archer node unlocks the Spirit Bow.
- **Levels and presets:** The growth level cap is 100, with three points awarded per level. Levels do not directly increase stats. Reset your nodes or switch between three saved presets to change your build.
- **More equipment slots:** Weapons, body armor, trousers, and boots span T1–T5. Defense is shared across the three armor pieces, which support armor glyphs. Boots also improve movement speed. Insufficient strength causes penalties rather than blocking equipment.
- **Artifacts and trading:** New artifacts pair benefits with drawbacks, such as healing versus potion efficiency, or acceleration followed by slowdown. Buy and sell supplies and T1–T5 equipment at the hub shop. Rings, magic wands, and growing artifacts cap at +15. Tiered equipment caps at tier × 3, up to +15. The Spirit Bow cannot use regular upgrade scrolls.
- **Chapter progression:** Farm the Sewers before taking on the substantially harder later chapters. Equipment drops follow the chapter's tier ceiling.

- **Hybrid combat trees:** Ninety new nodes connect six neighbouring class themes: Spellblade, Shadowcaster, Alchemical Hunter, Storm Skirmisher, Crusader, and Blood Knight. Alternate attacks, detonate marks, coat ranged weapons, build faith, or trade health for damage.
- **Hub contracts:** Accept up to three of 21 hunting, recovery, delivery, and hybrid training contracts. Extraction banks progress; first completion awards a choice of equipment, with a unique artifact for finishing the Sewers contract chain.

- **Starter supplies:** Missing weapons and body armor receive a free fallback sword and cloth armor without duplicate gear. Every new expedition receives three throwing stones and a waterskin when needed. Food comes from natural floor spawns, monster loot, and the shop.

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

## 내 원정 기록과 로비 / Records and hub

로비 오른쪽 위 **기록**에서 새 사망 원정의 원인·층·점수와 당시 캐릭터·장비를 확인합니다. 원작의 개인 기록창을 사용하며 기록에서 돌아오면 로비로 이동합니다. 기록 조회는 진행 중인 원정의 저장 파일을 바꾸지 않습니다. 이전 버전에서 미저장된 사망은 복원되지 않습니다.

상단 탭은 **준비·성장·원정·상점·의뢰·연금술** 여섯 개입니다. 작은 물약 구매 버튼은 제거했으며, 연금술 탭의 큰 **연금술 열기** 버튼을 사용합니다.

The hub's top-right **기록** button opens the original personal records screen. New deaths save their cause, floor, score, character, and equipment. Viewing records leaves the active expedition save intact. Deaths not saved by earlier releases cannot be recovered.

The hub has six tabs: Preparation, Growth, Expedition, Shop, Contracts, and Alchemy. The small quick-potion-purchase button is removed; use the full-width alchemy entry button in the Alchemy tab.

성장 지도는 고정된 검은 배경과 창 전체 높이를 사용합니다. 상단 정보와 버튼은 지도 위에 겹쳐 표시되어 노드가 상단 경계에서 잘리지 않습니다. The growth map uses a stable black background and the full window height. Header controls overlay the map, keeping nodes visible behind the header.

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
