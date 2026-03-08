package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tanxe.quran.R;
import android.util.SparseArray;

import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.entity.Tafseer;
import com.tanxe.quran.data.entity.Translation;
import com.tanxe.quran.data.entity.WordByWord;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.util.Localization;
import com.tanxe.quran.util.QuranDataParser;
import com.tanxe.quran.util.RukuData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AyahPagerAdapter extends RecyclerView.Adapter<AyahPagerAdapter.AyahViewHolder> {

    private final QuranRepository repository;
    private final ThemeManager theme;
    private Typeface arabicFont;
    private final Typeface urduFont;
    private final ExecutorService executor;

    // Display modes: "arabic", "translation", "tafseer", "wbw"
    private String displayMode = "translation";

    /** Callback for long-press on ayah */
    public interface OnAyahLongPressListener {
        void onAyahLongPress(int surah, int ayah, int position);
    }

    /** Callback for clicking translation/tafseer text */
    public interface OnContentClickListener {
        void onTranslationClick(int surah, int ayah);
        void onTafseerClick(int surah, int ayah);
    }

    private OnAyahLongPressListener longPressListener;
    private OnContentClickListener contentClickListener;

    // Font sizes for pinch-to-zoom
    private float arabicFontSize;
    private float translationFontSize;

    // Currently playing position for highlighting
    private int playingPosition = -1;

    // Cached preferences — avoid reading SharedPrefs on every bind
    private String cachedTranslation;
    private String cachedTafseer;
    private String cachedWbwLang;
    private String cachedLang;
    private Typeface cachedTransFont;
    private Typeface cachedTafseerFont;

    // Cached theme colors — avoid method calls on every bind
    private int cBg, cBgPlaying, cArabic, cArabicPlaying, cSecondary, cAccent, cDivider, cTranslation;

    // Cached localized ruku labels
    private String cachedRukuLabel, cachedSurahLabel, cachedJuzLabel;

    private static final int TOTAL_AYAHS = QuranDataParser.TOTAL_AYAHS;

    // Pre-computed position offset table for O(1) surah lookup
    private static final int[] SURAH_OFFSETS = new int[115]; // index 1..114
    static {
        SURAH_OFFSETS[1] = 0;
        for (int s = 1; s < 114; s++) {
            SURAH_OFFSETS[s + 1] = SURAH_OFFSETS[s] + QuranDataParser.SURAH_AYAH_COUNT[s - 1];
        }
    }

    // Surah-level preloaded cache: surah -> (ayahNumber -> BindData)
    private final SparseArray<Map<Integer, BindData>> preloadedSurahs = new SparseArray<>();
    private final Object preloadLock = new Object();
    // Track which surahs are currently being loaded to avoid duplicate loads
    private final java.util.Set<Integer> loadingSurahs = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    /** Holds all pre-fetched data needed to bind an ayah synchronously */
    private static class BindData {
        final Ayah ayah;
        final String translationText;
        final String tafseerText;
        final boolean isRukuEnd;
        BindData(Ayah a, String t, String tf, boolean r) {
            this.ayah = a; this.translationText = t; this.tafseerText = tf; this.isRukuEnd = r;
        }
    }

    public AyahPagerAdapter(QuranRepository repository, ThemeManager theme,
                            Typeface arabicFont, Typeface urduFont) {
        this.repository = repository;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.urduFont = urduFont;
        this.executor = repository.getExecutor();
        this.arabicFontSize = repository.getArabicFontSize();
        this.translationFontSize = repository.getTranslationFontSize();
        refreshCachedPrefs();
        setHasStableIds(true);
    }

    /** Refresh cached preferences and theme colors — call when edition/theme changes */
    public void refreshCachedPrefs() {
        cachedTranslation = repository.getSelectedTranslation();
        cachedTafseer = repository.getSelectedTafseer();
        cachedWbwLang = repository.getSelectedWbwLanguage();
        cachedLang = repository.getLanguage();
        // Pre-compute fonts
        cachedTransFont = (cachedTranslation.startsWith("ur.") || "ur".equals(cachedLang) || "fa".equals(cachedLang)) ? urduFont : Typeface.DEFAULT;
        cachedTafseerFont = (cachedTafseer != null && (cachedTafseer.contains(".ur.") || cachedTafseer.startsWith("ur.") || cachedTafseer.contains("urdu")))
                || "ur".equals(cachedLang) || "fa".equals(cachedLang) ? urduFont : Typeface.DEFAULT;
        // Cache theme colors
        cBg = theme.getBackgroundColor();
        cBgPlaying = theme.getPlayingAyahBg();
        cArabic = theme.getArabicTextColor();
        cArabicPlaying = theme.getActiveAyahTextColor();
        cSecondary = theme.getSecondaryTextColor();
        cAccent = theme.getAccentColor();
        cDivider = theme.getDividerColor();
        cTranslation = theme.getTranslationTextColor();
        // Cache localized ruku labels
        cachedRukuLabel = Localization.get(cachedLang, Localization.RUKU);
        cachedSurahLabel = Localization.get(cachedLang, Localization.SURAH);
        cachedJuzLabel = Localization.get(cachedLang, Localization.JUZ);
        // Clear preloaded surah cache (edition/language may have changed)
        synchronized (preloadLock) { preloadedSurahs.clear(); }
        loadingSurahs.clear();
    }

    public void setLongPressListener(OnAyahLongPressListener listener) {
        this.longPressListener = listener;
    }

    public void setContentClickListener(OnContentClickListener listener) {
        this.contentClickListener = listener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDisplayMode(String mode) {
        if (!this.displayMode.equals(mode)) {
            this.displayMode = mode;
            notifyDataSetChanged();
        }
    }

    public String getDisplayMode() {
        return displayMode;
    }

    // Keep for backward compat — no-ops
    public void setLearningMode(boolean learning) {}
    public boolean isLearningMode() { return false; }
    public void setShowTranslation(boolean show) {
        setDisplayMode(show ? "translation" : "arabic");
    }
    public boolean isShowTranslation() { return !"arabic".equals(displayMode); }

    public void updateFontSize(float arabicSp, float transSp) {
        this.arabicFontSize = arabicSp;
        this.translationFontSize = transSp;
        notifyDataSetChanged();
    }

    public float getArabicFontSize() { return arabicFontSize; }
    public float getTranslationFontSize() { return translationFontSize; }

    public void setArabicFont(Typeface font) {
        this.arabicFont = font;
    }

    /** Set the currently-playing position and highlight it */
    public void setPlayingPosition(int position) {
        int oldPos = playingPosition;
        playingPosition = position;
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (position >= 0) notifyItemChanged(position);
    }

    public int getPlayingPosition() { return playingPosition; }

    /** Convert flat position (0-6235) to [surah, ayah] — O(log n) via binary search */
    public static int[] positionToSurahAyah(int position) {
        // Binary search on SURAH_OFFSETS
        int lo = 1, hi = 114;
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (SURAH_OFFSETS[mid] <= position) lo = mid;
            else hi = mid - 1;
        }
        return new int[]{lo, position - SURAH_OFFSETS[lo] + 1};
    }

    /** Convert (surah, ayah) to flat position — O(1) */
    public static int surahAyahToPosition(int surah, int ayah) {
        return SURAH_OFFSETS[surah] + ayah - 1;
    }

    @NonNull
    @Override
    public AyahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ayah_page, parent, false);
        return new AyahViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position) {
        int[] sa = positionToSurahAyah(position);
        int surah = sa[0];
        int ayah = sa[1];
        String mode = this.displayMode;

        // Stale-bind detection
        final int generation = ++holder.bindGeneration;

        // Immediate theme setup using cached colors
        boolean isPlaying = (position == playingPosition);
        holder.container.setBackgroundColor(isPlaying ? cBgPlaying : cBg);
        holder.tvArabic.setTextColor(isPlaying ? cArabicPlaying : cArabic);
        holder.tvArabic.setTypeface(arabicFont);
        holder.tvAyahMarker.setTextColor(cSecondary);
        holder.tvAyahMarker.setText("\uFD3E " + ayah + " \uFD3F");
        holder.tvBismillah.setTextColor(cAccent);
        if (arabicFont != null) holder.tvBismillah.setTypeface(arabicFont);
        holder.dividerBottom.setBackgroundColor(cDivider);
        holder.dividerTop.setBackgroundColor(cDivider);
        holder.tvSurahHeader.setTextColor(cAccent);
        holder.tvArabic.setTextSize(arabicFontSize);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            holder.tvArabic.setJustificationMode(android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            holder.tvTranslation.setJustificationMode(android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            holder.tvExtraContent.setJustificationMode(android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            holder.tvArabic.setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
            holder.tvTranslation.setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
            holder.tvExtraContent.setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
        }
        holder.tvTranslation.setTextSize(translationFontSize);
        holder.tvExtraContent.setTextSize(translationFontSize);

        // Reset dynamic visibility
        holder.tvSurahHeader.setVisibility(View.GONE);
        holder.tvBismillah.setVisibility(View.GONE);
        holder.dividerTop.setVisibility(View.GONE);
        holder.tvRukuMarker.setVisibility(View.GONE);

        // Long press
        holder.container.setOnLongClickListener(v -> {
            if (longPressListener != null) {
                longPressListener.onAyahLongPress(surah, ayah, position);
                return true;
            }
            return false;
        });

        final Typeface transFont = cachedTransFont;
        final Typeface tafsFont = cachedTafseerFont;

        // === FAST PATH: synchronous bind from preloaded cache (non-wbw modes) ===
        if (!"wbw".equals(mode)) {
            BindData cached = null;
            synchronized (preloadLock) {
                Map<Integer, BindData> surahMap = preloadedSurahs.get(surah);
                if (surahMap != null) cached = surahMap.get(ayah);
            }
            if (cached != null) {
                bindAyahSync(holder, cached, surah, ayah, mode, transFont, tafsFont);
                // Proactively preload adjacent surahs for smooth transitions
                ensureSurahPreloaded(surah + 1);
                ensureSurahPreloaded(surah + 2);
                ensureSurahPreloaded(surah - 1);
                return;
            }
        }

        // === SLOW PATH: cache miss or wbw mode — async bind ===
        ensureSurahPreloaded(surah);
        ensureSurahPreloaded(surah + 1);

        final String edition = cachedTranslation;
        final String tafseerEdition = cachedTafseer;
        final String wbwLang = cachedWbwLang;

        executor.execute(() -> {
            if (holder.bindGeneration != generation) return;

            Ayah ayahData = repository.getAyah(surah, ayah);
            if (ayahData == null || holder.bindGeneration != generation) return;

            String contentText = null;
            String tafseerText = null;
            List<WordByWord> words = null;

            switch (mode) {
                case "translation":
                    if ("ur.jalandhry".equals(edition)) {
                        contentText = ayahData.defaultTranslation;
                    } else {
                        Translation trans = repository.getTranslation(surah, ayah, edition);
                        contentText = trans != null ? trans.text : ayahData.defaultTranslation;
                    }
                    break;
                case "tafseer":
                    if ("ur.jalandhry".equals(edition)) {
                        contentText = ayahData.defaultTranslation;
                    } else {
                        Translation trans = repository.getTranslation(surah, ayah, edition);
                        contentText = trans != null ? trans.text : ayahData.defaultTranslation;
                    }
                    Tafseer tafseer = repository.getTafseer(surah, ayah, tafseerEdition);
                    tafseerText = tafseer != null ? tafseer.text : "Tafseer not downloaded \u2014 go to Library to download";
                    break;
                case "wbw":
                    words = repository.getWords(surah, ayah, wbwLang);
                    break;
            }

            if (holder.bindGeneration != generation) return;

            final String fContent = contentText;
            final String fTafseer = tafseerText;
            final List<WordByWord> fWords = words;
            final boolean isRukuEnd = RukuData.isRukuEnd(surah, ayah);

            holder.itemView.post(() -> {
                if (holder.bindGeneration != generation) return;

                holder.tvArabic.setText(ayahData.arabicText);

                if (ayah == 1) {
                    holder.tvSurahHeader.setText(surah + ". " + ayahData.surahNameEn + " (" + ayahData.surahNameAr + ")");
                    holder.tvSurahHeader.setVisibility(View.VISIBLE);
                }
                if (ayah == 1 && surah != 1 && surah != 9) {
                    holder.tvBismillah.setVisibility(View.VISIBLE);
                    holder.dividerTop.setVisibility(View.VISIBLE);
                }

                bindContentViews(holder, mode, fContent, fTafseer, fWords, transFont, tafsFont, surah, ayah);

                if (isRukuEnd) {
                    bindRukuMarker(holder, surah, ayah);
                }
            });
        });
    }

    /** Bind ayah synchronously from preloaded cache — zero async overhead */
    private void bindAyahSync(AyahViewHolder holder, BindData data, int surah, int ayah,
                              String mode, Typeface transFont, Typeface tafsFont) {
        Ayah a = data.ayah;
        holder.tvArabic.setText(a.arabicText);

        if (ayah == 1) {
            holder.tvSurahHeader.setText(surah + ". " + a.surahNameEn + " (" + a.surahNameAr + ")");
            holder.tvSurahHeader.setVisibility(View.VISIBLE);
        }
        if (ayah == 1 && surah != 1 && surah != 9) {
            holder.tvBismillah.setVisibility(View.VISIBLE);
            holder.dividerTop.setVisibility(View.VISIBLE);
        }

        if ("arabic".equals(mode)) {
            holder.tvTranslation.setVisibility(View.GONE);
            holder.tvTranslation.setOnClickListener(null);
            holder.learningContent.setVisibility(View.GONE);
        } else if ("translation".equals(mode)) {
            holder.tvTranslation.setVisibility(View.VISIBLE);
            holder.tvTranslation.setText(data.translationText);
            holder.tvTranslation.setTextColor(cTranslation);
            holder.tvTranslation.setTypeface(transFont);
            holder.tvTranslation.setOnClickListener(v -> {
                if (contentClickListener != null) contentClickListener.onTranslationClick(surah, ayah);
            });
            holder.learningContent.setVisibility(View.GONE);
        } else if ("tafseer".equals(mode)) {
            holder.tvTranslation.setVisibility(View.VISIBLE);
            holder.tvTranslation.setText(data.translationText);
            holder.tvTranslation.setTextColor(cTranslation);
            holder.tvTranslation.setTypeface(transFont);
            holder.tvTranslation.setOnClickListener(v -> {
                if (contentClickListener != null) contentClickListener.onTranslationClick(surah, ayah);
            });
            holder.learningContent.setVisibility(View.VISIBLE);
            holder.rvWords.setVisibility(View.GONE);
            holder.tvExtraContent.setVisibility(View.VISIBLE);
            String tafsDisplay = data.tafseerText != null ? data.tafseerText : "Tafseer not downloaded \u2014 go to Library to download";
            holder.tvExtraContent.setText(tafsDisplay);
            holder.tvExtraContent.setTextColor(cSecondary);
            holder.tvExtraContent.setTypeface(tafsFont);
            holder.tvExtraContent.setOnClickListener(v -> {
                if (contentClickListener != null) contentClickListener.onTafseerClick(surah, ayah);
            });
            if (holder.tvExtraLabel != null) {
                holder.tvExtraLabel.setVisibility(View.VISIBLE);
                holder.tvExtraLabel.setText("Tafseer");
                holder.tvExtraLabel.setTextColor(cAccent);
            }
        }

        if (data.isRukuEnd) {
            bindRukuMarker(holder, surah, ayah);
        }
    }

    /** Shared content binding for async path */
    private void bindContentViews(AyahViewHolder holder, String mode, String content,
                                  String tafseerText, List<WordByWord> words,
                                  Typeface transFont, Typeface tafsFont,
                                  int surah, int ayah) {
        if ("arabic".equals(mode)) {
            holder.tvTranslation.setVisibility(View.GONE);
            holder.tvTranslation.setOnClickListener(null);
            holder.learningContent.setVisibility(View.GONE);
        } else if ("translation".equals(mode)) {
            holder.tvTranslation.setVisibility(View.VISIBLE);
            holder.tvTranslation.setText(content);
            holder.tvTranslation.setTextColor(cTranslation);
            holder.tvTranslation.setTypeface(transFont);
            holder.tvTranslation.setOnClickListener(v -> {
                if (contentClickListener != null) contentClickListener.onTranslationClick(surah, ayah);
            });
            holder.learningContent.setVisibility(View.GONE);
        } else if ("tafseer".equals(mode)) {
            holder.tvTranslation.setVisibility(View.VISIBLE);
            holder.tvTranslation.setText(content);
            holder.tvTranslation.setTextColor(cTranslation);
            holder.tvTranslation.setTypeface(transFont);
            holder.tvTranslation.setOnClickListener(v -> {
                if (contentClickListener != null) contentClickListener.onTranslationClick(surah, ayah);
            });
            holder.learningContent.setVisibility(View.VISIBLE);
            holder.rvWords.setVisibility(View.GONE);
            holder.tvExtraContent.setVisibility(View.VISIBLE);
            holder.tvExtraContent.setText(tafseerText);
            holder.tvExtraContent.setTextColor(cSecondary);
            holder.tvExtraContent.setTypeface(tafsFont);
            holder.tvExtraContent.setOnClickListener(v -> {
                if (contentClickListener != null) contentClickListener.onTafseerClick(surah, ayah);
            });
            if (holder.tvExtraLabel != null) {
                holder.tvExtraLabel.setVisibility(View.VISIBLE);
                holder.tvExtraLabel.setText("Tafseer");
                holder.tvExtraLabel.setTextColor(cAccent);
            }
        } else if ("wbw".equals(mode)) {
            holder.tvTranslation.setVisibility(View.GONE);
            if (words != null && !words.isEmpty()) {
                holder.learningContent.setVisibility(View.VISIBLE);
                holder.tvExtraContent.setVisibility(View.GONE);
                holder.rvWords.setVisibility(View.VISIBLE);
                if (!(holder.rvWords.getLayoutManager() instanceof GridLayoutManager)) {
                    holder.rvWords.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 4));
                    holder.rvWords.setHasFixedSize(true);
                    holder.rvWords.setNestedScrollingEnabled(false);
                }
                RecyclerView.Adapter<?> existing = holder.rvWords.getAdapter();
                if (existing instanceof WordAdapter) {
                    ((WordAdapter) existing).updateWords(words, arabicFontSize, translationFontSize);
                } else {
                    holder.rvWords.setAdapter(new WordAdapter(words, theme, arabicFont, arabicFontSize, translationFontSize));
                }
                if (holder.tvExtraLabel != null) holder.tvExtraLabel.setVisibility(View.GONE);
            } else {
                holder.learningContent.setVisibility(View.VISIBLE);
                holder.tvExtraContent.setVisibility(View.VISIBLE);
                holder.rvWords.setVisibility(View.GONE);
                holder.tvExtraContent.setText("Word by Word not downloaded \u2014 go to Library to download");
                holder.tvExtraContent.setTextColor(cSecondary);
                if (holder.tvExtraLabel != null) holder.tvExtraLabel.setVisibility(View.GONE);
            }
        }
    }

    /** Render the ruku end marker using cached labels */
    private void bindRukuMarker(AyahViewHolder holder, int surah, int ayah) {
        int surahRuku = RukuData.getSurahRukuNumber(surah, ayah);
        int juzRuku = RukuData.getJuzRukuNumber(surah, ayah);
        holder.tvRukuMarker.setVisibility(View.VISIBLE);
        holder.tvRukuMarker.setText("\u2500\u2500  \u06DC " + cachedSurahLabel + " " + cachedRukuLabel + " " + surahRuku
                + " \u2022 " + cachedJuzLabel + " " + cachedRukuLabel + " " + juzRuku + "  \u2500\u2500");
        holder.tvRukuMarker.setTextColor(cAccent);
    }

    /** Ensure a surah's bind data is preloaded. No-op if already cached or loading. */
    private void ensureSurahPreloaded(int surah) {
        if (surah < 1 || surah > 114) return;
        synchronized (preloadLock) {
            if (preloadedSurahs.get(surah) != null) return;
        }
        if (!loadingSurahs.add(surah)) return; // already loading

        final String edition = cachedTranslation;
        final String tafseerEdition = cachedTafseer;

        executor.execute(() -> {
            try {
                // Batch load: 3 queries total instead of N×2 per-ayah queries
                List<Ayah> ayahs = repository.getAyahsBySurah(surah);
                if (ayahs == null || ayahs.isEmpty()) return;

                // Batch load translations for entire surah (1 query)
                Map<Integer, String> transMap = new HashMap<>();
                if (!"ur.jalandhry".equals(edition)) {
                    List<Translation> transList = repository.getTranslationsBySurah(surah, edition);
                    if (transList != null) {
                        for (Translation t : transList) transMap.put(t.ayahNumber, t.text);
                    }
                }

                // Batch load tafseers for entire surah (1 query)
                Map<Integer, String> tafsMap = new HashMap<>();
                if (tafseerEdition != null) {
                    List<Tafseer> tafsList = repository.getTafseersBySurah(surah, tafseerEdition);
                    if (tafsList != null) {
                        for (Tafseer tf : tafsList) tafsMap.put(tf.ayahNumber, tf.text);
                    }
                }

                Map<Integer, BindData> map = new HashMap<>(ayahs.size());
                for (Ayah a : ayahs) {
                    String transText = "ur.jalandhry".equals(edition)
                            ? a.defaultTranslation
                            : transMap.getOrDefault(a.ayahNumber, a.defaultTranslation);
                    String tafsText = tafsMap.get(a.ayahNumber);
                    boolean ruku = RukuData.isRukuEnd(surah, a.ayahNumber);
                    map.put(a.ayahNumber, new BindData(a, transText, tafsText, ruku));
                }

                synchronized (preloadLock) {
                    // Evict oldest entries if cache is full (keep max 7 surahs)
                    while (preloadedSurahs.size() >= 7) {
                        preloadedSurahs.removeAt(0);
                    }
                    preloadedSurahs.put(surah, map);
                }
            } finally {
                loadingSurahs.remove(surah);
            }
        });
    }

    /** Call from fragment to prime the cache around the initial scroll position */
    public void preloadAround(int position) {
        int[] sa = positionToSurahAyah(position);
        int surah = sa[0];
        ensureSurahPreloaded(surah);
        ensureSurahPreloaded(surah + 1);
        if (surah > 1) ensureSurahPreloaded(surah - 1);
    }

    @Override
    public int getItemCount() {
        return TOTAL_AYAHS;
    }

    static class AyahViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvSurahHeader, tvBismillah, tvArabic, tvAyahMarker, tvTranslation;
        TextView tvExtraLabel, tvExtraContent, tvRukuMarker;
        View dividerTop, dividerBottom;
        View learningContent;
        RecyclerView rvWords;
        volatile int bindGeneration; // for stale-bind detection

        AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ayah_page_container);
            tvSurahHeader = itemView.findViewById(R.id.tv_surah_header);
            tvBismillah = itemView.findViewById(R.id.tv_bismillah);
            tvArabic = itemView.findViewById(R.id.tv_arabic);
            tvAyahMarker = itemView.findViewById(R.id.tv_ayah_marker);
            tvTranslation = itemView.findViewById(R.id.tv_translation);
            tvExtraLabel = itemView.findViewById(R.id.tv_extra_label);
            tvExtraContent = itemView.findViewById(R.id.tv_extra_content);
            tvRukuMarker = itemView.findViewById(R.id.tv_ruku_marker);
            dividerTop = itemView.findViewById(R.id.divider_top);
            dividerBottom = itemView.findViewById(R.id.divider_bottom);
            learningContent = itemView.findViewById(R.id.learning_content);
            rvWords = itemView.findViewById(R.id.rv_words);
        }
    }
}
