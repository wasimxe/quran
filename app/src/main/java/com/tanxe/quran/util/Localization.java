package com.tanxe.quran.util;

/**
 * Centralized UI string localization based on user-selected language.
 * Supports: en, ur, ar, fa, tr, id, bn, fr, de, ms, hi
 */
public class Localization {

    // Index constants for labels array
    public static final int RUKU = 0;
    public static final int SURAH = 1;
    public static final int JUZ = 2;
    public static final int BOOKMARKS = 3;
    public static final int SEARCH = 4;
    public static final int LIBRARY = 5;
    public static final int SETTINGS = 6;
    public static final int QURAN = 7;
    public static final int SURAHS = 8;
    public static final int ADD_BOOKMARK = 9;
    public static final int REMOVE_BOOKMARK = 10;
    public static final int SET_READING_POINT = 11;
    public static final int PLAY_AYAH = 12;
    public static final int SHARE = 13;
    public static final int SHARE_IMAGE = 14;
    public static final int COPY_ARABIC = 15;
    public static final int CLOSE = 16;
    public static final int NO_BOOKMARKS = 17;
    public static final int READING_POINT_SET = 18;
    public static final int COPIED = 19;
    public static final int STARTS = 20;
    public static final int MORE = 21;
    public static final int TAFSEER = 22;
    public static final int NO_TAFSEER = 23;
    public static final int NO_WBW = 24;
    public static final int MODE_ARABIC = 25;
    public static final int MODE_TRANSLATION = 26;
    public static final int MODE_TAFSEER = 27;
    public static final int MODE_WBW = 28;
    public static final int READING_PROGRESS = 29;
    public static final int TODAY = 30;
    public static final int KHATMAH = 31;
    public static final int STREAK = 32;
    public static final int THEME = 33;
    public static final int ARABIC_FONT_SIZE = 34;
    public static final int TRANSLATION_FONT_SIZE = 35;
    public static final int LANGUAGE = 36;
    public static final int ABOUT = 37;
    public static final int DEVELOPED_BY = 38;
    public static final int WEBSITE = 39;
    public static final int SEARCH_HINT = 40;
    public static final int SEARCH_ALL = 41;
    public static final int NO_RESULTS = 42;
    public static final int RESULTS = 43;
    public static final int TRANSLATIONS = 44;
    public static final int TAFSEERS = 45;
    public static final int WORD_BY_WORD = 46;
    public static final int AUDIO_RECITERS = 47;
    public static final int SEARCH_LIBRARY_HINT = 48;
    public static final int LOADING_CATALOG = 49;
    public static final int ACTIVE = 50;
    public static final int SEARCH_SURAH_HINT = 51;
    public static final int AYAHS = 52;
    public static final int MECCAN = 53;
    public static final int MEDINAN = 54;
    public static final int SELECT_SURAH = 55;
    public static final int SELECT_JUZ = 56;
    public static final int SELECT_RECITER = 57;
    public static final int GO_TO_AYAH = 58;
    public static final int AYAH = 59;
    public static final int BUILT_IN = 60;
    public static final int BOOKMARK_ADDED = 61;
    public static final int BOOKMARK_REMOVED = 62;
    public static final int SELECT_TEXT = 63;
    public static final int LEARN_MODE = 64;
    public static final int RESET = 65;
    public static final int DONT_KNOW = 66;
    public static final int KNOW = 67;
    public static final int SIMILAR = 68;
    public static final int PREV = 69;
    public static final int NEXT = 70;
    public static final int ALL_WORDS = 71;
    public static final int UNKNOWN_WORD = 72;
    public static final int KNOWN_WORD = 73;
    public static final int FULL_QURAN = 74;
    public static final int NO_WORD_DATA = 75;
    public static final int DOWNLOAD_WBW_FIRST = 76;
    public static final int SIMILAR_TO = 77;
    public static final int RESET_PROGRESS = 78;
    public static final int RESET_CONFIRM = 79;
    public static final int CANCEL = 80;
    public static final int WORDS = 81;
    public static final int COMPARE_TRANSLATIONS = 82;
    public static final int MODE_HAFIZ = 83;
    public static final int ARABIC_FONT = 84;
    public static final int MODE_LEARN = 85;
    public static final int SELECTED = 86;
    public static final int DESELECTED = 87;

    private static final int LABEL_COUNT = 88;

    public static String get(String lang, int key) {
        return getLabels(lang)[key];
    }

    public static String[] getLabels(String lang) {
        if (lang == null) lang = "en";
        switch (lang) {
            case "ur":
                return new String[]{
                    "\u0631\u06A9\u0648\u0639",             // رکوع
                    "\u0633\u0648\u0631\u06C1",             // سورہ
                    "\u067E\u0627\u0631\u06C1",             // پارہ
                    "\u0628\u06A9 \u0645\u0627\u0631\u06A9\u0633", // بک مارکس
                    "\u062A\u0644\u0627\u0634",             // تلاش
                    "\u0644\u0627\u0626\u0628\u0631\u06CC\u0631\u06CC", // لائبریری
                    "\u0645\u0632\u06CC\u062F",             // مزید
                    "\u0642\u0631\u0622\u0646",             // قرآن
                    "\u0633\u0648\u0631\u062A\u06CC\u06BA", // سورتیں
                    "\u0628\u06A9 \u0645\u0627\u0631\u06A9 \u06A9\u0631\u06CC\u06BA", // بک مارک کریں
                    "\u0628\u06A9 \u0645\u0627\u0631\u06A9 \u06C1\u0679\u0627\u0626\u06CC\u06BA", // بک مارک ہٹائیں
                    "\u067E\u0691\u06BE\u0646\u06D2 \u06A9\u0627 \u0645\u0642\u0627\u0645 \u0645\u0642\u0631\u0631 \u06A9\u0631\u06CC\u06BA", // پڑھنے کا مقام مقرر کریں
                    "\u06CC\u06C1 \u0622\u06CC\u062A \u0633\u0646\u06CC\u06BA", // یہ آیت سنیں
                    "\u0634\u06CC\u0626\u0631 \u06A9\u0631\u06CC\u06BA", // شیئر کریں
                    "\u062A\u0635\u0648\u06CC\u0631 \u0634\u06CC\u0626\u0631 \u06A9\u0631\u06CC\u06BA", // تصویر شیئر کریں
                    "\u0639\u0631\u0628\u06CC \u0645\u062A\u0646 \u06A9\u0627\u067E\u06CC \u06A9\u0631\u06CC\u06BA", // عربی متن کاپی کریں
                    "\u0628\u0646\u062F \u06A9\u0631\u06CC\u06BA", // بند کریں
                    "\u0627\u0628\u06BE\u06CC \u06A9\u0648\u0626\u06CC \u0628\u06A9 \u0645\u0627\u0631\u06A9 \u0646\u06C1\u06CC\u06BA", // ابھی کوئی بک مارک نہیں
                    "\u067E\u0691\u06BE\u0646\u06D2 \u06A9\u0627 \u0645\u0642\u0627\u0645 \u0645\u0642\u0631\u0631 \u06C1\u0648 \u06AF\u06CC\u0627", // پڑھنے کا مقام مقرر ہو گیا
                    "\u06A9\u0627\u067E\u06CC \u06C1\u0648 \u06AF\u06CC\u0627", // کاپی ہو گیا
                    "\u0634\u0631\u0648\u0639",             // شروع
                    "\u0645\u0632\u06CC\u062F",             // مزید
                    "\u062A\u0641\u0633\u06CC\u0631",       // تفسیر
                    "\u062A\u0641\u0633\u06CC\u0631 \u0688\u0627\u0624\u0646 \u0644\u0648\u0688 \u0646\u06C1\u06CC\u06BA \u06C1\u0648\u0626\u06CC", // تفسیر ڈاؤن لوڈ نہیں ہوئی
                    "\u0644\u0641\u0638 \u0628\u0644\u0641\u0638 \u0688\u0627\u0624\u0646 \u0644\u0648\u0688 \u0646\u06C1\u06CC\u06BA \u06C1\u0648\u0627", // لفظ بلفظ ڈاؤن لوڈ نہیں ہوا
                    "\u0639\u0631\u0628\u06CC",             // عربی
                    "\u062A\u0631\u062C\u0645\u06C1",       // ترجمہ
                    "\u062A\u0641\u0633\u06CC\u0631",       // تفسیر
                    "\u0644\u0641\u0638 \u0628\u0644\u0641\u0638", // لفظ بلفظ
                    "\u067E\u0691\u06BE\u0646\u06D2 \u06A9\u06CC \u067E\u06CC\u0634\u0631\u0641\u062A", // پڑھنے کی پیشرفت
                    "\u0622\u062C",                         // آج
                    "\u062E\u062A\u0645",                   // ختم
                    "\u0633\u0644\u0633\u0644\u06C1",       // سلسلہ
                    "\u062A\u06BE\u06CC\u0645",             // تھیم
                    "\u0639\u0631\u0628\u06CC \u0641\u0627\u0646\u0679 \u0633\u0627\u0626\u0632", // عربی فانٹ سائز
                    "\u062A\u0631\u062C\u0645\u06C1 \u0641\u0627\u0646\u0679 \u0633\u0627\u0626\u0632", // ترجمہ فانٹ سائز
                    "\u0632\u0628\u0627\u0646",             // زبان
                    "\u06A9\u06D2 \u0628\u0627\u0631\u06D2 \u0645\u06CC\u06BA", // کے بارے میں
                    "\u062A\u06CC\u0627\u0631 \u06A9\u0631\u062F\u06C1", // تیار کردہ
                    "\u0648\u06CC\u0628 \u0633\u0627\u0626\u0679", // ویب سائٹ
                    "\u0642\u0631\u0622\u0646 \u0645\u06CC\u06BA \u062A\u0644\u0627\u0634 \u06A9\u0631\u06CC\u06BA\u2026", // قرآن میں تلاش کریں…
                    "\u0633\u0628",                         // سب
                    "\u06A9\u0648\u0626\u06CC \u0646\u062A\u06CC\u062C\u06C1 \u0646\u06C1\u06CC\u06BA \u0645\u0644\u0627", // کوئی نتیجہ نہیں ملا
                    "\u0646\u062A\u0627\u0626\u062C",       // نتائج
                    "\u062A\u0631\u0627\u062C\u0645",       // تراجم
                    "\u062A\u0641\u0627\u0633\u06CC\u0631",  // تفاسیر
                    "\u0644\u0641\u0638 \u0628\u0644\u0641\u0638", // لفظ بلفظ
                    "\u0622\u0688\u06CC\u0648 \u0642\u0627\u0631\u06CC", // آڈیو قاری
                    "\u0644\u0627\u0626\u0628\u0631\u06CC\u0631\u06CC \u0645\u06CC\u06BA \u062A\u0644\u0627\u0634 \u06A9\u0631\u06CC\u06BA\u2026", // لائبریری میں تلاش کریں…
                    "\u06A9\u06CC\u0679\u0644\u0627\u06AF \u0644\u0648\u0688 \u06C1\u0648 \u0631\u06C1\u0627 \u06C1\u06D2\u2026", // کیٹلاگ لوڈ ہو رہا ہے…
                    "\u0641\u0639\u0627\u0644",             // فعال
                    "\u0633\u0648\u0631\u06C1 \u062A\u0644\u0627\u0634 \u06A9\u0631\u06CC\u06BA\u2026", // سورہ تلاش کریں…
                    "\u0622\u06CC\u0627\u062A",             // آیات
                    "\u0645\u06A9\u06CC",                   // مکی
                    "\u0645\u062F\u0646\u06CC",             // مدنی
                    "\u0633\u0648\u0631\u06C1 \u0645\u0646\u062A\u062E\u0628 \u06A9\u0631\u06CC\u06BA", // سورہ منتخب کریں
                    "\u067E\u0627\u0631\u06C1 \u0645\u0646\u062A\u062E\u0628 \u06A9\u0631\u06CC\u06BA", // پارہ منتخب کریں
                    "\u0642\u0627\u0631\u06CC \u0645\u0646\u062A\u062E\u0628 \u06A9\u0631\u06CC\u06BA", // قاری منتخب کریں
                    "\u0622\u06CC\u062A \u067E\u0631 \u062C\u0627\u0626\u06CC\u06BA", // آیت پر جائیں
                    "\u0622\u06CC\u062A",                   // آیت
                    "\u0628\u0644\u0679 \u0627\u0646", // بلٹ ان
                    "\u0628\u06A9 \u0645\u0627\u0631\u06A9 \u0634\u0627\u0645\u0644 \u06C1\u0648 \u06AF\u06CC\u0627", // بک مارک شامل ہو گیا
                    "\u0628\u06A9 \u0645\u0627\u0631\u06A9 \u06C1\u0679\u0627 \u062F\u06CC\u0627 \u06AF\u06CC\u0627", // بک مارک ہٹا دیا گیا
                    "\u0645\u062A\u0646 \u0645\u0646\u062A\u062E\u0628 \u06A9\u0631\u06CC\u06BA", // متن منتخب کریں
                    "\u0633\u06CC\u06A9\u06BE\u0646\u06D2 \u06A9\u0627 \u0645\u0648\u0688", // سیکھنے کا موڈ
                    "\u0631\u06CC \u0633\u06CC\u0679", // ری سیٹ
                    "\u0646\u06C1\u06CC\u06BA \u0622\u062A\u0627", // نہیں آتا
                    "\u0622\u062A\u0627 \u06C1\u06D2", // آتا ہے
                    "\u0645\u0644\u062A\u06D2 \u062C\u0644\u062A\u06D2", // ملتے جلتے
                    "\u067E\u0686\u06BE\u0644\u0627", // پچھلا
                    "\u0627\u06AF\u0644\u0627", // اگلا
                    "\u062A\u0645\u0627\u0645 \u0627\u0644\u0641\u0627\u0638", // تمام الفاظ
                    "\u0646\u0627\u0645\u0639\u0644\u0648\u0645", // نامعلوم
                    "\u0645\u0639\u0644\u0648\u0645", // معلوم
                    "\u0645\u06A9\u0645\u0644 \u0642\u0631\u0622\u0646", // مکمل قرآن
                    "\u06A9\u0648\u0626\u06CC \u0644\u0641\u0638 \u0688\u06CC\u0679\u0627 \u0646\u06C1\u06CC\u06BA", // کوئی لفظ ڈیٹا نہیں
                    "\u067E\u06C1\u0644\u06D2 \u0644\u0641\u0638 \u0628\u0644\u0641\u0638 \u0688\u06CC\u0679\u0627 \u0688\u0627\u0624\u0646 \u0644\u0648\u0688 \u06A9\u0631\u06CC\u06BA", // پہلے لفظ بلفظ ڈیٹا ڈاؤن لوڈ کریں
                    "\u0645\u0644\u062A\u06D2 \u062C\u0644\u062A\u06D2", // ملتے جلتے
                    "\u0633\u06CC\u06A9\u06BE\u0646\u06D2 \u06A9\u06CC \u067E\u06CC\u0634\u0631\u0641\u062A \u0631\u06CC \u0633\u06CC\u0679 \u06A9\u0631\u06CC\u06BA", // سیکھنے کی پیشرفت ری سیٹ کریں
                    "\u062A\u0645\u0627\u0645 \u0627\u0644\u0641\u0627\u0638 \u0646\u0627\u0645\u0639\u0644\u0648\u0645 \u06C1\u0648 \u062C\u0627\u0626\u06CC\u06BA \u06AF\u06D2\u06D4 \u06A9\u06CC\u0627 \u0622\u067E \u06A9\u0648 \u06CC\u0642\u06CC\u0646 \u06C1\u06D2\u061F", // تمام الفاظ نامعلوم ہو جائیں گے۔ کیا آپ کو یقین ہے؟
                    "\u0645\u0646\u0633\u0648\u062E", // منسوخ
                    "\u0627\u0644\u0641\u0627\u0638", // الفاظ
                    "\u062A\u0631\u0627\u062C\u0645 \u06A9\u0627 \u0645\u0648\u0627\u0632\u0646\u06C1", // تراجم کا موازنہ
                    "\u062D\u0627\u0641\u0638", // حافظ
                    "\u0639\u0631\u0628\u06CC \u0641\u0627\u0646\u0679", // عربی فانٹ
                    "\u0633\u06CC\u06A9\u06BE\u06CC\u06BA", // سیکھیں
                    "\u0645\u0646\u062A\u062E\u0628", // منتخب
                    "\u063A\u06CC\u0631 \u0645\u0646\u062A\u062E\u0628", // غیر منتخب
                };
            case "ar":
                return new String[]{
                    "\u0631\u0643\u0648\u0639",       // ركوع
                    "\u0633\u0648\u0631\u0629",       // سورة
                    "\u062C\u0632\u0621",             // جزء
                    "\u0627\u0644\u0645\u0641\u0636\u0644\u0627\u062A", // المفضلات
                    "\u0628\u062D\u062B",             // بحث
                    "\u0627\u0644\u0645\u0643\u062A\u0628\u0629", // المكتبة
                    "\u0627\u0644\u0645\u0632\u06CC\u062F", // المزید
                    "\u0627\u0644\u0642\u0631\u0622\u0646", // القرآن
                    "\u0627\u0644\u0633\u0648\u0631",  // السور
                    "\u0625\u0636\u0627\u0641\u0629 \u0639\u0644\u0627\u0645\u0629", // إضافة علامة
                    "\u0625\u0632\u0627\u0644\u0629 \u0627\u0644\u0639\u0644\u0627\u0645\u0629", // إزالة العلامة
                    "\u062A\u0639\u064A\u064A\u0646 \u0646\u0642\u0637\u0629 \u0627\u0644\u0642\u0631\u0627\u0621\u0629", // تعيين نقطة القراءة
                    "\u062A\u0634\u063A\u064A\u0644 \u0647\u0630\u0647 \u0627\u0644\u0622\u064A\u0629", // تشغيل هذه الآية
                    "\u0645\u0634\u0627\u0631\u0643\u0629", // مشاركة
                    "\u0645\u0634\u0627\u0631\u0643\u0629 \u0643\u0635\u0648\u0631\u0629", // مشاركة كصورة
                    "\u0646\u0633\u062E \u0627\u0644\u0646\u0635 \u0627\u0644\u0639\u0631\u0628\u064A", // نسخ النص العربي
                    "\u0625\u063A\u0644\u0627\u0642",  // إغلاق
                    "\u0644\u0627 \u062A\u0648\u062C\u062F \u0639\u0644\u0627\u0645\u0627\u062A", // لا توجد علامات
                    "\u062A\u0645 \u062A\u0639\u064A\u064A\u0646 \u0646\u0642\u0637\u0629 \u0627\u0644\u0642\u0631\u0627\u0621\u0629", // تم تعيين نقطة القراءة
                    "\u062A\u0645 \u0627\u0644\u0646\u0633\u062E", // تم النسخ
                    "\u064A\u0628\u062F\u0623",        // يبدأ
                    "\u0627\u0644\u0645\u0632\u064A\u062F", // المزيد
                    "\u062A\u0641\u0633\u064A\u0631",  // تفسير
                    "\u0644\u0645 \u064A\u062A\u0645 \u062A\u062D\u0645\u064A\u0644 \u0627\u0644\u062A\u0641\u0633\u064A\u0631", // لم يتم تحميل التفسير
                    "\u0644\u0645 \u064A\u062A\u0645 \u062A\u062D\u0645\u064A\u0644 \u0643\u0644\u0645\u0629 \u0628\u0643\u0644\u0645\u0629", // لم يتم تحميل كلمة بكلمة
                    "\u0639\u0631\u0628\u064A",             // عربي
                    "\u062A\u0631\u062C\u0645\u0629",       // ترجمة
                    "\u062A\u0641\u0633\u064A\u0631",       // تفسير
                    "\u0643\u0644\u0645\u0629 \u0628\u0643\u0644\u0645\u0629", // كلمة بكلمة
                    "\u062A\u0642\u062F\u0645 \u0627\u0644\u0642\u0631\u0627\u0621\u0629", // تقدم القراءة
                    "\u0627\u0644\u064A\u0648\u0645",       // اليوم
                    "\u062E\u062A\u0645\u0629",             // ختمة
                    "\u0627\u0644\u0633\u0644\u0633\u0644\u0629", // السلسلة
                    "\u0627\u0644\u0633\u0645\u0629",       // السمة
                    "\u062D\u062C\u0645 \u0627\u0644\u062E\u0637 \u0627\u0644\u0639\u0631\u0628\u064A", // حجم الخط العربي
                    "\u062D\u062C\u0645 \u062E\u0637 \u0627\u0644\u062A\u0631\u062C\u0645\u0629", // حجم خط الترجمة
                    "\u0627\u0644\u0644\u063A\u0629",       // اللغة
                    "\u062D\u0648\u0644",                   // حول
                    "\u062A\u0637\u0648\u064A\u0631",       // تطوير
                    "\u0627\u0644\u0645\u0648\u0642\u0639", // الموقع
                    "\u0627\u0628\u062D\u062B \u0641\u064A \u0627\u0644\u0642\u0631\u0622\u0646\u2026", // ابحث في القرآن…
                    "\u0627\u0644\u0643\u0644",             // الكل
                    "\u0644\u0627 \u062A\u0648\u062C\u062F \u0646\u062A\u0627\u0626\u062C", // لا توجد نتائج
                    "\u0646\u062A\u0627\u0626\u062C",       // نتائج
                    "\u0627\u0644\u062A\u0631\u062C\u0645\u0627\u062A", // الترجمات
                    "\u0627\u0644\u062A\u0641\u0627\u0633\u064A\u0631", // التفاسير
                    "\u0643\u0644\u0645\u0629 \u0628\u0643\u0644\u0645\u0629", // كلمة بكلمة
                    "\u0627\u0644\u0642\u0631\u0627\u0621 \u0627\u0644\u0635\u0648\u062A\u064A\u0648\u0646", // القراء الصوتيون
                    "\u0627\u0628\u062D\u062B \u0641\u064A \u0627\u0644\u0645\u0643\u062A\u0628\u0629\u2026", // ابحث في المكتبة…
                    "\u062C\u0627\u0631\u064A \u062A\u062D\u0645\u064A\u0644 \u0627\u0644\u0641\u0647\u0631\u0633\u2026", // جاري تحميل الفهرس…
                    "\u0646\u0634\u0637",                   // نشط
                    "\u0627\u0628\u062D\u062B \u0639\u0646 \u0633\u0648\u0631\u0629\u2026", // ابحث عن سورة…
                    "\u0622\u064A\u0627\u062A",             // آيات
                    "\u0645\u0643\u064A\u0629",             // مكية
                    "\u0645\u062F\u0646\u064A\u0629",       // مدنية
                    "\u0627\u062E\u062A\u0631 \u0633\u0648\u0631\u0629", // اختر سورة
                    "\u0627\u062E\u062A\u0631 \u062C\u0632\u0621", // اختر جزء
                    "\u0627\u062E\u062A\u0631 \u0642\u0627\u0631\u0626", // اختر قارئ
                    "\u0627\u0646\u062A\u0642\u0644 \u0625\u0644\u0649 \u0622\u064A\u0629", // انتقل إلى آية
                    "\u0622\u064A\u0629",                   // آية
                    "\u0645\u062F\u0645\u062C",             // مدمج
                    "\u062A\u0645\u062A \u0625\u0636\u0627\u0641\u0629 \u0627\u0644\u0639\u0644\u0627\u0645\u0629", // تمت إضافة العلامة
                    "\u062A\u0645\u062A \u0625\u0632\u0627\u0644\u0629 \u0627\u0644\u0639\u0644\u0627\u0645\u0629", // تمت إزالة العلامة
                    "\u062A\u062D\u062F\u064A\u062F \u0627\u0644\u0646\u0635", // تحديد النص
                    "\u0648\u0636\u0639 \u0627\u0644\u062A\u0639\u0644\u0645", // وضع التعلم
                    "\u0625\u0639\u0627\u062F\u0629 \u062A\u0639\u064A\u064A\u0646", // إعادة تعيين
                    "\u0644\u0627 \u0623\u0639\u0631\u0641", // لا أعرف
                    "\u0623\u0639\u0631\u0641", // أعرف
                    "\u0645\u0634\u0627\u0628\u0647", // مشابه
                    "\u0627\u0644\u0633\u0627\u0628\u0642", // السابق
                    "\u0627\u0644\u062A\u0627\u0644\u064A", // التالي
                    "\u0643\u0644 \u0627\u0644\u0643\u0644\u0645\u0627\u062A", // كل الكلمات
                    "\u063A\u064A\u0631 \u0645\u0639\u0631\u0648\u0641", // غير معروف
                    "\u0645\u0639\u0631\u0648\u0641", // معروف
                    "\u0627\u0644\u0642\u0631\u0622\u0646 \u0643\u0627\u0645\u0644\u0627\u064B", // القرآن كاملاً
                    "\u0644\u0627 \u062A\u0648\u062C\u062F \u0628\u064A\u0627\u0646\u0627\u062A \u0643\u0644\u0645\u0627\u062A", // لا توجد بيانات كلمات
                    "\u062D\u0645\u0651\u0644 \u0628\u064A\u0627\u0646\u0627\u062A \u0643\u0644\u0645\u0629 \u0628\u0643\u0644\u0645\u0629 \u0623\u0648\u0644\u0627\u064B", // حمّل بيانات كلمة بكلمة أولاً
                    "\u0645\u0634\u0627\u0628\u0647 \u0644\u0640", // مشابه لـ
                    "\u0625\u0639\u0627\u062F\u0629 \u062A\u0639\u064A\u064A\u0646 \u062A\u0642\u062F\u0645 \u0627\u0644\u062A\u0639\u0644\u0645", // إعادة تعيين تقدم التعلم
                    "\u0633\u064A\u062A\u0645 \u062A\u062D\u062F\u064A\u062F \u062C\u0645\u064A\u0639 \u0627\u0644\u0643\u0644\u0645\u0627\u062A \u0643\u063A\u064A\u0631 \u0645\u0639\u0631\u0648\u0641\u0629. \u0647\u0644 \u0623\u0646\u062A \u0645\u062A\u0623\u0643\u062F\u061F", // سيتم تحديد جميع الكلمات كغير معروفة. هل أنت متأكد؟
                    "\u0625\u0644\u063A\u0627\u0621", // إلغاء
                    "\u0643\u0644\u0645\u0627\u062A", // كلمات
                    "\u0645\u0642\u0627\u0631\u0646\u0629 \u0627\u0644\u062A\u0631\u062C\u0645\u0627\u062A", // مقارنة الترجمات
                    "\u062D\u0627\u0641\u0638", // حافظ
                    "\u0627\u0644\u062E\u0637 \u0627\u0644\u0639\u0631\u0628\u064A", // الخط العربي
                    "\u062A\u0639\u0644\u0645", // تعلم
                    "\u0645\u062E\u062A\u0627\u0631", // مختار
                    "\u063A\u064A\u0631 \u0645\u062E\u062A\u0627\u0631", // غير مختار
                };
            case "hi":
                return new String[]{
                    "\u0930\u0941\u0915\u0942\u0905", "\u0938\u0942\u0930\u0939", "\u092A\u093E\u0930\u093E",
                    "\u092C\u0941\u0915\u092E\u093E\u0930\u094D\u0915", "\u0916\u094B\u091C\u0947\u0902",
                    "\u0932\u093E\u0907\u092C\u094D\u0930\u0947\u0930\u0940", "\u0914\u0930",
                    "\u0915\u0941\u0930\u0906\u0928", "\u0938\u0942\u0930\u0924\u0947\u0902",
                    "\u092C\u0941\u0915\u092E\u093E\u0930\u094D\u0915 \u0915\u0930\u0947\u0902",
                    "\u092C\u0941\u0915\u092E\u093E\u0930\u094D\u0915 \u0939\u091F\u093E\u090F\u0902",
                    "\u092A\u0922\u093C\u0928\u0947 \u0915\u093E \u0938\u094D\u0925\u093E\u0928 \u0938\u0947\u091F \u0915\u0930\u0947\u0902",
                    "\u092F\u0939 \u0906\u092F\u0924 \u0938\u0941\u0928\u0947\u0902",
                    "\u0936\u0947\u092F\u0930 \u0915\u0930\u0947\u0902",
                    "\u091A\u093F\u0924\u094D\u0930 \u0936\u0947\u092F\u0930 \u0915\u0930\u0947\u0902",
                    "\u0905\u0930\u092C\u0940 \u091F\u0947\u0915\u094D\u0938\u094D\u091F \u0915\u0949\u092A\u0940 \u0915\u0930\u0947\u0902",
                    "\u092C\u0902\u0926 \u0915\u0930\u0947\u0902",
                    "\u0915\u094B\u0908 \u092C\u0941\u0915\u092E\u093E\u0930\u094D\u0915 \u0928\u0939\u0940\u0902",
                    "\u092A\u0922\u093C\u0928\u0947 \u0915\u093E \u0938\u094D\u0925\u093E\u0928 \u0938\u0947\u091F \u0939\u094B \u0917\u092F\u093E",
                    "\u0915\u0949\u092A\u0940 \u0939\u094B \u0917\u092F\u093E",
                    "\u0936\u0941\u0930\u0942",
                    "\u0914\u0930",
                    "\u0924\u092B\u0938\u0940\u0930",
                    "\u0924\u092B\u0938\u0940\u0930 \u0921\u093E\u0909\u0928\u0932\u094B\u0921 \u0928\u0939\u0940\u0902",
                    "\u0932\u092B\u093C\u094D\u091C\u093C \u092C \u0932\u092B\u093C\u094D\u091C\u093C \u0921\u093E\u0909\u0928\u0932\u094B\u0921 \u0928\u0939\u0940\u0902",
                    "\u0905\u0930\u092C\u0940",             // अरबी
                    "\u0905\u0928\u0941\u0935\u093E\u0926", // अनुवाद
                    "\u0924\u092B\u0938\u0940\u0930",       // तफसीर
                    "\u0936\u092C\u094D\u0926 \u0926\u0930 \u0936\u092C\u094D\u0926", // शब्द दर शब्द
                    "\u092A\u0920\u0928 \u092A\u094D\u0930\u0917\u0924\u093F", // पठन प्रगति
                    "\u0906\u091C",                         // आज
                    "\u0916\u0924\u094D\u092E",             // खत्म
                    "\u0932\u0917\u093E\u0924\u093E\u0930", // लगातार
                    "\u0925\u0940\u092E",                   // थीम
                    "\u0905\u0930\u092C\u0940 \u092B\u093C\u0949\u0928\u094D\u091F \u0906\u0915\u093E\u0930", // अरबी फ़ॉन्ट आकार
                    "\u0905\u0928\u0941\u0935\u093E\u0926 \u092B\u093C\u0949\u0928\u094D\u091F \u0906\u0915\u093E\u0930", // अनुवाद फ़ॉन्ट आकार
                    "\u092D\u093E\u0937\u093E",             // भाषा
                    "\u092C\u093E\u0930\u0947 \u092E\u0947\u0902", // बारे में
                    "\u0928\u093F\u0930\u094D\u092E\u093E\u0924\u093E", // निर्माता
                    "\u0935\u0947\u092C\u0938\u093E\u0907\u091F", // वेबसाइट
                    "\u0915\u0941\u0930\u0906\u0928 \u092E\u0947\u0902 \u0916\u094B\u091C\u0947\u0902\u2026", // कुरआन में खोजें…
                    "\u0938\u092D\u0940",                   // सभी
                    "\u0915\u094B\u0908 \u092A\u0930\u093F\u0923\u093E\u092E \u0928\u0939\u0940\u0902", // कोई परिणाम नहीं
                    "\u092A\u0930\u093F\u0923\u093E\u092E", // परिणाम
                    "\u0905\u0928\u0941\u0935\u093E\u0926", // अनुवाद
                    "\u0924\u092B\u0938\u0940\u0930\u0947\u0902", // तफसीरें
                    "\u0936\u092C\u094D\u0926 \u0926\u0930 \u0936\u092C\u094D\u0926", // शब्द दर शब्द
                    "\u0911\u0921\u093F\u092F\u094B \u0915\u093C\u093E\u0930\u0940", // ऑडियो क़ारी
                    "\u0932\u093E\u0907\u092C\u094D\u0930\u0947\u0930\u0940 \u092E\u0947\u0902 \u0916\u094B\u091C\u0947\u0902\u2026", // लाइब्रेरी में खोजें…
                    "\u0915\u0948\u091F\u0932\u0949\u0917 \u0932\u094B\u0921 \u0939\u094B \u0930\u0939\u093E \u0939\u0948\u2026", // कैटलॉग लोड हो रहा है…
                    "\u0938\u0915\u094D\u0930\u093F\u092F", // सक्रिय
                    "\u0938\u0942\u0930\u0939 \u0916\u094B\u091C\u0947\u0902\u2026", // सूरह खोजें…
                    "\u0906\u092F\u0924\u0947\u0902",       // आयतें
                    "\u092E\u0915\u094D\u0915\u0940",       // मक्की
                    "\u092E\u0926\u0928\u0940",             // मदनी
                    "\u0938\u0942\u0930\u0939 \u091A\u0941\u0928\u0947\u0902", // सूरह चुनें
                    "\u092A\u093E\u0930\u093E \u091A\u0941\u0928\u0947\u0902", // पारा चुनें
                    "\u0915\u093C\u093E\u0930\u0940 \u091A\u0941\u0928\u0947\u0902", // क़ारी चुनें
                    "\u0906\u092F\u0924 \u092A\u0930 \u091C\u093E\u090F\u0901", // आयत पर जाएँ
                    "\u0906\u092F\u0924",                   // आयत
                    "\u092C\u093F\u0932\u094D\u091F-\u0907\u0928", // बिल्ट-इन
                    "\u092C\u0941\u0915\u092E\u093E\u0930\u094D\u0915 \u091C\u094B\u0921\u093C\u093E \u0917\u092F\u093E", // बुकमार्क जोड़ा गया
                    "\u092C\u0941\u0915\u092E\u093E\u0930\u094D\u0915 \u0939\u091F\u093E\u092F\u093E \u0917\u092F\u093E", // बुकमार्क हटाया गया
                    "\u091F\u0947\u0915\u094D\u0938\u094D\u091F \u091A\u0941\u0928\u0947\u0902", // टेक्स्ट चुनें
                    "\u0938\u0940\u0916\u0928\u0947 \u0915\u093E \u092E\u094B\u0921", // सीखने का मोड
                    "\u0930\u0940\u0938\u0947\u091F", // रीसेट
                    "\u0928\u0939\u0940\u0902 \u092A\u0924\u093E", // नहीं पता
                    "\u092A\u0924\u093E \u0939\u0948", // पता है
                    "\u0938\u092E\u093E\u0928", // समान
                    "\u092A\u093F\u091B\u0932\u093E", // पिछला
                    "\u0905\u0917\u0932\u093E", // अगला
                    "\u0938\u092D\u0940 \u0936\u092C\u094D\u0926", // सभी शब्द
                    "\u0905\u091C\u094D\u091E\u093E\u0924", // अज्ञात
                    "\u091C\u094D\u091E\u093E\u0924", // ज्ञात
                    "\u092A\u0942\u0930\u093E \u0915\u0941\u0930\u0906\u0928", // पूरा कुरआन
                    "\u0915\u094B\u0908 \u0936\u092C\u094D\u0926 \u0921\u0947\u091F\u093E \u0928\u0939\u0940\u0902", // कोई शब्द डेटा नहीं
                    "\u092A\u0939\u0932\u0947 \u0936\u092C\u094D\u0926 \u0926\u0930 \u0936\u092C\u094D\u0926 \u0921\u0947\u091F\u093E \u0921\u093E\u0909\u0928\u0932\u094B\u0921 \u0915\u0930\u0947\u0902", // पहले शब्द दर शब्द डेटा डाउनलोड करें
                    "\u0907\u0938\u0938\u0947 \u092E\u093F\u0932\u0924\u0947 \u091C\u0941\u0932\u0924\u0947", // इससे मिलते जुलते
                    "\u0938\u0940\u0916\u0928\u0947 \u0915\u0940 \u092A\u094D\u0930\u0917\u0924\u093F \u0930\u0940\u0938\u0947\u091F \u0915\u0930\u0947\u0902", // सीखने की प्रगति रीसेट करें
                    "\u0938\u092D\u0940 \u0936\u092C\u094D\u0926 \u0905\u091C\u094D\u091E\u093E\u0924 \u0939\u094B \u091C\u093E\u090F\u0901\u0917\u0947\u0964 \u0915\u094D\u092F\u093E \u0906\u092A \u0928\u093F\u0936\u094D\u091A\u093F\u0924 \u0939\u0948\u0902?", // सभी शब्द अज्ञात हो जाएँगे। क्या आप निश्चित हैं?
                    "\u0930\u0926\u094D\u0926 \u0915\u0930\u0947\u0902", // रद्द करें
                    "\u0936\u092C\u094D\u0926", // शब्द
                    "\u0905\u0928\u0941\u0935\u093E\u0926\u094B\u0902 \u0915\u0940 \u0924\u0941\u0932\u0928\u093E", // अनुवादों की तुलना
                    "\u0939\u093E\u0931\u093F\u095B", // हाफ़िज़
                    "\u0905\u0930\u092C\u0940 \u092B\u093C\u0949\u0928\u094D\u091F", // अरबी फ़ॉन्ट
                    "\u0938\u0940\u0916\u0947\u0902", // सीखें
                    "\u091A\u0941\u0928\u093E \u0917\u092F\u093E", // चुना गया
                    "\u0905\u091A\u092F\u0928\u093F\u0924", // अचयनित
                };
            case "tr":
                return new String[]{
                    "Rek\u00E2t", "Sure", "C\u00FCz", "Yer \u0130mleri", "Ara",
                    "K\u00FCt\u00FCphane", "Daha Fazla", "Kur'an", "Sureler",
                    "Yer \u0130mi Ekle", "Yer \u0130mini Kald\u0131r",
                    "Okuma Noktas\u0131 Olarak Ayarla", "Bu Ayeti \u00C7al",
                    "Payla\u015F", "Resim Olarak Payla\u015F", "Arap\u00E7a Metni Kopyala",
                    "Kapat", "Hen\u00FCz yer imi yok", "Okuma noktas\u0131 ayarland\u0131",
                    "Kopyaland\u0131", "Ba\u015Flar", "Daha Fazla", "Tefsir",
                    "Tefsir indirilmedi", "Kelime kelime indirilmedi",
                    "Arap\u00E7a", "\u00C7eviri", "Tefsir", "Kelime Kelime",
                    "Okuma \u0130lerlemesi", "Bug\u00FCn", "Hatim", "Seri",
                    "Tema", "Arap\u00E7a Yaz\u0131 Boyutu", "\u00C7eviri Yaz\u0131 Boyutu",
                    "Dil", "Hakk\u0131nda", "Geli\u015Ftiren", "Web Sitesi",
                    "Kur'an'da ara\u2026", "T\u00FCm\u00FC", "Sonu\u00E7 bulunamad\u0131", "sonu\u00E7",
                    "\u00C7eviriler", "Tefsirler", "Kelime Kelime", "Ses Okuyucular\u0131",
                    "K\u00FCt\u00FCphanede ara\u2026", "Katalog y\u00FCkleniyor\u2026", "Aktif",
                    "Sure ara\u2026", "ayet", "Mekki", "Medeni",
                    "Sure Se\u00E7", "C\u00FCz Se\u00E7", "Okuyucu Se\u00E7", "Ayete Git",
                    "Ayet", "Yerle\u015Fik", "Yer imi eklendi", "Yer imi kald\u0131r\u0131ld\u0131",
                    "Metin Se\u00E7",
                    "\u00D6\u011Frenme Modu", "S\u0131f\u0131rla",
                    "Bilmiyorum", "Biliyorum", "Benzer",
                    "\u00D6nceki", "Sonraki", "T\u00FCm Kelimeler",
                    "Bilinmeyen", "Bilinen", "T\u00FCm Kur'an",
                    "Kelime verisi yok", "\u00D6nce kelime kelime verisini indirin",
                    "Benzer: ", "\u00D6\u011Frenme \u0130lerlemesini S\u0131f\u0131rla",
                    "T\u00FCm kelimeler bilinmeyen olarak i\u015Faretlenecek. Emin misiniz?",
                    "\u0130ptal", "kelime",
                    "\u00C7evirileri Kar\u015F\u0131la\u015Ft\u0131r",
                    "Hafiz",
                    "Arap\u00E7a Yaz\u0131 Tipi",
                    "\u00D6\u011Fren", // Öğren
                    "Se\u00E7ildi",
                    "Se\u00E7im kald\u0131r\u0131ld\u0131",
                };
            case "bn":
                return new String[]{
                    "\u09B0\u09C1\u0995\u09C1", "\u09B8\u09C2\u09B0\u09BE", "\u09AA\u09BE\u09B0\u09BE",
                    "\u09AC\u09C1\u0995\u09AE\u09BE\u09B0\u09CD\u0995", "\u0985\u09A8\u09C1\u09B8\u09A8\u09CD\u09A7\u09BE\u09A8",
                    "\u09B2\u09BE\u0987\u09AC\u09CD\u09B0\u09C7\u09B0\u09BF", "\u0986\u09B0\u0993",
                    "\u0995\u09C1\u09B0\u0986\u09A8", "\u09B8\u09C2\u09B0\u09BE\u09B8\u09AE\u09C2\u09B9",
                    "\u09AC\u09C1\u0995\u09AE\u09BE\u09B0\u09CD\u0995 \u0995\u09B0\u09C1\u09A8",
                    "\u09AC\u09C1\u0995\u09AE\u09BE\u09B0\u09CD\u0995 \u09B8\u09B0\u09BE\u09A8",
                    "\u09AA\u09A1\u09BC\u09BE\u09B0 \u09B8\u09CD\u09A5\u09BE\u09A8 \u09B8\u09C7\u099F \u0995\u09B0\u09C1\u09A8",
                    "\u098F\u0987 \u0986\u09AF\u09BC\u09BE\u09A4 \u09B6\u09C1\u09A8\u09C1\u09A8",
                    "\u09B6\u09C7\u09AF\u09BC\u09BE\u09B0 \u0995\u09B0\u09C1\u09A8",
                    "\u099B\u09AC\u09BF \u09B6\u09C7\u09AF\u09BC\u09BE\u09B0 \u0995\u09B0\u09C1\u09A8",
                    "\u0986\u09B0\u09AC\u09BF \u099F\u09C7\u0995\u09CD\u09B8\u099F \u0995\u09AA\u09BF \u0995\u09B0\u09C1\u09A8",
                    "\u09AC\u09A8\u09CD\u09A7 \u0995\u09B0\u09C1\u09A8",
                    "\u098F\u0996\u09A8\u09CB \u0995\u09CB\u09A8\u09CB \u09AC\u09C1\u0995\u09AE\u09BE\u09B0\u09CD\u0995 \u09A8\u09C7\u0987",
                    "\u09AA\u09A1\u09BC\u09BE\u09B0 \u09B8\u09CD\u09A5\u09BE\u09A8 \u09B8\u09C7\u099F \u09B9\u09AF\u09BC\u09C7\u099B\u09C7",
                    "\u0995\u09AA\u09BF \u09B9\u09AF\u09BC\u09C7\u099B\u09C7",
                    "\u09B6\u09C1\u09B0\u09C1",
                    "\u0986\u09B0\u0993",
                    "\u09A4\u09BE\u09AB\u09B8\u09BF\u09B0",
                    "\u09A4\u09BE\u09AB\u09B8\u09BF\u09B0 \u09A1\u09BE\u0989\u09A8\u09B2\u09CB\u09A1 \u09B9\u09AF\u09BC\u09A8\u09BF",
                    "\u09B6\u09AC\u09CD\u09A6 \u09AA\u09CD\u09B0\u09A4\u09BF \u09B6\u09AC\u09CD\u09A6 \u09A1\u09BE\u0989\u09A8\u09B2\u09CB\u09A1 \u09B9\u09AF\u09BC\u09A8\u09BF",
                    "\u0986\u09B0\u09AC\u09BF",             // আরবি
                    "\u0985\u09A8\u09C1\u09AC\u09BE\u09A6", // অনুবাদ
                    "\u09A4\u09BE\u09AB\u09B8\u09BF\u09B0", // তাফসির
                    "\u09B6\u09AC\u09CD\u09A6 \u09AA\u09CD\u09B0\u09A4\u09BF \u09B6\u09AC\u09CD\u09A6", // শব্দ প্রতি শব্দ
                    "\u09AA\u09A0\u09A8 \u0985\u0997\u09CD\u09B0\u0997\u09A4\u09BF", // পঠন অগ্রগতি
                    "\u0986\u099C",                         // আজ
                    "\u0996\u09A4\u09AE",                   // খতম
                    "\u09A7\u09BE\u09B0\u09BE\u09AC\u09BE\u09B9\u09BF\u0995", // ধারাবাহিক
                    "\u09A5\u09BF\u09AE",                   // থিম
                    "\u0986\u09B0\u09AC\u09BF \u09AB\u09A8\u09CD\u099F \u0986\u0995\u09BE\u09B0", // আরবি ফন্ট আকার
                    "\u0985\u09A8\u09C1\u09AC\u09BE\u09A6 \u09AB\u09A8\u09CD\u099F \u0986\u0995\u09BE\u09B0", // অনুবাদ ফন্ট আকার
                    "\u09AD\u09BE\u09B7\u09BE",             // ভাষা
                    "\u09B8\u09AE\u09CD\u09AA\u09B0\u09CD\u0995\u09C7", // সম্পর্কে
                    "\u09A4\u09C8\u09B0\u09BF\u0995\u09BE\u09B0\u0995", // তৈরিকারক
                    "\u0993\u09AF\u09BC\u09C7\u09AC\u09B8\u09BE\u0987\u099F", // ওয়েবসাইট
                    "\u0995\u09C1\u09B0\u0986\u09A8\u09C7 \u0985\u09A8\u09C1\u09B8\u09A8\u09CD\u09A7\u09BE\u09A8 \u0995\u09B0\u09C1\u09A8\u2026", // কুরআনে অনুসন্ধান করুন…
                    "\u09B8\u09AC",                         // সব
                    "\u0995\u09CB\u09A8\u09CB \u09AB\u09B2\u09BE\u09AB\u09B2 \u09A8\u09C7\u0987", // কোনো ফলাফল নেই
                    "\u09AB\u09B2\u09BE\u09AB\u09B2",       // ফলাফল
                    "\u0985\u09A8\u09C1\u09AC\u09BE\u09A6\u09B8\u09AE\u09C2\u09B9", // অনুবাদসমূহ
                    "\u09A4\u09BE\u09AB\u09B8\u09BF\u09B0\u09B8\u09AE\u09C2\u09B9", // তাফসিরসমূহ
                    "\u09B6\u09AC\u09CD\u09A6 \u09AA\u09CD\u09B0\u09A4\u09BF \u09B6\u09AC\u09CD\u09A6", // শব্দ প্রতি শব্দ
                    "\u0985\u09A1\u09BF\u0993 \u0995\u09BE\u09B0\u09C0", // অডিও কারী
                    "\u09B2\u09BE\u0987\u09AC\u09CD\u09B0\u09C7\u09B0\u09BF\u09A4\u09C7 \u0985\u09A8\u09C1\u09B8\u09A8\u09CD\u09A7\u09BE\u09A8 \u0995\u09B0\u09C1\u09A8\u2026", // লাইব্রেরিতে অনুসন্ধান করুন…
                    "\u0995\u09CD\u09AF\u09BE\u099F\u09BE\u09B2\u0997 \u09B2\u09CB\u09A1 \u09B9\u099A\u09CD\u099B\u09C7\u2026", // ক্যাটালগ লোড হচ্ছে…
                    "\u09B8\u0995\u09CD\u09B0\u09BF\u09AF\u09BC",   // সক্রিয়
                    "\u09B8\u09C2\u09B0\u09BE \u0985\u09A8\u09C1\u09B8\u09A8\u09CD\u09A7\u09BE\u09A8 \u0995\u09B0\u09C1\u09A8\u2026", // সূরা অনুসন্ধান করুন…
                    "\u0986\u09AF\u09BC\u09BE\u09A4",       // আয়াত
                    "\u09AE\u0995\u09CD\u0995\u09BF",       // মক্কি
                    "\u09AE\u09BE\u09A6\u09BE\u09A8\u09BF", // মাদানি
                    "\u09B8\u09C2\u09B0\u09BE \u09A8\u09BF\u09B0\u09CD\u09AC\u09BE\u099A\u09A8 \u0995\u09B0\u09C1\u09A8", // সূরা নির্বাচন করুন
                    "\u09AA\u09BE\u09B0\u09BE \u09A8\u09BF\u09B0\u09CD\u09AC\u09BE\u099A\u09A8 \u0995\u09B0\u09C1\u09A8", // পারা নির্বাচন করুন
                    "\u0995\u09BE\u09B0\u09C0 \u09A8\u09BF\u09B0\u09CD\u09AC\u09BE\u099A\u09A8 \u0995\u09B0\u09C1\u09A8", // কারী নির্বাচন করুন
                    "\u0986\u09AF\u09BC\u09BE\u09A4\u09C7 \u09AF\u09BE\u09A8", // আয়াতে যান
                    "\u0986\u09AF\u09BC\u09BE\u09A4",       // আয়াত
                    "\u09AC\u09BF\u09B2\u09CD\u099F-\u0987\u09A8", // বিল্ট-ইন
                    "\u09AC\u09C1\u0995\u09AE\u09BE\u09B0\u09CD\u0995 \u09AF\u09CB\u0997 \u09B9\u09AF\u09BC\u09C7\u099B\u09C7", // বুকমার্ক যোগ হয়েছে
                    "\u09AC\u09C1\u0995\u09AE\u09BE\u09B0\u09CD\u0995 \u09B8\u09B0\u09BE\u09A8\u09CB \u09B9\u09AF\u09BC\u09C7\u099B\u09C7", // বুকমার্ক সরানো হয়েছে
                    "\u099F\u09C7\u0995\u09CD\u09B8\u099F \u09A8\u09BF\u09B0\u09CD\u09AC\u09BE\u099A\u09A8 \u0995\u09B0\u09C1\u09A8", // টেক্সট নির্বাচন করুন
                    "\u09B6\u09C7\u0996\u09BE\u09B0 \u09AE\u09CB\u09A1", // শেখার মোড
                    "\u09B0\u09BF\u09B8\u09C7\u099F", // রিসেট
                    "\u099C\u09BE\u09A8\u09BF \u09A8\u09BE", // জানি না
                    "\u099C\u09BE\u09A8\u09BF", // জানি
                    "\u09B8\u09AE\u09BE\u09A8", // সমান
                    "\u0986\u0997\u09C7\u09B0", // আগের
                    "\u09AA\u09B0\u09C7\u09B0", // পরের
                    "\u09B8\u09AC \u09B6\u09AC\u09CD\u09A6", // সব শব্দ
                    "\u0985\u099C\u09BE\u09A8\u09BE", // অজানা
                    "\u099C\u09BE\u09A8\u09BE", // জানা
                    "\u09B8\u09AE\u09CD\u09AA\u09C2\u09B0\u09CD\u09A3 \u0995\u09C1\u09B0\u0986\u09A8", // সম্পূর্ণ কুরআন
                    "\u0995\u09CB\u09A8\u09CB \u09B6\u09AC\u09CD\u09A6 \u09A1\u09C7\u099F\u09BE \u09A8\u09C7\u0987", // কোনো শব্দ ডেটা নেই
                    "\u0986\u0997\u09C7 \u09B6\u09AC\u09CD\u09A6 \u09AA\u09CD\u09B0\u09A4\u09BF \u09B6\u09AC\u09CD\u09A6 \u09A1\u09C7\u099F\u09BE \u09A1\u09BE\u0989\u09A8\u09B2\u09CB\u09A1 \u0995\u09B0\u09C1\u09A8", // আগে শব্দ প্রতি শব্দ ডেটা ডাউনলোড করুন
                    "\u098F\u09B0 \u09B8\u09AE\u09BE\u09A8", // এর সমান
                    "\u09B6\u09C7\u0996\u09BE\u09B0 \u0985\u0997\u09CD\u09B0\u0997\u09A4\u09BF \u09B0\u09BF\u09B8\u09C7\u099F \u0995\u09B0\u09C1\u09A8", // শেখার অগ্রগতি রিসেট করুন
                    "\u09B8\u09AC \u09B6\u09AC\u09CD\u09A6 \u0985\u099C\u09BE\u09A8\u09BE \u09B9\u09AF\u09BC\u09C7 \u09AF\u09BE\u09AC\u09C7\u0964 \u0986\u09AA\u09A8\u09BF \u0995\u09BF \u09A8\u09BF\u09B6\u09CD\u099A\u09BF\u09A4?", // সব শব্দ অজানা হয়ে যাবে। আপনি কি নিশ্চিত?
                    "\u09AC\u09BE\u09A4\u09BF\u09B2", // বাতিল
                    "\u09B6\u09AC\u09CD\u09A6", // শব্দ
                    "\u0985\u09A8\u09C1\u09AC\u09BE\u09A6 \u09A4\u09C1\u09B2\u09A8\u09BE", // অনুবাদ তুলনা
                    "\u09B9\u09BE\u09AB\u09BF\u099C", // হাফিজ
                    "\u0986\u09B0\u09AC\u09BF \u09AB\u09A8\u09CD\u099F", // আরবি ফন্ট
                    "\u09B6\u09BF\u0996\u09C1\u09A8", // শিখুন
                    "\u09A8\u09BF\u09B0\u09CD\u09AC\u09BE\u099A\u09BF\u09A4", // নির্বাচিত
                    "\u0985\u09A8\u09BF\u09B0\u09CD\u09AC\u09BE\u099A\u09BF\u09A4", // অনির্বাচিত
                };
            default: // en, id, ms, fr, de, fa and fallback
                return new String[]{
                    "Ruku", "Surah", "Juz",
                    "Bookmarks", "Search", "Library", "More",
                    "Quran", "Surahs",
                    "Add Bookmark", "Remove Bookmark",
                    "Set as Reading Point", "Play This Ayah",
                    "Share", "Share as Image", "Copy Arabic Text",
                    "Close", "No bookmarks yet",
                    "Reading point set", "Copied to clipboard",
                    "Starts", "More", "Tafseer",
                    "Tafseer not downloaded", "Word by Word not downloaded",
                    "Arabic", "Translation", "Tafseer", "WBW",
                    "Reading Progress", "Today", "Khatmah", "Streak",
                    "Theme", "Arabic Font Size", "Translation Font Size",
                    "Language", "About", "Developed by", "Website",
                    "Search Quran\u2026", "All", "No results found", "results",
                    "Translations", "Tafseers", "Word by Word", "Audio Reciters",
                    "Search library\u2026", "Loading catalog\u2026", "Active",
                    "Search surah\u2026", "ayahs", "Meccan", "Medinan",
                    "Select Surah", "Select Juz", "Select Reciter", "Go to Ayah",
                    "Ayah", "Built-in", "Bookmark added", "Bookmark removed",
                    "Select Text",
                    "Learn Mode", "Reset",
                    "Don't Know", "Know", "Similar",
                    "Prev", "Next", "All Words",
                    "Unknown", "Known", "Full Quran",
                    "No word data", "Download Word by Word data first",
                    "Similar to: ", "Reset Learning Progress",
                    "This will mark all words as unknown. Are you sure?",
                    "Cancel", "words",
                    "Compare Translations",
                    "Hafiz",
                    "Arabic Font",
                    "Learn",
                    "Selected",
                    "Deselected",
                };
        }
    }
}
