package com.tanxe.quran.ui.adapter;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.Layout;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
import com.tanxe.quran.util.Localization;
import com.tanxe.quran.util.RukuData;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Mushaf-style adapter: one item per surah, continuous Arabic text with
 * inline ayah number markers. Supports tap-to-highlight and long-press per ayah.
 */
public class MushafAdapter extends RecyclerView.Adapter<MushafAdapter.MushafViewHolder> {

    private final QuranRepository repository;
    private final ThemeManager theme;
    private Typeface arabicFont;
    private final ExecutorService executor;
    private float arabicFontSize;

    public interface OnAyahInteractionListener {
        void onAyahTapped(int surah, int ayah);
        void onAyahLongPressed(int surah, int ayah);
    }

    private OnAyahInteractionListener interactionListener;

    // Track highlighted ayah
    private int highlightedSurah = -1;
    private int highlightedAyah = -1;

    // Cache built spannable text per surah to avoid rebuilding on re-scroll
    private final android.util.SparseArray<CachedSurahData> surahCache = new android.util.SparseArray<>();

    private static class CachedSurahData {
        final CharSequence spannableText;
        final String headerText;
        CachedSurahData(CharSequence text, String header) {
            this.spannableText = text;
            this.headerText = header;
        }
    }

    public MushafAdapter(QuranRepository repository, ThemeManager theme, Typeface arabicFont) {
        this.repository = repository;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.executor = repository.getExecutor();
        this.arabicFontSize = repository.getArabicFontSize();
        setHasStableIds(true);
    }

    /** Clear the text cache (call on theme change) */
    public void clearCache() {
        surahCache.clear();
    }

    public void setInteractionListener(OnAyahInteractionListener listener) {
        this.interactionListener = listener;
    }

    // Keep backward compat
    public interface OnSurahLongPressListener {
        void onSurahLongPress(int surah);
    }
    public void setLongPressListener(OnSurahLongPressListener listener) {
        // Wrap old interface
    }

    public void setHighlightedAyah(int surah, int ayah) {
        int oldSurah = highlightedSurah;
        highlightedSurah = surah;
        highlightedAyah = ayah;
        if (oldSurah > 0) notifyItemChanged(oldSurah - 1);
        if (surah > 0 && surah != oldSurah) notifyItemChanged(surah - 1);
    }

    public void updateFontSize(float arabicSp) {
        this.arabicFontSize = arabicSp;
        notifyDataSetChanged();
    }

    public float getArabicFontSize() { return arabicFontSize; }

    public void setArabicFont(Typeface font) {
        this.arabicFont = font;
        clearCache();
    }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public int getItemCount() { return 114; }

    public int getSurahNumber(int position) { return position + 1; }

    public static int surahToPosition(int surah) { return surah - 1; }

    @NonNull
    @Override
    public MushafViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mushaf_page, parent, false);
        return new MushafViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MushafViewHolder holder, int position) {
        int surah = position + 1;

        // Stale-bind detection
        final int generation = ++holder.bindGeneration;

        // Apply theme immediately
        holder.container.setBackgroundColor(theme.getBackgroundColor());
        holder.tvSurahHeader.setTextColor(theme.getAccentColor());
        holder.tvBismillah.setTextColor(theme.getAccentColor());
        if (arabicFont != null) holder.tvBismillah.setTypeface(arabicFont);
        holder.tvMushafText.setTextColor(theme.getArabicTextColor());
        if (arabicFont != null) holder.tvMushafText.setTypeface(arabicFont);
        holder.tvMushafText.setTextSize(arabicFontSize);
        holder.tvMushafText.setElegantTextHeight(true);
        holder.tvMushafText.setIncludeFontPadding(true);
        // Non-selectable + null movement method = justified alignment preserved permanently
        holder.tvMushafText.setTextIsSelectable(false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            holder.tvMushafText.setJustificationMode(android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            holder.tvMushafText.setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
        }
        holder.dividerBottom.setBackgroundColor(theme.getDividerColor());
        holder.dividerTop.setBackgroundColor(theme.getDividerColor());
        holder.tvMushafText.setMovementMethod(null);
        holder.tvMushafText.setHighlightColor(0x00000000);
        holder.tvMushafText.setClickable(true);
        holder.tvMushafText.setLongClickable(true);
        // Tap = highlight + compare sheet; finger slide ignored; long press = compare sheet (all)
        final float[] touchDown = new float[2];
        final boolean[] touchSlid = {false};
        final int touchSlop = android.view.ViewConfiguration.get(holder.tvMushafText.getContext()).getScaledTouchSlop();
        holder.tvMushafText.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    touchDown[0] = event.getX();
                    touchDown[1] = event.getY();
                    touchSlid[0] = false;
                    break;
                case android.view.MotionEvent.ACTION_MOVE:
                    if (!touchSlid[0]) {
                        float mdx = event.getX() - touchDown[0];
                        float mdy = event.getY() - touchDown[1];
                        // Use larger threshold (touchSlop*4) so natural finger jitter
                        // during genuine long press doesn't cancel it
                        if (mdx * mdx + mdy * mdy > touchSlop * touchSlop * 16) {
                            touchSlid[0] = true;
                            v.cancelLongPress();
                        }
                    }
                    break;
                case android.view.MotionEvent.ACTION_UP:
                    if (touchSlid[0]) break;
                    float dx = event.getX() - touchDown[0];
                    float dy = event.getY() - touchDown[1];
                    if (dx * dx + dy * dy > touchSlop * touchSlop * 4) break;
                    TextView tv = (TextView) v;
                    android.text.Layout layout = tv.getLayout();
                    if (layout != null) {
                        int x = (int) event.getX() - tv.getTotalPaddingLeft() + tv.getScrollX();
                        int y = (int) event.getY() - tv.getTotalPaddingTop() + tv.getScrollY();
                        int line = layout.getLineForVertical(y);
                        int offset = layout.getOffsetForHorizontal(line, x);
                        CharSequence text = tv.getText();
                        if (text instanceof android.text.Spanned) {
                            android.text.style.ClickableSpan[] spans =
                                    ((android.text.Spanned) text).getSpans(offset, offset, android.text.style.ClickableSpan.class);
                            if (spans.length > 0) {
                                spans[0].onClick(tv);
                                return true;
                            }
                        }
                    }
                    break;
            }
            return false;
        });
        // Long press listener set synchronously to guarantee it's always available
        setupLongPress(holder, surah);

        // Bismillah visibility (known statically)
        if (surah != 1 && surah != 9) {
            holder.tvBismillah.setVisibility(View.VISIBLE);
            holder.dividerTop.setVisibility(View.VISIBLE);
        } else {
            holder.tvBismillah.setVisibility(View.GONE);
            holder.dividerTop.setVisibility(View.GONE);
        }

        // Check cache first — if available, bind immediately without async
        CachedSurahData cached = surahCache.get(surah);
        if (cached != null) {
            holder.tvSurahHeader.setText(cached.headerText);
            holder.tvMushafText.setText(cached.spannableText);
            // Just invalidate to refresh highlight colors via updateDrawState
            holder.tvMushafText.invalidate();
            return;
        }

        // Load on background thread
        executor.execute(() -> {
            if (holder.bindGeneration != generation) return;

            List<Ayah> ayahs = repository.getAyahsBySurah(surah);
            if (ayahs == null || ayahs.isEmpty() || holder.bindGeneration != generation) return;

            // Build continuous text with clickable ayah spans
            SpannableStringBuilder sb = new SpannableStringBuilder();
            int accentColor = theme.getAccentColor();
            int activeColor = theme.getActiveAyahTextColor();
            int normalColor = theme.getArabicTextColor();
            String lang = repository.getLanguage();

            for (int i = 0; i < ayahs.size(); i++) {
                Ayah ayah = ayahs.get(i);
                String text = ayah.arabicText.replaceAll("[\\r\\n]+", " ").trim();

                if (ayah.ayahNumber == 1 && surah != 1 && surah != 9) {
                    text = stripBismillah(text);
                }

                int ayahTextStart = sb.length();
                sb.append(text);
                int ayahTextEnd = sb.length();

                final int ayahNum = ayah.ayahNumber;
                final int surahNum = surah;

                sb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        highlightedSurah = surahNum;
                        highlightedAyah = ayahNum;
                        widget.postInvalidate();
                        if (interactionListener != null) {
                            interactionListener.onAyahTapped(surahNum, ayahNum);
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                        boolean active = (surahNum == highlightedSurah && ayahNum == highlightedAyah);
                        ds.setColor(active ? activeColor : normalColor);
                    }
                }, ayahTextStart, ayahTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                String marker = " (" + ayah.ayahNumber + ") ";
                int markerStart = sb.length();
                sb.append(marker);
                int markerEnd = sb.length();
                sb.setSpan(new RelativeSizeSpan(0.6f), markerStart, markerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(accentColor), markerStart, markerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (RukuData.isRukuEnd(surah, ayah.ayahNumber)) {
                    int surahRuku = RukuData.getSurahRukuNumber(surah, ayah.ayahNumber);
                    int juzRuku = RukuData.getJuzRukuNumber(surah, ayah.ayahNumber);
                    String[] labels = getRukuLabels(lang);
                    String ruku = labels[0];
                    String surahL = labels[1];
                    String juzL = labels[2];
                    String rukuLine = "\n\u2500\u2500\u2500  \u06DC " + surahL + " " + ruku + " " + surahRuku
                            + " \u2022 " + juzL + " " + ruku + " " + juzRuku
                            + "  \u2500\u2500\u2500";
                    int rukuStart = sb.length();
                    sb.append(rukuLine);
                    int rukuEnd = sb.length();
                    sb.setSpan(new RelativeSizeSpan(0.45f), rukuStart, rukuEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new ForegroundColorSpan(accentColor), rukuStart, rukuEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), rukuStart, rukuEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.append("\n");
                }
            }

            String headerText = surah + ". " + ayahs.get(0).surahNameEn + " (" + ayahs.get(0).surahNameAr + ")";

            // Cache the built text for this surah
            surahCache.put(surah, new CachedSurahData(sb, headerText));

            if (holder.bindGeneration != generation) return;

            holder.itemView.post(() -> {
                if (holder.bindGeneration != generation) return;
                holder.tvSurahHeader.setText(headerText);
                holder.tvMushafText.setText(sb);
            });
        });
    }

    private void setupLongPress(MushafViewHolder holder, int surah) {
        holder.tvMushafText.setOnLongClickListener(v -> {
            int targetAyah = (highlightedSurah == surah && highlightedAyah > 0) ? highlightedAyah : 1;
            if (interactionListener != null) {
                interactionListener.onAyahLongPressed(surah, targetAyah);
            }
            return true;
        });
    }

    /** Strip the Bismillah prefix from ayah 1 text to avoid duplication */
    public static String stripBismillah(String text) {
        // Strip all diacritics and normalize alef variants to plain alef for comparison
        String normalized = text
                .replaceAll("[\\u064B-\\u0652\\u0670\\u06D6-\\u06ED]", "") // diacritics
                .replace('\u0671', '\u0627'); // alef wasla → alef
        String baseBismillah = "\u0628\u0633\u0645 \u0627\u0644\u0644\u0647 \u0627\u0644\u0631\u062D\u0645\u0646 \u0627\u0644\u0631\u062D\u064A\u0645"; // بسم الله الرحمن الرحيم
        // Also try without the yaa diacritic
        String baseBismillah2 = "\u0628\u0633\u0645 \u0627\u0644\u0644\u0647 \u0627\u0644\u0631\u062D\u0645\u0646 \u0627\u0644\u0631\u062D\u0645"; // بسم الله الرحمن الرحم

        if (normalized.startsWith(baseBismillah) || normalized.startsWith(baseBismillah2)) {
            // Map normalized index back to original text index
            int normIdx = 0;
            int origIdx = 0;
            int targetLen = normalized.startsWith(baseBismillah) ? baseBismillah.length() : baseBismillah2.length();
            while (normIdx < targetLen && origIdx < text.length()) {
                char origChar = text.charAt(origIdx);
                // Skip diacritics in original
                if ((origChar >= 0x064B && origChar <= 0x0652) || origChar == 0x0670 ||
                    (origChar >= 0x06D6 && origChar <= 0x06ED)) {
                    origIdx++;
                    continue;
                }
                normIdx++;
                origIdx++;
            }
            // Also skip any trailing space
            while (origIdx < text.length() && text.charAt(origIdx) == ' ') origIdx++;
            return text.substring(origIdx).trim();
        }
        return text;
    }

    /** Returns localized labels: [ruku, surah, juz] */
    public static String[] getRukuLabels(String lang) {
        if (lang == null) lang = "en";
        switch (lang) {
            case "ur": return new String[]{"\u0631\u06A9\u0648\u0639", "\u0633\u0648\u0631\u06C1", "\u067E\u0627\u0631\u06C1"}; // رکوع، سورہ، پارہ
            case "ar": return new String[]{"\u0631\u0643\u0648\u0639", "\u0633\u0648\u0631\u0629", "\u062C\u0632\u0621"}; // ركوع، سورة، جزء
            case "fa": return new String[]{"\u0631\u06A9\u0648\u0639", "\u0633\u0648\u0631\u0647", "\u062C\u0632\u0621"}; // رکوع، سوره، جزء
            case "tr": return new String[]{"Rek\u00E2t", "Sure", "C\u00FCz"};
            case "id": return new String[]{"Ruku", "Surah", "Juz"};
            case "bn": return new String[]{"\u09B0\u09C1\u0995\u09C1", "\u09B8\u09C2\u09B0\u09BE", "\u09AA\u09BE\u09B0\u09BE"}; // রুকু, সূরা, পারা
            case "fr": return new String[]{"Ruku", "Sourate", "Juz"};
            case "de": return new String[]{"Ruku", "Sure", "Juz"};
            case "ms": return new String[]{"Ruku", "Surah", "Juz"};
            case "hi": return new String[]{"\u0930\u0941\u0915\u0942\u0905", "\u0938\u0942\u0930\u0939", "\u092A\u093E\u0930\u093E"}; // रुकूअ, सूरह, पारा
            default:   return new String[]{"Ruku", "Surah", "Juz"};
        }
    }

    static class MushafViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tvSurahHeader, tvBismillah, tvMushafText;
        View dividerTop, dividerBottom;
        volatile int bindGeneration;

        MushafViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.mushaf_container);
            tvSurahHeader = itemView.findViewById(R.id.tv_surah_header);
            tvBismillah = itemView.findViewById(R.id.tv_bismillah);
            tvMushafText = itemView.findViewById(R.id.tv_mushaf_text);
            dividerTop = itemView.findViewById(R.id.divider_top);
            dividerBottom = itemView.findViewById(R.id.divider_bottom);
        }
    }
}
