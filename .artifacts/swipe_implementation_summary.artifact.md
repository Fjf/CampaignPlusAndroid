# Swipe Navigation Implementation

## Summary
Successfully added swipe functionality to navigate between the 4 main pages on the home screen (Player Account, Items, Spells, and Class Information).

## Changes Made

### 1. Added ViewPager2 Dependency
**File**: [build.gradle](file:///G:/StudioProjects/StudioProjects/app/build.gradle)
- Added `implementation 'androidx.viewpager2:viewpager2:1.1.0'`

### 2. Created ViewPager2 Adapter
**File**: [PlayerViewPagerAdapter.java](file:///G:/StudioProjects/StudioProjects/app/src/main/java/com/example/campaignplus/player/PlayerViewPagerAdapter.java)
- New file created
- Manages the 4 fragments in the ViewPager2
- Provides bidirectional mapping between page positions and bottom navigation item IDs
- Fragments in order:
  0. PlayerViewFragment (Account)
  1. ItemViewFragment (Items)
  2. SpellViewFragment (Spells)
  3. ClassInformationFragment (Class Info)

### 3. Updated Layout
**File**: [activity_player_info.xml](file:///G:/StudioProjects/StudioProjects/app/src/main/res/layout/activity_player_info.xml)
- Replaced `LinearLayout` with `ViewPager2`
- ViewPager2 is constrained between toolbar and bottom navigation

### 4. Updated PlayerInfoActivity
**File**: [PlayerInfoActivity.java](file:///G:/StudioProjects/StudioProjects/app/src/main/java/com/example/campaignplus/player/PlayerInfoActivity.java)
- Added ViewPager2 and adapter fields
- Initialized ViewPager2 with adapter
- Set `offscreenPageLimit` to 1 to keep adjacent pages in memory
- Updated bottom navigation listener to use `setCurrentItem()` instead of fragment transactions
- Added ViewPager2 callback to sync page changes with bottom navigation selection
- Updated `openDefaultFragment()` to use ViewPager2

## How It Works

1. **Swipe Left/Right**: Users can now swipe horizontally between the 4 pages
2. **Bottom Navigation**: Tapping bottom navigation icons still works and scrolls to the corresponding page
3. **Synchronization**: The ViewPager and bottom navigation stay in sync - when you swipe, the bottom navigation highlights the correct icon, and when you tap an icon, it scrolls to that page
4. **Page Limit**: Set to 1 to keep adjacent pages loaded for smooth transitions while not consuming too much memory

## Testing
The implementation has been built successfully. To test:
1. Run the app on a device or emulator
2. Navigate to the Player Info activity
3. Swipe left/right between the 4 tabs
4. Verify that the bottom navigation stays synchronized with the current page
