package com.tanxe.quran.ui.reading;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.tanxe.quran.R;
import com.tanxe.quran.audio.AudioPlayerManager;
import com.tanxe.quran.data.dao.AyahDao;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.AyahPagerAdapter;
import com.tanxe.quran.util.QuranDataParser;

import java.util.ArrayList;
import java.util.List;

public class ReadingFragment extends Fragment {
    private QuranRepository repository;
    private ThemeManager theme;
    private AudioPlayerManager audioPlayer;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private AyahPagerAdapter pagerAdapter;

    // Pinch-to-zoom
    private ScaleGestureDetector scaleGestureDetector;
    private static final float MIN_ARABIC_SP = 16f;
    private static final float MAX_ARABIC_SP = 60f;
    private float currentArabicSize;
    private float currentTransSize;

    // Header views
    private TextView tvSurahName, tvAyahCounter, tvJuzBadge;
    private ImageButton btnBookmark;
    private TextView btnModeReading, btnModeLearning;

    // Source bar (learning mode)
    private View sourceBar;
    private Spinner spinnerSource;
    private ChipGroup sourceChips;

    // Toolbar
    private ImageButton btnPrevious, btnPlay, btnNext, btnRepeat, btnShare, btnRandom;
    private View floatingToolbar;

    private Typeface arabicFont, urduFont;
    private List<AyahDao.SurahInfo> surahs = new ArrayList<>();
    private int currentSurah = 1;
    private int currentAyah = 1;
    private boolean isLearningMode = false;
    private String displayMode = "translation";
    private boolean isPlaying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = QuranRepository.getInstance(requireContext());
        theme = ThemeManager.getInstance(requireContext());
        audioPlayer = AudioPlayerManager.getInstance(requireContext());

        try {
            arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/al_mushaf.ttf");
            urduFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/gulzar.ttf");
        } catch (Exception e) {
            arabicFont = Typeface.DEFAULT;
            urduFont = Typeface.DEFAULT;
        }

        // Init font sizes from prefs
        currentArabicSize = repository.getArabicFontSize();
        currentTransSize = repository.getTranslationFontSize();

        initViews(view);
        applyTheme();
        loadSurahs();

        // Restore position
        int[] pos = repository.getCurrentPosition();
        currentSurah = pos[0];
        currentAyah = pos[1];
        displayMode = repository.getDisplayMode();

        setupRecyclerView();
        setupPinchToZoom();
        setupGestures(view);
    }

    private void initViews(View view) {
        // Header
        tvSurahName = view.findViewById(R.id.tv_surah_name);
        tvAyahCounter = view.findViewById(R.id.tv_ayah_counter);
        tvJuzBadge = view.findViewById(R.id.tv_juz_badge);
        btnBookmark = view.findViewById(R.id.btn_bookmark);

        // Mode toggle
        btnModeReading = view.findViewById(R.id.btn_mode_reading);
        btnModeLearning = view.findViewById(R.id.btn_mode_learning);

        // Source bar
        sourceBar = view.findViewById(R.id.source_bar);
        spinnerSource = view.findViewById(R.id.spinner_source);
        sourceChips = view.findViewById(R.id.source_chips);

        // RecyclerView
        recyclerView = view.findViewById(R.id.rv_ayahs);

        // Toolbar
        floatingToolbar = view.findViewById(R.id.floating_toolbar);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPlay = view.findViewById(R.id.btn_play);
        btnNext = view.findViewById(R.id.btn_next);
        btnRepeat = view.findViewById(R.id.btn_repeat);
        btnShare = view.findViewById(R.id.btn_share);
        btnRandom = view.findViewById(R.id.btn_random);

        // Tap surah name → open bottom sheet
        tvSurahName.setOnClickListener(v -> openSurahSelector());

        // Tap ayah counter → number picker dialog
        tvAyahCounter.setOnClickListener(v -> openAyahPicker());

        // Mode toggle
        btnModeReading.setOnClickListener(v -> setMode(false));
        btnModeLearning.setOnClickListener(v -> setMode(true));

        // Bookmark
        btnBookmark.setOnClickListener(v -> toggleBookmark());

        // Toolbar buttons
        btnPrevious.setOnClickListener(v -> navigatePrev());
        btnNext.setOnClickListener(v -> navigateNext());
        btnPlay.setOnClickListener(v -> togglePlay());
        btnRepeat.setOnClickListener(v -> toggleRepeat());
        btnShare.setOnClickListener(v -> shareAyah());
        btnRandom.setOnClickListener(v -> loadRandom());

        // Source chips (learning mode)
        sourceChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_translation) displayMode = "translation";
            else if (id == R.id.chip_tafseer) displayMode = "tafseer";
            else if (id == R.id.chip_wbw) displayMode = "wbw";
            repository.saveDisplayMode(displayMode);
            setupSourceSpinner();
            if (pagerAdapter != null) {
                pagerAdapter.setDisplayMode(displayMode);
            }
        });

        btnRepeat.setAlpha(repository.getRepeatMode() ? 1.0f : 0.5f);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        pagerAdapter = new AyahPagerAdapter(repository, theme, arabicFont, urduFont);
        pagerAdapter.setLearningMode(isLearningMode);
        pagerAdapter.setDisplayMode(displayMode);
        recyclerView.setAdapter(pagerAdapter);

        // Scroll to saved position
        int startPos = AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah);
        recyclerView.scrollToPosition(startPos);

        // Track scroll to update header
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible != RecyclerView.NO_POSITION) {
                    int[] sa = AyahPagerAdapter.positionToSurahAyah(firstVisible);
                    if (sa[0] != currentSurah || sa[1] != currentAyah) {
                        currentSurah = sa[0];
                        currentAyah = sa[1];
                        updateHeader();
                        repository.saveCurrentPosition(currentSurah, currentAyah);
                    }
                }
            }
        });

        updateHeader();
    }

    private void setupPinchToZoom() {
        scaleGestureDetector = new ScaleGestureDetector(requireContext(),
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float factor = detector.getScaleFactor();
                    float newArabic = Math.max(MIN_ARABIC_SP, Math.min(MAX_ARABIC_SP, currentArabicSize * factor));
                    // Keep proportional ratio
                    float ratio = currentTransSize / currentArabicSize;
                    float newTrans = newArabic * ratio;

                    currentArabicSize = newArabic;
                    currentTransSize = newTrans;
                    pagerAdapter.updateFontSize(currentArabicSize, currentTransSize);
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    // Persist to prefs
                    repository.setArabicFontSize(currentArabicSize);
                    repository.setTranslationFontSize(currentTransSize);
                }
            });

        recyclerView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            // Don't consume if not scaling — let RecyclerView scroll
            return scaleGestureDetector.isInProgress();
        });
    }

    private void setupGestures(View view) {
        // Double-tap on header to toggle learning mode
        GestureDetector gestureDetector = new GestureDetector(requireContext(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    setMode(!isLearningMode);
                    return true;
                }
            });

        view.findViewById(R.id.header_bar).setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    private void setMode(boolean learning) {
        isLearningMode = learning;

        // Update pill appearance
        btnModeReading.setBackground(learning ? null :
            requireContext().getDrawable(R.drawable.bg_mode_pill_active));
        btnModeLearning.setBackground(learning ?
            requireContext().getDrawable(R.drawable.bg_mode_pill_active) : null);

        btnModeReading.setTextColor(learning ? theme.getSecondaryTextColor() : theme.getPrimaryTextColor());
        btnModeLearning.setTextColor(learning ? theme.getPrimaryTextColor() : theme.getSecondaryTextColor());

        // Show/hide source bar
        sourceBar.setVisibility(learning ? View.VISIBLE : View.GONE);
        spinnerSource.setVisibility(learning ? View.VISIBLE : View.GONE);

        if (learning) {
            setupSourceSpinner();
        }

        // Update adapter
        if (pagerAdapter != null) {
            pagerAdapter.setLearningMode(learning);
        }
    }

    private void setupSourceSpinner() {
        repository.getExecutor().execute(() -> {
            List<String> sources = new ArrayList<>();
            if ("translation".equals(displayMode)) {
                sources.add("ur.jalandhry (Built-in)");
                List<String> editions = repository.getAvailableTranslations();
                if (editions != null) sources.addAll(editions);
            } else if ("tafseer".equals(displayMode)) {
                List<String> editions = repository.getAvailableTafseers();
                if (editions != null) sources.addAll(editions);
                if (sources.isEmpty()) sources.add("No tafseers downloaded");
            } else {
                List<String> langs = repository.getAvailableWbwLanguages();
                if (langs != null) {
                    for (String l : langs) sources.add("WBW - " + l);
                }
                if (sources.isEmpty()) sources.add("No WBW data downloaded");
            }

            if (getActivity() != null) {
                List<String> finalSources = sources;
                requireActivity().runOnUiThread(() -> {
                    spinnerSource.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item, finalSources));
                });
            }
        });
    }

    private void updateHeader() {
        if (surahs.isEmpty() || currentSurah < 1 || currentSurah > surahs.size()) return;

        AyahDao.SurahInfo surah = surahs.get(currentSurah - 1);
        tvSurahName.setText(currentSurah + ". " + surah.surahNameEn + " (" + surah.surahNameAr + ")");

        int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[currentSurah - 1];
        tvAyahCounter.setText(currentAyah + " / " + maxAyah);

        // Determine juz
        repository.getExecutor().execute(() -> {
            Ayah ayah = repository.getAyah(currentSurah, currentAyah);
            if (ayah != null && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    tvJuzBadge.setText("Juz " + ayah.juzNumber);
                });

                // Update bookmark icon
                boolean bookmarked = repository.isBookmarked(currentSurah, currentAyah);
                requireActivity().runOnUiThread(() -> {
                    btnBookmark.setImageResource(bookmarked ?
                        R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
                });
            }
        });
    }

    private void loadSurahs() {
        repository.getExecutor().execute(() -> {
            surahs = repository.getAllSurahs();
            if (getActivity() != null) {
                requireActivity().runOnUiThread(this::updateHeader);
            }
        });
    }

    private void openSurahSelector() {
        SurahSelectorBottomSheet sheet = new SurahSelectorBottomSheet();
        sheet.setSurahs(surahs);
        sheet.setOnSurahSelectedListener(surahNumber -> {
            navigateToAyah(surahNumber, 1);
        });
        sheet.show(getChildFragmentManager(), "surah_selector");
    }

    private void openAyahPicker() {
        int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[currentSurah - 1];

        NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(1);
        picker.setMaxValue(maxAyah);
        picker.setValue(currentAyah);
        picker.setWrapSelectorWheel(false);

        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.go_to_ayah))
            .setView(picker)
            .setPositiveButton(android.R.string.ok, (d, w) -> {
                navigateToAyah(currentSurah, picker.getValue());
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    public void navigateToAyah(int surah, int ayah) {
        currentSurah = surah;
        currentAyah = ayah;
        int pos = AyahPagerAdapter.surahAyahToPosition(surah, ayah);
        if (recyclerView != null) {
            recyclerView.scrollToPosition(pos);
        }
        updateHeader();
    }

    private void navigateNext() {
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        if (firstVisible < QuranDataParser.TOTAL_AYAHS - 1) {
            recyclerView.smoothScrollToPosition(firstVisible + 1);
        }
    }

    private void navigatePrev() {
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        if (firstVisible > 0) {
            recyclerView.smoothScrollToPosition(firstVisible - 1);
        }
    }

    private void loadRandom() {
        repository.getExecutor().execute(() -> {
            Ayah ayah = repository.getRandomAyah();
            if (ayah != null && getActivity() != null) {
                requireActivity().runOnUiThread(() ->
                    navigateToAyah(ayah.surahNumber, ayah.ayahNumber));
            }
        });
    }

    private void toggleBookmark() {
        repository.getExecutor().execute(() -> {
            boolean isBookmarked = repository.isBookmarked(currentSurah, currentAyah);
            if (isBookmarked) {
                repository.removeBookmark(currentSurah, currentAyah);
            } else {
                String surahName = "";
                if (!surahs.isEmpty() && currentSurah <= surahs.size()) {
                    surahName = surahs.get(currentSurah - 1).surahNameEn;
                }
                repository.addBookmark(new Bookmark(currentSurah, currentAyah, surahName, ""));
            }
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    btnBookmark.setImageResource(!isBookmarked ?
                        R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
                    Toast.makeText(requireContext(),
                        isBookmarked ? R.string.bookmark_removed : R.string.bookmark_added,
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void shareAyah() {
        repository.getExecutor().execute(() -> {
            Ayah ayah = repository.getAyah(currentSurah, currentAyah);
            if (ayah == null || getActivity() == null) return;

            String text = ayah.arabicText + "\n\n" + ayah.defaultTranslation +
                "\n\n[" + ayah.surahNameEn + " " + currentSurah + ":" + currentAyah + "]";

            requireActivity().runOnUiThread(() -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_ayah)));
            });
        });
    }

    private void togglePlay() {
        if (isPlaying) {
            audioPlayer.stop();
            btnPlay.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        } else {
            String url = String.format("https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3",
                currentSurah, currentAyah);
            audioPlayer.playUrl(url, repository.getRepeatMode());
            btnPlay.setImageResource(R.drawable.ic_pause);
            isPlaying = true;
        }
    }

    private void toggleRepeat() {
        boolean repeat = !repository.getRepeatMode();
        repository.setRepeatMode(repeat);
        btnRepeat.setAlpha(repeat ? 1.0f : 0.5f);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save scroll position
        if (layoutManager != null) {
            repository.saveCurrentPosition(currentSurah, currentAyah);
        }
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());

        View container = getView().findViewById(R.id.reading_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        View headerBar = getView().findViewById(R.id.header_bar);
        headerBar.setBackgroundColor(theme.getSurfaceColor());

        tvSurahName.setTextColor(theme.getAccentColor());
        tvAyahCounter.setTextColor(theme.getPrimaryTextColor());
        tvJuzBadge.setTextColor(theme.getSecondaryTextColor());

        // Update mode pill colors
        btnModeReading.setTextColor(isLearningMode ?
            theme.getSecondaryTextColor() : theme.getPrimaryTextColor());
        btnModeLearning.setTextColor(isLearningMode ?
            theme.getPrimaryTextColor() : theme.getSecondaryTextColor());

        // Toolbar icon tints
        int iconColor = theme.getPrimaryTextColor();
        btnPrevious.setColorFilter(iconColor);
        btnPlay.setColorFilter(iconColor);
        btnNext.setColorFilter(iconColor);
        btnRepeat.setColorFilter(iconColor);
        btnShare.setColorFilter(iconColor);
        btnRandom.setColorFilter(iconColor);
        btnBookmark.setColorFilter(theme.getAccentColor());

        // Refresh adapter
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }
    }
}
