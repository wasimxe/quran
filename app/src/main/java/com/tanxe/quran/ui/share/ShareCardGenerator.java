package com.tanxe.quran.ui.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.core.content.FileProvider;

import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.theme.ThemeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareCardGenerator {

    private static final int CARD_WIDTH = 1080;
    private static final int CARD_PADDING = 80;
    private static final int CORNER_RADIUS = 32;

    public static void shareAyahAsImage(Context context, Ayah ayah, String translationText,
                                         Typeface arabicFont) {
        Bitmap bitmap = generateCard(context, ayah, translationText, arabicFont);
        if (bitmap == null) return;

        try {
            File cacheDir = new File(context.getCacheDir(), "share");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File imageFile = new File(cacheDir, "ayah_" + ayah.surahNumber + "_" + ayah.ayahNumber + ".png");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share Ayah"));
        } catch (IOException e) {
            // Fall back to text sharing
        } finally {
            bitmap.recycle();
        }
    }

    private static Bitmap generateCard(Context context, Ayah ayah, String translationText,
                                        Typeface arabicFont) {
        ThemeManager theme = ThemeManager.getInstance(context);

        // Measure text heights to determine card height
        int textWidth = CARD_WIDTH - (CARD_PADDING * 2);

        // Arabic text paint
        TextPaint arabicPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        arabicPaint.setColor(theme.getArabicTextColor());
        arabicPaint.setTextSize(56);
        if (arabicFont != null) arabicPaint.setTypeface(arabicFont);
        arabicPaint.setTextAlign(Paint.Align.RIGHT);

        StaticLayout arabicLayout = StaticLayout.Builder
                .obtain(ayah.arabicText, 0, ayah.arabicText.length(), arabicPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                .setLineSpacing(16, 1.0f)
                .build();

        // Translation text paint
        TextPaint transPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        transPaint.setColor(theme.getTranslationTextColor());
        transPaint.setTextSize(36);
        transPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        String transText = translationText != null && !translationText.isEmpty()
                ? translationText : ayah.defaultTranslation;
        if (transText == null) transText = "";

        StaticLayout transLayout = StaticLayout.Builder
                .obtain(transText, 0, transText.length(), transPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(8, 1.0f)
                .build();

        // Reference text paint
        TextPaint refPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        refPaint.setColor(theme.getAccentColor());
        refPaint.setTextSize(32);
        refPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

        String reference = "\u2014 " + ayah.surahNameEn + " " + ayah.surahNumber + ":" + ayah.ayahNumber;

        // Calculate total height
        int headerHeight = 100;
        int arabicHeight = arabicLayout.getHeight();
        int gap1 = 48;
        int transHeight = transLayout.getHeight();
        int gap2 = 40;
        int refHeight = 50;
        int footerHeight = 80;
        int totalHeight = headerHeight + arabicHeight + gap1 + transHeight + gap2 + refHeight + footerHeight;

        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background with gradient
        int bgColor = theme.getBackgroundColor();
        int surfaceColor = theme.getSurfaceColor();
        Paint bgPaint = new Paint();
        bgPaint.setShader(new LinearGradient(0, 0, 0, totalHeight,
                bgColor, surfaceColor, Shader.TileMode.CLAMP));

        RectF rect = new RectF(0, 0, CARD_WIDTH, totalHeight);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, bgPaint);

        // Decorative accent line at top
        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(theme.getAccentColor());
        canvas.drawRoundRect(new RectF(CARD_PADDING, 24, CARD_WIDTH - CARD_PADDING, 28),
                2, 2, accentPaint);

        // Bismillah header
        TextPaint headerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(theme.getAccentColor());
        headerPaint.setTextSize(28);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        if (arabicFont != null) headerPaint.setTypeface(arabicFont);
        canvas.drawText("\u0628\u0650\u0633\u0652\u0645\u0650 \u0627\u0644\u0644\u0651\u064E\u0647\u0650",
                CARD_WIDTH / 2f, 70, headerPaint);

        // Draw Arabic text
        float y = headerHeight;
        canvas.save();
        canvas.translate(CARD_PADDING, y);
        arabicLayout.draw(canvas);
        canvas.restore();
        y += arabicHeight + gap1;

        // Divider line
        Paint dividerPaint = new Paint();
        dividerPaint.setColor(theme.getDividerColor());
        canvas.drawRect(CARD_PADDING + 100, y - 24,
                CARD_WIDTH - CARD_PADDING - 100, y - 23, dividerPaint);

        // Draw translation
        canvas.save();
        canvas.translate(CARD_PADDING, y);
        transLayout.draw(canvas);
        canvas.restore();
        y += transHeight + gap2;

        // Draw reference
        canvas.drawText(reference, CARD_PADDING, y, refPaint);

        // Footer: app name
        TextPaint footerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setColor(Color.argb(100, 255, 255, 255));
        footerPaint.setTextSize(22);
        footerPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Al-Quran App", CARD_WIDTH / 2f, totalHeight - 24, footerPaint);

        return bitmap;
    }
}
