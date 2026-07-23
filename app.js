const CAMPUSES = {
  xianlin: {
    name: "南京邮电大学仙林校区",
    shortName: "仙林校区",
    address: "栖霞区文苑路 9 号",
    image: "assets/map-xianlin-campus.jpg",
  },
  sanpailou: {
    name: "南京邮电大学三牌楼校区",
    shortName: "三牌楼校区",
    address: "鼓楼区新模范马路 66 号",
    image: "assets/map-sanpailou.jpg",
  },
};

const GUIDE_GROUPS = {
  prepare: { label: "入学准备", icon: "compass", pages: ["map", "dorms", "schedule"] },
  life: { label: "校园生活", icon: "coffee", pages: ["food", "gallery"] },
  growth: { label: "学习成长", icon: "sparkles", pages: ["clubs", "competitions", "majors"] },
};

const GUIDE_MODULES = [
  { page: "map", group: "prepare", href: "map.html", title: "校园地图", nav: "我走过的南邮", detail: "校区位置与周边地点", icon: "map" },
  { page: "dorms", group: "prepare", href: "dorms.html", title: "宿舍评价", nav: "我住下的南邮", detail: "宿舍片区与生活信息", icon: "bed-double" },
  { page: "schedule", group: "prepare", href: "schedule.html", title: "作息时间", nav: "我生活的南邮", detail: "晨跑与上课时间", icon: "clock-3" },
  { page: "food", group: "life", href: "food.html", title: "南邮美食", nav: "我品过的南邮", detail: "校园餐饮信息", icon: "utensils" },
  { page: "gallery", group: "life", href: "gallery.html", title: "南邮风光", nav: "我见过的南邮", detail: "校园照片与日常", icon: "images" },
  { page: "clubs", group: "growth", href: "clubs.html", title: "社团一览", nav: "我遇见的南邮", detail: "社团名录与分类筛选", icon: "users" },
  { page: "competitions", group: "growth", href: "competitions.html", title: "竞赛目录", nav: "我挑战的南邮", detail: "校级认定竞赛项目", icon: "trophy" },
  { page: "majors", group: "growth", href: "majors.html", title: "专业指南", nav: "我选择的南邮", detail: "转专业计划与细则", icon: "graduation-cap" },
];

const CLUB_TYPES = ["全部", "科技创新", "文体艺术", "体育运动", "公益实践", "校园发展"];
const CATEGORY_TYPE_MAP = {
  科技创新类: "科技创新",
  创业就业类: "科技创新",
  时尚艺术类: "文体艺术",
  传统文化类: "文体艺术",
  体育类: "体育运动",
  体育运动类: "体育运动",
  志愿服务类: "公益实践",
  医疗卫生类: "公益实践",
};
const TYPE_RULES = {
  科技创新: /科学技术|科协|微动|环保|创新创业|安全协会|消防/,
  文体艺术: /舞|音|艺术|国韵|漫|语林|动漫|辩论|文学|国学|光影|翻译|龙狮|讲解|通讯社|媒体|文化/,
  体育运动: /手球|武道|极限|跃动|体育/,
  公益实践: /志愿|红十字|爱心|勤助|国旗|伙食|服务|社工|渡桥/,
};

let clubs = [];
let visibleClubCount = 12;
let activeClubType = "全部";
let competitionCatalog = [];
let visibleCompetitionCount = 20;
let activeCompetitionLevel = "全部";
let majorGuides = [];
let revealObserver = null;

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#039;", '"': "&quot;",
  })[char]);
}

function getClubType(club) {
  if (CATEGORY_TYPE_MAP[club.category]) return CATEGORY_TYPE_MAP[club.category];
  const text = `${club.name} ${club.unit}`;
  const match = Object.entries(TYPE_RULES).find(([, pattern]) => pattern.test(text));
  return match?.[0] ?? "校园发展";
}

function initSiteGuide() {
  const header = document.querySelector(".site-header");
  const mainNav = document.querySelector(".main-nav");
  if (!header || !mainNav) return;

  if (!mainNav.querySelector('[data-nav="majors"]')) {
    const majorLink = document.createElement("a");
    majorLink.href = "majors.html";
    majorLink.dataset.nav = "majors";
    majorLink.innerHTML = '<i data-lucide="graduation-cap"></i><span>我选择的南邮</span>';
    mainNav.appendChild(majorLink);
  }

  const primaryNav = document.createElement("nav");
  primaryNav.className = "guide-primary-nav";
  primaryNav.setAttribute("aria-label", "指南分类");
  primaryNav.innerHTML = Object.entries(GUIDE_GROUPS).map(([key, group]) =>
    `<a href="index.html#${key}"><span>${group.label}</span><small>${String(group.pages.length).padStart(2, "0")}</small></a>`
  ).join("");
  header.insertBefore(primaryNav, mainNav);

  const headerTools = document.createElement("div");
  headerTools.className = "header-tools";
  headerTools.innerHTML = '<button class="guide-search-trigger" type="button" aria-label="搜索指南"><i data-lucide="search"></i><span>搜索</span></button>';
  header.insertBefore(headerTools, mainNav);

  const searchDialog = document.createElement("dialog");
  searchDialog.className = "guide-search-dialog";
  searchDialog.setAttribute("aria-labelledby", "guide-search-title");
  searchDialog.innerHTML = `
    <div class="guide-search-panel">
      <div class="guide-search-heading">
        <div><p class="section-index">QUICK SEARCH</p><h2 id="guide-search-title">查找指南</h2></div>
        <button class="guide-search-close" type="button" aria-label="关闭搜索"><i data-lucide="x"></i></button>
      </div>
      <label class="guide-search-box"><i data-lucide="search"></i><span class="sr-only">搜索指南</span><input type="search" placeholder="搜索地图、宿舍、社团或竞赛" autocomplete="off" /></label>
      <div class="guide-search-results">
        ${GUIDE_MODULES.map((module, index) => `<a href="${module.href}" data-search-text="${module.title} ${module.nav} ${module.detail} ${GUIDE_GROUPS[module.group].label}"><span>${String(index + 1).padStart(2, "0")}</span><i data-lucide="${module.icon}"></i><div><strong>${module.title}</strong><small>${module.detail}</small></div><i data-lucide="arrow-up-right"></i></a>`).join("")}
      </div>
      <p class="guide-search-empty" hidden>没有找到匹配的内容。</p>
    </div>`;
  document.body.appendChild(searchDialog);

  const searchInput = searchDialog.querySelector("input");
  const searchLinks = [...searchDialog.querySelectorAll("[data-search-text]")];
  const searchEmpty = searchDialog.querySelector(".guide-search-empty");
  const openSearch = () => {
    if (typeof searchDialog.showModal === "function" && !searchDialog.open) searchDialog.showModal();
    searchInput.value = "";
    searchLinks.forEach((link) => { link.hidden = false; });
    searchEmpty.hidden = true;
    window.requestAnimationFrame(() => searchInput.focus());
  };
  headerTools.querySelector("button").addEventListener("click", openSearch);
  searchDialog.querySelector(".guide-search-close").addEventListener("click", () => searchDialog.close());
  searchDialog.addEventListener("click", (event) => { if (event.target === searchDialog) searchDialog.close(); });
  searchInput.addEventListener("input", () => {
    const query = searchInput.value.trim().toLowerCase();
    let visible = 0;
    searchLinks.forEach((link) => {
      const matches = !query || link.dataset.searchText.toLowerCase().includes(query);
      link.hidden = !matches;
      if (matches) visible += 1;
    });
    searchEmpty.hidden = visible !== 0;
  });
  document.addEventListener("keydown", (event) => {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
      event.preventDefault();
      openSearch();
    }
  });

  const currentPage = document.body.dataset.page;
  const currentModule = GUIDE_MODULES.find((module) => module.page === currentPage);
  if (currentModule) {
    const group = GUIDE_GROUPS[currentModule.group];
    const context = document.createElement("div");
    context.className = "page-context";
    context.innerHTML = `
      <div class="page-context-inner">
        <nav class="breadcrumb" aria-label="当前位置"><a href="index.html">首页</a><i data-lucide="chevron-right"></i><span>${group.label}</span><i data-lucide="chevron-right"></i><strong>${currentModule.title}</strong></nav>
        <nav class="context-siblings" aria-label="${group.label}">
          ${GUIDE_MODULES.filter((module) => module.group === currentModule.group).map((module) => `<a href="${module.href}"${module.page === currentPage ? ' class="active" aria-current="page"' : ""}>${module.title}</a>`).join("")}
        </nav>
      </div>`;
    document.querySelector("main").before(context);
  }

  window.lucide?.createIcons();
}

function initNavigation() {
  const currentPage = document.body.dataset.page;
  const nav = document.querySelector(".main-nav");
  if (!nav) return;

  const toggle = document.createElement("button");
  toggle.className = "nav-toggle";
  toggle.type = "button";
  toggle.setAttribute("aria-expanded", "false");
  toggle.setAttribute("aria-label", "展开导航");
  toggle.innerHTML = '<i data-lucide="menu"></i>';
  nav.prepend(toggle);

  const setNavigationOpen = (open) => {
    nav.classList.toggle("is-open", open);
    toggle.setAttribute("aria-expanded", String(open));
    toggle.setAttribute("aria-label", open ? "收起导航" : "展开导航");
    toggle.innerHTML = `<i data-lucide="${open ? "x" : "menu"}"></i>`;
    window.lucide?.createIcons();
  };

  toggle.addEventListener("click", (event) => {
    event.stopPropagation();
    setNavigationOpen(!nav.classList.contains("is-open"));
  });
  nav.querySelectorAll("[data-nav]").forEach((link) => {
    link.addEventListener("click", () => setNavigationOpen(false));
  });
  document.addEventListener("click", (event) => {
    if (!nav.contains(event.target)) setNavigationOpen(false);
  });
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") setNavigationOpen(false);
  });

  document.querySelectorAll("[data-nav]").forEach((link) => {
    const active = link.dataset.nav === currentPage;
    link.classList.toggle("active", active);
    if (active) link.setAttribute("aria-current", "page");
  });
  window.lucide?.createIcons();
}

function registerRevealItems(root = document) {
  if (!revealObserver) return;
  const selector = ".guide-stage, .module-link, .official-links a, .campus-image-shell, .club-card, .catalog-row, .schedule-block, .day-part, .dorm-card, .gallery-item, .food-card, .major-card";
  root.querySelectorAll(selector).forEach((element, index) => {
    if (element.classList.contains("reveal-item")) return;
    element.classList.add("reveal-item");
    element.style.setProperty("--reveal-delay", `${Math.min(index % 6, 5) * 45}ms`);
    revealObserver.observe(element);
  });
}

function initMotion() {
  document.body.classList.add("page-enter");
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches || !("IntersectionObserver" in window)) return;
  document.body.classList.add("motion-ready");
  revealObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add("is-visible");
      revealObserver.unobserve(entry.target);
    });
  }, { threshold: 0.08, rootMargin: "0px 0px -24px" });
  registerRevealItems();
}

function initDormSearch() {
  const input = document.querySelector("#dorm-search");
  if (!input) return;
  const cards = [...document.querySelectorAll(".dorm-card")];
  const count = document.querySelector("#dorm-count");
  const empty = document.querySelector("#dorm-empty");
  input.addEventListener("input", () => {
    const query = input.value.trim().toLowerCase();
    let visible = 0;
    cards.forEach((card) => {
      const matches = card.dataset.dorm.toLowerCase().includes(query);
      card.hidden = !matches;
      if (matches) visible += 1;
    });
    count.textContent = visible;
    empty.hidden = visible !== 0;
  });
}

function initCampusImages() {
  const image = document.querySelector("#campus-map-image");
  if (!image) return;
  const statusText = document.querySelector("#map-campus-status-text");
  const campusButtons = [...document.querySelectorAll("[data-campus-key]")];
  const showCampus = (campusKey) => {
    const campus = CAMPUSES[campusKey] || CAMPUSES.xianlin;
    image.classList.add("is-switching");
    image.src = campus.image;
    image.alt = `${campus.name}静态地图，地址：${campus.address}`;
    if (statusText) statusText.textContent = `${campus.shortName} · ${campus.address}`;
    campusButtons.forEach((button) => {
      const selected = button.dataset.campusKey === campusKey;
      button.classList.toggle("active", selected);
      button.setAttribute("aria-pressed", String(selected));
    });
    image.decode().catch(() => {}).finally(() => image.classList.remove("is-switching"));
  };

  campusButtons.forEach((button) => button.addEventListener("click", () => showCampus(button.dataset.campusKey)));
}

async function loadClubs() {
  const grid = document.querySelector("#club-grid");
  if (!grid) return;
  try {
    const response = await fetch("clubs.json?v=2");
    if (!response.ok) throw new Error("club data unavailable");
    clubs = (await response.json()).map((club) => ({ ...club, type: getClubType(club) }));
    initClubFilters();
    renderClubs();
  } catch {
    grid.innerHTML = '<div class="empty-state"><p>社团名录暂时无法加载，请通过本地服务器打开网站。</p></div>';
  }
}

function initClubFilters() {
  const typeFilter = document.querySelector("#club-type-filter");
  typeFilter.innerHTML = CLUB_TYPES.map((type) => `<button class="filter-button${type === "全部" ? " active" : ""}" type="button" data-club-type="${type}">${type}</button>`).join("");
  typeFilter.addEventListener("click", (event) => {
    const button = event.target.closest("[data-club-type]");
    if (!button) return;
    activeClubType = button.dataset.clubType;
    visibleClubCount = 12;
    typeFilter.querySelectorAll("button").forEach((item) => item.classList.toggle("active", item === button));
    renderClubs();
  });

  const units = [...new Set(clubs.map((club) => club.unit))].sort((a, b) => a.localeCompare(b, "zh-CN"));
  document.querySelector("#unit-filter").insertAdjacentHTML("beforeend", units.map((unit) => `<option value="${escapeHtml(unit)}">${escapeHtml(unit)}</option>`).join(""));
}

function getFilteredClubs() {
  const query = document.querySelector("#club-search").value.trim().toLowerCase();
  const unit = document.querySelector("#unit-filter").value;
  return clubs.filter((club) => {
    const matchesQuery = !query || `${club.name} ${club.unit} ${club.category ?? ""}`.toLowerCase().includes(query);
    const matchesUnit = unit === "all" || club.unit === unit;
    const matchesType = activeClubType === "全部" || club.type === activeClubType;
    return matchesQuery && matchesUnit && matchesType;
  });
}

function renderClubs() {
  const filtered = getFilteredClubs();
  const visible = filtered.slice(0, visibleClubCount);
  const grid = document.querySelector("#club-grid");
  grid.innerHTML = visible.length ? visible.map((club) => `
    <article class="club-card" data-type="${escapeHtml(club.type)}">
      <span class="club-tag">${escapeHtml(club.category || club.type)}</span>
      <h3>${escapeHtml(club.name)}</h3>
      <p>${escapeHtml(club.unit)}</p>
    </article>
  `).join("") : '<div class="empty-state"><i data-lucide="search-x"></i><p>没有找到匹配的社团，换个关键词试试。</p></div>';

  document.querySelector("#club-count").textContent = filtered.length;
  document.querySelector("#club-summary").textContent = `已显示 ${Math.min(visible.length, filtered.length)} / ${filtered.length} 个社团`;
  const loadMore = document.querySelector("#load-more-clubs");
  loadMore.hidden = visibleClubCount >= filtered.length;
  window.lucide?.createIcons();
  registerRevealItems(grid);
}

async function loadCompetitionCatalog() {
  const catalog = document.querySelector("#competition-catalog");
  if (!catalog) return;
  try {
    const response = await fetch("competitions.json");
    if (!response.ok) throw new Error("competition data unavailable");
    competitionCatalog = await response.json();
    initCompetitionFilters();
    renderCompetitionCatalog();
  } catch {
    catalog.innerHTML = '<div class="empty-state"><p>竞赛目录暂时无法加载，请通过本地服务器打开网站。</p></div>';
  }
}

function initCompetitionFilters() {
  const levels = ["全部", "A类", "B类", "B/C类", "C类", "C2类"];
  const levelFilter = document.querySelector("#competition-level-filter");
  levelFilter.innerHTML = levels.map((level) => {
    const count = level === "全部" ? competitionCatalog.length : competitionCatalog.filter((item) => item.level === level).length;
    return `<button class="filter-button${level === "全部" ? " active" : ""}" type="button" data-competition-level="${level}">${level}<span>${count}</span></button>`;
  }).join("");
  levelFilter.addEventListener("click", (event) => {
    const button = event.target.closest("[data-competition-level]");
    if (!button) return;
    activeCompetitionLevel = button.dataset.competitionLevel;
    visibleCompetitionCount = 20;
    levelFilter.querySelectorAll("button").forEach((item) => item.classList.toggle("active", item === button));
    renderCompetitionCatalog();
  });

  const departments = [...new Set(competitionCatalog.map((item) => item.department))].sort((a, b) => a.localeCompare(b, "zh-CN"));
  document.querySelector("#competition-department").insertAdjacentHTML("beforeend", departments.map((department) => `<option value="${escapeHtml(department)}">${escapeHtml(department)}</option>`).join(""));
}

function getFilteredCompetitions() {
  const query = document.querySelector("#competition-search").value.trim().toLowerCase();
  const department = document.querySelector("#competition-department").value;
  return competitionCatalog.filter((competition) => {
    const matchesQuery = !query || `${competition.name} ${competition.department}`.toLowerCase().includes(query);
    const matchesDepartment = department === "all" || competition.department === department;
    const matchesLevel = activeCompetitionLevel === "全部" || competition.level === activeCompetitionLevel;
    return matchesQuery && matchesDepartment && matchesLevel;
  });
}

function renderCompetitionCatalog() {
  const catalog = document.querySelector("#competition-catalog");
  if (!catalog) return;
  const filtered = getFilteredCompetitions();
  const visible = filtered.slice(0, visibleCompetitionCount);
  catalog.innerHTML = visible.length ? visible.map((competition) => {
    const rowNote = competition.levelDetail || (competition.id <= 88 ? competition.note : "");
    return `
      <article class="catalog-row" data-level="${escapeHtml(competition.level)}">
        <span class="catalog-id">${String(competition.id).padStart(2, "0")}</span>
        <div class="catalog-name"><h3>${escapeHtml(competition.name)}</h3>${rowNote ? `<p><i data-lucide="info"></i>${escapeHtml(rowNote)}</p>` : ""}</div>
        <span class="level-badge">${escapeHtml(competition.level)}</span>
        <p class="catalog-department">${escapeHtml(competition.department)}</p>
      </article>
    `;
  }).join("") : '<div class="empty-state"><i data-lucide="search-x"></i><p>没有找到匹配的竞赛项目，换个关键词试试。</p></div>';

  document.querySelector("#competition-count").textContent = filtered.length;
  document.querySelector("#competition-summary").textContent = `已显示 ${Math.min(visible.length, filtered.length)} / ${filtered.length} 个项目`;
  const loadMore = document.querySelector("#load-more-competitions");
  loadMore.hidden = visibleCompetitionCount >= filtered.length;
  window.lucide?.createIcons();
  registerRevealItems(catalog);
}

async function loadMajorGuides() {
  const list = document.querySelector("#major-list");
  if (!list) return;
  try {
    const response = await fetch("majors.json");
    if (!response.ok) throw new Error("major guide data unavailable");
    majorGuides = await response.json();
    updateMajorStats();
    renderMajorGuides();
  } catch {
    list.innerHTML = '<div class="empty-state"><p>专业指南暂时无法加载，请通过本地服务器打开网站。</p></div>';
  }
}

function updateMajorStats() {
  const programs = majorGuides.flatMap((guide) => guide.programs);
  const sumSeats = (key) => programs.reduce((total, program) => total + (program[key] ?? 0), 0);
  document.querySelector("#major-college-total").textContent = majorGuides.length;
  document.querySelector("#major-program-total").textContent = programs.length;
  document.querySelector("#major-freshman-total").textContent = sumSeats("freshman");
  document.querySelector("#major-sophomore-total").textContent = sumSeats("sophomore");
}

function renderMajorGuides() {
  const list = document.querySelector("#major-list");
  if (!list) return;
  const query = document.querySelector("#major-search").value.trim().toLowerCase();
  const filtered = majorGuides.filter((guide) => {
    const text = `${guide.college} ${guide.requirements} ${guide.programs.map((program) => program.name).join(" ")}`.toLowerCase();
    return !query || text.includes(query);
  });

  const seat = (value) => value === null ? "-" : String(value);
  list.innerHTML = filtered.map((guide) => {
    const pdfHref = `专业指导/${encodeURIComponent(guide.file)}`;
    return `
      <article class="major-card">
        <header class="major-card-header">
          <div><span class="major-index">${String(guide.id).padStart(2, "0")}</span><h2>${escapeHtml(guide.college)}</h2></div>
          <div class="major-score" aria-label="考核计分构成">
            <span>学业表现<strong>${guide.weights.academic}%</strong></span>
            <span>笔试<strong>${guide.weights.written}%</strong></span>
            <span>面试<strong>${guide.weights.interview}%</strong></span>
          </div>
        </header>
        <p class="major-requirement"><i data-lucide="check-circle-2"></i>${escapeHtml(guide.requirements)}</p>
        ${guide.note ? `<p class="major-special"><i data-lucide="badge-alert"></i>${escapeHtml(guide.note)}</p>` : ""}
        <div class="major-programs">
          <div class="major-program-header"><span>接收专业</span><span>大二计划</span><span>大一计划</span></div>
          ${guide.programs.map((program) => `<div class="major-program-row"><strong>${escapeHtml(program.name)}</strong><span>${seat(program.sophomore)}</span><span>${seat(program.freshman)}</span></div>`).join("")}
        </div>
        <a class="major-pdf-link" href="${pdfHref}" target="_blank" rel="noreferrer"><i data-lucide="file-text"></i>查看学院完整细则<i data-lucide="arrow-up-right"></i></a>
      </article>
    `;
  }).join("");

  document.querySelector("#major-count").textContent = filtered.length;
  document.querySelector("#major-empty").hidden = filtered.length !== 0;
  window.lucide?.createIcons();
  registerRevealItems(list);
}

function filterUndergraduateCatalog() {
  const input = document.querySelector("#undergraduate-search");
  const list = document.querySelector("#college-major-list");
  if (!input || !list) return;
  const query = input.value.trim().toLowerCase();
  const items = [...list.querySelectorAll(".college-major-item")];
  let visibleCount = 0;

  items.forEach((item) => {
    const matches = !query || item.dataset.search.toLowerCase().includes(query);
    item.hidden = !matches;
    if (matches) visibleCount += 1;
  });

  document.querySelector("#undergraduate-count").textContent = visibleCount;
  document.querySelector("#undergraduate-empty").hidden = visibleCount !== 0;
}

function updateRunStatus() {
  const statusElement = document.querySelector("#run-status");
  const progressElement = document.querySelector("#timeline-progress");
  if (!statusElement || !progressElement) return;
  const now = new Date();
  const minutes = now.getHours() * 60 + now.getMinutes();
  const start = 6 * 60 + 30;
  const end = 7 * 60 + 10;
  const progress = Math.max(0, Math.min(100, ((minutes - start) / (end - start)) * 100));
  progressElement.style.width = `${progress}%`;
  const status = minutes < start
    ? "今日晨跑尚未开始 · 06:30 开放"
    : minutes <= end
      ? `晨跑时段进行中 · 距结束约 ${end - minutes} 分钟`
      : "今日晨跑时段已结束 · 明早 06:30 开放";
  statusElement.textContent = status;
}

function initGallery() {
  const dialog = document.querySelector("#gallery-dialog");
  const items = [...document.querySelectorAll(".gallery-item")];
  if (!dialog || !items.length || typeof dialog.showModal !== "function") return;

  const dialogImage = dialog.querySelector("#gallery-dialog-image");
  const dialogCaption = dialog.querySelector("#gallery-dialog-caption");
  let activeIndex = 0;

  const showImage = (index) => {
    activeIndex = (index + items.length) % items.length;
    const item = items[activeIndex];
    const image = item.querySelector("img");
    const title = item.querySelector("strong")?.textContent ?? image.alt;
    const detail = item.querySelector("small")?.textContent;
    dialogImage.src = image.currentSrc || image.src;
    dialogImage.alt = image.alt;
    dialogCaption.textContent = detail ? `${title} · ${detail}` : title;
  };

  items.forEach((item, index) => {
    item.addEventListener("click", () => {
      showImage(index);
      dialog.showModal();
    });
  });

  dialog.querySelector("[data-gallery-close]").addEventListener("click", () => dialog.close());
  dialog.querySelector("[data-gallery-prev]").addEventListener("click", () => showImage(activeIndex - 1));
  dialog.querySelector("[data-gallery-next]").addEventListener("click", () => showImage(activeIndex + 1));
  dialog.addEventListener("click", (event) => {
    if (event.target === dialog) dialog.close();
  });
  dialog.addEventListener("keydown", (event) => {
    if (event.key === "ArrowLeft") showImage(activeIndex - 1);
    if (event.key === "ArrowRight") showImage(activeIndex + 1);
  });
}

document.addEventListener("DOMContentLoaded", () => {
  window.lucide?.createIcons();
  initSiteGuide();
  initNavigation();
  initMotion();
  initDormSearch();
  initCampusImages();
  updateRunStatus();
  initGallery();
  loadClubs();
  loadCompetitionCatalog();
  loadMajorGuides();

  document.querySelector("#club-search")?.addEventListener("input", () => { visibleClubCount = 12; renderClubs(); });
  document.querySelector("#unit-filter")?.addEventListener("change", () => { visibleClubCount = 12; renderClubs(); });
  document.querySelector("#load-more-clubs")?.addEventListener("click", () => { visibleClubCount += 12; renderClubs(); });
  document.querySelector("#competition-search")?.addEventListener("input", () => { visibleCompetitionCount = 20; renderCompetitionCatalog(); });
  document.querySelector("#competition-department")?.addEventListener("change", () => { visibleCompetitionCount = 20; renderCompetitionCatalog(); });
  document.querySelector("#load-more-competitions")?.addEventListener("click", () => { visibleCompetitionCount += 20; renderCompetitionCatalog(); });
  document.querySelector("#major-search")?.addEventListener("input", renderMajorGuides);
  document.querySelector("#undergraduate-search")?.addEventListener("input", filterUndergraduateCatalog);
});
