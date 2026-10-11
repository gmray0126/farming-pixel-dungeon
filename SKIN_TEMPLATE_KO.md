# 캐릭터 스킨 등록 / Character skin import

설정 → 화면 탭(모니터 아이콘) → 캐릭터 스킨 → PNG 파일 등록.
Choose Settings → Display (monitor icon) → Character skin → Import PNG.

v0.51은 원본과 동일한 **256×128 PNG**만 지원합니다. 투명 배경을 유지하고 크기를 바꾸지 마세요.
Version 0.51 accepts the original **256×128 PNG** layout. Keep the transparent background and exact dimensions.
최대 파일 크기는 4 MB입니다. 파일을 선택한 뒤 미리보기에서 적용해야 저장됩니다.
The size limit is 4 MB. Selecting a file only opens a preview; confirm Apply to save it.

## 프레임 양식 / Frame layout

각 프레임은 **12×15 픽셀**입니다. 0부터 세어 가로로 21개 동작 프레임, 세로로 8개 장비 외형 행이 있습니다.
Each frame is **12×15 pixels**. There are 21 animation frames per row and eight appearance rows, indexed from zero.
가로 마지막 4픽셀, 세로 마지막 8픽셀은 여백입니다. 움직이는 캐릭터를 그릴 때 발의 위치를 일정하게 유지하세요.
The final four columns and eight bottom rows are padding. Keep the feet aligned across frames.

| 프레임 / Frames | 동작 / Animation |
| --- | --- |
| 0–1 | 대기 / Idle |
| 2–7 | 걷기 / Walk |
| 8–12 | 사망 / Death (11 repeats at the end) |
| 13–15 | 공격·마법 / Attack and casting (returns to 0) |
| 16–17 | 상호작용 / Operate |
| 18 | 비행 / Flying |
| 19–20 | 스크롤 읽기 / Reading |

미리보기에서는 대기·걷기·공격, 장비 외형 행 전환, 좌우 반전을 확인할 수 있습니다. 취소하면 기존 외형을 유지합니다.
Preview idle, walking, attacks, different appearance rows, and horizontal mirroring. Cancel keeps your existing skin.
등록하면 실제 캐릭터와 상태창 초상화에 적용됩니다. 능력치·직업·장비 성능은 바뀌지 않습니다.
Applied skins affect the player and their status portrait, without changing stats, class, or equipment.
선택한 파일은 앱의 저장 영역에 복사하므로 원본 PNG를 이동해도 스킨은 유지됩니다. 설정의 기본 외형으로 복구로 되돌릴 수 있습니다.
The selected PNG is copied into game storage, so moving the source image does not break it. Restore default appearance in Settings to revert.
변신 효과 동안에는 해당 효과의 외형을 사용합니다.
Disguise effects temporarily use their own appearance.

## 원본 사냥꾼 / Original Huntress

릴리스의 `Huntress_Original_256x128.png`는 게임 소스의 `core/src/main/assets/sprites/huntress.png`를 수정 없이 복사한 파일입니다.
The release's `Huntress_Original_256x128.png` is an unchanged copy of `core/src/main/assets/sprites/huntress.png`.
Shattered Pixel Dungeon: Evan Debenham / 00-Evan and upstream contributors. Original Pixel Dungeon: Oleg Dolya / Watabou.
원본 자산은 프로젝트의 GPL-3.0-or-later 라이선스와 기존 저작권 고지를 따릅니다. 원작의 미술 자산을 이 모드의 새 창작물로 표시하지 않습니다.
The upstream asset retains the project's GPL-3.0-or-later license and existing copyright notices. It is not new artwork created for this mod.
https://github.com/00-Evan/shattered-pixel-dungeon
https://github.com/gmray0126/farming-pixel-dungeon/tree/extraction
