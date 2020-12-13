package com.dkanada.gramophone.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.dkanada.gramophone.databinding.ActivitySettingsBinding;
import com.dkanada.gramophone.preferences.DirectPlayPreference;
import com.dkanada.gramophone.preferences.DirectPlayPreferenceDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.shortcuts.DynamicShortcutManager;
import com.dkanada.gramophone.preferences.CategoryPreference;
import com.dkanada.gramophone.preferences.CategoryPreferenceDialog;
import com.dkanada.gramophone.preferences.NowPlayingScreenPreference;
import com.dkanada.gramophone.preferences.NowPlayingScreenPreferenceDialog;
import com.dkanada.gramophone.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.util.PreferenceUtil;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setDrawUnderStatusbar();
        setStatusbarColorAuto();

        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        binding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(binding.toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.pref_title_primary_color:
                ThemeStore.editTheme(this).primaryColor(selectedColor).commit();
                break;
            case R.string.pref_title_accent_color:
                ThemeStore.editTheme(this).accentColor(selectedColor).commit();
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }

        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_library);
            addPreferencesFromResource(R.xml.pref_interface);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_now_playing);
            addPreferencesFromResource(R.xml.pref_lock_screen);
            addPreferencesFromResource(R.xml.pref_playback);
            addPreferencesFromResource(R.xml.pref_cache);
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            } else if (preference instanceof CategoryPreference) {
                return CategoryPreferenceDialog.newInstance();
            } else if (preference instanceof DirectPlayPreference) {
                return DirectPlayPreferenceDialog.newInstance();
            }

            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            invalidateSettings();
            PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            PreferenceUtil.getInstance(getActivity()).unregisterOnSharedPreferenceChangedListener(this);
        }

        private void invalidateSettings() {
            final Preference generalTheme = findPreference(PreferenceUtil.GENERAL_THEME);
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener((preference, o) -> {
                String themeName = (String) o;
                setSummary(generalTheme, o);

                ThemeStore.markChanged(requireActivity());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    // set the new theme so that updateAppShortcuts can pull it
                    requireActivity().setTheme(PreferenceUtil.getThemeResource(themeName));
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }

                requireActivity().recreate();
                return true;
            });

            final ATEColorPreference primaryColorPref = findPreference(PreferenceUtil.PRIMARY_COLOR);
            final int primaryColor = ThemeStore.primaryColor(requireActivity());
            primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
            primaryColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(requireActivity(), R.string.pref_title_primary_color)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(primaryColor)
                        .show(requireActivity());
                return true;
            });

            final ATEColorPreference accentColorPref = findPreference(PreferenceUtil.ACCENT_COLOR);
            final int accentColor = ThemeStore.accentColor(requireActivity());
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));
            accentColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(requireActivity(), R.string.pref_title_accent_color)
                        .accentMode(true)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(accentColor)
                        .show(requireActivity());
                return true;
            });

            final TwoStatePreference classicNotification = findPreference(PreferenceUtil.CLASSIC_NOTIFICATION);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                classicNotification.setVisible(false);
            } else {
                classicNotification.setChecked(PreferenceUtil.getInstance(getActivity()).getClassicNotification());
                classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    PreferenceUtil.getInstance(getActivity()).setClassicNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference coloredNotification = findPreference(PreferenceUtil.COLORED_NOTIFICATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                coloredNotification.setEnabled(PreferenceUtil.getInstance(getActivity()).getClassicNotification());
            } else {
                coloredNotification.setChecked(PreferenceUtil.getInstance(getActivity()).getColoredNotification());
                coloredNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    PreferenceUtil.getInstance(getActivity()).setColoredNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference colorAppShortcuts = findPreference(PreferenceUtil.COLORED_SHORTCUTS);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                colorAppShortcuts.setVisible(false);
            } else {
                colorAppShortcuts.setChecked(PreferenceUtil.getInstance(getActivity()).getColoredShortcuts());
                colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                    PreferenceUtil.getInstance(getActivity()).setColoredShortcuts((Boolean) newValue);

                    // update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                    return true;
                });
            }

            updateNowPlayingScreenSummary();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.NOW_PLAYING_SCREEN:
                    updateNowPlayingScreenSummary();
                    break;
                case PreferenceUtil.CLASSIC_NOTIFICATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        findPreference(PreferenceUtil.COLORED_NOTIFICATION).setEnabled(sharedPreferences.getBoolean(key, false));
                    }
                    break;
            }
        }

        private void updateNowPlayingScreenSummary() {
            findPreference(PreferenceUtil.NOW_PLAYING_SCREEN).setSummary(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().titleRes);
        }
    }
}
