<div align="center">

<img src="app/src/main/res/drawable/ic_varta_logo.jpg" width="108" style="border-radius:20px" alt="Vārta logo" />

# Vārta
### वार्ता — *The news, distilled.*

**A native Android news reader, reborn as an editorial product.**
Live multi-source RSS aggregation · full-article extraction · a bilingual English ⇄ Hindi reading experience · a distraction-free, newsprint-grade UI — plus a matching broadsheet-style companion website.

<br />

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-23-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tools/releases/platforms)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tools/releases/platforms)
[![License](https://img.shields.io/badge/License-Unspecified-lightgrey?style=for-the-badge)](#license)

**[⬇ Download the APK](./varta.apk)** &nbsp;·&nbsp; **[🌐 Visit the website](#-companion-website)** &nbsp;·&nbsp; **[📱 Get Started](#-getting-started)**

</div>

<br />

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [How It Works](#how-it-works)
- [News Categories](#news-categories)
- [Design Language](#design-language)
- [Companion Website](#-companion-website)
- [Download the APK](#-download-the-apk)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [Acknowledgments](#acknowledgments)
- [License](#license)

<br />

## Overview

**Vārta** (वार्ता — "discourse" or "news" in Sanskrit/Hindi) is a native Android news reader built end-to-end in **Jetpack Compose**. It pulls live headlines from multiple trusted Indian publishers in parallel, de-duplicates and merges them into a single resilient feed, then re-fetches and cleans the *full* article body — hero image, byline, publish date, and readable paragraphs — so you read a clean, ad-free, letterpress-style layout instead of a cluttered mobile website.

It isn't a thin wrapper around one API. It's a small, self-healing news pipeline: if a publisher's feed goes down, gets rate-limited, or throws up a bot-detection wall, Vārta silently works through a chain of fallbacks — a JSON proxy, a reader-mode proxy, or a clearly-labeled editorial summary — so the feed rarely feels empty, and a broken source never breaks the UI. Every category, search result, and full article is also available live in **Hindi**, translated on the fly rather than pre-bundled.

<br />

## Features

**📰 Reading experience**
- **Full article extraction** — scrapes and cleans the source publisher's page (hero image, byline, date, body copy) instead of a truncated RSS snippet
- **Two feed densities** — an immersive *Magazine* layout and a dense *Compact* list
- **Estimated read time** and auto-generated key highlights on every article
- **Listen to any article** — on-device text-to-speech reads the full piece aloud, with play / pause / resume, adjustable narration speed, and automatic Hindi/English voice switching per paragraph
- **In-reader typography controls** — three text sizes (Compact · Standard · Comfort) and a Serif/Sans font toggle, tuned live while you read
- **Save for later** — bookmark stories to a local, on-device Room library
- **Live search** across Google News' full index, not just cached categories
- **Native share sheet** integration for any article

**🌐 Bilingual — English ⇄ Hindi**
- **One-tap language switch** — a header chip (and a matching toggle in Settings) flips the entire app between English and हिन्दी instantly, with your choice remembered across launches
- **Fully localized chrome** — every screen (Feed, Search, Saved, Full Article, Settings), category label, empty state, and toast is translated, not just a handful of strings
- **Live content translation** — feed headlines, descriptions, and full scraped article bodies are translated on the fly through a dual-provider pipeline (Google's translate endpoint, with MyMemory as a fallback), with in-memory caching so repeat views don't re-translate
- **Native category titles** — each category already carries its own Hindi name (भारत, व्यापार, प्रौद्योगिकी…), used directly when Hindi is active

**🔔 Notifications & freshness**
- **Breaking-news push notifications** — a periodic `WorkManager` background job checks Top Stories/India and notifies you of new breaking headlines, complete with a user-facing on/off toggle and a test-notification action
- **Live "new stories" banner** — foreground polling surfaces fresh headlines without yanking your scroll position

**🎨 Personalization**
- **Dark & light editorial themes** — two fully custom, non-default palettes ("Midnight Press" and "Newsprint"), not stock Material colors

**📡 Data & reliability**
- **Multi-source aggregation** — every category merges feeds from The Times of India, The Hindu, NDTV, and Google News, de-duplicated by a Unicode-aware normalized headline key (so Hindi and English headlines both de-dupe correctly)
- **Google News redirect decoding** — resolves obfuscated `news.google.com` article links back to the real publisher URL via Google's internal batch-execute endpoint
- **Bot-wall & WAF detection** — recognizes Cloudflare / PerimeterX / Akamai challenge pages and gracefully falls back instead of rendering garbage
- **Multi-tier scraping chain** — direct fetch → `rss2json` proxy for feeds → a Jina Reader proxy pass (HTML, then Markdown, retried against both the resolved and the raw article link) for full articles → a clearly-labeled editorial fallback, so a single blocked source rarely means a broken read
- **Honest fallback labeling** — if every extraction attempt fails, the reader shows an explicit "Summary preview" state with a one-tap **Load Full Story** retry, instead of silently passing off a short summary as the real article
- **Self-healing cache** — stale or fallback-only cached articles are detected and cleared automatically on retry, so a bad first attempt doesn't stick around forever
- **Room-backed local persistence** for your saved reading list

<br />

## Screenshots

<div align="center">

<img src="feed-india.png" width="230" alt="India category feed" />&nbsp;&nbsp;
<img src="full-article.png" width="230" alt="Full article reader with listen &amp; font controls" />&nbsp;&nbsp;
<img src="search-index.png" width="230" alt="Search with recent &amp; trending topics" />

*Category feed · Full-article reader with Listen and Serif/Sans font controls · Search with recent & trending topics*

</div>

<br />

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | [Kotlin](https://kotlinlang.org) 2.2.10 |
| **UI Toolkit** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material 3 |
| **Architecture** | MVVM — `ViewModel` + unidirectional `StateFlow` |
| **Networking** | [OkHttp](https://square.github.io/okhttp/), [Retrofit](https://square.github.io/retrofit/), custom RSS/XML parser |
| **HTML Parsing** | [Jsoup](https://jsoup.org/) — publisher page scraping & OpenGraph/JSON-LD extraction |
| **Local Storage** | [Room](https://developer.android.com/training/data-storage/room) — bookmarked article persistence |
| **JSON** | [Moshi](https://github.com/square/moshi) with KSP codegen |
| **Image Loading** | [Coil](https://coil-kt.github.io/coil/) |
| **Background Work** | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) — periodic breaking-news checks |
| **Speech** | Android `TextToSpeech` — on-device, language-aware article read-aloud |
| **Translation** | Google translate endpoint (primary) + [MyMemory](https://mymemory.translated.net/) API (fallback), with in-memory caching |
| **Reader Proxy** | [Jina AI Reader](https://jina.ai/reader/) (`r.jina.ai`) — secondary extraction pass for bot-walled publisher pages |
| **Async** | Kotlin Coroutines & `Flow` |
| **Cloud (scaffolded)** | Firebase (App Check, Auth, Firestore, AI SDK) — provisioned for future Gemini-powered features |
| **Build** | Gradle Kotlin DSL, AGP 9.1.1, KSP |

<br />

## Architecture

Vārta follows a straightforward, testable MVVM pipeline — a single `NewsViewModel` exposes state to Compose screens, backed by one repository that arbitrates between the network and the local cache.

```mermaid
flowchart TD
    subgraph UI["Presentation — Jetpack Compose"]
        Feed[FeedScreen]
        Search[SearchScreen]
        Saved[SavedScreen]
        Article[FullArticleScreen]
    end

    VM["NewsViewModel<br/>(StateFlow / UI State)"]

    subgraph Domain["Repository"]
        Repo[NewsRepository]
    end

    subgraph Data["Data Sources"]
        FeedSvc["NewsFeedService<br/>(RSS + rss2json fallback)"]
        ScrapeSvc["ArticleScraperService<br/>(Jsoup + Jina Reader)"]
        Decoder["GoogleNewsUrlDecoder<br/>(batchexecute RPC)"]
        Translate["TranslationService<br/>(EN → HI)"]
        Speech["ArticleSpeechHelper<br/>(TextToSpeech)"]
        Worker["VartaBackgroundWorker<br/>(WorkManager)"]
        DB[("Room Database<br/>Bookmarked Articles")]
    end

    subgraph External["External Sources"]
        RSS[("Times of India · The Hindu<br/>NDTV · Google News")]
        Web[("Publisher Websites")]
        Reader[("Jina Reader<br/>r.jina.ai")]
        Trans[("Translate / MyMemory APIs")]
    end

    Feed --> VM
    Search --> VM
    Saved --> VM
    Article --> VM
    Article --> Speech
    VM --> Repo
    Repo --> FeedSvc
    Repo --> ScrapeSvc
    Repo --> Translate
    Repo --> DB
    Worker --> FeedSvc
    ScrapeSvc --> Decoder
    ScrapeSvc --> Reader
    FeedSvc --> RSS
    FeedSvc --> Translate
    Translate --> Trans
    Decoder --> Web
    ScrapeSvc --> Web
```

<br />

## How It Works

1. **Fetch** — for the selected category, Vārta queries several publisher RSS feeds in parallel via OkHttp, stopping early once enough uniquely-illustrated stories are collected.
2. **Merge & de-duplicate** — headlines are normalized (Unicode-aware, so this works for Hindi too) and de-duplicated across sources, then sorted by recency.
3. **Resolve** — when a headline links through `news.google.com`, the decoder replays Google's internal signed batch-execute call to recover the real publisher URL.
4. **Scrape** — on open, `ArticleScraperService` first tries a direct fetch with a realistic browser fingerprint, parsing OpenGraph/Twitter/JSON-LD metadata for the hero image, byline, and body. If that's blocked or thin, it retries the same URL — and, separately, the original `article.link` — through the **Jina Reader** proxy in both HTML and Markdown modes.
5. **Fall back gracefully, honestly** — if every extraction attempt fails, Vārta builds a clean editorial summary from the RSS description and clearly labels it a **Summary preview** with a **Load Full Story** retry, instead of quietly passing it off as the full article.
6. **Translate on demand** — when Hindi is selected, headlines, descriptions, and full article text are routed through `TranslationService` (with an in-memory cache) before reaching the UI, so nothing needs to ship pre-translated.
7. **Stay fresh** — a foreground polling loop surfaces a non-intrusive "new stories" banner, while a background `WorkManager` job checks periodically and raises a system notification for genuinely new breaking stories.

<br />

## News Categories

| Category | हिंदी | Sources |
|---|---|---|
| Top Stories | प्रमुख समाचार | TOI, The Hindu, NDTV, Google News |
| India | भारत | TOI, The Hindu, NDTV, Google News |
| Business | व्यापार | TOI, The Hindu, NDTV Profit, Google News |
| Technology | प्रौद्योगिकी | TOI, Gadgets 360, The Hindu, Google News |
| Sports | खेल | TOI, The Hindu, NDTV Sports, Google News |
| Entertainment | मनोरंजन | NDTV Movies, TOI, The Hindu, Google News |
| Science | विज्ञान | The Hindu, Google News |
| Health | स्वास्थ्य | TOI, The Hindu, Google News |

Plus free-text **search** across Google News' full index. Every category, headline, and article body above is available live in Hindi via the header language chip.

<br />

## Design Language

Vārta is built as a genuine editorial product — a "letterpress" identity shared across the app and its companion website, not a default Material theme.

<table>
<tr>
<td valign="top" width="50%">

**🌙 Dark — "Midnight Press"**

| | Hex |
|---|---|
| ![#0F0F12](https://placehold.co/14x14/0F0F12/0F0F12.png) Midnight ink | `#0F0F12` |
| ![#1F1F28](https://placehold.co/14x14/1F1F28/1F1F28.png) Card | `#1F1F28` |
| ![#F4EFE6](https://placehold.co/14x14/F4EFE6/F4EFE6.png) Newsprint ivory | `#F4EFE6` |
| ![#DCA148](https://placehold.co/14x14/DCA148/DCA148.png) Saffron | `#DCA148` |
| ![#D94848](https://placehold.co/14x14/D94848/D94848.png) Press red | `#D94848` |

</td>
<td valign="top" width="50%">

**☀️ Light — "Newsprint"**

| | Hex |
|---|---|
| ![#F7F4EC](https://placehold.co/14x14/F7F4EC/F7F4EC.png) Parchment | `#F7F4EC` |
| ![#EFE9DD](https://placehold.co/14x14/EFE9DD/EFE9DD.png) Card | `#EFE9DD` |
| ![#131211](https://placehold.co/14x14/131211/131211.png) Letterpress ink | `#131211` |
| ![#B8621B](https://placehold.co/14x14/B8621B/B8621B.png) Ochre | `#B8621B` |
| ![#9E2A2B](https://placehold.co/14x14/9E2A2B/9E2A2B.png) Vermilion | `#9E2A2B` |

</td>
</tr>
</table>

Typography pairs a **serif** display face for headlines and body copy with a **sans-serif** for labels, tabs, and metadata — a deliberate nod to print-newspaper hierarchy. In-app, readers can toggle between Serif and Sans, step through three text sizes, and flip the masthead itself between **EN** and **हि** with a single header chip; the companion website layers on **Playfair Display**, **Merriweather**, and **Rozha One** (Devanagari) alongside **Inter** for a true broadsheet feel.

<br />

## 🌐 Companion Website

**THE VĀRTA DISPATCH** is a static, broadsheet-styled web edition of Vārta — same masthead, same dual light/dark "Newsprint ⇄ Midnight Press" palette, built with plain HTML/CSS/JS in [`website/`](./website).

**Live site:** https://newsvarta.netlify.app/ <!-- add your deployed website URL here -->

To run it locally, just open [`website/index.html`](./website/index.html) in a browser, or serve the folder with any static host (GitHub Pages, Netlify, Vercel).

<br />

## 📥 Download the APK

A signed, ready-to-install build ships in the repo for quick sideloading — no build step required:

| | |
|---|---|
| **App APK** | [`varta.apk`](./varta.apk) |
| **Website mirror** | [`website/varta.apk`](./website/varta.apk) |

Enable **Install from unknown sources** on your Android device, transfer the APK, and tap to install. Requires **Android 6.0 (API 23)** or higher.

<br />

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended)
- JDK 11+
- An Android device or emulator running **API 23 (Android 6.0)** or higher

### Clone & Open

```bash
git clone https://github.com/Adarsh311/Varta.git
cd Varta
```

Open the folder in Android Studio and let Gradle sync — all dependencies are pinned in `gradle/libs.versions.toml`.

### Optional: Gemini API Key

Firebase AI is provisioned in the build but not yet wired into any feature. If you plan to build on it:

```bash
cp .env.example .env
# then uncomment and fill in:
# GEMINI_API_KEY=your_key_here
```

### Build & Run

```bash
./gradlew installDebug
```

Or press **Run ▶** in Android Studio with a connected device/emulator selected.

<br />

## Project Structure

```
Varta/
├── app/
│   └── src/main/java/com/example/
│       ├── MainActivity.kt
│       ├── VartaApp.kt
│       ├── data/
│       │   ├── NewsRepository.kt           # Orchestrates feed/scrape/translate + cache
│       │   └── local/                      # Room database, DAO, entities
│       ├── model/                          # NewsArticle, NewsCategory, AppLanguage, ScrapedArticle…
│       ├── network/
│       │   ├── NewsFeedService.kt          # RSS + rss2json fallback fetching
│       │   ├── ArticleScraperService.kt    # Direct + Jina Reader full-article extraction
│       │   ├── GoogleNewsUrlDecoder.kt     # Redirect resolution
│       │   ├── TranslationService.kt       # EN → HI translation (Google/MyMemory)
│       │   └── RssFeedParser.kt
│       ├── ui/
│       │   ├── NewsViewModel.kt
│       │   ├── VartaAppScreen.kt
│       │   ├── components/                 # Story cards, preview sheet, notification dialog, skeletons
│       │   ├── screens/                    # Feed, Search, Saved, Full Article
│       │   └── theme/                      # Color, Type, Theme
│       ├── util/                           # Date, image, TTS, notification helpers, VartaStrings (i18n)
│       └── worker/
│           └── VartaBackgroundWorker.kt    # Periodic breaking-news check
├── website/                                # Static broadsheet-style companion web edition
│   ├── index.html
│   ├── styles.css
│   ├── app.js
│   └── varta.apk
├── build.gradle.kts
├── settings.gradle.kts
└── varta.apk
```

<br />

## Roadmap

Ideas for future iterations:

- [ ] Home-screen widget with top headlines
- [ ] Offline caching of full article bodies (not just bookmark metadata)
- [ ] Gemini-powered summarization, using the already-provisioned Firebase AI setup
- [ ] Regional languages beyond Hindi (Marathi, Tamil, Bengali…)
- [ ] On-device/offline translation to remove the runtime dependency on translate APIs
- [ ] Deploy and link the companion website publicly

<br />

## Acknowledgments

Vārta aggregates public RSS feeds from **The Times of India**, **The Hindu**, **NDTV**, and **Google News**, and is built on the shoulders of [Jetpack Compose](https://developer.android.com/jetpack/compose), [Jsoup](https://jsoup.org/), [Retrofit](https://square.github.io/retrofit/), [OkHttp](https://square.github.io/okhttp/), [Coil](https://coil-kt.github.io/coil/), [Moshi](https://github.com/square/moshi), and [Room](https://developer.android.com/training/data-storage/room). All trademarks and article content belong to their respective publishers — Vārta only links to and republishes freely available RSS summaries for personal reading convenience.

<br />

## License

No license file is currently included in this repository, which by default means all rights are reserved. If you intend to share or open-source this project, add a `LICENSE` file (e.g. [MIT](https://choosealicense.com/licenses/mit/) is a common permissive choice for personal Android projects).

<br />

<div align="center">

Built by [**Adarsh**](https://github.com/Adarsh311)

</div>
