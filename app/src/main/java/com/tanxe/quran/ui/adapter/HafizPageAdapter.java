package com.tanxe.quran.ui.adapter;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.QuranDataParser;
import com.tanxe.quran.util.QuranPageData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

/**
 * Adapter for Hafiz mode — displays one Quran page at a time.
 * Page boundaries enforce that every juz starts on a new page.
 * Font size is calibrated once from Juz 1 content so each page fills ~16 lines.
 */
public class HafizPageAdapter extends RecyclerView.Adapter<HafizPageAdapter.PageViewHolder> {

    private static final int LINES_PER_PAGE = 16;
    private static final float LINE_SPACING_MULT = 0.78f;

    private final QuranRepository repository;
    private final ThemeManager theme;
    private Typeface arabicFont;
    private final ExecutorService executor;

    /** Effective page boundaries — PAGE_STARTS adjusted for juz alignment. */
    private final int[][] effectivePages;

    private volatile float cachedFontSizeSp = -1;
    private volatile int cachedWidthPx = -1;
    private volatile int cachedHeightPx = -1;
    private volatile float cachedDensity = -1;
    private volatile boolean fontCalibrated = false;
    private volatile boolean calibrating = false;

    public HafizPageAdapter(QuranRepository repository, ThemeManager theme, Typeface arabicFont) {
        this.repository = repository;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.executor = repository.getExecutor();
        this.effectivePages = computeEffectivePages();
        setHasStableIds(true);
    }

    /**
     * Compute page boundaries that ensure every juz starts on its own page.
     * Merges PAGE_STARTS with JUZ_STARTS, removes near-duplicate entries,
     * and removes {2,135} so Juz 1 fits in 20 pages.
     */
    private static int[][] computeEffectivePages() {
        // Encode as surah*10000+ayah for easy comparison
        TreeMap<Long, Boolean> allPages = new TreeMap<>(); // key -> isJuzStart

        for (int[] ps : QuranPageData.PAGE_STARTS) {
            allPages.put(encodeKey(ps[0], ps[1]), false);
        }
        // Insert juz starts (skip juz 1 since {1,1} is already there)
        for (int j = 1; j < QuranPageData.JUZ_STARTS.length; j++) {
            int[] js = QuranPageData.JUZ_STARTS[j];
            allPages.put(encodeKey(js[0], js[1]), true); // mark as juz start
        }

        // Find pages to remove:
        // 1. Pages <=2 ayahs from a juz start (same surah) — they'd be too short
        // 2. {2,135} — so Juz 1 = 20 pages (Juz 2 starts at page 21)
        Set<Long> toRemove = new HashSet<>();
        toRemove.add(encodeKey(2, 135)); // Juz 1 = 20 pages

        List<Long> keys = new ArrayList<>(allPages.keySet());
        for (int i = 0; i < keys.size(); i++) {
            Boolean isJuz = allPages.get(keys.get(i));
            if (isJuz == null || !isJuz) continue; // only process juz starts

            long juzKey = keys.get(i);
            int juzS = surahOf(juzKey), juzA = ayahOf(juzKey);

            // Check page immediately BEFORE this juz start
            if (i > 0) {
                long prevKey = keys.get(i - 1);
                Boolean prevIsJuz = allPages.get(prevKey);
                if (prevIsJuz != null && !prevIsJuz) {
                    int prevS = surahOf(prevKey), prevA = ayahOf(prevKey);
                    if (prevS == juzS && (juzA - prevA) <= 2) {
                        toRemove.add(prevKey);
                    }
                }
            }

            // Check page immediately AFTER this juz start
            if (i + 1 < keys.size()) {
                long nextKey = keys.get(i + 1);
                Boolean nextIsJuz = allPages.get(nextKey);
                if (nextIsJuz != null && !nextIsJuz) {
                    int nextS = surahOf(nextKey), nextA = ayahOf(nextKey);
                    if (nextS == juzS && (nextA - juzA) <= 2) {
                        toRemove.add(nextKey);
                    }
                }
            }
        }

        // Build final list
        List<int[]> result = new ArrayList<>();
        for (long k : allPages.keySet()) {
            if (!toRemove.contains(k)) {
                result.add(new int[]{surahOf(k), ayahOf(k)});
            }
        }

        return result.toArray(new int[0][]);
    }

    private static long encodeKey(int surah, int ayah) {
        return (long) surah * 10000 + ayah;
    }

    private static int surahOf(long key) {
        return (int) (key / 10000);
    }

    private static int ayahOf(long key) {
        return (int) (key % 10000);
    }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public int getItemCount() { return effectivePages.length; }

    public void updateFontSize(float size) { }

    public void setArabicFont(Typeface font) {
        this.arabicFont = font;
        this.cachedFontSizeSp = -1;
        this.fontCalibrated = false;
        this.calibrating = false;
    }

    /** Get page index (0-based) for a given surah:ayah using effective pages. */
    public int getPageIndex(int surah, int ayah) {
        for (int i = effectivePages.length - 1; i >= 0; i--) {
            int ps = effectivePages[i][0], pa = effectivePages[i][1];
            if (surah > ps || (surah == ps && ayah >= pa)) {
                return i;
            }
        }
        return 0;
    }

    /** Get the {surah, ayah} start of a page by index. */
    public int[] getPageStart(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= effectivePages.length) return effectivePages[0];
        return effectivePages[pageIndex];
    }

    /**
     * Build SpannableStringBuilder for a page using effectivePages boundaries.
     */
    private SpannableStringBuilder buildPageText(int pageIndex, int accentColor, int secondaryColor) {
        int startSurah = effectivePages[pageIndex][0];
        int startAyah = effectivePages[pageIndex][1];
        int endSurah, endAyah;

        if (pageIndex + 1 < effectivePages.length) {
            endSurah = effectivePages[pageIndex + 1][0];
            endAyah = effectivePages[pageIndex + 1][1] - 1;
            if (endAyah < 1) {
                endSurah--;
                endAyah = QuranDataParser.SURAH_AYAH_COUNT[endSurah - 1];
            }
        } else {
            endSurah = 114;
            endAyah = 6;
        }

        SpannableStringBuilder sb = new SpannableStringBuilder();
        int curS = startSurah, curA = startAyah;

        while (curS < endSurah || (curS == endSurah && curA <= endAyah)) {
            if (curA == 1) {
                Ayah first = repository.getAyah(curS, 1);
                if (first != null) {
                    if (sb.length() > 0) sb.append("\n");
                    int hStart = sb.length();
                    sb.append("\u2500 " + first.surahNameAr + " \u2500");
                    sb.setSpan(new ForegroundColorSpan(accentColor),
                            hStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                            hStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.append("\n");

                    if (curS != 1 && curS != 9) {
                        int bStart = sb.length();
                        sb.append("\u0628\u0650\u0633\u0652\u0645\u0650 \u0627\u0644\u0644\u0651\u064E\u0647\u0650 \u0627\u0644\u0631\u0651\u064E\u062D\u0652\u0645\u064E\u0670\u0646\u0650 \u0627\u0644\u0631\u0651\u064E\u062D\u0650\u064A\u0645\u0650");
                        sb.setSpan(new ForegroundColorSpan(accentColor),
                                bStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                                bStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.append("\n");
                    }
                }
            }

            Ayah ayah = repository.getAyah(curS, curA);
            if (ayah != null) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                    sb.append(" ");
                }
                String text = ayah.arabicText;
                if (curA == 1 && curS != 1 && curS != 9) {
                    text = MushafAdapter.stripBismillah(text);
                }
                sb.append(text);
                sb.append(" ");
                int mStart = sb.length();
                sb.append("\uFD3F" + curA + "\uFD3E");
                sb.setSpan(new ForegroundColorSpan(secondaryColor),
                        mStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.65f),
                        mStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[curS - 1];
            if (curA >= maxAyah) { curS++; curA = 1; }
            else { curA++; }
        }

        return sb;
    }

    /**
     * Calibrate font size using representative Juz 1 pages (middle pages only,
     * avoiding boundary pages). Finds the smallest font where ALL calibration
     * pages produce >= 16 lines, and 16 lines fit within available height.
     */
    private float calibrateFontSize(int widthPx, int heightPx, float density) {
        int accentColor = theme.getAccentColor();
        int secondaryColor = theme.getSecondaryTextColor();

        // Reserve bottom margin so text doesn't touch the bottom edge
        int safeHeight = heightPx - (int)(24 * density);

        // Batch-load surahs 1 & 2 (populates repository cache)
        repository.getAyahsBySurah(1);
        repository.getAyahsBySurah(2);

        // Use representative pages from middle of Juz 1 (indices 4-15).
        // These are standard mid-surah pages, avoiding the merged last page.
        int calibStart = 4;
        int calibEnd = 15;
        int numPages = calibEnd - calibStart + 1;

        SpannableStringBuilder[] pageTexts = new SpannableStringBuilder[numPages];
        for (int i = 0; i < numPages; i++) {
            pageTexts[i] = buildPageText(calibStart + i, accentColor, secondaryColor);
        }

        float lo = 5f, hi = 50f;
        for (int iter = 0; iter < 30; iter++) {
            float mid = (lo + hi) / 2f;
            TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            if (arabicFont != null) tp.setTypeface(arabicFont);
            tp.setTextSize(mid * density);

            // Check ALL calibration pages for both line count and height
            boolean fits = true;
            int minLines = Integer.MAX_VALUE;
            for (SpannableStringBuilder text : pageTexts) {
                StaticLayout layout = StaticLayout.Builder
                        .obtain(text, 0, text.length(), tp, widthPx)
                        .setLineSpacing(0f, LINE_SPACING_MULT)
                        .setIncludePad(true)
                        .setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
                        .setJustificationMode(android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD)
                        .build();
                int lc = layout.getLineCount();
                minLines = Math.min(minLines, lc);
                // Height constraint: 16 lines must fit within safe height
                if (lc >= LINES_PER_PAGE) {
                    int h16 = layout.getLineBottom(LINES_PER_PAGE - 1);
                    if (h16 > safeHeight) {
                        fits = false;
                        break;
                    }
                }
            }

            if (!fits) {
                hi = mid; // Too tall, need smaller font
            } else if (minLines >= LINES_PER_PAGE) {
                hi = mid; // All pages have enough lines, try smaller font
            } else {
                lo = mid; // Some page has < 16 lines, need bigger font
            }
        }

        return hi;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hafiz_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        final int generation = ++holder.bindGeneration;

        // Theme
        holder.container.setBackgroundColor(theme.getBackgroundColor());
        holder.tvPageContent.setTextColor(theme.getArabicTextColor());
        if (arabicFont != null) holder.tvPageContent.setTypeface(arabicFont);
        holder.tvPageContent.setIncludeFontPadding(true);
        holder.tvPageContent.setElegantTextHeight(true);
        holder.tvPageContent.setLineSpacing(0f, LINE_SPACING_MULT);
        holder.tvPageContent.setMaxLines(LINES_PER_PAGE);
        holder.tvPageContent.setText("");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            holder.tvPageContent.setJustificationMode(android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            holder.tvPageContent.setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
        }

        if (fontCalibrated && cachedFontSizeSp > 0) {
            holder.tvPageContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, cachedFontSizeSp);
            loadPageContent(holder, position, generation);
        } else {
            holder.tvPageContent.post(() -> {
                if (holder.bindGeneration != generation) return;
                if (cachedWidthPx <= 0) {
                    cachedWidthPx = holder.tvPageContent.getWidth();
                    cachedHeightPx = holder.tvPageContent.getHeight();
                    cachedDensity = holder.tvPageContent.getContext()
                            .getResources().getDisplayMetrics().scaledDensity;
                }
                if (cachedWidthPx <= 0) return;

                if (!calibrating) {
                    calibrating = true;
                    executor.execute(() -> {
                        cachedFontSizeSp = calibrateFontSize(
                                cachedWidthPx, cachedHeightPx, cachedDensity);
                        fontCalibrated = true;
                        calibrating = false;
                        holder.itemView.post(() -> notifyDataSetChanged());
                    });
                }
            });
        }
    }

    private void loadPageContent(PageViewHolder holder, int position, int generation) {
        int accentColor = theme.getAccentColor();
        int secondaryColor = theme.getSecondaryTextColor();

        executor.execute(() -> {
            if (holder.bindGeneration != generation) return;

            // Pre-load surahs needed for this page
            int startSurah = effectivePages[position][0];
            int endSurah = (position + 1 < effectivePages.length)
                    ? effectivePages[position + 1][0] : 114;
            for (int s = startSurah; s <= endSurah; s++) {
                repository.getAyahsBySurah(s);
            }

            if (holder.bindGeneration != generation) return;

            SpannableStringBuilder sb = buildPageText(position, accentColor, secondaryColor);

            if (holder.bindGeneration != generation) return;

            holder.itemView.post(() -> {
                if (holder.bindGeneration != generation) return;
                holder.tvPageContent.setText(sb);
            });
        });
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvPageContent;
        volatile int bindGeneration;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.hafiz_page_container);
            tvPageContent = itemView.findViewById(R.id.tv_page_content);
        }
    }
}
