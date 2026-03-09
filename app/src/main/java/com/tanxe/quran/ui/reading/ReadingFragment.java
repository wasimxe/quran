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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.NumberPicker;
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
import com.tanxe.quran.ui.adapter.MushafAdapter;
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

    // Font size controls
    private static final float MIN_ARABIC_SP = 16f;
    private static final float MAX_ARABIC_SP = 60f;
    private static final float FONT_STEP = 2f;
    private float currentArabicSize;
    private float currentTransSize;
    private TextView btnFontDecrease, btnFontIncrease;

    // Header views
    private TextView tvSurahName, tvAyahCounter, tvJuzBadge;
    private ImageButton btnBookmark;

    // Display mode buttons (instant switching)
    private TextView btnModeArabic, btnModeWbw;

    // Toolbar
    private ImageButton btnPrevious, btnPlay, btnNext, btnRepeat, btnRandom;
    private TextView btnSpeed, tvReciterName;
    private View floatingToolbar;

    // Static font cache — fonts are expensive to load, share across instances
    private static Typeface cachedArabicFont;
    private static Typeface cachedUrduFont;
    private static String cachedArabicFontFile;

    private Typeface arabicFont, urduFont;
    private List<AyahDao.SurahInfo> surahs = new ArrayList<>();
    private int currentSurah = 1;
    private int currentAyah = 1;
    private String displayMode = "arabic"; // arabic, wbw
    private boolean isPlaying = false;
    private boolean continuousPlay = false;
    private int playingSurah = -1;
    private int playingAyah = -1;

    // Reading progress tracking
    private long sessionStartTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Scroll guard: suppress scroll listener updates during programmatic navigation
    private boolean suppressScrollUpdates = false;

    // Fullscreen state
    private boolean isFullscreen = false;
    private ImageButton btnFullscreen;
    private ImageButton btnExitFullscreen;
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

        // Use cached fonts to avoid expensive reloads
        String fontFile = repository.getSelectedArabicFont();
        if (cachedArabicFont != null && fontFile.equals(cachedArabicFontFile)) {
            arabicFont = cachedArabicFont;
        } else {
            try {
                arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/" + fontFile);
                cachedArabicFont = arabicFont;
                cachedArabicFontFile = fontFile;
            } catch (Exception e) {
                arabicFont = Typeface.DEFAULT;
            }
        }
        if (cachedUrduFont != null) {
            urduFont = cachedUrduFont;
        } else {
            try {
                urduFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/gulzar.ttf");
                cachedUrduFont = urduFont;
            } catch (Exception e) {
                urduFont = Typeface.DEFAULT;
            }
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
        // Migrate removed modes to arabic
        if (displayMode == null || displayMode.isEmpty() ||
                "translation".equals(displayMode) || "tafseer".equals(displayMode) ||
                "hafiz".equals(displayMode)) {
            displayMode = "arabic";
            repository.saveDisplayMode("arabic");
        }

        setupRecyclerView();
        setupGestures(view);
        setupAudioCallback();
        updateModeButtons();

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
        btnModeWbw = view.findViewById(R.id.btn_mode_wbw);
        // RecyclerView
        recyclerView = view.findViewById(R.id.rv_ayahs);

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

        // Instant mode switching
        btnModeArabic.setOnClickListener(v -> switchDisplayMode("arabic"));
        btnModeWbw.setOnClickListener(v -> switchDisplayMode("wbw"));

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

        updateRepeatButton(repository.getRepeatModeInt());

        // Reading Point button (in toolbar)
        ImageButton btnReadingPoint = view.findViewById(R.id.fab_reading_point);
        btnReadingPoint.setOnClickListener(v -> {
            int[] pos = repository.getCurrentPosition();
            navigateToAyah(pos[0], pos[1]);
        });

        // Fullscreen button
        btnFullscreen = view.findViewById(R.id.btn_fullscreen);
        btnFullscreen.setOnClickListener(v -> {
            if (isFullscreen) exitFullscreen();
            else enterFullscreen();
        });

        // Floating exit fullscreen button (visible only in fullscreen mode)
        btnExitFullscreen = (ImageButton) view.findViewById(R.id.btn_exit_fullscreen);
        btnExitFullscreen.setOnClickListener(v -> exitFullscreen());

        // Font size +/- buttons
        btnFontDecrease = view.findViewById(R.id.btn_font_decrease);
        btnFontIncrease = view.findViewById(R.id.btn_font_increase);
        btnFontDecrease.setOnClickListener(v -> changeFontSize(-FONT_STEP));
        btnFontIncrease.setOnClickListener(v -> changeFontSize(FONT_STEP));
    }

    private void switchDisplayMode(String mode) {
        String oldMode = displayMode;
        if (mode.equals(oldMode)) return; // No-op if same mode
        displayMode = mode;
        repository.saveDisplayMode(mode);
        updateModeButtons();

        if ("arabic".equals(mode) && !"arabic".equals(oldMode)) {
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
            // Switch back to ayah adapter (wbw)
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

    private void updateModeButtons() {
        // Set localized labels
        String lang = repository.getLanguage();
        btnModeArabic.setText(Localization.get(lang, Localization.MODE_ARABIC));
        btnModeWbw.setText(Localization.get(lang, Localization.MODE_WBW));

        // Reset all buttons to inactive style
        TextView[] allBtns = {btnModeArabic, btnModeWbw};
        for (TextView btn : allBtns) {
            btn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btn.setTextColor(theme.getSecondaryTextColor());
        }

        // Highlight active button with themed pill
        TextView activeBtn;
        switch (displayMode) {
            case "wbw": activeBtn = btnModeWbw; break;
            default: activeBtn = btnModeArabic; break;
        }
        GradientDrawable activeBg = new GradientDrawable();
        activeBg.setColor(theme.getModePillActiveColor());
        activeBg.setCornerRadius(60);
        activeBtn.setBackground(activeBg);
        activeBtn.setTextColor(theme.isDarkTheme() ? 0xFFFFFFFF : 0xFF000000);
    }

    private void setupAudioCallback() {
        audioPlayer.setCallback(new AudioPlayerManager.PlaybackCallback() {
            @Override
            public void onPlaybackEnded() {
                handler.post(() -> {
                    int repeatMode = repository.getRepeatModeInt();

                    // Repeat ayah mode (mode 1) is handled by ExoPlayer REPEAT_MODE_ONE,
                    // so onPlaybackEnded won't fire for it. This callback fires for modes 0 and 2.

                    if (repeatMode == 2) {
                        // Repeat surah: play next ayah in same surah, loop back to ayah 1
                        int maxAyah = QuranDataParser.SURAH_AYAH_COUNT[playingSurah - 1];
                        int nextAyah = playingAyah + 1;
                        if (nextAyah > maxAyah) {
                            nextAyah = 1; // loop back to first ayah of surah
                        }
                        playingAyah = nextAyah;
                        currentSurah = playingSurah;
                        currentAyah = playingAyah;
                        audioPlayer.playAyah(playingSurah, playingAyah, false);
                        highlightPlayingAyah();
                        scrollToPlayingAyah();
                        updateHeader();
                    } else if (continuousPlay || repository.getContinuousPlay()) {
                        // Continuous play: advance to next ayah across surahs
                        int pos = AyahPagerAdapter.surahAyahToPosition(playingSurah, playingAyah);
                        if (pos < QuranDataParser.TOTAL_AYAHS - 1) {
                            int nextPos = pos + 1;
                            int[] sa = AyahPagerAdapter.positionToSurahAyah(nextPos);
                            playingSurah = sa[0];
                            playingAyah = sa[1];
                            currentSurah = playingSurah;
                            currentAyah = playingAyah;
                            audioPlayer.playAyah(playingSurah, playingAyah, false);
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

    /** Scroll to keep the playing ayah visible — stops any ongoing scroll first */
    private void scrollToPlayingAyah() {
        scrollToPlayingAyahInstant();
    }

    /** Snap the playing ayah into view — only scrolls if not already visible */
    private void scrollToPlayingAyahInstant() {
        if ("arabic".equals(displayMode)) {
            int surahPos = MushafAdapter.surahToPosition(playingSurah);
            if (!isPositionVisible(surahPos)) {
                suppressScrollUpdates = true;
                recyclerView.stopScroll();
                layoutManager.scrollToPositionWithOffset(surahPos, 0);
                if (playingAyah > 1) {
                    recyclerView.post(() -> scrollToAyahInMushaf(surahPos, playingAyah));
                }
                handler.postDelayed(() -> suppressScrollUpdates = false, 200);
            }
        } else {
            int pos = AyahPagerAdapter.surahAyahToPosition(playingSurah, playingAyah);
            if (!isPositionVisible(pos)) {
                suppressScrollUpdates = true;
                recyclerView.stopScroll();
                layoutManager.scrollToPositionWithOffset(pos, 0);
                handler.postDelayed(() -> suppressScrollUpdates = false, 200);
            }
        }
    }

    /** Check if an adapter position is currently visible on screen */
    private boolean isPositionVisible(int pos) {
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        return first != RecyclerView.NO_POSITION && pos >= first && pos <= last;
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setInitialPrefetchItemCount(8);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setHasFixedSize(true);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 25);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        pagerAdapter = new AyahPagerAdapter(repository, theme, arabicFont, urduFont);
        pagerAdapter.setDisplayMode(displayMode);
        // Prime the preload cache for the initial position
        pagerAdapter.preloadAround(AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah));

        // Tap = highlight + compare sheet, Long press = compare sheet (all, with selectable text)
        pagerAdapter.setTapListener((surah, ayah, position) -> {
            currentSurah = surah;
            currentAyah = ayah;
            updateHeader();
            openCompareSheet(surah, ayah, "translation");
        });
        pagerAdapter.setLongPressListener((surah, ayah, position) -> openCompareSheet(surah, ayah, "all"));

        if ("arabic".equals(displayMode)) {
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
            private int lastReportedFirstVisible = -1;

            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                // Skip if programmatic navigation is in progress
                if (suppressScrollUpdates) return;

                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible == RecyclerView.NO_POSITION || firstVisible == lastReportedFirstVisible) return;
                lastReportedFirstVisible = firstVisible;

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

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateHeader();
                    // Save position when scroll stops
                    repository.saveCurrentPosition(currentSurah, currentAyah);
                }
            }
        });

        updateHeader();
    }

    private void changeFontSize(float delta) {
        float ratio = currentTransSize / Math.max(currentArabicSize, 1f);
        currentArabicSize = Math.max(MIN_ARABIC_SP, Math.min(MAX_ARABIC_SP, currentArabicSize + delta));
        currentTransSize = currentArabicSize * ratio;
        pagerAdapter.updateFontSize(currentArabicSize, currentTransSize);
        if (mushafAdapter != null) mushafAdapter.updateFontSize(currentArabicSize);
        repository.setArabicFontSize(currentArabicSize);
        repository.setTranslationFontSize(currentTransSize);
    }

    private void setupGestures(View view) {
        // Double-tap on surah name to cycle modes
        GestureDetector gestureDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        switch (displayMode) {
                            case "arabic": switchDisplayMode("wbw"); break;
                            case "wbw": switchDisplayMode("arabic"); break;
                            default: switchDisplayMode("arabic"); break;
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

        if (recyclerView != null && layoutManager != null) {
            if ("arabic".equals(displayMode)) {
                int surahPos = MushafAdapter.surahToPosition(surah);
                if (!isPositionVisible(surahPos)) {
                    suppressScrollUpdates = true;
                    recyclerView.stopScroll();
                    layoutManager.scrollToPositionWithOffset(surahPos, 0);
                    if (ayah > 1) {
                        recyclerView.post(() -> scrollToAyahInMushaf(surahPos, ayah));
                    }
                }
            } else {
                int pos = AyahPagerAdapter.surahAyahToPosition(surah, ayah);
                if (!isPositionVisible(pos)) {
                    suppressScrollUpdates = true;
                    recyclerView.stopScroll();
                    layoutManager.scrollToPositionWithOffset(pos, 0);
                }
            }
        }
        updateHeader();

        // Post highlight after layout, then re-enable scroll listener
        if (recyclerView != null) {
            recyclerView.post(() -> {
                highlightActiveAyah();
                handler.postDelayed(() -> suppressScrollUpdates = false, 200);
            });
        } else {
            highlightActiveAyah();
            suppressScrollUpdates = false;
        }
    }

    /** In mushaf mode, scroll within the surah item to make the target ayah visible */
    private void scrollToAyahInMushaf(int surahPos, int ayah) {
        scrollToAyahInMushafRetry(surahPos, ayah, 0);
    }

    private void scrollToAyahInMushafRetry(int surahPos, int ayah, int retryCount) {
        if (retryCount > 3) return; // give up after 3 retries
        View surahView = layoutManager.findViewByPosition(surahPos);
        if (surahView == null) {
            recyclerView.post(() -> scrollToAyahInMushafRetry(surahPos, ayah, retryCount + 1));
            return;
        }
        TextView tvText = surahView.findViewById(R.id.tv_mushaf_text);
        if (tvText == null || tvText.getLayout() == null) {
            recyclerView.post(() -> scrollToAyahInMushafRetry(surahPos, ayah, retryCount + 1));
            return;
        }
        // Find the ayah marker " (ayah) " with spaces to avoid false matches like (1) matching (10)
        String text = tvText.getText().toString();
        String marker = " (" + ayah + ") ";
        int idx = text.indexOf(marker);
        if (idx < 0) {
            // Fallback: try without trailing space (last ayah might not have trailing space)
            marker = " (" + ayah + ")";
            idx = text.indexOf(marker);
        }
        if (idx < 0) return;
        // Get y position of the ayah text (go back to find the start of the ayah text before the marker)
        android.text.Layout layout = tvText.getLayout();
        int line = layout.getLineForOffset(idx);
        // Go back a few lines to show the ayah start, not just the marker
        int targetLine = Math.max(0, line - 2);
        int lineTop = layout.getLineTop(targetLine);
        // Scroll so the ayah appears at the top of the RecyclerView
        int targetY = surahView.getTop() + tvText.getTop() + lineTop;
        recyclerView.scrollBy(0, targetY);
    }

    private void navigateNext() {
        int nextAyah = currentAyah + 1;
        int nextSurah = currentSurah;
        int ayahCount = QuranDataParser.SURAH_AYAH_COUNT[currentSurah - 1];
        if (nextAyah > ayahCount) {
            if (nextSurah >= 114) return;
            nextSurah++;
            nextAyah = 1;
        }
        currentSurah = nextSurah;
        currentAyah = nextAyah;

        // If audio is playing, advance playback to the new ayah
        if (isPlaying) {
            playingSurah = currentSurah;
            playingAyah = currentAyah;
            audioPlayer.playAyah(playingSurah, playingAyah, repository.getRepeatModeInt() == 1);
            highlightPlayingAyah();
            scrollToPlayingAyahInstant();
            updateHeader();
        } else {
            navigateToAyah(currentSurah, currentAyah);
        }
    }

    private void navigatePrev() {
        int prevAyah = currentAyah - 1;
        int prevSurah = currentSurah;
        if (prevAyah < 1) {
            if (prevSurah <= 1) return;
            prevSurah--;
            prevAyah = QuranDataParser.SURAH_AYAH_COUNT[prevSurah - 1];
        }
        currentSurah = prevSurah;
        currentAyah = prevAyah;

        // If audio is playing, advance playback to the new ayah
        if (isPlaying) {
            playingSurah = currentSurah;
            playingAyah = currentAyah;
            audioPlayer.playAyah(playingSurah, playingAyah, repository.getRepeatModeInt() == 1);
            highlightPlayingAyah();
            scrollToPlayingAyahInstant();
            updateHeader();
        } else {
            navigateToAyah(currentSurah, currentAyah);
        }
    }

    private void highlightActiveAyah() {
        if ("arabic".equals(displayMode) && mushafAdapter != null) {
            mushafAdapter.setHighlightedAyah(currentSurah, currentAyah);
        } else {
            int pos = AyahPagerAdapter.surahAyahToPosition(currentSurah, currentAyah);
            pagerAdapter.setPlayingPosition(pos);
        }
        updateHeader();
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

    public void showBookmarks() {
        showBookmarkList();
    }

    private void showBookmarkList() {
        BookmarksBottomSheet sheet = new BookmarksBottomSheet();
        sheet.setOnBookmarkClickListener((surah, ayah) -> {
            navigateToAyah(surah, ayah);
        });
        sheet.show(getChildFragmentManager(), "bookmarks");
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
            audioPlayer.playAyah(playingSurah, playingAyah, repository.getRepeatModeInt() == 1);
            btnPlay.setImageResource(R.drawable.ic_pause);
            isPlaying = true;

            highlightPlayingAyah();
        }
    }

    private void toggleRepeat() {
        // Cycle: 0 (off) → 1 (repeat ayah) → 2 (repeat surah) → 0 (off)
        int mode = (repository.getRepeatModeInt() + 1) % 3;
        repository.setRepeatModeInt(mode);
        audioPlayer.setRepeatModeInt(mode);
        updateRepeatButton(mode);
    }

    private void updateRepeatButton(int mode) {
        switch (mode) {
            case 1: // Repeat ayah
                btnRepeat.setImageResource(R.drawable.ic_repeat_one);
                btnRepeat.setAlpha(1.0f);
                break;
            case 2: // Repeat surah
                btnRepeat.setImageResource(R.drawable.ic_repeat);
                btnRepeat.setAlpha(1.0f);
                break;
            default: // Off
                btnRepeat.setImageResource(R.drawable.ic_repeat);
                btnRepeat.setAlpha(0.35f);
                break;
        }
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
                                audioPlayer.playAyah(currentSurah, currentAyah, repository.getRepeatModeInt() == 1);
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

    private void setupMushafListener() {
        if (mushafAdapter == null) return;
        mushafAdapter.setInteractionListener(new MushafAdapter.OnAyahInteractionListener() {
            @Override
            public void onAyahTapped(int surah, int ayah) {
                currentSurah = surah;
                currentAyah = ayah;
                updateHeader();
                openCompareSheet(surah, ayah, "translation");
            }

            @Override
            public void onAyahLongPressed(int surah, int ayah) {
                openCompareSheet(surah, ayah, "all");
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
                        audioPlayer.playAyah(surah, ayah, repository.getRepeatModeInt() == 1);
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
                        openCompareSheet(surah, ayah, "all");
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

    private boolean compareSheetOpen = false;

    private void openCompareSheet(int surah, int ayah, String initialFilter) {
        if (compareSheetOpen) return;
        compareSheetOpen = true;
        repository.getExecutor().execute(() -> {
            com.tanxe.quran.data.entity.Ayah ayahData = repository.getAyah(surah, ayah);
            if (getActivity() == null) { compareSheetOpen = false; return; }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) { compareSheetOpen = false; return; }
                String surahName = ayahData != null ? ayahData.surahNameEn : "";
                TranslationCompareBottomSheet compareSheet = new TranslationCompareBottomSheet();
                compareSheet.setData(surah, ayah, surahName,
                        ayahData != null ? ayahData.arabicText : "",
                        ayahData != null ? ayahData.defaultTranslation : "",
                        arabicFont, urduFont);
                compareSheet.setInitialFilter(initialFilter);
                compareSheet.setOnDismissListener(() -> compareSheetOpen = false);
                try {
                    compareSheet.show(getChildFragmentManager(), "compare_translations");
                } catch (Exception e) {
                    compareSheetOpen = false;
                }
            });
        });
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

        // Hide UI chrome instantly
        View headerBar = getView().findViewById(R.id.header_bar);
        headerBar.setVisibility(View.GONE);
        floatingToolbar.setVisibility(View.GONE);
        if (getActivity() instanceof MainActivity) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }

        // Immersive fullscreen
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                requireActivity().getWindow(), requireActivity().getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Hide ruku markers in fullscreen
        if (pagerAdapter != null) pagerAdapter.setShowRukuMarkers(false);
        if (mushafAdapter != null) mushafAdapter.setShowRukuMarkers(false);

        btnExitFullscreen.setVisibility(View.VISIBLE);
        fullscreenBackCallback.setEnabled(true);
    }

    public void exitFullscreen() {
        if (!isFullscreen || getActivity() == null) return;
        isFullscreen = false;

        // Show UI chrome instantly
        View headerBar = getView().findViewById(R.id.header_bar);
        headerBar.setVisibility(View.VISIBLE);
        floatingToolbar.setVisibility(View.VISIBLE);
        if (getActivity() instanceof MainActivity) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }

        // Restore system bars
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(
                requireActivity().getWindow(), requireActivity().getWindow().getDecorView());
        insetsController.show(WindowInsetsCompat.Type.systemBars());

        // Restore status/nav bar colors only (no full theme refresh)
        requireActivity().getWindow().setStatusBarColor(theme.getBackgroundColor());
        requireActivity().getWindow().setNavigationBarColor(theme.getSurfaceColor());

        // Restore ruku markers
        if (pagerAdapter != null) pagerAdapter.setShowRukuMarkers(true);
        if (mushafAdapter != null) mushafAdapter.setShowRukuMarkers(true);

        btnExitFullscreen.setVisibility(View.GONE);
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

        // Reading point button tint
        ImageButton btnRP = getView().findViewById(R.id.fab_reading_point);
        if (btnRP != null) btnRP.setColorFilter(theme.getAccentColor());
        if (btnFullscreen != null) btnFullscreen.setColorFilter(theme.getAccentColor());
        if (btnExitFullscreen != null) {
            btnExitFullscreen.setColorFilter(theme.getAccentColor());
            GradientDrawable exitBg = new GradientDrawable();
            exitBg.setShape(GradientDrawable.OVAL);
            exitBg.setColor(theme.getToolbarColor());
            btnExitFullscreen.setBackground(exitBg);
        }

        if (btnSpeed != null) btnSpeed.setTextColor(theme.getAccentColor());
        if (btnFontDecrease != null) btnFontDecrease.setTextColor(theme.getAccentColor());
        if (btnFontIncrease != null) btnFontIncrease.setTextColor(theme.getAccentColor());
        if (tvReciterName != null) tvReciterName.setTextColor(theme.getSecondaryTextColor());

        // Reload arabic font (use cache)
        String fontFile2 = repository.getSelectedArabicFont();
        if (cachedArabicFont != null && fontFile2.equals(cachedArabicFontFile)) {
            arabicFont = cachedArabicFont;
        } else {
            try {
                arabicFont = Typeface.createFromAsset(requireContext().getAssets(), "fonts/" + fontFile2);
                cachedArabicFont = arabicFont;
                cachedArabicFontFile = fontFile2;
            } catch (Exception e) {
                arabicFont = Typeface.DEFAULT;
            }
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
    }
}
