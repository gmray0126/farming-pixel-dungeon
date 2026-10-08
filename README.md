# 파밍픽셀던전 / Farming Pixel Dungeon

**Shattered Pixel Dungeon 모딩 작품입니다. AI를 사용했습니다.**

**An extraction and equipment-farming mod of Shattered Pixel Dungeon, developed with AI assistance.**

## 다운로드 / Downloads

[v0.39.0-farming — 개발 버전 / Development release](https://github.com/gmray0126/farming-pixel-dungeon/releases/tag/v0.39.0-farming)

- [Android APK](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.39.0-farming/Farming_Pixel_Dungeon_v0.39.0.apk)
- [Windows 64-bit ZIP](https://github.com/gmray0126/farming-pixel-dungeon/releases/download/v0.39.0-farming/Farming_Pixel_Dungeon_PC_v0.39.0_Windows.zip) — ZIP 전체를 압축 해제한 뒤 `FarmingPixelDungeon/FarmingPixelDungeon.exe`를 실행합니다. Extract the entire ZIP and run that executable; Java is bundled.

모딩 소스는 [`extraction` 브랜치](https://github.com/gmray0126/farming-pixel-dungeon/tree/extraction)에 있습니다. 기본 브랜치의 소개에서 아래 링크를 통해 실제 게임 소스로 이동할 수 있습니다.

The mod's game source is on the [`extraction` branch](https://github.com/gmray0126/farming-pixel-dungeon/tree/extraction). Use that branch when building or exploring the changes described here.

## 한국어 소개

파밍픽셀던전은 장비를 챙겨 원정을 떠나고, 보스를 쓰러뜨린 뒤 탈출하여 다음 원정을 준비하는 게임입니다. 거점의 창고에 장비를 보관하고 성장 노드를 선택하면서 반복 플레이로 캐릭터를 만들어 갑니다.

- **원정과 탈출:** 각 챕터는 독립된 5층 원정입니다. 보스를 처치하고 다음 계단을 이용하면 탈출합니다. 탈출에 성공하면 장비를 보관하고, 남은 모든 포션과 스크롤을 골드로 정산합니다.
- **사망 후에도 성장:** 사망하거나 원정을 포기하면 원정 물품과 전리품을 잃지만, 창고·성장 노드·획득한 성장 경험치는 유지됩니다.
- **직업 대신 성장 노드:** 무기 계열과 기존 직업·2차 직업의 특성 및 유틸리티를 노드로 선택합니다. 힘도 노드로 성장하며, 힘의 물약 효과는 해당 원정에서만 유지됩니다. 영혼의 활은 궁수 계열의 첫 노드를 통해 얻습니다.
- **레벨과 프리셋:** 성장 레벨은 최대 100이며 레벨마다 3포인트를 얻습니다. 레벨 자체는 능력치를 올리지 않습니다. 노드 초기화와 3개의 프리셋으로 구성을 바꿀 수 있습니다.
- **장비 파밍:** 무기·갑옷·바지·신발은 T1~T5로 구성됩니다. 갑옷의 방어 성능을 세 부위에 나누고, 문양은 세 부위 모두에 적용할 수 있습니다. 신발에는 이동 속도 효과도 있습니다. 힘이 부족해도 장착할 수 있지만 성능에 불이익을 받습니다.
- **유물과 상점:** 신규 유물에는 회복과 물약 효율, 탐욕과 받는 피해, 시간 가속과 둔화처럼 이점과 대가가 함께 있습니다. 상점에서는 물품을 사고팔고 T1~T5 장비를 구매할 수 있습니다. 반지와 유물의 강화 상한은 +15이며 T5 무기도 +15까지 강화할 수 있습니다.
- **단계별 난이도:** 하수도에서 장비와 성장 포인트를 준비하고 더 강한 적이 있는 다음 챕터에 도전합니다. 챕터별 장비 드롭 상한이 적용됩니다.

## English overview

Farming Pixel Dungeon is built around repeated expeditions: prepare equipment at the hub, defeat a chapter boss, extract, and use the rewards to prepare for your next run. Your stash and a branching growth tree carry progression between expeditions.

- **Five-floor expeditions:** Each chapter is a separate run. After defeating its boss, take the next staircase to extract. Successful extraction stores your equipment and converts all remaining potions and scrolls into gold.
- **Progress through defeat:** Death or abandonment costs expedition supplies and loot. Your stash, learned growth nodes, and earned growth experience remain.
- **A classless growth tree:** Choose weapon paths, utility skills, and traits adapted from the original classes and subclasses. Permanent strength comes from nodes; strength potions last for the current expedition. The first archer node unlocks the Spirit Bow.
- **Levels and presets:** The growth level cap is 100, with three points awarded per level. Levels do not directly increase stats. Reset your nodes or switch between three saved presets to change your build.
- **More equipment slots:** Weapons, body armor, trousers, and boots span T1–T5. Defense is shared across the three armor pieces, which support armor glyphs. Boots also improve movement speed. Insufficient strength causes penalties rather than blocking equipment.
- **Artifacts and trading:** New artifacts pair benefits with drawbacks, such as healing versus potion efficiency, or acceleration followed by slowdown. Buy and sell supplies and T1–T5 equipment at the hub shop. Rings and artifacts have a +15 enhancement ceiling; T5 weapons can also reach +15.
- **Chapter progression:** Farm the Sewers before taking on the substantially harder later chapters. Equipment drops follow the chapter's tier ceiling.

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

로비 상단 여섯 번째 **연금술** 탭의 **연금술 열기** 버튼에서 재료를 최대 3개 넣고 기존 연금술 제작법으로 제작합니다. 에너지 1은 10 G로 구매하거나 창고 재료를 분해하여 얻습니다. 재료·에너지·완성품은 한 번에 저장되며 원정 중에는 이용할 수 없습니다. 장신구 촉매의 세 선택지는 저장됩니다.

Use **연금술 열기** in the sixth **연금술** hub tab to craft with up to three ingredients and the original alchemy recipes. Buy energy for 10 gold per unit or energize stash materials. Ingredients, energy, and outputs are saved together; hub crafting is unavailable during an active expedition. Catalyst choices persist when reopening the window.

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
