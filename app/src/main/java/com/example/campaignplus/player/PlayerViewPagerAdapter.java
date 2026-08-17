package com.example.campaignplus.player;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.campaignplus.R;
import com.example.campaignplus.player.MainFragments.ClassInformationFragment;
import com.example.campaignplus.player.MainFragments.ItemViewFragment;
import com.example.campaignplus.player.MainFragments.PlayerViewFragment;
import com.example.campaignplus.player.MainFragments.SpellViewFragment;
import com.example.campaignplus._data.PlayerData;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager2 adapter for the home screen pages
 */
public class PlayerViewPagerAdapter extends FragmentStateAdapter {

    private final PlayerData selectedPlayer;
    private final List<Fragment> fragmentList = new ArrayList<>();

    public PlayerViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, PlayerData selectedPlayer) {
        super(fragmentActivity);
        this.selectedPlayer = selectedPlayer;
        initializeFragments();
    }

    private void initializeFragments() {
        fragmentList.add(new PlayerViewFragment());
        fragmentList.add(new ItemViewFragment());
        fragmentList.add(new SpellViewFragment());
        fragmentList.add(ClassInformationFragment.newInstance(selectedPlayer.mainClassIds, selectedPlayer.subClassIds));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public int getPositionForItemId(int itemId) {
        if (itemId == R.id.page_account) {
            return 0;
        } else if (itemId == R.id.page_items) {
            return 1;
        } else if (itemId == R.id.page_spells) {
            return 2;
        } else if (itemId == R.id.page_class) {
            return 3;
        }
        return 0;
    }

    public int getItemIdForPosition(int position) {
        if (position == 0) {
            return R.id.page_account;
        } else if (position == 1) {
            return R.id.page_items;
        } else if (position == 2) {
            return R.id.page_spells;
        } else if (position == 3) {
            return R.id.page_class;
        }
        return R.id.page_account;
    }
}
