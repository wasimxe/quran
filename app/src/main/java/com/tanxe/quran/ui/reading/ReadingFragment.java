package com.tanxe.quran.ui.reading;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tanxe.quran.MainActivity;
import com.tanxe.quran.R;
import com.tanxe.quran.audio.AudioPlayerManager;
import com.tanxe.quran.data.dao.AyahDao;
import com.tanxe.quran.data.entity.Ayah;
import com.tanxe.quran.data.entity.Bookmark;
import com.tanxe.quran.data.entity.ReciterInfo;
import com.tanxe.quran.data.repository.QuranRepository;
import com.tanxe.quran.theme.ThemeManager;
import com.tanxe.quran.ui.adapter.AyahPagerAdapter;
import com.tanxe.quran.ui.adapter.HafizPageAdapter;
import com.tanxe.quran.ui.adapter.MushafAdapter;
import com.tanxe.quran.ui.learn.LearnFragment;
import com.tanxe.quran.ui.share.ShareCardGenerator;
import com.tanxe.quran.util.Localization;
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
    private MushafAdapter mushafAdapter;

    // Hafiz mode
    private ViewPager2 hafizPager;
    private HafizPageAdapter hafizAdapter;

    // Pinch-to-zoom
    private ScaleGestureDetector scaleGestureDetector;
    private static final float MIN_ARABIC_SP = 16f;
    private static final float MAX_ARABIC_SP = 60f;
    private float currentArabicSize;
    private float currentTransSize;

    // Header views
    private TextView tvSurahName, tvAyahCounter, tvJuzBadge;
    private ImageButton btnBookmark;

    // Display mode buttons (instant switching)
    private TextView btnModeArabic, btnModeTranslation, btnModeTafseer, btnModeWbw, btnModeHafiz;

    // Source spinner
    private Spinner spinnerSource;

    // Toolbar
    private ImageButton btnPrevious, btnPlay, btnNext, btnRepeat, btnRandom;
    private TextView btnSpeed, tvReciterName;
    private View floatingToolbar;

    private Typeface arabicFont, urduFont;
    private List<AyahDao.SurahInfo> surahs = new ArrayList<>();
    private int currentSurah = 1;
    private int currentAyah = 1;
    private String displayMode = "translation"; // arabic, translation, tafseer, wbw, hafiz
    private boolean isPlaying = false;
    private boolean continuousPlay = false;
    private int playingSurah = -1;
    private int playingAyah = -1;

    // Reading progress tracking
    private long sessionStartTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Fullscreen state
    private boolean isFullscreen = false;
    private ImageButton btnFullscreen, btnExitFullscreen;
    private OnBackPressedCallback fullscreenBackCallback;

    // Playback speeds
    private final float[] SPEEDS = {0.75f, 1.0f, 1.25f, 1.5f, 2.0f};
    private int currentSpeedIndex = 1;

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
            arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/indopak.ttf");
        } catch (Exception e) {
            arabicFont = Typeface.DEFAULT;
        }
        try {
            urduFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/gulzar.ttf");
        } catch (Exception e) {
            urduFont = Typeface.DEFAULT;
        }

        currentArabicSize = repository.getArabicFontSize();
        currentTransSize = repository.getTranslationFontSize();
        continuousPlay = repository.getContinuousPlay();

        // Set reciter from preferences
        String reciterFolder = repository.getSelectedReciter();
        audioPlayer.setReciter(reciterFolder);
        audioPlayer.setPlaybackSpeed(repository.getPlaybackSpeed());

        // Restore speed index
        float savedSpeed = repository.getPlaybackSpeed();
        for (int i = 0; i < SPEEDS.length; i++) {
            if (Math.abs(SPEEDS[i] - savedSpeed) < 0.01f) {
                currentSpeedIndex = i;
                break;
            }
        }

        initViews(view);
        applyTheme();
        loadSurahs();

        // Restore position and display mode
        int[] pos = repository.getCurrentPosition();
        currentSurah = pos[0];
        currentAyah = pos[1];
        displayMode = repository.getDisplayMode();
        if (displayMode == null || displayMode.isEmpty()) displayMode = "translation";

        setupRecyclerView();
        setupPinchToZoom();
        setupGestures(view);
        setupAudioCallback();
        updateModeButtons();
        updateSourceSpinner();

        // Back press handler for fullscreen exit
        fullscreenBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                exitFullscreen();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), fullscreenBackCallback);
    }

    private void initViews(View view) {
        // Header
        tvSurahName = view.findViewById(R.id.tv_surah_name);
        tvAyahCounter = view.findViewById(R.id.tv_ayah_counter);
        tvJuzBadge = view.findViewById(R.id.tv_juz_badge);
        btnBookmark = view.findViewById(R.id.btn_bookmark);

        // Display mode buttons
        btnModeArabic = view.findViewById(R.id.btn_mode_arabic);
        btnModeTranslation = view.findViewById(R.id.btn_mode_translation);
        btnModeTafseer = view.findViewById(R.id.btn_mode_tafseer);
        btnModeWbw = view.findViewById(R.id.btn_mode_wbw);
        btnModeHafiz = view.findViewById(R.id.btn_mode_hafiz);

        // Source spinner
        spinnerSource = view.findViewById(R.id.spinner_source);

        // RecyclerView
        recyclerView = view.findViewById(R.id.rv_ayahs);

        // Hafiz ViewPager2
        hafizPager = view.findViewById(R.id.vp_hafiz);
        hafizPager.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // Toolbar
        floatingToolbar = view.findViewById(R.id.floating_toolbar);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPlay = view.findViewById(R.id.btn_play);
        btnNext = view.findViewById(R.id.btn_next);
        btnRepeat = view.findViewById(R.id.btn_repeat);
        btnRandom = view.findViewById(R.id.btn_random);
        btnSpeed = view.findViewById(R.id.btn_speed);
        tvReciterName = view.findViewById(R.id.tv_reciter_name);

        // Set speed text
        btnSpeed.setText(SPEEDS[currentSpeedIndex] + "x");

        // Set reciter name
        updateReciterName();

        // Tap surah name → surah selector
        tvSurahName.setOnClickListener(v -> openSurahSelector());
        // Tap ayah counter → ayah picker
        tvAyahCounter.setOnClickListener(v -> openAyahPicker());
        // Tap juz badge → juz selector
        tvJuzBadge.setOnClickListener(v -> openJuzSelector());

        btnBookmark.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToBookmarks();
            }
        });

        // Instant mode switching — ensure clickable
        btnModeArabic.setClickable(true);
        btnModeTranslation.setClickable(true);
        btnModeTafseer.setClickable(true);
        btnModeWbw.setClickable(true);
        btnModeHafiz.setClickable(true);
        btnModeArabic.setOnClickListener(v -> { android.util.Log.d("ReadingFragment", "MODE CLICK: arabic"); switchDisplayMode("arabic"); });
        btnModeTranslation.setOnClickListener(v -> { android.util.Log.d("ReadingFragment", "MODE CLICK: translation"); switchDisplayMode("translation"); });
        btnModeTafseer.setOnClickListener(v -> { android.util.Log.d("ReadingFragment", "MODE CLICK: tafseer"); switchDisplayMode("tafseer"); });
        btnModeWbw.setOnClickListener(v -> { android.util.Log.d("ReadingFragment", "MODE CLICK: wbw"); switchDisplayMode("wbw"); });
        btnModeHafiz.setOnClickListener(v -> { android.util.Log.d("ReadingFragment", "MODE CLICK: hafiz"); switchDisplayMode("hafiz"); });

        // Toolbar buttons
        btnPrevious.setOnClickListener(v -> navigatePrev());
        btnNext.setOnClickListener(v -> navigateNext());
        btnPlay.setOnClickListener(v -> togglePlay());
        btnPlay.setOnLongClickListener(v -> { showReciterSelector(); return true; });
        btnRepeat.setOnClickListener(v -> toggleRepeat());
        btnRandom.setOnClickListener(v -> loadRandom());

        // Speed button
        btnSpeed.setOnClickListener(v -> cycleSpeed());

        // Reciter selector via long-press on play button (reciter name hidden)

        btnRepeat.setAlpha(repository.getRepeatMode() ? 1.0f : 0.5f);

        // Reading Point button (in toolbar)
        ImageButton btnReadingPoint = view.findViewById(R.id.fab_reading_point);
        btnReadingPoint.setOnClickListener(v -> {
            int[] pos = repository.getCurrentPosition();
            int rpSurah = pos[0];
            int rpAyah = pos[1];
            navigateToAyah(rpSurah, rpAyah);
            // Highlight after scroll settles (not applicable in hafiz mode)
            if (!"hafiz".equals(displayMode)) {
                recyclerView.post(() -> {
                    if ("arabic".equals(displayMode)) {
                        if (mushafAdapter != null) {
                            mushafAdapter.setHighlightedAyah(rpSurah, rpAyah);
                        }
                    } else {
                        int flatPos = AyahPagerAdapter.surahAyahToPosition(rpSurah, rpAyah);
                        pagerAdapter.setPlayingPosition(flatPos);
                    }
                });
            }
        });

        // Learn Mode button (in toolbar)
        ImageButton btnLearn = view.findViewById(R.id.fab_learn);
        btnLearn.setOnClickListener(v -> {
            LearnFragment learnDialog = new LearnFragment();
            learnDialog.show(getChildFragmentManager(), "learn_mode");
        });

        // Fullscreen button
        btnFullscreen = view.findViewById(R.id.btn_fullscreen);
        btnFullscreen.setOnClickListener(v -> {
            if (isFullscreen) exitFullscreen();
            else enterFullscreen();
        });

        // Floating exit fullscreen button (visible only in fullscreen mode)
        btnExitFullscreen = view.findViewById(R.id.btn_exit_fullscreen);
        btnExitFullscreen.setOnClickListener(v -> exitFullscreen());
    }

    private void switchDisplayMode(String mode) {
        android.util.Log.d("ReadingFragment", "switchDisplayMode: " + displayMode + " → " + mode);
        String oldMode = displayMode;
        displayMode = mode;
        repository.saveDisplayMode(mode);
        updateModeButtons();
        updateSourceSpinner();

        // Refresh cached preferences in adapter (edition selection may have changed)
        if (pagerAdapter != null) pagerAdapter.refreshCachedPrefs();

        boolean wasHafiz = "hafiz".equals(oldMode);
        boolean isHafiz = "hafiz".equals(mode);

        if (isHafiz) {
            // Switch to Hafiz mode — show ViewPager2, hide RecyclerView & audio bar
            recyclerView.setVisibility(View.GONE);
            hafizPager.setVisibility(View.VISIBLE);
            floatingToolbar.setVisibility(View.GONE);
            if (hafizAdapter == null) {
                hafizAdapter = new HafizPageAdapter(repository, theme, arabicFont);
            }
            hafizPager.setAdapter(hafizAdapter);
            int pageIndex = hafizAdapter.getPageIndex(currentSurah, currentAyah);
            hafizPager.setCurrentItem(pageIndex, false);
            setupHafizPageChangeListener();
        } else if (wasHafiz) {
            // Switching away from hafiz — show RecyclerView, hide ViewPager2, restore audio bar
            hafizPager.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            floatingToolbar.setVisibility(View.VISIBLE);

            if ("arabic".equals(mode)) {
                if (mushafAdapter == null) {
                    mushafAdapter = new MushafAdapter(repository, theme, arabicFont);
                    setupMushafListener();
                }
                recyclerView.setAdapter(mushafAdapter);
                recyclerView.scrollToPosition(MushafAdapter.surahToPosition(currentSurah));
            } else {
                recyclerView.setAdapter(pagerAdapter);
                pagerAdapter.setDisplayMode(mode);
                int pos = AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah);
                recyclerView.scrollToPosition(pos);
            }
        } else if ("arabic".equals(mode) && !"arabic".equals(oldMode)) {
            // Switch to mushaf adapter (surah-level continuous text)
            if (mushafAdapter == null) {
                mushafAdapter = new MushafAdapter(repository, theme, arabicFont);
                setupMushafListener();
            }
            recyclerView.setAdapter(mushafAdapter);
            recyclerView.scrollToPosition(MushafAdapter.surahToPosition(currentSurah));
            if (isPlaying && playingSurah > 0) {
                mushafAdapter.setHighlightedAyah(playingSurah, playingAyah);
            }
        } else if (!"arabic".equals(mode) && "arabic".equals(oldMode)) {
            // Switch back to ayah adapter
            recyclerView.setAdapter(pagerAdapter);
            pagerAdapter.setDisplayMode(mode);
            int pos = AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah);
            recyclerView.scrollToPosition(pos);
            if (isPlaying && playingSurah > 0) {
                int playPos = AyahPagerAdapter.surahAyahToPosition(playingSurah, playingAyah);
                pagerAdapter.setPlayingPosition(playPos);
            }
        } else if (pagerAdapter != null) {
            pagerAdapter.setDisplayMode(mode);
        }
    }

    private void setupHafizPageChangeListener() {
        hafizPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int[] start = hafizAdapter.getPageStart(position);
                currentSurah = start[0];
                currentAyah = start[1];
                updateHeader();
            }
        });
    }

    private void updateModeButtons() {
        // Set localized labels
        String lang = repository.getLanguage();
        btnModeArabic.setText(Localization.get(lang, Localization.MODE_ARABIC));
        btnModeTranslation.setText(Localization.get(lang, Localization.MODE_TRANSLATION));
        btnModeTafseer.setText(Localization.get(lang, Localization.MODE_TAFSEER));
        btnModeWbw.setText(Localization.get(lang, Localization.MODE_WBW));
        btnModeHafiz.setText(Localization.get(lang, Localization.MODE_HAFIZ));

        // Reset all buttons to inactive style
        TextView[] allBtns = {btnModeArabic, btnModeTranslation, btnModeTafseer, btnModeWbw, btnModeHafiz};
        for (TextView btn : allBtns) {
            btn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btn.setTextColor(theme.getSecondaryTextColor());
        }

        // Highlight active button with themed pill
        TextView activeBtn;
        switch (displayMode) {
            case "arabic": activeBtn = btnModeArabic; break;
            case "tafseer": activeBtn = btnModeTafseer; break;
            case "wbw": activeBtn = btnModeWbw; break;
            case "hafiz": activeBtn = btnModeHafiz; break;
            default: activeBtn = btnModeTranslation; break;
        }
        GradientDrawable activeBg = new GradientDrawable();
        activeBg.setColor(theme.getModePillActiveColor());
        activeBg.setCornerRadius(60);
        activeBtn.setBackground(activeBg);
        activeBtn.setTextColor(theme.isDarkTheme() ? 0xFFFFFFFF : 0xFF000000);
    }

    private void updateSourceSpinner() {
        // Show spinner for tafseer and translation (to pick edition), hide for arabic/wbw/hafiz
        if ("tafseer".equals(displayMode) || "translation".equals(displayMode)) {
            spinnerSource.setVisibility(View.VISIBLE);
            setupSourceSpinner();
        } else if ("wbw".equals(displayMode)) {
            spinnerSource.setVisibility(View.VISIBLE);
            setupSourceSpinner();
        } else {
            spinnerSource.setVisibility(View.GONE);
        }
    }

    // Stores the raw edition identifiers parallel to the spinner display names
    private List<String> sourceIdentifiers = new ArrayList<>();
    private boolean spinnerInitializing = false;

    private void setupSourceSpinner() {
        repository.getExecutor().execute(() -> {
            List<String> displayNames = new ArrayList<>();
            List<String> identifiers = new ArrayList<>();
            int selectedIndex = 0;

            if ("translation".equals(displayMode)) {
                String currentEdition = repository.getSelectedTranslation();
                displayNames.add("ur.jalandhry (" + Localization.get(repository.getLanguage(), Localization.BUILT_IN) + ")");
                identifiers.add("ur.jalandhry");
                List<String> editions = repository.getAvailableTranslations();
                if (editions != null) {
                    for (String e : editions) {
                        displayNames.add(e);
                        identifiers.add(e);
                    }
                }
                // Find selected index
                for (int i = 0; i < identifiers.size(); i++) {
                    if (identifiers.get(i).equals(currentEdition)) { selectedIndex = i; break; }
                }
            } else if ("tafseer".equals(displayMode)) {
                String currentEdition = repository.getSelectedTafseer();
                List<String> editions = repository.getAvailableTafseers();
                if (editions != null && !editions.isEmpty()) {
                    for (String e : editions) {
                        displayNames.add(e);
                        identifiers.add(e);
                    }
                    for (int i = 0; i < identifiers.size(); i++) {
                        if (identifiers.get(i).equals(currentEdition)) { selectedIndex = i; break; }
                    }
                } else {
                    displayNames.add(Localization.get(repository.getLanguage(), Localization.NO_TAFSEER));
                    identifiers.add("");
                }
            } else if ("wbw".equals(displayMode)) {
                String currentLang = repository.getSelectedWbwLanguage();
                List<String> langs = repository.getAvailableWbwLanguages();
                if (langs != null && !langs.isEmpty()) {
                    for (String l : langs) {
                        displayNames.add(Localization.get(repository.getLanguage(), Localization.MODE_WBW) + " - " + l);
                        identifiers.add(l);
                    }
                    for (int i = 0; i < identifiers.size(); i++) {
                        if (identifiers.get(i).equals(currentLang)) { selectedIndex = i; break; }
                    }
                } else {
                    displayNames.add(Localization.get(repository.getLanguage(), Localization.NO_WBW));
                    identifiers.add("");
                }
            }

            if (getActivity() != null) {
                List<String> finalNames = displayNames;
                List<String> finalIds = identifiers;
                int finalSelected = selectedIndex;
                requireActivity().runOnUiThread(() -> {
                    sourceIdentifiers = finalIds;
                    spinnerInitializing = true;
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, finalNames) {
                        @Override
                        public View getView(int pos, View convertView, android.view.ViewGroup parent) {
                            View view = super.getView(pos, convertView, parent);
                            if (view instanceof TextView) {
                                ((TextView) view).setTextColor(theme.getPrimaryTextColor());
                            }
                            return view;
                        }
                        @Override
                        public View getDropDownView(int pos, View convertView, android.view.ViewGroup parent) {
                            View view = super.getDropDownView(pos, convertView, parent);
                            if (view instanceof TextView) {
                                ((TextView) view).setTextColor(theme.getPrimaryTextColor());
                                view.setBackgroundColor(theme.getSurfaceColor());
                            }
                            return view;
                        }
                    };
                    spinnerSource.setAdapter(spinnerAdapter);
                    spinnerSource.setSelection(finalSelected);
                    spinnerSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                            if (spinnerInitializing) { spinnerInitializing = false; return; }
                            if (position >= sourceIdentifiers.size()) return;
                            String selectedId = sourceIdentifiers.get(position);
                            if (selectedId.isEmpty()) return;

                            switch (displayMode) {
                                case "translation":
                                    repository.saveSelectedTranslation(selectedId);
                                    break;
                                case "tafseer":
                                    repository.saveSelectedTafseer(selectedId);
                                    break;
                                case "wbw":
                                    repository.saveSelectedWbwLanguage(selectedId);
                                    break;
                            }
                            // Refresh cached prefs and content
                            if (pagerAdapter != null) {
                                pagerAdapter.refreshCachedPrefs();
                                pagerAdapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                });
            }
        });
    }

    private void setupAudioCallback() {
        audioPlayer.setCallback(new AudioPlayerManager.PlaybackCallback() {
            @Override
            public void onPlaybackEnded() {
                handler.post(() -> {
                    if (continuousPlay || repository.getContinuousPlay()) {
                        // Use playing position, not scroll position
                        int pos = AyahPagerAdapter.surahAyahToPosition(playingSurah, playingAyah);
                        if (pos < QuranDataParser.TOTAL_AYAHS - 1) {
                            int nextPos = pos + 1;
                            int[] sa = AyahPagerAdapter.positionToSurahAyah(nextPos);
                            playingSurah = sa[0];
                            playingAyah = sa[1];
                            currentSurah = playingSurah;
                            currentAyah = playingAyah;
                            audioPlayer.playAyah(playingSurah, playingAyah, repository.getRepeatMode());
                            highlightPlayingAyah();
                            scrollToPlayingAyah();
                            updateHeader();
                        } else {
                            stopPlayback();
                        }
                    } else {
                        stopPlayback();
                    }
                });
            }

            @Override
            public void onError(String message) {
                handler.post(() -> stopPlayback());
            }
        });
    }

    private void stopPlayback() {
        isPlaying = false;
        playingSurah = -1;
        playingAyah = -1;
        btnPlay.setImageResource(R.drawable.ic_play);
        pagerAdapter.setPlayingPosition(-1);
        if (mushafAdapter != null) {
            mushafAdapter.setHighlightedAyah(-1, -1);
        }
    }

    /** Highlight the currently playing ayah in whichever adapter is active */
    private void highlightPlayingAyah() {
        if ("arabic".equals(displayMode) && mushafAdapter != null) {
            mushafAdapter.setHighlightedAyah(playingSurah, playingAyah);
        } else {
            int pos = AyahPagerAdapter.surahAyahToPosition(playingSurah, playingAyah);
            pagerAdapter.setPlayingPosition(pos);
        }
    }

    /** Scroll to keep the playing ayah visible in whichever adapter is active */
    private void scrollToPlayingAyah() {
        if ("hafiz".equals(displayMode)) {
            int pageIndex = hafizAdapter.getPageIndex(playingSurah, playingAyah);
            hafizPager.setCurrentItem(pageIndex, true);
        } else if ("arabic".equals(displayMode)) {
            // In mushaf mode, scroll to the surah containing the playing ayah
            int surahPos = MushafAdapter.surahToPosition(playingSurah);
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            // Only scroll if the surah is not already visible
            if (surahPos != firstVisible) {
                layoutManager.scrollToPositionWithOffset(surahPos, 0);
            }
        } else {
            int pos = AyahPagerAdapter.surahAyahToPosition(playingSurah, playingAyah);
            layoutManager.scrollToPositionWithOffset(pos, 0);
        }
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);

        pagerAdapter = new AyahPagerAdapter(repository, theme, arabicFont, urduFont);
        pagerAdapter.setDisplayMode(displayMode);
        pagerAdapter.setLongPressListener((surah, ayah, position) -> showAyahActionsSheet(surah, ayah, position));
        // Prime the preload cache for the initial position
        pagerAdapter.preloadAround(AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah));

        if ("hafiz".equals(displayMode)) {
            // Start with hafiz mode
            recyclerView.setVisibility(View.GONE);
            hafizPager.setVisibility(View.VISIBLE);
            floatingToolbar.setVisibility(View.GONE);
            hafizAdapter = new HafizPageAdapter(repository, theme, arabicFont);
            hafizPager.setAdapter(hafizAdapter);
            int pageIndex = hafizAdapter.getPageIndex(currentSurah, currentAyah);
            hafizPager.setCurrentItem(pageIndex, false);
            setupHafizPageChangeListener();
        } else if ("arabic".equals(displayMode)) {
            // Start with mushaf adapter for arabic mode
            mushafAdapter = new MushafAdapter(repository, theme, arabicFont);
            setupMushafListener();
            recyclerView.setAdapter(mushafAdapter);
            recyclerView.scrollToPosition(MushafAdapter.surahToPosition(currentSurah));
        } else {
            recyclerView.setAdapter(pagerAdapter);
            int startPos = AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah);
            recyclerView.scrollToPosition(startPos);
        }

        // Track scroll — update position immediately, defer header UI until idle
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible != RecyclerView.NO_POSITION) {
                    if ("arabic".equals(displayMode)) {
                        int surah = firstVisible + 1;
                        if (surah != currentSurah) {
                            currentSurah = surah;
                            currentAyah = 1;
                            updateHeaderLightweight();
                        }
                    } else {
                        int[] sa = AyahPagerAdapter.positionToSurahAyah(firstVisible);
                        if (sa[0] != currentSurah || sa[1] != currentAyah) {
                            currentSurah = sa[0];
                            currentAyah = sa[1];
                            updateHeaderLightweight();
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateHeader();
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
                        if ("hafiz".equals(displayMode)) return false;
                        float factor = detector.getScaleFactor();
                        float newArabic = Math.max(MIN_ARABIC_SP, Math.min(MAX_ARABIC_SP, currentArabicSize * factor));
                        float ratio = currentTransSize / currentArabicSize;
                        float newTrans = newArabic * ratio;

                        currentArabicSize = newArabic;
                        currentTransSize = newTrans;
                        pagerAdapter.updateFontSize(currentArabicSize, currentTransSize);
                        if (mushafAdapter != null) mushafAdapter.updateFontSize(currentArabicSize);
                        if (hafizAdapter != null) hafizAdapter.updateFontSize(currentArabicSize);
                        return true;
                    }

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        repository.setArabicFontSize(currentArabicSize);
                        repository.setTranslationFontSize(currentTransSize);
                    }
                });

        recyclerView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return scaleGestureDetector.isInProgress();
        });
    }

    private void setupGestures(View view) {
        // Double-tap on surah name to cycle modes (not on the whole header to avoid touch conflicts)
        GestureDetector gestureDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        switch (displayMode) {
                            case "translation": switchDisplayMode("tafseer"); break;
                            case "tafseer": switchDisplayMode("wbw"); break;
                            case "wbw": switchDisplayMode("hafiz"); break;
                            case "hafiz": switchDisplayMode("arabic"); break;
                            default: switchDisplayMode("translation"); break;
                        }
                        return true;
                    }
                });

        // Only attach double-tap to surah name row, NOT the whole header (avoids blocking mode buttons)
        tvSurahName.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    /** Lightweight header update — only set text labels, no DB queries. Safe during scroll. */
    private void updateHeaderLightweight() {
        if (surahs.isEmpty() || currentSurah < 1 || currentSurah > surahs.size()) return;
        AyahDao.SurahInfo surah = surahs.get(currentSurah - 1);
        tvSurahName.setText(currentSurah + ". " + surah.surahNameEn + " (" + surah.surahNameAr + ")");
        int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[currentSurah - 1];
        tvAyahCounter.setText(currentAyah + " / " + maxAyah);
    }

    /** Full header update — includes async juz badge query. Call when scroll stops. */
    private void updateHeader() {
        if (surahs.isEmpty() || currentSurah < 1 || currentSurah > surahs.size()) return;

        AyahDao.SurahInfo surah = surahs.get(currentSurah - 1);
        tvSurahName.setText(currentSurah + ". " + surah.surahNameEn + " (" + surah.surahNameAr + ")");

        int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[currentSurah - 1];
        String ayahLabel = Localization.get(repository.getLanguage(), Localization.AYAH);
        tvAyahCounter.setText(ayahLabel + " " + currentAyah + " / " + maxAyah);

        final int querySurah = currentSurah;
        final int queryAyah = currentAyah;
        repository.getExecutor().execute(() -> {
            Ayah ayah = repository.getAyah(querySurah, queryAyah);
            if (ayah != null && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    // Only update if position hasn't changed
                    if (currentSurah == querySurah && currentAyah == queryAyah) {
                        String juzLabel = Localization.get(repository.getLanguage(), Localization.JUZ);
                        tvJuzBadge.setText(juzLabel + " " + ayah.juzNumber);
                    }
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
        sheet.setOnSurahSelectedListener(surahNumber -> navigateToAyah(surahNumber, 1));
        sheet.show(getChildFragmentManager(), "surah_selector");
    }

    private void openJuzSelector() {
        JuzSelectorBottomSheet sheet = new JuzSelectorBottomSheet();
        sheet.setOnJuzSelectedListener((surah, ayah) -> navigateToAyah(surah, ayah));
        sheet.show(getChildFragmentManager(), "juz_selector");
    }

    private void openAyahPicker() {
        int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[currentSurah - 1];

        NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(1);
        picker.setMaxValue(maxAyah);
        picker.setValue(currentAyah);
        picker.setWrapSelectorWheel(false);

        new AlertDialog.Builder(requireContext())
                .setTitle(Localization.get(repository.getLanguage(), Localization.GO_TO_AYAH))
                .setView(picker)
                .setPositiveButton(android.R.string.ok, (d, w) -> navigateToAyah(currentSurah, picker.getValue()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void navigateToAyah(int surah, int ayah) {
        currentSurah = surah;
        currentAyah = ayah;
        if ("hafiz".equals(displayMode)) {
            if (hafizPager != null) {
                int pageIndex = hafizAdapter.getPageIndex(surah, ayah);
                hafizPager.setCurrentItem(pageIndex, true);
            }
        } else if (recyclerView != null && layoutManager != null) {
            if ("arabic".equals(displayMode)) {
                layoutManager.scrollToPositionWithOffset(MushafAdapter.surahToPosition(surah), 0);
            } else {
                int pos = AyahPagerAdapter.surahAyahToPosition(surah, ayah);
                layoutManager.scrollToPositionWithOffset(pos, 0);
            }
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

    private void showBookmarkList() {
        repository.getExecutor().execute(() -> {
            List<Bookmark> bookmarks = repository.getAllBookmarks();
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() -> {
                if (bookmarks == null || bookmarks.isEmpty()) {
                    Toast.makeText(requireContext(), Localization.get(repository.getLanguage(), Localization.NO_BOOKMARKS), Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] items = new String[bookmarks.size()];
                for (int i = 0; i < bookmarks.size(); i++) {
                    Bookmark b = bookmarks.get(i);
                    String name = (b.surahName != null && !b.surahName.isEmpty()) ? b.surahName : Localization.get(repository.getLanguage(), Localization.SURAH) + " " + b.surahNumber;
                    items[i] = name + " — " + Localization.get(repository.getLanguage(), Localization.AYAH) + " " + b.ayahNumber;
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle(Localization.get(repository.getLanguage(), Localization.BOOKMARKS))
                        .setItems(items, (dialog, which) -> {
                            Bookmark b = bookmarks.get(which);
                            navigateToAyah(b.surahNumber, b.ayahNumber);
                            if (!"arabic".equals(displayMode)) {
                                int flatPos = AyahPagerAdapter.surahAyahToPosition(b.surahNumber, b.ayahNumber);
                                pagerAdapter.setPlayingPosition(flatPos);
                            }
                        })
                        .setNegativeButton(Localization.get(repository.getLanguage(), Localization.CLOSE), null)
                        .show();
            });
        });
    }

    private void shareAyah() {
        repository.getExecutor().execute(() -> {
            Ayah ayah = repository.getAyah(currentSurah, currentAyah);
            if (ayah == null || getActivity() == null) return;

            String translationText = "";
            String edition = repository.getSelectedTranslation();
            if ("ur.jalandhry".equals(edition)) {
                translationText = ayah.defaultTranslation;
            } else {
                com.tanxe.quran.data.entity.Translation trans = repository.getTranslation(
                        currentSurah, currentAyah, edition);
                translationText = trans != null ? trans.text : ayah.defaultTranslation;
            }

            String finalTransText = translationText;
            requireActivity().runOnUiThread(() -> {
                String lang = repository.getLanguage();
                String[] options = {Localization.get(lang, Localization.SHARE), Localization.get(lang, Localization.SHARE_IMAGE)};
                new AlertDialog.Builder(requireContext())
                        .setTitle(Localization.get(lang, Localization.SHARE))
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                String text = ayah.arabicText + "\n\n" +
                                        (finalTransText != null ? finalTransText : "") +
                                        "\n\n[" + ayah.surahNameEn + " " + currentSurah + ":" + currentAyah + "]";
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_ayah)));
                            } else {
                                ShareCardGenerator.shareAyahAsImage(requireContext(), ayah,
                                        finalTransText, arabicFont);
                            }
                        })
                        .show();
            });
        });
    }

    private void togglePlay() {
        if (isPlaying) {
            audioPlayer.stop();
            stopPlayback();
        } else {
            continuousPlay = true;
            repository.setContinuousPlay(true);

            playingSurah = currentSurah;
            playingAyah = currentAyah;
            audioPlayer.playAyah(playingSurah, playingAyah, repository.getRepeatMode());
            btnPlay.setImageResource(R.drawable.ic_pause);
            isPlaying = true;

            highlightPlayingAyah();
        }
    }

    private void toggleRepeat() {
        boolean repeat = !repository.getRepeatMode();
        repository.setRepeatMode(repeat);
        btnRepeat.setAlpha(repeat ? 1.0f : 0.5f);
        audioPlayer.setRepeatMode(repeat);
    }

    private void cycleSpeed() {
        currentSpeedIndex = (currentSpeedIndex + 1) % SPEEDS.length;
        float speed = SPEEDS[currentSpeedIndex];
        audioPlayer.setPlaybackSpeed(speed);
        repository.setPlaybackSpeed(speed);
        btnSpeed.setText(speed + "x");
    }

    private void showReciterSelector() {
        repository.getExecutor().execute(() -> {
            List<ReciterInfo> reciters = repository.getAllReciters();
            if (reciters == null || reciters.isEmpty() || getActivity() == null) return;

            String[] names = new String[reciters.size()];
            String[] folders = new String[reciters.size()];
            for (int i = 0; i < reciters.size(); i++) {
                names[i] = reciters.get(i).name;
                folders[i] = reciters.get(i).subfolder;
            }

            requireActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle(Localization.get(repository.getLanguage(), Localization.SELECT_RECITER))
                        .setItems(names, (dialog, which) -> {
                            repository.saveSelectedReciter(folders[which]);
                            audioPlayer.setReciter(folders[which]);
                            updateReciterName();
                            if (isPlaying) {
                                audioPlayer.playAyah(currentSurah, currentAyah, repository.getRepeatMode());
                            }
                        })
                        .show();
            });
        });
    }

    private void updateReciterName() {
        repository.getExecutor().execute(() -> {
            String name = repository.getSelectedReciterName();
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (tvReciterName != null) tvReciterName.setText(name);
                });
            }
        });
    }

    /** Fancy bottom sheet for ayah actions on long press */
    private void setupMushafListener() {
        if (mushafAdapter == null) return;
        mushafAdapter.setInteractionListener(new MushafAdapter.OnAyahInteractionListener() {
            @Override
            public void onAyahTapped(int surah, int ayah) {
                currentSurah = surah;
                currentAyah = ayah;
                updateHeader();
            }

            @Override
            public void onAyahLongPressed(int surah, int ayah) {
                showAyahActionsSheet(surah, ayah, MushafAdapter.surahToPosition(surah));
            }
        });
    }

    private void showAyahActionsSheet(int surah, int ayah, int position) {
        repository.getExecutor().execute(() -> {
            boolean isBookmarked = repository.isBookmarked(surah, ayah);
            Ayah ayahData = repository.getAyah(surah, ayah);
            String surahName = "";
            if (!surahs.isEmpty() && surah <= surahs.size()) {
                surahName = surahs.get(surah - 1).surahNameEn;
            }

            String finalSurahName = surahName;
            if (getActivity() == null) return;

            requireActivity().runOnUiThread(() -> {
                AyahActionsBottomSheet sheet = new AyahActionsBottomSheet();
                sheet.setData(surah, ayah, finalSurahName,
                        ayahData != null ? ayahData.arabicText : "",
                        isBookmarked, arabicFont);
                sheet.setActionListener(new AyahActionsBottomSheet.ActionListener() {
                    @Override
                    public void onBookmarkToggle() {
                        repository.getExecutor().execute(() -> {
                            if (isBookmarked) {
                                repository.removeBookmark(surah, ayah);
                            } else {
                                repository.addBookmark(new Bookmark(surah, ayah, finalSurahName, ""));
                            }
                            if (getActivity() != null) {
                                requireActivity().runOnUiThread(() -> {
                                    updateHeader();
                                    String bmMsg = Localization.get(repository.getLanguage(),
                                            isBookmarked ? Localization.BOOKMARK_REMOVED : Localization.BOOKMARK_ADDED);
                                    Toast.makeText(requireContext(), bmMsg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onSetReadingPoint() {
                        currentSurah = surah;
                        currentAyah = ayah;
                        repository.saveCurrentPosition(surah, ayah);
                        updateHeader();
                        String msg = Localization.get(repository.getLanguage(), Localization.READING_POINT_SET);
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPlay() {
                        currentSurah = surah;
                        currentAyah = ayah;
                        playingSurah = surah;
                        playingAyah = ayah;
                        audioPlayer.playAyah(surah, ayah, repository.getRepeatMode());
                        btnPlay.setImageResource(R.drawable.ic_pause);
                        isPlaying = true;
                        continuousPlay = true;
                        highlightPlayingAyah();
                    }

                    @Override
                    public void onShare() {
                        shareAyahText(surah, ayah);
                    }

                    @Override
                    public void onShareAsImage() {
                        repository.getExecutor().execute(() -> {
                            Ayah data = repository.getAyah(surah, ayah);
                            if (data != null && getActivity() != null) {
                                requireActivity().runOnUiThread(() ->
                                        ShareCardGenerator.shareAyahAsImage(requireContext(), data,
                                                data.defaultTranslation, arabicFont));
                            }
                        });
                    }

                    @Override
                    public void onCopyArabic() {
                        if (ayahData != null) {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                    requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Ayah", ayahData.arabicText));
                            Toast.makeText(requireContext(), Localization.get(repository.getLanguage(), Localization.COPIED), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSelectText() {
                        enableTextSelectionAt(position);
                    }

                    @Override
                    public void onCompareTranslations() {
                        TranslationCompareBottomSheet compareSheet = new TranslationCompareBottomSheet();
                        compareSheet.setData(surah, ayah, finalSurahName,
                                ayahData != null ? ayahData.arabicText : "",
                                ayahData != null ? ayahData.defaultTranslation : "",
                                arabicFont, urduFont);
                        compareSheet.show(getChildFragmentManager(), "compare_translations");
                    }
                });
                sheet.show(getChildFragmentManager(), "ayah_actions");
            });
        });
    }

    private void enableTextSelectionAt(int position) {
        if (recyclerView == null) return;
        // Delay to let bottom sheet dismiss before enabling selection
        recyclerView.postDelayed(() -> {
            if ("arabic".equals(displayMode)) {
                RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(
                        MushafAdapter.surahToPosition(currentSurah));
                if (vh != null) {
                    TextView tvMushaf = vh.itemView.findViewById(R.id.tv_mushaf_text);
                    if (tvMushaf != null) {
                        // Remove ClickableSpans that conflict with text selection
                        CharSequence text = tvMushaf.getText();
                        if (text instanceof android.text.Spannable) {
                            android.text.Spannable spannable = (android.text.Spannable) text;
                            android.text.style.ClickableSpan[] clicks = spannable.getSpans(
                                    0, spannable.length(), android.text.style.ClickableSpan.class);
                            for (android.text.style.ClickableSpan cs : clicks) {
                                spannable.removeSpan(cs);
                            }
                        }
                        // Remove long-click & movement method that block selection
                        tvMushaf.setOnLongClickListener(null);
                        tvMushaf.setMovementMethod(null);
                        tvMushaf.setTextIsSelectable(true);
                        tvMushaf.setHighlightColor(0x440088FF); // restore selection highlight
                        tvMushaf.requestFocus();
                        // Trigger native selection on next frame after view is ready
                        tvMushaf.post(() -> tvMushaf.performLongClick());
                    }
                }
            } else {
                RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
                if (vh != null) {
                    TextView tvArabic = vh.itemView.findViewById(R.id.tv_arabic);
                    TextView tvTranslation = vh.itemView.findViewById(R.id.tv_translation);
                    TextView tvExtra = vh.itemView.findViewById(R.id.tv_extra_content);
                    if (tvArabic != null) {
                        tvArabic.setTextIsSelectable(true);
                        tvArabic.requestFocus();
                        selectAllText(tvArabic);
                    }
                    if (tvTranslation != null) tvTranslation.setTextIsSelectable(true);
                    if (tvExtra != null && tvExtra.getVisibility() == View.VISIBLE) tvExtra.setTextIsSelectable(true);
                }
            }
        }, 350);
    }

    private void selectAllText(TextView tv) {
        if (tv.getText() instanceof android.text.Spannable) {
            android.text.Selection.selectAll((android.text.Spannable) tv.getText());
        }
    }

    private void shareAyahText(int surah, int ayah) {
        repository.getExecutor().execute(() -> {
            Ayah ayahData = repository.getAyah(surah, ayah);
            if (ayahData == null || getActivity() == null) return;

            String text = ayahData.arabicText + "\n\n" + ayahData.defaultTranslation +
                    "\n\n[" + ayahData.surahNameEn + " " + surah + ":" + ayah + "]";

            requireActivity().runOnUiThread(() -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_ayah)));
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        sessionStartTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFullscreen) exitFullscreen();

        if (sessionStartTime > 0) {
            int durationSeconds = (int) ((System.currentTimeMillis() - sessionStartTime) / 1000);
            if (durationSeconds > 5) {
                repository.getExecutor().execute(() ->
                        repository.recordReadingSession(currentSurah, currentAyah, durationSeconds, "read"));
            }
            sessionStartTime = 0;
        }
    }

    // ──────── Fullscreen mode ────────

    private void enterFullscreen() {
        if (isFullscreen || getActivity() == null) return;
        isFullscreen = true;

        // Hide UI chrome — keep current display mode as-is
        View headerBar = getView().findViewById(R.id.header_bar);
        headerBar.setVisibility(View.GONE);
        spinnerSource.setVisibility(View.GONE);
        floatingToolbar.setVisibility(View.GONE);

        // Hide bottom navigation
        if (getActivity() instanceof MainActivity) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }

        // Immersive fullscreen — hide status & navigation bars
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                requireActivity().getWindow(), requireActivity().getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Show floating exit button, hide toolbar fullscreen button
        btnExitFullscreen.setVisibility(View.VISIBLE);

        // Enable back press callback
        fullscreenBackCallback.setEnabled(true);
    }

    private void exitFullscreen() {
        if (!isFullscreen || getActivity() == null) return;
        isFullscreen = false;

        // Show UI chrome
        View headerBar = getView().findViewById(R.id.header_bar);
        headerBar.setVisibility(View.VISIBLE);
        floatingToolbar.setVisibility(View.VISIBLE);

        // Show bottom navigation
        if (getActivity() instanceof MainActivity) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }

        // Restore system bars
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                requireActivity().getWindow(), requireActivity().getWindow().getDecorView());
        insetsController.show(WindowInsetsCompat.Type.systemBars());

        // Restore source spinner if needed
        updateSourceSpinner();

        // Re-apply theme (restores status bar colors)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshTheme();
        }

        // Hide floating exit button
        btnExitFullscreen.setVisibility(View.GONE);

        // Disable back press callback
        fullscreenBackCallback.setEnabled(false);
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void applyTheme() {
        if (getView() == null) return;
        theme = ThemeManager.getInstance(requireContext());

        View container = getView().findViewById(R.id.reading_container);
        container.setBackgroundColor(theme.getBackgroundColor());

        // Header bar with rounded bottom corners
        View headerBar = getView().findViewById(R.id.header_bar);
        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColor(theme.getSurfaceColor());
        headerBg.setCornerRadii(new float[]{0, 0, 0, 0, 48, 48, 48, 48}); // bottom corners
        headerBar.setBackground(headerBg);

        tvSurahName.setTextColor(theme.getAccentColor());
        tvAyahCounter.setTextColor(theme.getPrimaryTextColor());

        // Juz badge with themed background
        GradientDrawable juzBg = new GradientDrawable();
        juzBg.setColor(theme.getBadgeColor());
        juzBg.setCornerRadius(36);
        tvJuzBadge.setBackground(juzBg);
        tvJuzBadge.setTextColor(theme.isDarkTheme() ? 0xFFFFFFFF : 0xFFFFFFFF);

        // Mode pill container
        View modePillContainer = getView().findViewById(R.id.mode_toggle_container);
        GradientDrawable modePillBg = new GradientDrawable();
        modePillBg.setColor(theme.getModePillColor());
        modePillBg.setCornerRadius(60);
        modePillContainer.setBackground(modePillBg);

        updateModeButtons();

        // Source spinner theming
        if (spinnerSource != null) {
            spinnerSource.setPopupBackgroundDrawable(new android.graphics.drawable.ColorDrawable(theme.getSurfaceColor()));
            updateSourceSpinner(); // rebuild adapter with current theme colors
        }

        // Floating toolbar with themed background
        GradientDrawable toolbarBg = new GradientDrawable();
        toolbarBg.setColor(theme.getToolbarColor());
        toolbarBg.setCornerRadius(84);
        floatingToolbar.setBackground(toolbarBg);

        // Toolbar dividers
        View v = getView();
        for (int i = 0; i < ((ViewGroup) floatingToolbar).getChildCount(); i++) {
            View child = ((ViewGroup) floatingToolbar).getChildAt(i);
            if (child instanceof ViewGroup) {
                ViewGroup row = (ViewGroup) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View item = row.getChildAt(j);
                    if (item.getId() == View.NO_ID && item.getLayoutParams().width == 1) {
                        // This is a divider
                        item.setBackgroundColor(theme.getDividerColor());
                    }
                }
            }
        }

        // Toolbar icon tints
        int iconColor = theme.getPrimaryTextColor();
        btnPrevious.setColorFilter(iconColor);
        btnPlay.setColorFilter(iconColor);
        btnNext.setColorFilter(iconColor);
        btnRepeat.setColorFilter(iconColor);
        btnRandom.setColorFilter(iconColor);
        btnBookmark.setColorFilter(theme.getAccentColor());

        // Reading point & Learn button tints
        ImageButton btnRP = getView().findViewById(R.id.fab_reading_point);
        ImageButton btnLrn = getView().findViewById(R.id.fab_learn);
        if (btnRP != null) btnRP.setColorFilter(theme.getAccentColor());
        if (btnLrn != null) btnLrn.setColorFilter(theme.getAccentColor());
        if (btnFullscreen != null) btnFullscreen.setColorFilter(theme.getAccentColor());

        if (btnSpeed != null) btnSpeed.setTextColor(theme.getAccentColor());
        if (tvReciterName != null) tvReciterName.setTextColor(theme.getSecondaryTextColor());

        // Reload arabic font
        try {
            arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/indopak.ttf");
        } catch (Exception e) {
            arabicFont = Typeface.DEFAULT;
        }

        // Refresh adapters (clear caches since theme colors changed)
        if (pagerAdapter != null) {
            pagerAdapter.setArabicFont(arabicFont);
            pagerAdapter.refreshCachedPrefs();
            pagerAdapter.notifyDataSetChanged();
        }
        if (mushafAdapter != null) {
            mushafAdapter.setArabicFont(arabicFont);
            mushafAdapter.notifyDataSetChanged();
        }
        if (hafizAdapter != null) {
            hafizAdapter.setArabicFont(arabicFont);
            hafizAdapter.notifyDataSetChanged();
        }
    }
}
