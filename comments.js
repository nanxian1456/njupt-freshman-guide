const COMMENT_API = "/api/comments";

function getCommentClientId() {
  const storageKey = "xszn-comment-session";
  try {
    const existing = sessionStorage.getItem(storageKey);
    if (existing) return existing;
    const bytes = new Uint8Array(18);
    crypto.getRandomValues(bytes);
    const value = btoa(String.fromCharCode(...bytes)).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
    sessionStorage.setItem(storageKey, value);
    return value;
  } catch {
    return `session-${crypto.randomUUID().replaceAll("-", "")}`;
  }
}

const COMMENT_CLIENT_ID = getCommentClientId();

function commentElement(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

async function commentRequest(url, options = {}) {
  const response = await fetch(url, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json", "X-Comment-Client": COMMENT_CLIENT_ID, ...(options.headers || {}) },
    ...options,
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(payload.message || "请求失败，请稍后重试");
  return payload;
}

function formatCommentTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit" }).format(date);
}

function renderComment(container, comment) {
  const article = commentElement("article", "community-comment");
  const header = commentElement("header");
  header.append(commentElement("strong", "", comment.nickname), commentElement("time", "", formatCommentTime(comment.createdAt)));
  article.append(header, commentElement("p", "", comment.content));
  container.append(article);
}

function replaceTargetOptions(select, targets) {
  if (!Array.isArray(targets) || !targets.length) return;
  const selectedKey = select.value;
  const groups = new Map();
  targets.forEach((target) => {
    const groupName = target.groupName || "其他";
    if (!groups.has(groupName)) groups.set(groupName, []);
    groups.get(groupName).push(target);
  });
  select.replaceChildren();
  groups.forEach((items, groupName) => {
    const group = document.createElement("optgroup");
    group.label = groupName;
    items.forEach((target) => {
      const option = commentElement("option", "", target.name);
      option.value = target.targetKey;
      group.append(option);
    });
    select.append(group);
  });
  if (targets.some((target) => target.targetKey === selectedKey)) {
    select.value = selectedKey;
  }
}

async function loadCommentTargets(targetType, select) {
  try {
    const result = await commentRequest(`/api/catalog?type=${encodeURIComponent(targetType)}`);
    replaceTargetOptions(select, result.targets);
  } catch {
    // The static options remain available when the catalog API is offline.
  }
}

function initCommentWidget(widget) {
  const targetType = widget.dataset.targetType;
  const targetSelect = widget.querySelector("[data-comment-target]");
  const list = widget.querySelector("[data-comment-list]");
  const moreButton = widget.querySelector("[data-comment-more]");
  const form = widget.querySelector("[data-comment-form]");
  const submitButton = form.querySelector("button[type='submit']");
  const status = form.querySelector("[data-comment-submit-status]");
  const contentInput = form.elements.content;
  const count = form.querySelector("[data-comment-count]");
  let formToken = "";
  let page = 0;

  const setState = (message, kind = "") => {
    status.textContent = message;
    status.dataset.state = kind;
  };

  const refreshToken = async (preserveMessage = false) => {
    submitButton.disabled = true;
    try {
      const result = await commentRequest(`${COMMENT_API}/form-token`);
      formToken = result.token;
      submitButton.disabled = false;
      if (!preserveMessage) setState("评价审核后公开，通常不会立即显示。", "ready");
    } catch {
      formToken = "";
      setState("评论服务暂时不可用，页面其他内容不受影响。", "error");
    }
  };

  const loadComments = async (reset = true) => {
    if (reset) {
      page = 0;
      list.replaceChildren(commentElement("p", "comment-state", "正在加载评价..."));
    }
    moreButton.disabled = true;
    try {
      const query = new URLSearchParams({ targetType, targetKey: targetSelect.value, page: String(page) });
      const result = await commentRequest(`${COMMENT_API}?${query}`);
      if (reset) list.replaceChildren();
      result.comments.forEach((comment) => renderComment(list, comment));
      if (!list.children.length) list.append(commentElement("p", "comment-state", "暂时还没有公开评价。"));
      moreButton.hidden = !result.hasNext;
      moreButton.disabled = false;
    } catch {
      list.replaceChildren(commentElement("p", "comment-state comment-state-error", "评论暂时无法加载，宿舍和美食信息仍可正常浏览。"));
      moreButton.hidden = true;
    }
  };

  targetSelect.addEventListener("change", () => loadComments(true));
  moreButton.addEventListener("click", () => { page += 1; loadComments(false); });
  contentInput.addEventListener("input", () => { count.textContent = `${contentInput.value.length} / 300`; });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!form.reportValidity() || !formToken) return;
    submitButton.disabled = true;
    setState("正在安全提交...", "loading");
    try {
      const result = await commentRequest(COMMENT_API, {
        method: "POST",
        body: JSON.stringify({
          targetType,
          targetKey: targetSelect.value,
          nickname: form.elements.nickname.value,
          content: contentInput.value,
          formToken,
          website: form.elements.website.value,
        }),
      });
      form.reset();
      count.textContent = "0 / 300";
      setState(result.message, "success");
    } catch (error) {
      setState(error.message, "error");
    } finally {
      await refreshToken(true);
    }
  });

  loadCommentTargets(targetType, targetSelect).then(() => loadComments(true));
  refreshToken();
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-comment-widget]").forEach(initCommentWidget);
});
