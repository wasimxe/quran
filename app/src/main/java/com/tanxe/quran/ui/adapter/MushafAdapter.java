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
    private final Typeface arabicFont;
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

    public MushafAdapter(QuranRepository repository, ThemeManager theme, Typeface arabicFont) {
        this.repository = repository;
        this.theme = theme;
        this.arabicFont = arabicFont;
        this.executor = repository.getExecutor();
        this.arabicFontSize = repository.getArabicFontSize();
        setHasStableIds(true);
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

        // Apply theme
        holder.container.setBackgroundColor(theme.getBackgroundColor());
        holder.tvSurahHeader.setTextColor(theme.getAccentColor());
        holder.tvBismillah.setTextColor(theme.getAccentColor());
        if (arabicFont != null) holder.tvBismillah.setTypeface(arabicFont);
        holder.tvMushafText.setTextColor(theme.getArabicTextColor());
        if (arabicFont != null) holder.tvMushafText.setTypeface(arabicFont);
        holder.tvMushafText.setTextSize(arabicFontSize);
        holder.tvMushafText.setTextIsSelectable(false);
        holder.dividerBottom.setBackgroundColor(theme.getDividerColor());
        holder.dividerTop.setBackgroundColor(theme.getDividerColor());

        // Enable clickable spans
        holder.tvMushafText.setMovementMethod(LinkMovementMethod.getInstance());
        holder.tvMushafText.setHighlightColor(0x00000000); // remove default highlight

        // Load all ayahs for this surah on background thread
        executor.execute(() -> {
            List<Ayah> ayahs = repository.getAyahsBySurah(surah);
            if (ayahs == null || ayahs.isEmpty()) return;

            // Build continuous text with clickable ayah spans
            SpannableStringBuilder sb = new SpannableStringBuilder();
            int accentColor = theme.getAccentColor();
            int activeColor = theme.getActiveAyahTextColor();

            for (int i = 0; i < ayahs.size(); i++) {
                Ayah ayah = ayahs.get(i);
                String text = ayah.arabicText.replaceAll("[\\r\\n]+", " ").trim();

                // Strip Bismillah from ayah 1 text for surahs that show separate Bismillah header
                if (ayah.ayahNumber == 1 && surah != 1 && surah != 9) {
                    text = stripBismillah(text);
                }

                int ayahTextStart = sb.length();
                sb.append(text);
                int ayahTextEnd = sb.length();

                final int ayahNum = ayah.ayahNumber;
                final int surahNum = surah;

                // Make ayah text clickable (tap + long press)
                sb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        highlightedSurah = surahNum;
                        highlightedAyah = ayahNum;
                        // Just invalidate to repaint spans — no setText/re-layout
                        widget.invalidate();
                        if (interactionListener != null) {
                            interactionListener.onAyahTapped(surahNum, ayahNum);
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                        // Read highlight state live so it's always current
                        boolean active = (surahNum == highlightedSurah && ayahNum == highlightedAyah);
                        ds.setColor(active ? activeColor : theme.getArabicTextColor());
                    }
                }, ayahTextStart, ayahTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Add ayah number marker: ﴿123﴾
                String marker = " \uFD3F" + ayah.ayahNumber + "\uFD3E ";
                int markerStart = sb.length();
                sb.append(marker);
                int markerEnd = sb.length();

                sb.setSpan(new RelativeSizeSpan(0.6f), markerStart, markerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(accentColor), markerStart, markerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Ruku end divider with surah & juz ruku numbers
                if (RukuData.isRukuEnd(surah, ayah.ayahNumber)) {
                    int surahRuku = RukuData.getSurahRukuNumber(surah, ayah.ayahNumber);
                    int juzRuku = RukuData.getJuzRukuNumber(surah, ayah.ayahNumber);
                    String lang = repository.getLanguage();
                    String ruku = Localization.get(lang, Localization.RUKU);
                    String surahL = Localization.get(lang, Localization.SURAH);
                    String juzL = Localization.get(lang, Localization.JUZ);
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

            // Long press on the whole text area for actions
            holder.itemView.post(() -> {
                holder.tvSurahHeader.setText(headerText);

                if (surah != 1 && surah != 9) {
                    holder.tvBismillah.setVisibility(View.VISIBLE);
                    holder.dividerTop.setVisibility(View.VISIBLE);
                } else {
                    holder.tvBismillah.setVisibility(View.GONE);
                    holder.dividerTop.setVisibility(View.GONE);
                }

                holder.tvMushafText.setText(sb);

                // Long press on text → use highlighted ayah or default to ayah 1
                holder.tvMushafText.setOnLongClickListener(v -> {
                    int targetAyah = (highlightedSurah == surah && highlightedAyah > 0) ? highlightedAyah : 1;
                    if (interactionListener != null) {
                        interactionListener.onAyahLongPressed(surah, targetAyah);
                    }
                    return true;
                });
            });
        });
    }

    /** Strip the Bismillah prefix from ayah 1 text to avoid duplication */
    private static String stripBismillah(String text) {
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
