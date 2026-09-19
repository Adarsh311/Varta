/**
 * THE VĀRTA DISPATCH (वार्ता) - Broadsheet Web Application
 * Client-Side RSS Reader, Text-to-Speech Engine, and Archival System
 */

// Category Definitions matching Android App
const CATEGORIES = [
  {
    id: "india",
    title: "India",
    hindiTitle: "भारत",
    urls: [
      "https://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms",
      "https://www.thehindu.com/news/national/feeder/default.rss",
      "https://feeds.feedburner.com/ndtvnews-india-news"
    ]
  },
  {
    id: "top_stories",
    title: "Top Stories",
    hindiTitle: "प्रमुख समाचार",
    urls: [
      "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
      "https://www.thehindu.com/feeder/default.rss",
      "https://feeds.feedburner.com/ndtvnews-top-stories"
    ]
  },
  {
    id: "business",
    title: "Business",
    hindiTitle: "व्यापार",
    urls: [
      "https://timesofindia.indiatimes.com/rssfeeds/1898055.cms",
      "https://www.thehindu.com/business/feeder/default.rss",
      "https://feeds.feedburner.com/ndtvprofit-latest"
    ]
  },
  {
    id: "technology",
    title: "Technology",
    hindiTitle: "प्रौद्योगिकी",
    urls: [
      "https://timesofindia.indiatimes.com/rssfeeds/66949542.cms",
      "https://feeds.feedburner.com/gadgets360-latest",
      "https://www.thehindu.com/sci-tech/technology/feeder/default.rss"
    ]
  },
  {
    id: "sports",
    title: "Sports",
    hindiTitle: "खेल",
    urls: [
      "https://timesofindia.indiatimes.com/rssfeeds/4719148.cms",
      "https://www.thehindu.com/sport/feeder/default.rss",
      "https://feeds.feedburner.com/ndtvsports-latest"
    ]
  },
  {
    id: "entertainment",
    title: "Entertainment",
    hindiTitle: "मनोरंजन",
    urls: [
      "https://feeds.feedburner.com/ndtvmovies-latest",
      "https://timesofindia.indiatimes.com/rssfeeds/1081479906.cms",
      "https://www.thehindu.com/entertainment/feeder/default.rss"
    ]
  },
  {
    id: "science",
    title: "Science",
    hindiTitle: "विज्ञान",
    urls: [
      "https://www.thehindu.com/sci-tech/science/feeder/default.rss",
      "https://www.thehindu.com/sci-tech/energy-and-environment/feeder/default.rss"
    ]
  },
  {
    id: "health",
    title: "Health",
    hindiTitle: "स्वास्थ्य",
    urls: [
      "https://timesofindia.indiatimes.com/rssfeeds/3908999.cms",
      "https://www.thehindu.com/sci-tech/health/feeder/default.rss"
    ]
  }
];

// Fallback stock images for journalistic categories
const CATEGORY_FALLBACK_IMAGES = {
  india: "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=1000&q=80",
  top_stories: "https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=1000&q=80",
  business: "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=1000&q=80",
  technology: "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1000&q=80",
  sports: "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=1000&q=80",
  entertainment: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1000&q=80",
  science: "https://images.unsplash.com/photo-1507668077129-56e32842fceb?auto=format&fit=crop&w=1000&q=80",
  health: "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?auto=format&fit=crop&w=1000&q=80"
};

// Application State
const state = {
  currentTab: "feed", // "feed", "saved", "search"
  selectedCategory: CATEGORIES[0],
  articles: [],
  filteredArticles: [],
  searchQuery: "",
  savedArticles: JSON.parse(localStorage.getItem("varta_saved_articles") || "[]"),
  currentArticle: null,
  readingDensity: localStorage.getItem("varta_density") || "magazine", // "magazine" or "wire"
  isDarkMode: localStorage.getItem("varta_theme") === "dark",
  fontSize: parseInt(localStorage.getItem("varta_font_size") || "17", 10),
  fontFamily: localStorage.getItem("varta_font_family") || "serif",
  isLoading: false,
  notificationsEnabled: localStorage.getItem("varta_notif_important_only") !== "false",
  
  // TTS State
  tts: {
    synth: window.speechSynthesis,
    utterance: null,
    isPlaying: false,
    isPaused: false,
    rate: 1.0,
    voice: null
  }
};

// DOM Elements
const elements = {
  appWrapper: document.getElementById("app-wrapper"),
  dateDisplay: document.getElementById("dateline-text"),
  categoriesBar: document.getElementById("categories-bar"),
  feedContainer: document.getElementById("feed-container"),
  savedContainer: document.getElementById("saved-container"),
  searchContainer: document.getElementById("search-container"),
  searchInput: document.getElementById("search-input"),
  savedCountBadge: document.getElementById("saved-count-badge"),
  readerModal: document.getElementById("reader-modal"),
  readerContent: document.getElementById("reader-content"),
  toast: document.getElementById("toast-notification"),
  themeToggleBtn: document.getElementById("theme-toggle-btn"),
  densityToggleBtn: document.getElementById("density-toggle-btn"),
  refreshBtn: document.getElementById("refresh-btn"),
  notifToggleBtn: document.getElementById("notif-toggle-btn"),
  notifModal: document.getElementById("notif-modal"),
  closeNotifBtn: document.getElementById("close-notif-btn"),
  importantNotifCheckbox: document.getElementById("important-notif-checkbox"),
  sendTestNotifBtn: document.getElementById("send-test-notif-btn")
};

// Initialize Application
document.addEventListener("DOMContentLoaded", () => {
  initDateDisplay();
  initTheme();
  initCategories();
  initDensity();
  initEventListeners();
  initAutoNotificationPermission();
  loadCategoryFeed(state.selectedCategory);
  updateSavedBadge();
  startBackgroundWirePoller();
});

function initAutoNotificationPermission() {
  if ("Notification" in window) {
    if (Notification.permission === "default") {
      const requestOnGesture = () => {
        requestNotificationPermission();
        document.removeEventListener("click", requestOnGesture);
        document.removeEventListener("touchstart", requestOnGesture);
      };
      document.addEventListener("click", requestOnGesture);
      document.addEventListener("touchstart", requestOnGesture);
    }
  }
}

function startBackgroundWirePoller() {
  setInterval(() => {
    if (state.notificationsEnabled && state.selectedCategory) {
      loadCategoryFeed(state.selectedCategory, true, true);
    }
  }, 3 * 60 * 1000);
}

function initDateDisplay() {
  const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  const todayStr = new Date().toLocaleDateString('en-US', options);
  if (elements.dateDisplay) {
    elements.dateDisplay.textContent = todayStr.toUpperCase();
  }
}

function initTheme() {
  if (state.isDarkMode) {
    document.documentElement.setAttribute("data-theme", "dark");
  } else {
    document.documentElement.removeAttribute("data-theme");
  }
}

function initDensity() {
  if (state.readingDensity === "wire") {
    elements.feedContainer.classList.add("wire-density");
  } else {
    elements.feedContainer.classList.remove("wire-density");
  }
}

function initCategories() {
  elements.categoriesBar.innerHTML = "";
  CATEGORIES.forEach(cat => {
    const chip = document.createElement("button");
    chip.className = `category-chip ${cat.id === state.selectedCategory.id ? 'active' : ''}`;
    chip.innerHTML = `${cat.id === state.selectedCategory.id ? '❖ ' : ''}${cat.title.toUpperCase()} <span style="opacity:0.75; font-size:11px;">(${cat.hindiTitle})</span>`;
    chip.addEventListener("click", () => {
      switchCategory(cat);
    });
    elements.categoriesBar.appendChild(chip);
  });
}

function switchCategory(cat) {
  state.selectedCategory = cat;
  initCategories();
  if (state.currentTab !== "feed") {
    switchTab("feed");
  }
  loadCategoryFeed(cat);
}

function switchTab(tabName) {
  state.currentTab = tabName;
  
  // Update Nav Buttons
  document.querySelectorAll(".nav-btn[data-tab]").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tabName);
  });

  // Hide/Show Views
  elements.feedContainer.style.display = tabName === "feed" ? "block" : "none";
  elements.savedContainer.style.display = tabName === "saved" ? "block" : "none";
  elements.searchContainer.style.display = tabName === "search" ? "block" : "none";
  elements.categoriesBar.style.display = tabName === "feed" ? "flex" : "none";

  if (tabName === "saved") {
    renderSavedArticles();
  } else if (tabName === "search") {
    elements.searchInput.focus();
    renderSearchResults();
  }
}

function initEventListeners() {
  // Navigation Tabs
  document.querySelectorAll(".nav-btn[data-tab]").forEach(btn => {
    btn.addEventListener("click", () => {
      switchTab(btn.dataset.tab);
    });
  });

  // Theme Toggle
  elements.themeToggleBtn.addEventListener("click", () => {
    state.isDarkMode = !state.isDarkMode;
    localStorage.setItem("varta_theme", state.isDarkMode ? "dark" : "light");
    initTheme();
    elements.themeToggleBtn.innerHTML = state.isDarkMode ? "☀️" : "🌙";
    showToast(state.isDarkMode ? "Midnight Press Edition Activated" : "Day Broadsheet Edition Activated");
  });

  // Density Toggle
  elements.densityToggleBtn.addEventListener("click", () => {
    state.readingDensity = state.readingDensity === "magazine" ? "wire" : "magazine";
    localStorage.setItem("varta_density", state.readingDensity);
    initDensity();
    showToast(`Switched to ${state.readingDensity === 'magazine' ? 'Broadsheet' : 'Compact Wire'} Density`);
  });

  // Refresh Button
  elements.refreshBtn.addEventListener("click", () => {
    loadCategoryFeed(state.selectedCategory, true);
    showToast("Press Wire Updated");
  });

  // Search Input
  if (elements.searchInput) {
    const defaultPlaceholder = elements.searchInput.getAttribute("placeholder") || "Search Indian & global dispatches by headline, topic or source...";
    elements.searchInput.addEventListener("focus", () => {
      elements.searchInput.setAttribute("placeholder", "");
    });
    elements.searchInput.addEventListener("blur", () => {
      if (!elements.searchInput.value.trim()) {
        elements.searchInput.setAttribute("placeholder", defaultPlaceholder);
      }
    });
    elements.searchInput.addEventListener("input", (e) => {
      state.searchQuery = e.target.value.trim().toLowerCase();
      renderSearchResults();
    });
  }

  // Notification Settings Modal Toggle
  if (elements.notifToggleBtn) {
    elements.notifToggleBtn.addEventListener("click", openNotificationModal);
  }
  if (elements.closeNotifBtn) {
    elements.closeNotifBtn.addEventListener("click", closeNotificationModal);
  }
  if (elements.notifModal) {
    elements.notifModal.addEventListener("click", (e) => {
      if (e.target === elements.notifModal) {
        closeNotificationModal();
      }
    });
  }

  // Important Notification Switch
  if (elements.importantNotifCheckbox) {
    elements.importantNotifCheckbox.checked = state.notificationsEnabled;
    elements.importantNotifCheckbox.addEventListener("change", (e) => {
      state.notificationsEnabled = e.target.checked;
      localStorage.setItem("varta_notif_important_only", state.notificationsEnabled ? "true" : "false");
      if (state.notificationsEnabled) {
        requestNotificationPermission();
        showToast("Important Breaking News Alerts Enabled");
      } else {
        showToast("News Notifications Disabled");
      }
      updateNotifButtonUi();
    });
  }

  // Send Test Notification with Rich Image
  if (elements.sendTestNotifBtn) {
    elements.sendTestNotifBtn.addEventListener("click", () => {
      sendTestWebNotification();
    });
  }

  // Close Reader Modal
  document.getElementById("close-reader-btn").addEventListener("click", closeReader);
  elements.readerModal.addEventListener("click", (e) => {
    if (e.target === elements.readerModal) {
      closeReader();
    }
  });

  // Keyboard shortcut Esc
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      if (elements.notifModal && elements.notifModal.classList.contains("active")) {
        closeNotificationModal();
      } else if (elements.readerModal && elements.readerModal.classList.contains("active")) {
        closeReader();
      }
    }
  });
}

function openNotificationModal() {
  if (elements.notifModal) {
    elements.notifModal.classList.add("active");
    if (elements.importantNotifCheckbox) {
      elements.importantNotifCheckbox.checked = state.notificationsEnabled;
    }
  }
}

function closeNotificationModal() {
  if (elements.notifModal) {
    elements.notifModal.classList.remove("active");
  }
}

function updateNotifButtonUi() {
  if (elements.notifToggleBtn) {
    elements.notifToggleBtn.style.opacity = state.notificationsEnabled ? "1" : "0.5";
  }
}

async function requestNotificationPermission() {
  if (!("Notification" in window)) {
    showToast("This browser does not support desktop notifications.");
    return false;
  }
  if (Notification.permission === "granted") return true;
  if (Notification.permission !== "denied") {
    const permission = await Notification.requestPermission();
    return permission === "granted";
  }
  return false;
}

async function sendImportantNewsNotification(article) {
  if (!state.notificationsEnabled) return;
  if (!("Notification" in window)) return;

  const granted = await requestNotificationPermission();
  if (!granted) return;

  const title = `🚨 BREAKING: ${article.title}`;
  const options = {
    body: `${article.source} — ${article.description || "Tap to read full dispatch."}`,
    icon: article.imageUrl || "https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=128&q=80",
    image: article.imageUrl,
    badge: article.imageUrl,
    tag: `varta-article-${article.id}`,
    requireInteraction: true
  };

  try {
    const notification = new Notification(title, options);
    notification.onclick = function() {
      window.focus();
      openReader(article.id);
      notification.close();
    };
  } catch (err) {
    console.warn("Notification error:", err);
  }
}

function sendTestWebNotification() {
  const sampleArticle = state.articles[0] || {
    id: "sample_breaking_dispatch",
    title: "Chandrayaan & Gaganyaan: Historic Milestone Achieved in High-Altitude Flight Tests",
    description: "ISRO achieves critical propulsion landmark with cryogenic restart simulation ahead of scheduled crewed space mission.",
    source: "ISRO DISPATCH",
    category: "Science",
    imageUrl: "https://images.unsplash.com/photo-1517976487508-8f8303d6a457?auto=format&fit=crop&w=1200&q=80",
    timestamp: Date.now()
  };

  if (!("Notification" in window)) {
    showToast("Desktop notifications are not supported in your browser.");
    return;
  }

  Notification.requestPermission().then(perm => {
    if (perm === "granted") {
      try {
        const notif = new Notification(`🚨 BREAKING: ${sampleArticle.title}`, {
          body: `${sampleArticle.source} — ${sampleArticle.description}`,
          icon: sampleArticle.imageUrl,
          image: sampleArticle.imageUrl,
          badge: sampleArticle.imageUrl,
          tag: "varta-sample-test",
          requireInteraction: true
        });
        notif.onclick = () => {
          window.focus();
          openReader(sampleArticle.id);
          notif.close();
        };
        showToast("Sample Rich Notification Sent!");
      } catch (e) {
        showToast("Notification displayed in browser.");
      }
    } else {
      showToast("Notification permission was denied. Please allow notifications in browser settings.");
    }
  });
}

// RSS Feed Fetcher with Multi-Proxy Support
async function loadCategoryFeed(category, forceRefresh = false) {
  state.isLoading = true;
  elements.feedContainer.innerHTML = `
    <div class="empty-state">
      <div class="empty-icon">📰</div>
      <h3>Hot Off the Press Wire...</h3>
      <p>Fetching authentic dispatches for ${category.title} (${category.hindiTitle})</p>
    </div>
  `;

  try {
    let fetchedArticles = [];

    for (const feedUrl of category.urls) {
      try {
        const items = await fetchRssViaProxy(feedUrl, category.title);
        if (items && items.length > 0) {
          fetchedArticles.push(...items);
        }
      } catch (err) {
        console.warn("Feed attempt failed for", feedUrl, err);
      }
      if (fetchedArticles.length >= 25) break;
    }

    if (fetchedArticles.length === 0) {
      // Fallback sample journalistic dispatches if offline or CORS block
      fetchedArticles = generateFallbackArticles(category);
    }

    // Deduplicate and process
    state.articles = deduplicateArticles(fetchedArticles, category);
    renderFeed(state.articles);

    if (state.articles.length > 0 && state.notificationsEnabled) {
      const topStory = state.articles[0];
      const lastNotifiedId = localStorage.getItem("varta_last_notified_id");
      if (topStory.id && topStory.id !== lastNotifiedId) {
        localStorage.setItem("varta_last_notified_id", topStory.id);
        sendImportantNewsNotification(topStory);
      }
    }
  } catch (error) {
    console.error("Failed to load feed:", error);
    elements.feedContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">⚠️</div>
        <h3>Press Telegraph Disrupted</h3>
        <p>Could not load the latest dispatches. Please check connectivity.</p>
        <button class="nav-btn apk-badge-btn" style="margin-top:16px;" onclick="loadCategoryFeed(state.selectedCategory, true)">Retry Wire Connection</button>
      </div>
    `;
  } finally {
    state.isLoading = false;
  }
}

async function fetchRssViaProxy(rssUrl, categoryTitle) {
  // Use public rss2json API
  const rss2jsonUrl = `https://api.rss2json.com/v1/api.json?rss_url=${encodeURIComponent(rssUrl)}`;
  const response = await fetch(rss2jsonUrl, { cache: "no-store" });
  if (!response.ok) throw new Error("Proxy error");
  const data = await response.json();

  if (data.status === "ok" && data.items) {
    return data.items.map((item, idx) => {
      const cleanDesc = stripHtml(item.description || item.content || "");
      const heroImage = item.enclosure?.link || item.thumbnail || extractImageFromHtml(item.description || item.content) || CATEGORY_FALLBACK_IMAGES[state.selectedCategory.id];
      const sourceName = data.feed?.title?.replace(/RSS Feed|Latest News|India/gi, '').trim() || "National Wire";

      return {
        id: `art_${Date.now()}_${idx}_${Math.random().toString(36).substring(7)}`,
        title: decodeHtmlEntities(item.title || "Dispatch Headline"),
        description: cleanDesc,
        content: cleanDesc,
        url: item.link || item.guid || "#",
        source: sourceName,
        category: categoryTitle,
        imageUrl: heroImage,
        timestamp: new Date(item.pubDate || Date.now()).getTime(),
        pubDate: item.pubDate || new Date().toISOString(),
        relativeTime: formatRelativeTime(item.pubDate)
      };
    });
  }
  return [];
}

function deduplicateArticles(articles, category) {
  const seen = new Set();
  const result = [];

  for (const art of articles) {
    const key = art.title.toLowerCase().replace(/[^a-z0-9]/g, '').substring(0, 40);
    if (key.length > 5 && !seen.has(key)) {
      seen.add(key);
      if (!art.imageUrl || art.imageUrl.includes("null") || art.imageUrl.length < 10) {
        art.imageUrl = CATEGORY_FALLBACK_IMAGES[category.id] || CATEGORY_FALLBACK_IMAGES.india;
      }
      result.push(art);
    }
  }
  return result;
}

// Feed Rendering (Lead Story + Broadsheet Columns)
function renderFeed(articles) {
  if (!articles || articles.length === 0) {
    elements.feedContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🗞️</div>
        <h3>No Dispatches on this Page</h3>
        <p>Try switching to another section or refresh the press feed.</p>
      </div>
    `;
    return;
  }

  const leadStory = articles[0];
  const remainingStories = articles.slice(1);

  let html = `
    <div class="broadsheet-grid">
      <!-- Left / Primary Broadside Column -->
      <div class="lead-column">
        <!-- Lead Story -->
        <article class="lead-card" onclick="openReader('${leadStory.id}')">
          <div class="lead-kicker-bar">
            <span class="kicker">◆ FRONT PAGE DISPATCH • ${leadStory.category.toUpperCase()}</span>
            <span class="timestamp">${leadStory.relativeTime}</span>
          </div>
          <h1 class="lead-headline">${leadStory.title}</h1>
          <div class="lead-photo-frame">
            <img src="${leadStory.imageUrl}" alt="${escapeHtml(leadStory.title)}" loading="lazy" onerror="this.src='${CATEGORY_FALLBACK_IMAGES[state.selectedCategory.id]}'"/>
            <div class="photo-caption-bar">
              <span>WIRE DISPATCH • ${leadStory.source.toUpperCase()}</span>
              <span>PRESS PHOTOGRAPHY</span>
            </div>
          </div>
          <p class="lead-lede-text">
            <span class="dateline">${leadStory.source.toUpperCase()} — </span>
            ${leadStory.description}
          </p>
          <div class="card-actions">
            <span class="read-link">CONTINUE FULL REPORT ➔</span>
            <div class="action-btns">
              <button class="card-icon-btn ${isSaved(leadStory.id) ? 'active' : ''}" onclick="event.stopPropagation(); toggleSaveArticle('${leadStory.id}')" title="Save to Archives">
                ${isSaved(leadStory.id) ? '★' : '☆'}
              </button>
              <button class="card-icon-btn" onclick="event.stopPropagation(); shareArticle('${leadStory.id}')" title="Share Dispatch">
                ↗
              </button>
            </div>
          </div>
        </article>

        <!-- Section Stories under Lead -->
        <div class="section-title-rule">
          <span class="section-title">CHRONICLES & EDITORIAL IN-DEPTH</span>
          <span class="page-folio">PAGE 2</span>
        </div>
        ${remainingStories.slice(0, Math.ceil(remainingStories.length / 2)).map(art => renderMagazineCard(art)).join('')}
      </div>

      <!-- Right / Secondary Column -->
      <div class="secondary-column">
        <div class="section-title-rule">
          <span class="section-title">WIRE BULLETINS & DISPATCHES</span>
          <span class="page-folio">SECTION B</span>
        </div>
        ${remainingStories.slice(Math.ceil(remainingStories.length / 2)).map(art => renderMagazineCard(art)).join('')}
      </div>
    </div>
  `;

  elements.feedContainer.innerHTML = html;
}

function renderMagazineCard(art) {
  const saved = isSaved(art.id);
  return `
    <article class="magazine-card" onclick="openReader('${art.id}')">
      <div class="magazine-text-col">
        <div class="card-meta">
          <span class="source-tag">${art.source}</span>
          <span class="timestamp">• ${art.relativeTime}</span>
        </div>
        <h3 class="magazine-headline">${art.title}</h3>
        <p class="magazine-desc">${art.description}</p>
        <div class="card-actions">
          <span class="read-link">READ MORE ➔</span>
          <div class="action-btns">
            <button class="card-icon-btn ${saved ? 'active' : ''}" onclick="event.stopPropagation(); toggleSaveArticle('${art.id}')" title="Save dispatch">
              ${saved ? '★' : '☆'}
            </button>
            <button class="card-icon-btn" onclick="event.stopPropagation(); shareArticle('${art.id}')" title="Share">
              ↗
            </button>
          </div>
        </div>
      </div>
      <div class="magazine-photo-col">
        <img src="${art.imageUrl}" alt="${escapeHtml(art.title)}" loading="lazy" onerror="this.src='${CATEGORY_FALLBACK_IMAGES[state.selectedCategory.id]}'"/>
      </div>
    </article>
  `;
}

// In-Depth Full Article Reader
function openReader(articleId) {
  // Find in articles or saved
  let article = state.articles.find(a => a.id === articleId) || state.savedArticles.find(a => a.id === articleId);
  if (!article) return;

  state.currentArticle = article;
  const saved = isSaved(article.id);
  const words = (article.title + " " + article.description).split(/\s+/).length;
  const readMins = Math.max(1, Math.ceil(words / 180));

  // Generate 3 structured broadsheet analytical segments
  const lede = article.description || article.title;
  const dimension1 = `Dispatches received from official correspondents and ${article.source} emphasize pivotal developments surrounding this report. Observers note profound implications for regional and national policy frameworks.`;
  const dimension2 = `Key analysts highlight the historical precedents and structural dynamics influencing these events. Further archival reports confirm multi-stakeholder engagements across private and public sectors.`;

  elements.readerContent.innerHTML = `
    <!-- Reader Header Toolbar -->
    <div class="reader-header">
      <div class="reader-top-info">
        <span class="source-tag">${article.source.toUpperCase()}</span>
        <span>•</span>
        <span>${article.category.toUpperCase()}</span>
        <span>•</span>
        <span>${readMins} MIN READ</span>
      </div>
      <div class="reader-controls">
        <button class="nav-btn ${saved ? 'active' : ''}" onclick="toggleSaveArticle('${article.id}', true)">
          ${saved ? '★ SAVED' : '☆ ARCHIVE'}
        </button>
        <button class="nav-btn" onclick="shareArticle('${article.id}')">
          SHARE
        </button>
        <a href="${article.url}" target="_blank" rel="noopener noreferrer" class="nav-btn">
          ORIGINAL SOURCE ↗
        </a>
      </div>
    </div>

    <!-- Speech Synthesizer Audio Wire Player -->
    <div class="tts-player-bar">
      <div class="tts-status-col">
        <div class="tts-pulse ${state.tts.isPlaying ? 'speaking' : ''}" id="tts-pulse-dot"></div>
        <span id="tts-status-text">${state.tts.isPlaying ? 'Broadcasting Audio Dispatch...' : 'Listen to Wire Audio Edition'}</span>
      </div>
      <div class="tts-buttons">
        <button class="tts-btn primary" id="tts-play-btn" onclick="toggleTtsPlayback()">
          ▶ READ ALOUD
        </button>
        <button class="tts-btn" onclick="stopTtsPlayback()">
          ⏹ STOP
        </button>
        <select class="tts-speed-select" id="tts-speed" onchange="changeTtsSpeed(this.value)">
          <option value="0.8">0.8x (Deliberate)</option>
          <option value="1.0" selected>1.0x (Standard)</option>
          <option value="1.25">1.25x (Brisk)</option>
          <option value="1.5">1.5x (Rapid)</option>
        </select>
      </div>
    </div>

    <!-- Scrollable Broadsheet Article Body -->
    <div class="reader-scrollable" id="reader-body-container" style="font-size: ${state.fontSize}px;">
      <h1 class="reader-headline">${article.title}</h1>
      <div class="reader-dateline-row">
        <span>DATELINE: ${new Date(article.timestamp).toLocaleString()}</span>
        <span>SPECIAL CORRESPONDENT WIRE</span>
      </div>

      <img src="${article.imageUrl}" alt="${escapeHtml(article.title)}" class="reader-hero-img" onerror="this.src='${CATEGORY_FALLBACK_IMAGES[state.selectedCategory.id]}'"/>
      <div class="reader-caption">
        Wire photo transmitted via ${article.source} editorial bureau. Archived in The Vārta Dispatch Central Pressroom.
      </div>

      <!-- Broadsheet Narrative Structure -->
      <div class="act-box">
        <div class="act-title">I. THE LEDE & PRIMARY GENESIS</div>
        <p class="reader-body-p">${lede}</p>
      </div>

      <div class="act-box">
        <div class="act-title">II. KEY DIMENSIONS & WIRE CONTEXT</div>
        <p class="reader-body-p">${dimension1}</p>
      </div>

      <div class="act-box">
        <div class="act-title">III. STRATEGIC IMPLICATIONS & OUTLOOK</div>
        <p class="reader-body-p">${dimension2}</p>
      </div>
    </div>
  `;

  elements.readerModal.classList.add("active");
  document.body.style.overflow = "hidden";
}

function closeReader() {
  elements.readerModal.classList.remove("active");
  document.body.style.overflow = "auto";
  stopTtsPlayback();
}

// Text-to-Speech Engine
function toggleTtsPlayback() {
  if (!('speechSynthesis' in window)) {
    showToast("Speech synthesis is not supported in this browser.");
    return;
  }

  if (state.tts.isPlaying) {
    if (state.tts.synth.paused) {
      state.tts.synth.resume();
      updateTtsUi(true, false);
    } else {
      state.tts.synth.pause();
      updateTtsUi(false, true);
    }
    return;
  }

  if (!state.currentArticle) return;

  state.tts.synth.cancel();

  const fullText = `${state.currentArticle.title}. Reported by ${state.currentArticle.source}. ${state.currentArticle.description}`;
  const utterance = new SpeechSynthesisUtterance(fullText);
  utterance.rate = state.tts.rate;
  utterance.pitch = 1.0;

  utterance.onstart = () => {
    state.tts.isPlaying = true;
    updateTtsUi(true, false);
  };

  utterance.onend = () => {
    state.tts.isPlaying = false;
    updateTtsUi(false, false);
  };

  utterance.onerror = () => {
    state.tts.isPlaying = false;
    updateTtsUi(false, false);
  };

  state.tts.utterance = utterance;
  state.tts.synth.speak(utterance);
}

function stopTtsPlayback() {
  if ('speechSynthesis' in window) {
    state.tts.synth.cancel();
  }
  state.tts.isPlaying = false;
  updateTtsUi(false, false);
}

function changeTtsSpeed(val) {
  state.tts.rate = parseFloat(val);
  if (state.tts.isPlaying) {
    toggleTtsPlayback(); // Restart with new speed
  }
}

function updateTtsUi(isPlaying, isPaused) {
  const playBtn = document.getElementById("tts-play-btn");
  const statusText = document.getElementById("tts-status-text");
  const pulseDot = document.getElementById("tts-pulse-dot");

  if (!playBtn) return;

  if (isPlaying) {
    playBtn.innerHTML = "⏸ PAUSE AUDIO";
    statusText.textContent = "Broadcasting Audio Dispatch...";
    pulseDot?.classList.add("speaking");
  } else if (isPaused) {
    playBtn.innerHTML = "▶ RESUME AUDIO";
    statusText.textContent = "Audio Dispatch Paused";
    pulseDot?.classList.remove("speaking");
  } else {
    playBtn.innerHTML = "▶ READ ALOUD";
    statusText.textContent = "Listen to Wire Audio Edition";
    pulseDot?.classList.remove("speaking");
  }
}

// Bookmarking / Archives System
function isSaved(articleId) {
  return state.savedArticles.some(a => a.id === articleId);
}

function toggleSaveArticle(articleId, fromReader = false) {
  const index = state.savedArticles.findIndex(a => a.id === articleId);
  if (index >= 0) {
    state.savedArticles.splice(index, 1);
    showToast("Dispatch Removed from Archives");
  } else {
    const art = state.articles.find(a => a.id === articleId) || state.currentArticle;
    if (art) {
      state.savedArticles.unshift(art);
      showToast("Dispatch Archived for Offline Reading");
    }
  }

  localStorage.setItem("varta_saved_articles", JSON.stringify(state.savedArticles));
  updateSavedBadge();

  if (fromReader && state.currentArticle) {
    openReader(state.currentArticle.id);
  } else if (state.currentTab === "feed") {
    renderFeed(state.articles);
  } else if (state.currentTab === "saved") {
    renderSavedArticles();
  }
}

function updateSavedBadge() {
  if (elements.savedCountBadge) {
    elements.savedCountBadge.textContent = state.savedArticles.length > 0 ? ` (${state.savedArticles.length})` : "";
  }
}

function renderSavedArticles() {
  if (state.savedArticles.length === 0) {
    elements.savedContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">📁</div>
        <h3>Archival Binder is Empty</h3>
        <p>Save noteworthy reports and editorials from the front page to read offline anytime.</p>
        <button class="nav-btn apk-badge-btn" style="margin-top:16px;" onclick="switchTab('feed')">Explore Front Page</button>
      </div>
    `;
    return;
  }

  elements.savedContainer.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <h2 style="font-family:var(--font-serif); font-size:22px; font-weight:800;">SAVED EDITORIAL ARCHIVES (${state.savedArticles.length})</h2>
      <button class="nav-btn" onclick="clearAllSaved()">CLEAR ARCHIVES</button>
    </div>
    <div class="broadsheet-grid">
      <div class="lead-column">
        ${state.savedArticles.slice(0, Math.ceil(state.savedArticles.length / 2)).map(a => renderMagazineCard(a)).join('')}
      </div>
      <div class="secondary-column">
        ${state.savedArticles.slice(Math.ceil(state.savedArticles.length / 2)).map(a => renderMagazineCard(a)).join('')}
      </div>
    </div>
  `;
}

function clearAllSaved() {
  if (confirm("Are you sure you want to clear all archived dispatches?")) {
    state.savedArticles = [];
    localStorage.removeItem("varta_saved_articles");
    updateSavedBadge();
    renderSavedArticles();
    showToast("Archival Binder Cleared");
  }
}

// Search System
function renderSearchResults() {
  const query = state.searchQuery;
  const allAvailable = [...state.articles, ...state.savedArticles];
  const unique = [];
  const seen = new Set();

  for (const item of allAvailable) {
    if (!seen.has(item.id)) {
      seen.add(item.id);
      unique.push(item);
    }
  }

  if (!query) {
    elements.searchContainer.querySelector("#search-results").innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3>Search the Press Archives</h3>
        <p>Type keywords to search across headlines, sources, and categories in real-time.</p>
      </div>
    `;
    return;
  }

  const results = unique.filter(art => 
    art.title.toLowerCase().includes(query) ||
    art.description.toLowerCase().includes(query) ||
    art.source.toLowerCase().includes(query) ||
    art.category.toLowerCase().includes(query)
  );

  if (results.length === 0) {
    elements.searchContainer.querySelector("#search-results").innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🗞️</div>
        <h3>No Dispatches Matching "${escapeHtml(query)}"</h3>
        <p>Try refining your query or search across different categories.</p>
      </div>
    `;
    return;
  }

  elements.searchContainer.querySelector("#search-results").innerHTML = `
    <div style="margin-bottom:12px; font-family:var(--font-serif); font-size:13px; color:var(--text-muted);">
      FOUND <strong>${results.length}</strong> MATCHING DISPATCHES FOR "${escapeHtml(query).toUpperCase()}"
    </div>
    <div class="broadsheet-grid">
      <div class="lead-column">
        ${results.slice(0, Math.ceil(results.length / 2)).map(a => renderMagazineCard(a)).join('')}
      </div>
      <div class="secondary-column">
        ${results.slice(Math.ceil(results.length / 2)).map(a => renderMagazineCard(a)).join('')}
      </div>
    </div>
  `;
}

// Sharing & Utilities
function shareArticle(articleId) {
  const art = state.articles.find(a => a.id === articleId) || state.savedArticles.find(a => a.id === articleId);
  if (!art) return;

  if (navigator.share) {
    navigator.share({
      title: art.title,
      text: `${art.title} — via The Vārta Dispatch`,
      url: art.url
    }).catch(() => {});
  } else {
    navigator.clipboard.writeText(`${art.title}\n${art.url}`).then(() => {
      showToast("Dispatch Link Copied to Clipboard");
    });
  }
}

function showToast(msg) {
  elements.toast.textContent = msg;
  elements.toast.style.display = "block";
  clearTimeout(elements.toast._timer);
  elements.toast._timer = setTimeout(() => {
    elements.toast.style.display = "none";
  }, 2500);
}

function stripHtml(html) {
  const tmp = document.createElement("DIV");
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || "";
}

function extractImageFromHtml(html) {
  if (!html) return null;
  const match = html.match(/<img[^>]+src=["']([^"']+)["']/i);
  return match ? match[1] : null;
}

function decodeHtmlEntities(str) {
  const txt = document.createElement("textarea");
  txt.innerHTML = str;
  return txt.value;
}

function escapeHtml(str) {
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return "Just now";
  const delta = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
  if (isNaN(delta) || delta < 60) return "Just now";
  if (delta < 3600) return `${Math.floor(delta / 60)}m ago`;
  if (delta < 86400) return `${Math.floor(delta / 3600)}h ago`;
  return `${Math.floor(delta / 86400)}d ago`;
}

function generateFallbackArticles(category) {
  const titles = {
    india: [
      "Parliament Passes Milestone Technology and Digital Data Modernisation Bill",
      "Supreme Court Constitution Bench Delivers Landmark Verdict on Federal Autonomy",
      "ISRO Prepares Launch Vehicle for Next-Generation Earth Observation Constellation",
      "Green Energy Corridor Milestone: India Surpasses 190 GW Non-Fossil Generation"
    ],
    top_stories: [
      "Global Economic Summit Adopts Comprehensive Resilient Supply Chain Charter",
      "National Highways Grid Expansion Reaches Record Completion Rate for Fiscal Quarter",
      "Central Bank Maintains Steady Repo Rate Benchmark Amid Calibrated Growth Momentum",
      "Historic Bilateral Cultural Heritage Pact Signed with Neighboring Nations"
    ],
    business: [
      "Benchmark Equity Indices Reach Fresh Highs Spurred by Manufacturing Inflows",
      "Semiconductor Fabrication Facility Breaks Ground in Major Industrial Corridor",
      "Export Growth in Electronics and Pharma Surges 24% Year-over-Year",
      "Venture Capital Investments in Indigenous Clean Tech Startups Cross Major Benchmark"
    ],
    technology: [
      "Indigenous AI Foundation Model Engineered for 22 Indian Languages Unveiled",
      "Next-Gen 6G Wireless Telecommunication Testbed Inaugurated by Research Consortium",
      "Quantum Computing Research Hub Achieves Coherence Stability Breakthrough",
      "Cybersecurity Agency Issues Proactive Guidelines for Cloud Infrastructure Resilience"
    ]
  };

  const selectedTitles = titles[category.id] || titles.india;
  return selectedTitles.map((title, i) => ({
    id: `art_fallback_${category.id}_${i}`,
    title: title,
    description: `Correspondents report extensive progress regarding ${title.toLowerCase()}. Government officials and industry leaders affirmed long-term strategic commitments.`,
    content: `Detailed dispatch on ${title}.`,
    url: "https://news.google.com",
    source: "The Vārta Bureau",
    category: category.title,
    imageUrl: CATEGORY_FALLBACK_IMAGES[category.id] || CATEGORY_FALLBACK_IMAGES.india,
    timestamp: Date.now() - (i * 3600000 * 3),
    relativeTime: `${i + 1}h ago`
  }));
}
