package com.dkanada.gramophone.preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.heinrichreimersoftware.materialintro.view.InkPageIndicator;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.ui.fragments.player.NowPlayingScreen;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.ViewUtil;

public class NowPlayingScreenPreferenceDialog extends DialogFragment implements MaterialDialog.SingleButtonCallback, ViewPager.OnPageChangeListener {

    private DialogAction whichButtonClicked;
    private int viewPagerPosition;

    public static NowPlayingScreenPreferenceDialog newInstance() {
        return new NowPlayingScreenPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_dialog_now_playing, null);
        ViewPager viewPager = view.findViewById(R.id.now_playing_screen_view_pager);
        viewPager.setAdapter(new NowPlayingScreenAdapter(getContext()));
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageMargin((int) ViewUtil.convertDpToPixel(32, getResources()));
        viewPager.setCurrentItem(PreferenceUtil.getInstance(getContext()).getNowPlayingScreen().ordinal());

        InkPageIndicator pageIndicator = view.findViewById(R.id.page_indicator);
        pageIndicator.setViewPager(viewPager);
        pageIndicator.onPageSelected(viewPager.getCurrentItem());

        return new MaterialDialog.Builder(requireActivity())
                .title(R.string.pref_title_now_playing_appearance)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onAny(this)
                .customView(view, false)
                .build();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        whichButtonClicked = which;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (whichButtonClicked == DialogAction.POSITIVE) {
            PreferenceUtil.getInstance(getContext()).setNowPlayingScreen(NowPlayingScreen.values()[viewPagerPosition]);
        }
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
        private Context context;

        public NowPlayingScreenAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            NowPlayingScreen nowPlayingScreen = NowPlayingScreen.values()[position];

            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.preference_dialog_now_playing_item, collection, false);
            collection.addView(layout);

            ImageView image = layout.findViewById(R.id.image);
            TextView title = layout.findViewById(R.id.title);
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
