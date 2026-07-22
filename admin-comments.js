const adminForm = document.querySelector("#admin-token-form");
const adminTokenInput = document.querySelector("#admin-token");
const adminStatus = document.querySelector("#admin-status");
const adminList = document.querySelector("#admin-comment-list");
let adminToken = "";

function adminElement(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function setAdminStatus(message, state = "") {
  adminStatus.textContent = message;
  adminStatus.dataset.state = state;
}

async function adminRequest(url, options = {}) {
  const response = await fetch(url, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json", "X-Admin-Token": adminToken },
    ...options,
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(payload.message || "审核接口请求失败");
  return payload;
}

function renderAdminComment(comment) {
  const article = adminElement("article", "admin-comment-card");
  const content = adminElement("div");
  const meta = adminElement("div", "admin-comment-meta");
  meta.append(
    adminElement("strong", "", comment.targetName),
    adminElement("span", "", comment.nickname),
    adminElement("time", "", new Date(comment.createdAt).toLocaleString("zh-CN")),
    adminElement("span", "", `#${comment.id}`),
  );
  content.append(meta, adminElement("p", "", comment.content));

  const actions = adminElement("div", "admin-comment-actions");
  [
    ["APPROVE", "通过"],
    ["REJECT", "驳回"],
  ].forEach(([action, label]) => {
    const button = adminElement("button", "", label);
    button.type = "button";
    button.dataset.action = action;
    button.addEventListener("click", async () => {
      actions.querySelectorAll("button").forEach((item) => { item.disabled = true; });
      try {
        await adminRequest(`/api/admin/comments/${comment.id}`, {
          method: "PATCH",
          body: JSON.stringify({ action }),
        });
        article.remove();
        if (!adminList.children.length) setAdminStatus("当前没有待审核评价。", "success");
      } catch (error) {
        setAdminStatus(error.message, "error");
        actions.querySelectorAll("button").forEach((item) => { item.disabled = false; });
      }
    });
    actions.append(button);
  });
  article.append(content, actions);
  adminList.append(article);
}

async function loadPendingComments() {
  adminList.replaceChildren();
  setAdminStatus("正在读取待审核评价...");
  try {
    const result = await adminRequest("/api/admin/comments?page=0");
    result.comments.forEach(renderAdminComment);
    setAdminStatus(result.comments.length ? `待审核 ${result.comments.length} 条。` : "当前没有待审核评价。", "success");
  } catch (error) {
    setAdminStatus(error.message, "error");
  }
}

adminForm.addEventListener("submit", (event) => {
  event.preventDefault();
  if (!adminForm.reportValidity()) return;
  adminToken = adminTokenInput.value;
  loadPendingComments();
});
