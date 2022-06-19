package com.dkanada.gramophone.dialogs;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.databinding.DialogSleepTimerBinding;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.service.MusicService;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.triggertrap.seekarc.SeekArc;

public class SleepTimerDialog extends DialogFragment {
    private DialogSleepTimerBinding binding;

    private TimerUpdater timerUpdater;
    private MaterialDialog materialDialog;

    private int seekArcProgress;

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        timerUpdater.cancel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogSleepTimerBinding.inflate(getLayoutInflater());

        timerUpdater = new TimerUpdater();
        materialDialog = new MaterialDialog.Builder(requireActivity())
                .customView(binding.getRoot(), false)
                .title(R.string.action_sleep_timer)
                .positiveText(R.string.action_set)
                .onPositive((dialog, which) -> {
                    PreferenceUtil.getInstance(getActivity()).setSleepTimerFinishMusic(binding.shouldFinishLastSong.isChecked());

                    final int minutes = seekArcProgress;

                    PendingIntent pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);

                    final long nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000;
                    PreferenceUtil.getInstance(getActivity()).setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime);
                    AlarmManager am = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime, pi);

                    Toast.makeText(getActivity(), requireActivity().getResources().getString(R.string.sleep_timer_set, minutes), Toast.LENGTH_SHORT).show();
                })
                .onNeutral((dialog, which) -> {
                    final PendingIntent previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE);
                    if (previous != null) {
                        AlarmManager am = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
                        am.cancel(previous);
                        previous.cancel();
                        Toast.makeText(getActivity(), requireActivity().getResources().getString(R.string.sleep_timer_canceled), Toast.LENGTH_SHORT).show();
                    }

                    MusicService musicService = MusicPlayerRemote.musicService;
                    if (musicService != null && musicService.pendingQuit) {
                        musicService.pendingQuit = false;
                        Toast.makeText(getActivity(), requireActivity().getResources().getString(R.string.sleep_timer_canceled), Toast.LENGTH_SHORT).show();
                    }
                })
                .showListener(dialog -> {
                    if (makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null) {
                        timerUpdater.start();
                    }
                })
                .build();

        if (materialDialog.getCustomView() == null) {
            return materialDialog;
        }

        boolean finishMusic = PreferenceUtil.getInstance(getActivity()).getSleepTimerFinishMusic();
        binding.shouldFinishLastSong.setChecked(finishMusic);

        binding.seekArc.setProgressColor(PreferenceUtil.getInstance(getActivity()).getAccentColor());
        binding.seekArc.setThumbColor(PreferenceUtil.getInstance(getActivity()).getAccentColor());

        binding.seekArc.post(() -> {
            int width = binding.seekArc.getWidth();
            int height = binding.seekArc.getHeight();
            int small = Math.min(width, height);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(binding.seekArc.getLayoutParams());
            layoutParams.height = small;
            binding.seekArc.setLayoutParams(layoutParams);
        });

        seekArcProgress = PreferenceUtil.getInstance(getActivity()).getLastSleepTimerValue();
        updateTimeDisplayTime();
        binding.seekArc.setProgress(seekArcProgress);

        binding.seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekArc seekArc, int i, boolean b) {
                if (i < 1) {
                    seekArc.setProgress(1);
                    return;
                }

                seekArcProgress = i;
                updateTimeDisplayTime();
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                PreferenceUtil.getInstance(getActivity()).setLastSleepTimerValue(seekArcProgress);
            }
        });

        return materialDialog;
    }

    private void updateTimeDisplayTime() {
        binding.timerDisplay.setText(seekArcProgress + " min");
    }

    private PendingIntent makeTimerPendingIntent(int flag) {
        return PendingIntent.getService(getActivity(), 0, makeTimerIntent(), flag);
    }

    private Intent makeTimerIntent() {
        Intent intent = new Intent(getActivity(), MusicService.class);
        if (binding.shouldFinishLastSong.isChecked()) {
            return intent.setAction(MusicService.ACTION_PENDING_QUIT);
        }

        return intent.setAction(MusicService.ACTION_QUIT);
    }

    private void updateCancelButton() {
        MusicService musicService = MusicPlayerRemote.musicService;
        if (musicService != null && musicService.pendingQuit) {
            materialDialog.setActionButton(DialogAction.NEUTRAL, materialDialog.getContext().getString(R.string.cancel_current_timer));
        } else {
            materialDialog.setActionButton(DialogAction.NEUTRAL, null);
        }
    }

    private class TimerUpdater extends CountDownTimer {
        public TimerUpdater() {
            super(PreferenceUtil.getInstance(getActivity()).getNextSleepTimerElapsedRealTime() - SystemClock.elapsedRealtime(), 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            materialDialog.setActionButton(DialogAction.NEUTRAL, materialDialog.getContext().getString(R.string.cancel_current_timer) + " (" + MusicUtil.getReadableDurationString(millisUntilFinished) + ")");
        }

        @Override
        public void onFinish() {
            updateCancelButton();
        }
    }
}
