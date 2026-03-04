package com.tanxe.quran.ui.learn;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tanxe.quran.R;
import com.tanxe.quran.data.dao.WordByWordDao;
import com.tanxe.quran.data.entity.KnownWord;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.WordFrequencyAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LearnFragment extends DialogFragment {
    private QuranRepository repository;
    private ThemeManager theme;

    // Views
    private TextView tvLearnTitle, tvProgressText;
    private LinearProgressIndicator progressLearn;
    private TextView btnTabWordList, btnTabFlashcard;
    private RecyclerView rvWordList;
    private View flashcardContainer;
    private CardView cardFlashcard;
    private TextView tvLearnArabic, tvLearnTranslation, tvLearnTransliteration;
    private MaterialButton btnReveal, btnKnow, btnDontKnow;

    private Typeface arabicFont;
    private List<WordByWordDao.WordFrequency> wordList = new ArrayList<>();
    private Set<String> knownWords = new HashSet<>();
    private WordFrequencyAdapter wordListAdapter;

    // Flashcard state
    private List<WordByWordDao.WordFrequency> shuffledWords = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isWordListMode = true;

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
        loadWords();
    }

    private void initViews(View view) {
        tvLearnTitle = view.findViewById(R.id.tv_learn_title);
        tvProgressText = view.findViewById(R.id.tv_progress_text);
        progressLearn = view.findViewById(R.id.progress_learn);

        btnTabWordList = view.findViewById(R.id.btn_tab_wordlist);
        btnTabFlashcard = view.findViewById(R.id.btn_tab_flashcard);

        rvWordList = view.findViewById(R.id.rv_word_list);
        rvWordList.setLayoutManager(new LinearLayoutManager(requireContext()));

        flashcardContainer = view.findViewById(R.id.flashcard_container);
        cardFlashcard = view.findViewById(R.id.card_flashcard);
        tvLearnArabic = view.findViewById(R.id.tv_learn_arabic);
        tvLearnTranslation = view.findViewById(R.id.tv_learn_translation);
        tvLearnTransliteration = view.findViewById(R.id.tv_learn_transliteration);
        btnReveal = view.findViewById(R.id.btn_reveal);
        btnKnow = view.findViewById(R.id.btn_know);
        btnDontKnow = view.findViewById(R.id.btn_dont_know);

        if (arabicFont != null) tvLearnArabic.setTypeface(arabicFont);

        // Tab switching
        btnTabWordList.setOnClickListener(v -> setTab(true));
        btnTabFlashcard.setOnClickListener(v -> setTab(false));

        // Filter chips
        ChipGroup filterChips = view.findViewById(R.id.filter_chips);
        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String filter = "all";
            if (id == R.id.chip_known) filter = "known";
            else if (id == R.id.chip_unknown) filter = "unknown";
            if (wordListAdapter != null) wordListAdapter.setFilter(filter);
        });

        // Flashcard buttons
        btnReveal.setOnClickListener(v -> revealAnswer());
        btnKnow.setOnClickListener(v -> markKnown());
        btnDontKnow.setOnClickListener(v -> markUnknown());
    }

    private void setTab(boolean wordListMode) {
        isWordListMode = wordListMode;

        btnTabWordList.setBackground(wordListMode ?
            requireContext().getDrawable(R.drawable.bg_mode_pill_active) : null);
        btnTabFlashcard.setBackground(wordListMode ?
            null : requireContext().getDrawable(R.drawable.bg_mode_pill_active));

        btnTabWordList.setTextColor(wordListMode ? theme.getPrimaryTextColor() : theme.getSecondaryTextColor());
        btnTabFlashcard.setTextColor(wordListMode ? theme.getSecondaryTextColor() : theme.getPrimaryTextColor());

        rvWordList.setVisibility(wordListMode ? View.VISIBLE : View.GONE);
        flashcardContainer.setVisibility(wordListMode ? View.GONE : View.VISIBLE);

        if (!wordListMode && !shuffledWords.isEmpty()) {
            showCurrentFlashcard();
        }
    }

    private void loadWords() {
        repository.getExecutor().execute(() -> {
            String lang = repository.getSelectedWbwLanguage();
            wordList = repository.getWords_frequencies(lang);

            if (wordList == null) wordList = new ArrayList<>();

            // Load known words
            List<KnownWord> knownList = repository.getAllKnownWords();
            knownWords.clear();
            if (knownList != null) {
                for (KnownWord kw : knownList) {
                    knownWords.add(kw.arabicWord);
                }
            }

            // Prepare shuffled list for flashcards
            shuffledWords = new ArrayList<>(wordList);
            Collections.shuffle(shuffledWords);
            currentIndex = 0;

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (wordList.isEmpty()) {
                        tvLearnArabic.setText("No word data available");
                        tvLearnTranslation.setText("Download Word by Word data first");
                        tvLearnTranslation.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Setup word list adapter
                    wordListAdapter = new WordFrequencyAdapter(wordList, knownWords, theme, arabicFont,
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
                    rvWordList.setAdapter(wordListAdapter);

                    updateProgress();
                });
            }
        });
    }

    private void showCurrentFlashcard() {
        if (shuffledWords.isEmpty() || currentIndex >= shuffledWords.size()) {
            currentIndex = 0;
            if (shuffledWords.isEmpty()) return;
        }

        WordByWordDao.WordFrequency word = shuffledWords.get(currentIndex);
        tvLearnArabic.setText(word.arabicWord);
        tvLearnTranslation.setVisibility(View.INVISIBLE);
        tvLearnTransliteration.setVisibility(View.INVISIBLE);
        btnReveal.setVisibility(View.VISIBLE);
    }

    private void revealAnswer() {
        if (shuffledWords.isEmpty() || currentIndex >= shuffledWords.size()) return;

        WordByWordDao.WordFrequency word = shuffledWords.get(currentIndex);
        tvLearnTranslation.setText("Frequency: " + word.frequency);
        tvLearnTranslation.setVisibility(View.VISIBLE);
        tvLearnTransliteration.setText(knownWords.contains(word.arabicWord) ? "✓ Already known" : "");
        tvLearnTransliteration.setVisibility(View.VISIBLE);
        btnReveal.setVisibility(View.GONE);
    }

    private void markKnown() {
        if (shuffledWords.isEmpty() || currentIndex >= shuffledWords.size()) return;

        WordByWordDao.WordFrequency word = shuffledWords.get(currentIndex);
        knownWords.add(word.arabicWord);
        repository.getExecutor().execute(() -> {
            repository.markWordKnown(new KnownWord(word.arabicWord, word.frequency));
            if (getActivity() != null) {
                requireActivity().runOnUiThread(this::updateProgress);
            }
        });

        currentIndex++;
        showCurrentFlashcard();
    }

    private void markUnknown() {
        if (shuffledWords.isEmpty() || currentIndex >= shuffledWords.size()) return;

        WordByWordDao.WordFrequency word = shuffledWords.get(currentIndex);
        knownWords.remove(word.arabicWord);
        repository.getExecutor().execute(() -> {
            repository.markWordUnknown(word.arabicWord);
            if (getActivity() != null) {
                requireActivity().runOnUiThread(this::updateProgress);
            }
        });

        currentIndex++;
        showCurrentFlashcard();
    }

    private void updateProgress() {
        if (wordListAdapter == null) return;

        int totalFreq = wordListAdapter.getTotalFrequency();
        int knownFreq = wordListAdapter.getKnownFrequency();
        int knownCount = wordListAdapter.getKnownCount();
        int totalCount = wordListAdapter.getTotalCount();
        int percent = totalFreq > 0 ? (int) ((knownFreq * 100.0) / totalFreq) : 0;

        progressLearn.setProgress(percent);
        tvProgressText.setText("Words: " + knownCount + "/" + totalCount + " known • " + percent + "%");
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
        if (cardFlashcard != null) cardFlashcard.setCardBackgroundColor(theme.getCardColor());
    }
}
