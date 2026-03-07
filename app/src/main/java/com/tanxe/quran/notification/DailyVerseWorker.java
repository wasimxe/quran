package com.tanxe.quran.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.repository.QuranRepository;

public class DailyVerseWorker extends Worker {
    private static final String CHANNEL_ID = "daily_verse";
    private static final int NOTIFICATION_ID = 1001;

    public DailyVerseWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            QuranRepository repository = QuranRepository.getInstance(getApplicationContext());
            Ayah verse = repository.getDailyVerse();

            if (verse != null) {
                showNotification(verse);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private void showNotification(Ayah verse) {
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_daily),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notification_channel_desc));
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = context.getString(R.string.daily_verse) + " - " +
                verse.surahNameEn + " " + verse.surahNumber + ":" + verse.ayahNumber;

        String body = verse.arabicText;
        if (verse.defaultTranslation != null && !verse.defaultTranslation.isEmpty()) {
            body += "\n\n" + verse.defaultTranslation;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_quran)
                .setContentTitle(title)
                .setContentText(verse.arabicText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
