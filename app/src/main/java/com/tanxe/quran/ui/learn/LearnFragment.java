package com.tanxe.quran.ui.learn;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tanxe.quran.R;
import com.tanxe.quran.data.dao.AyahDao;
import com.tanxe.quran.data.dao.WordByWordDao;
import com.tanxe.quran.data.entity.KnownWord;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.WordFrequencyAdapter;
import com.tanxe.quran.util.ArabicUtils;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LearnFragment extends DialogFragment {
    private QuranRepository repository;
    private ThemeManager theme;

    // Views
    private TextView tvLearnTitle, tvProgressText;
    private LinearProgressIndicator progressLearn;
    private ImageButton btnClose;
    private Spinner spinnerScope;
    private MaterialButton btnReset;
    private CardView cardFlashcard;
    private TextView tvLearnArabic, tvLearnFreq, tvLearnTranslation;
    private MaterialButton btnReveal, btnKnow, btnDontKnow;
    private MaterialButton btnPrevWord, btnNextWord, btnSearchWord;
    private TextView tvWordIndex;
    private RecyclerView rvWordList;

    private Typeface arabicFont;
    private List<WordByWordDao.WordWithTranslation> allWords = new ArrayList<>();
    private Set<String> knownWords = new HashSet<>();
    private WordFrequencyAdapter wordListAdapter;

    // Scope: 0 = Full Quran, 1-114 = specific surah
    private int currentScope = 0;
    private List<AyahDao.SurahInfo> surahList = new ArrayList<>();

    // Flashcard navigation state
    private int currentWordIndex = 0;
    private boolean translationRevealed = false;

    // Similar words: base form → list of words sharing that base
    private Map<String, List<WordByWordDao.WordWithTranslation>> baseFormMap = new HashMap<>();
    private static final String[] ARABIC_PREFIXES = {"و", "ب", "ل", "ف", "ك", "وال", "بال", "لل", "فال", "ال"};
    private List<WordByWordDao.WordWithTranslation> currentSimilarWords = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_NoActionBar);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());

        try {
            arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/al_mushaf.ttf");
        } catch (Exception ignored) {}

        initViews(view);
        applyTheme();
        loadSurahList();
    }

    private void initViews(View view) {
        tvLearnTitle = view.findViewById(R.id.tv_learn_title);
        tvProgressText = view.findViewById(R.id.tv_progress_text);
        progressLearn = view.findViewById(R.id.progress_learn);
        btnClose = view.findViewById(R.id.btn_close);
        spinnerScope = view.findViewById(R.id.spinner_scope);
        btnReset = view.findViewById(R.id.btn_reset);
        cardFlashcard = view.findViewById(R.id.card_flashcard);
        tvLearnArabic = view.findViewById(R.id.tv_learn_arabic);
        tvLearnFreq = view.findViewById(R.id.tv_learn_freq);
        tvLearnTranslation = view.findViewById(R.id.tv_learn_translation);
        btnReveal = view.findViewById(R.id.btn_reveal);
        btnKnow = view.findViewById(R.id.btn_know);
        btnDontKnow = view.findViewById(R.id.btn_dont_know);
        btnPrevWord = view.findViewById(R.id.btn_prev_word);
        btnNextWord = view.findViewById(R.id.btn_next_word);
        btnSearchWord = view.findViewById(R.id.btn_search_word);
        tvWordIndex = view.findViewById(R.id.tv_word_index);
        rvWordList = view.findViewById(R.id.rv_word_list);
        rvWordList.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (arabicFont != null) {
            tvLearnArabic.setTypeface(arabicFont);
        }

        // Close button
        btnClose.setOnClickListener(v -> dismiss());

        // Swipe gesture on flashcard for prev/next
        GestureDetector gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                if (Math.abs(dx) > 100 && Math.abs(velocityX) > 200) {
                    if (dx > 0) navigateWord(-1); // swipe right = prev
                    else navigateWord(1);           // swipe left = next
                    return true;
                }
                return false;
            }
            @Override
            public boolean onDown(MotionEvent e) { return true; }
        });
        cardFlashcard.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // Similar words popup button
        btnReveal.setOnClickListener(v -> showSimilarWordsPopup());
        btnKnow.setOnClickListener(v -> markCurrentWord(true));
        btnDontKnow.setOnClickListener(v -> markCurrentWord(false));
        btnPrevWord.setOnClickListener(v -> navigateWord(-1));
        btnNextWord.setOnClickListener(v -> navigateWord(1));
        btnSearchWord.setOnClickListener(v -> searchCurrentWordInQuran());

        // Reset button
        btnReset.setOnClickListener(v -> confirmReset());

        // Filter chips
        ChipGroup filterChips = view.findViewById(R.id.filter_chips);
        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String filter = "all";
            if (id == R.id.chip_known) filter = "known";
            else if (id == R.id.chip_unknown) filter = "unknown";
            if (wordListAdapter != null) {
                wordListAdapter.setFilter(filter);
                // Reset to first word in filtered list
                currentWordIndex = 0;
                showCurrentWord();
            }
        });

        // Scope spinner selection
        spinnerScope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                currentScope = position; // 0 = Full Quran, 1+ = surah number
                loadWords();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSurahList() {
        repository.getExecutor().execute(() -> {
            surahList = repository.getAllSurahs();
            if (surahList == null) surahList = new ArrayList<>();

            // Load known words
            List<KnownWord> knownList = repository.getAllKnownWords();
            knownWords.clear();
            if (knownList != null) {
                for (KnownWord kw : knownList) {
                    knownWords.add(kw.arabicWord);
                }
            }

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    // Build scope spinner items
                    List<String> scopeItems = new ArrayList<>();
                    scopeItems.add("Full Quran");
                    for (AyahDao.SurahInfo s : surahList) {
                        scopeItems.add(s.surahNumber + ". " + s.surahNameEn);
                    }
                    ArrayAdapter<String> scopeAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, scopeItems);
                    scopeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerScope.setAdapter(scopeAdapter);
                    // This triggers onItemSelected → loadWords()
                });
            }
        });
    }

    private void loadWords() {
        repository.getExecutor().execute(() -> {
            String lang = repository.getSelectedWbwLanguage();
            Log.d("LearnFragment", "loadWords: lang=" + lang + " scope=" + currentScope);

            if (currentScope == 0) {
                allWords = repository.getWordsWithTranslations(lang);
            } else if (currentScope > 0 && currentScope <= surahList.size()) {
                int surahNum = surahList.get(currentScope - 1).surahNumber;
                allWords = repository.getWordsWithTranslationsBySurah(lang, surahNum);
            }

            if (allWords == null) allWords = new ArrayList<>();
            Log.d("LearnFragment", "loadWords: found " + allWords.size() + " words");

            // Build base form map for similar words lookup
            Log.d("LearnFragment", "buildBaseFormMap start");
            buildBaseFormMap();
            Log.d("LearnFragment", "buildBaseFormMap done, getActivity=" + (getActivity() != null));

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    Log.d("LearnFragment", "UI thread: setting up adapter, words=" + allWords.size());
                    if (allWords.isEmpty()) {
                        tvLearnArabic.setText("No word data");
                        tvLearnFreq.setText("Download Word by Word data first");
                        tvLearnFreq.setVisibility(View.VISIBLE);
                        tvLearnTranslation.setVisibility(View.GONE);
                        return;
                    }

                    // Setup adapter
                    wordListAdapter = new WordFrequencyAdapter(allWords, knownWords, theme, arabicFont,
                            (word, markKnown) -> {
                                repository.getExecutor().execute(() -> {
                                    if (markKnown) {
                                        repository.markWordKnown(new KnownWord(word.arabicWord, word.frequency));
                                    } else {
                                        repository.markWordUnknown(word.arabicWord);
                                    }
                                    if (getActivity() != null) {
                                        requireActivity().runOnUiThread(this::updateProgress);
                                    }
                                });
                            });

                    wordListAdapter.setClickListener((word, index) -> {
                        currentWordIndex = index;
                        showCurrentWord();
                    });

                    rvWordList.setAdapter(wordListAdapter);
                    Log.d("LearnFragment", "adapter set, calling showCurrentWord");
                    currentWordIndex = 0;
                    showCurrentWord();
                    updateProgress();
                });
            }
        });
    }

    private void showCurrentWord() {
        Log.d("LearnFragment", "showCurrentWord: adapter=" + (wordListAdapter != null) + " index=" + currentWordIndex);
        if (wordListAdapter == null) return;

        WordByWordDao.WordWithTranslation word = wordListAdapter.getWordAt(currentWordIndex);
        if (word == null) {
            tvLearnArabic.setText("—");
            tvLearnFreq.setText("");
            tvLearnTranslation.setVisibility(View.INVISIBLE);
            tvWordIndex.setText("");
            return;
        }

        tvLearnArabic.setText(word.arabicWord);
        tvLearnFreq.setText("×" + word.frequency);
        tvLearnFreq.setTextColor(theme.getAccentColor());

        // Show translation immediately, similar words button shown after load
        tvLearnTranslation.setText("...");
        tvLearnTranslation.setVisibility(View.VISIBLE);
        btnReveal.setVisibility(View.GONE); // will be shown if similar words found
        translationRevealed = true;

        // Load translations + similar words in background
        loadTranslationAndSimilar(word);

        // Update word index display
        int total = wordListAdapter.getFilteredCount();
        tvWordIndex.setText((currentWordIndex + 1) + " / " + total);
        tvWordIndex.setTextColor(theme.getSecondaryTextColor());

        // Highlight in list
        wordListAdapter.setSelectedIndex(currentWordIndex);

        // Scroll list to show current word
        if (currentWordIndex >= 0) {
            rvWordList.scrollToPosition(currentWordIndex);
        }
    }

    private void loadTranslationAndSimilar(WordByWordDao.WordWithTranslation word) {
        repository.getExecutor().execute(() -> {
            String lang = repository.getSelectedWbwLanguage();
            List<WordByWordDao.TranslationCount> translations =
                    repository.getTranslationsForWord(lang, word.arabicWord);
            // Cache similar words for popup
            currentSimilarWords = findSimilarWords(word);

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    // Show only the top (most common) translation
                    String meaning;
                    if (translations != null && !translations.isEmpty()) {
                        meaning = translations.get(0).translation;
                    } else {
                        meaning = word.translation != null ? word.translation : "—";
                    }

                    tvLearnTranslation.setText(meaning);
                    tvLearnTranslation.setVisibility(View.VISIBLE);

                    // Show/hide similar words button
                    if (currentSimilarWords != null && !currentSimilarWords.isEmpty()) {
                        btnReveal.setText("Similar (" + currentSimilarWords.size() + ")");
                        btnReveal.setVisibility(View.VISIBLE);
                    } else {
                        btnReveal.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void showSimilarWordsPopup() {
        if (currentSimilarWords == null || currentSimilarWords.isEmpty()) return;

        WordByWordDao.WordWithTranslation current = wordListAdapter != null ?
                wordListAdapter.getWordAt(currentWordIndex) : null;
        String title = current != null ? "Similar to: " + current.arabicWord : "Similar Words";

        // Build list items: "arabicWord  =  translation  (×frequency)"
        String[] items = new String[currentSimilarWords.size()];
        for (int i = 0; i < currentSimilarWords.size(); i++) {
            WordByWordDao.WordWithTranslation sw = currentSimilarWords.get(i);
            String trans = sw.translation != null ? sw.translation : "—";
            items[i] = sw.arabicWord + "  =  " + trans + "  (×" + sw.frequency + ")";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(items, null)
                .setPositiveButton("Close", null)
                .show();
    }

    /** Build a map of normalized base form → list of words for fast similar word lookup */
    private void buildBaseFormMap() {
        baseFormMap.clear();
        for (WordByWordDao.WordWithTranslation w : allWords) {
            if (w.arabicWord == null || w.arabicWord.isEmpty()) continue;
            String base = ArabicUtils.normalizeArabic(w.arabicWord);
            baseFormMap.computeIfAbsent(base, k -> new ArrayList<>()).add(w);

            // Also index without common prefixes
            for (String prefix : ARABIC_PREFIXES) {
                if (base.startsWith(prefix) && base.length() > prefix.length()) {
                    String root = base.substring(prefix.length());
                    baseFormMap.computeIfAbsent(root, k -> new ArrayList<>()).add(w);
                }
            }
        }
    }

    /** Find similar words by matching base forms and prefix variants (like AHK script) */
    private List<WordByWordDao.WordWithTranslation> findSimilarWords(WordByWordDao.WordWithTranslation currentWord) {
        if (currentWord == null || currentWord.arabicWord == null) return new ArrayList<>();

        String base = ArabicUtils.normalizeArabic(currentWord.arabicWord);
        Set<String> seen = new HashSet<>();
        seen.add(currentWord.arabicWord); // exclude the current word itself
        List<WordByWordDao.WordWithTranslation> results = new ArrayList<>();

        // Collect candidate base forms to look up
        List<String> candidates = new ArrayList<>();
        candidates.add(base);
        // Strip prefixes to find root
        for (String prefix : ARABIC_PREFIXES) {
            if (base.startsWith(prefix) && base.length() > prefix.length()) {
                candidates.add(base.substring(prefix.length()));
            }
        }
        // Add prefixed variants
        List<String> roots = new ArrayList<>(candidates);
        for (String root : roots) {
            for (String prefix : ARABIC_PREFIXES) {
                candidates.add(prefix + root);
            }
        }

        // Look up each candidate in the base form map
        for (String candidate : candidates) {
            List<WordByWordDao.WordWithTranslation> matches = baseFormMap.get(candidate);
            if (matches == null) continue;
            for (WordByWordDao.WordWithTranslation match : matches) {
                if (results.size() >= 8) break;
                if (!seen.contains(match.arabicWord)) {
                    seen.add(match.arabicWord);
                    results.add(match);
                }
            }
            if (results.size() >= 8) break;
        }

        return results;
    }

    private void markCurrentWord(boolean known) {
        WordByWordDao.WordWithTranslation word = wordListAdapter != null ?
                wordListAdapter.getWordAt(currentWordIndex) : null;
        if (word == null) return;

        if (known) {
            knownWords.add(word.arabicWord);
        } else {
            knownWords.remove(word.arabicWord);
        }

        repository.getExecutor().execute(() -> {
            if (known) {
                repository.markWordKnown(new KnownWord(word.arabicWord, word.frequency));
            } else {
                repository.markWordUnknown(word.arabicWord);
            }
            if (getActivity() != null) {
                requireActivity().runOnUiThread(this::updateProgress);
            }
        });

        // Update adapter's known set and refresh
        if (wordListAdapter != null) {
            wordListAdapter.toggleKnown(word.arabicWord, known);
        }

        // Auto-advance to next word
        navigateWord(1);
    }

    private void navigateWord(int delta) {
        if (wordListAdapter == null) return;
        int count = wordListAdapter.getFilteredCount();
        if (count == 0) return;

        currentWordIndex += delta;
        if (currentWordIndex < 0) currentWordIndex = count - 1;
        if (currentWordIndex >= count) currentWordIndex = 0;

        showCurrentWord();
    }

    private void confirmReset() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Learning Progress")
                .setMessage("This will mark all words as unknown. Are you sure?")
                .setPositiveButton("Reset", (d, w) -> resetProgress())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetProgress() {
        repository.getExecutor().execute(() -> {
            repository.clearAllKnownWords();
            knownWords.clear();

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    // Reload words to refresh adapter
                    loadWords();
                });
            }
        });
    }

    private void updateProgress() {
        if (wordListAdapter == null) return;

        int totalFreq = wordListAdapter.getTotalFrequency();
        int knownFreq = wordListAdapter.getKnownFrequency();
        int knownCount = wordListAdapter.getKnownCount();
        int totalCount = wordListAdapter.getTotalCount();
        int percent = totalFreq > 0 ? (int) ((knownFreq * 100.0) / totalFreq) : 0;

        progressLearn.setProgress(percent);
        tvProgressText.setText(percent + "% | " + knownCount + "/" + totalCount + " words");
    }

    private void searchCurrentWordInQuran() {
        WordByWordDao.WordWithTranslation word = wordListAdapter != null ?
                wordListAdapter.getWordAt(currentWordIndex) : null;
        if (word == null || word.arabicWord == null || word.arabicWord.isEmpty()) return;

        dismiss();
        if (getActivity() instanceof com.tanxe.quran.MainActivity) {
            ((com.tanxe.quran.MainActivity) getActivity()).navigateToSearch(word.arabicWord);
        }
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());
        View container = getView().findViewById(R.id.learn_container);
        container.setBackgroundColor(theme.getBackgroundColor());
        tvLearnTitle.setTextColor(theme.getPrimaryTextColor());
        tvProgressText.setTextColor(theme.getSecondaryTextColor());
        tvLearnArabic.setTextColor(theme.getArabicTextColor());
        tvLearnTranslation.setTextColor(theme.getTranslationTextColor());
        if (tvLearnFreq != null) tvLearnFreq.setTextColor(theme.getAccentColor());
        if (cardFlashcard != null) cardFlashcard.setCardBackgroundColor(theme.getCardColor());
    }
}
