package com.dkanada.gramophone.dialogs.preferences;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.fragments.player.NowPlayingScreen;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.pixelcan.inkpageindicator.InkPageIndicator;

public class NowPlayingPreferenceDialog extends DialogFragment implements ViewPager.OnPageChangeListener {
    public static final String TAG = NowPlayingPreferenceDialog.class.getSimpleName();

    private int viewPagerPosition;

    public static NowPlayingPreferenceDialog create() {
        return new NowPlayingPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_dialog_now_playing, null);
        ViewPager viewPager = view.findViewById(R.id.now_playing_screen_view_pager);
        InkPageIndicator pageIndicator = view.findViewById(R.id.page_indicator);

        viewPager.setAdapter(new NowPlayingScreenAdapter(getContext()));
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(PreferenceUtil.getInstance(getContext()).getNowPlayingScreen().ordinal());

        pageIndicator.setViewPager(viewPager);
        pageIndicator.onPageSelected(viewPager.getCurrentItem());

        return new MaterialDialog.Builder(requireActivity())
                .customView(view, false)
                .title(R.string.pref_title_now_playing_appearance)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, action) -> PreferenceUtil.getInstance(getContext()).setNowPlayingScreen(NowPlayingScreen.values()[viewPagerPosition]))
                .build();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        this.viewPagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private static class NowPlayingScreenAdapter extends PagerAdapter {
        private final Context context;

        public NowPlayingScreenAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            NowPlayingScreen nowPlayingScreen = NowPlayingScreen.values()[position];

            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.preference_dialog_now_playing_item, collection, false);

            ImageView image = layout.findViewById(R.id.image);
            TextView title = layout.findViewById(R.id.title);

            collection.addView(layout);
            image.setImageResource(nowPlayingScreen.drawableRes);
            title.setText(nowPlayingScreen.titleRes);

            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return NowPlayingScreen.values().length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return context.getString(NowPlayingScreen.values()[position].titleRes);
        }
    }
}
