const adminForm = document.querySelector("#admin-token-form");
const adminTokenInput = document.querySelector("#admin-token");
const adminStatus = document.querySelector("#admin-status");
const adminWorkspace = document.querySelector("#admin-workspace");
const adminPendingList = document.querySelector("#admin-pending-list");
const adminManagedList = document.querySelector("#admin-managed-list");
const adminCatalogList = document.querySelector("#admin-catalog-list");
const adminOperationList = document.querySelector("#admin-operation-list");
const confirmDialog = document.querySelector("#admin-confirm-dialog");
const confirmForm = document.querySelector("#admin-confirm-form");
const confirmTitle = document.querySelector("#admin-confirm-title");
const confirmMessage = document.querySelector("#admin-confirm-message");
const confirmInputWrap = document.querySelector("#admin-confirm-input-wrap");
const confirmInput = document.querySelector("#admin-confirm-input");
const confirmHint = document.querySelector("#admin-confirm-hint");
const confirmError = document.querySelector("#admin-confirm-error");
const confirmSubmit = document.querySelector("#admin-confirm-submit");
const confirmCancel = document.querySelector("#admin-confirm-cancel");

let adminToken = "";
let activeView = "pending";
let pendingConfirmation = null;

const actionLabels = {
  COMMENT_APPROVE: "通过评价",
  COMMENT_REJECT: "驳回评价",
  COMMENT_WITHDRAW: "撤回评价",
  COMMENT_REPUBLISH: "重新发布",
  COMMENT_DELETE: "永久删除",
  CATALOG_UPDATE: "修改目录",
};

function adminElement(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function iconElement(name) {
  const icon = adminElement("i");
  icon.dataset.lucide = name;
  return icon;
}

function refreshIcons() {
  window.lucide?.createIcons();
}

function setAdminStatus(message, state = "") {
  adminStatus.textContent = message;
  adminStatus.dataset.state = state;
}

function formatAdminTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleString("zh-CN", { hour12: false });
}

async function adminRequest(url, options = {}) {
  const response = await fetch(url, {
    credentials: "same-origin",
    ...options,
    headers: {
      "Content-Type": "application/json",
      "X-Admin-Token": adminToken,
      ...(options.headers || {}),
    },
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(payload.message || "管理接口请求失败");
  return payload;
}

function renderEmpty(container, message) {
  container.replaceChildren(adminElement("p", "admin-empty-state", message));
}

function setActionButtonsBusy(container, busy) {
  container.querySelectorAll("button").forEach((button) => { button.disabled = busy; });
}

function openConfirmation({ title, message, phrase = "", label, danger = false, action }) {
  pendingConfirmation = action;
  confirmTitle.textContent = title;
  confirmMessage.textContent = message;
  confirmInput.value = "";
  confirmError.textContent = "";
  confirmInputWrap.hidden = !phrase;
  confirmInput.required = Boolean(phrase);
  confirmInput.dataset.phrase = phrase;
  confirmHint.textContent = phrase ? `请输入：${phrase}` : "";
  confirmSubmit.textContent = label;
  confirmSubmit.classList.toggle("danger", danger);
  confirmSubmit.disabled = Boolean(phrase);
  if (typeof confirmDialog.showModal === "function") {
    confirmDialog.showModal();
  } else {
    confirmDialog.setAttribute("open", "");
    document.body.classList.add("admin-dialog-open");
  }
  if (phrase) confirmInput.focus();
  refreshIcons();
}

function closeConfirmation() {
  pendingConfirmation = null;
  if (typeof confirmDialog.close === "function") {
    confirmDialog.close();
  } else {
    confirmDialog.removeAttribute("open");
    document.body.classList.remove("admin-dialog-open");
  }
}

confirmInput.addEventListener("input", () => {
  confirmSubmit.disabled = confirmInput.value.trim() !== confirmInput.dataset.phrase;
});

confirmCancel.addEventListener("click", closeConfirmation);
confirmDialog.addEventListener("close", () => { pendingConfirmation = null; });

confirmForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!pendingConfirmation || confirmSubmit.disabled) return;
  confirmSubmit.disabled = true;
  confirmCancel.disabled = true;
  confirmError.textContent = "正在执行...";
  try {
    await pendingConfirmation();
    closeConfirmation();
  } catch (error) {
    confirmError.textContent = error.message;
    confirmSubmit.disabled = false;
  } finally {
    confirmCancel.disabled = false;
  }
});

function createActionButton(comment, action, label, iconName, danger = false) {
  const button = adminElement("button", danger ? "danger" : "");
  button.type = "button";
  button.dataset.action = action;
  button.append(iconElement(iconName), adminElement("span", "", label));
  button.addEventListener("click", () => {
    const isDelete = action === "DELETE";
    const phrase = isDelete ? `永久删除 #${comment.id}` : "";
    const messages = {
      APPROVE: `确认发布 #${comment.id} 这条评价？`,
      REJECT: `确认驳回 #${comment.id} 这条评价？`,
      WITHDRAW: `撤回后评价将立即从公开页面消失。确认撤回 #${comment.id}？`,
      REPUBLISH: `确认重新发布 #${comment.id} 这条评价？`,
      DELETE: `永久删除后无法恢复，操作记录仍会保留。`,
    };
    openConfirmation({
      title: `${label} · #${comment.id}`,
      message: messages[action],
      phrase,
      label,
      danger: action === "REJECT" || action === "WITHDRAW" || isDelete,
      action: async () => {
        if (isDelete) {
          await adminRequest(`/api/admin/comments/${comment.id}`, {
            method: "DELETE",
            headers: { "X-Confirm-Comment-Id": String(comment.id) },
          });
        } else {
          const confirmation = action === "WITHDRAW" || action === "REPUBLISH"
            ? `${action}:${comment.id}`
            : null;
          await adminRequest(`/api/admin/comments/${comment.id}`, {
            method: "PATCH",
            body: JSON.stringify({ action, confirmation }),
          });
        }
        setAdminStatus(`${label}成功。`, "success");
        await loadActiveView();
      },
    });
  });
  return button;
}

function renderAdminComment(container, comment, view) {
  const article = adminElement("article", "admin-comment-card");
  const content = adminElement("div", "admin-comment-content");
  const meta = adminElement("div", "admin-comment-meta");
  meta.append(
    adminElement("strong", "", comment.targetName),
    adminElement("span", "", comment.nickname),
    adminElement("time", "", formatAdminTime(comment.createdAt)),
    adminElement("span", "", `#${comment.id}`),
  );
  if (view === "managed") {
    meta.append(adminElement(
      "span",
      `admin-status-badge ${comment.status === "APPROVED" ? "published" : "withdrawn"}`,
      comment.status === "APPROVED" ? "公开中" : "已撤回",
    ));
  }
  content.append(meta, adminElement("p", "", comment.content));

  const actions = adminElement("div", "admin-comment-actions");
  if (view === "pending") {
    actions.append(
      createActionButton(comment, "APPROVE", "通过", "check"),
      createActionButton(comment, "REJECT", "驳回", "x", true),
    );
  } else if (comment.status === "APPROVED") {
    actions.append(
      createActionButton(comment, "WITHDRAW", "撤回", "eye-off", true),
      createActionButton(comment, "DELETE", "永久删除", "trash-2", true),
    );
  } else {
    actions.append(
      createActionButton(comment, "REPUBLISH", "重新发布", "rotate-ccw"),
      createActionButton(comment, "DELETE", "永久删除", "trash-2", true),
    );
  }
  article.append(content, actions);
  container.append(article);
}

async function loadCommentsView(view) {
  const container = view === "pending" ? adminPendingList : adminManagedList;
  const endpoint = view === "pending" ? "/api/admin/comments?page=0" : "/api/admin/comments/managed?page=0";
  container.replaceChildren();
  setAdminStatus("正在读取评价...");
  const result = await adminRequest(endpoint);
  result.comments.forEach((comment) => renderAdminComment(container, comment, view));
  if (!result.comments.length) {
    renderEmpty(container, view === "pending" ? "当前没有待审核评价。" : "当前没有已发布或已撤回评价。");
  }
  setAdminStatus(`已读取 ${result.comments.length} 条评价。`, "success");
  refreshIcons();
}

function catalogTypeName(type) {
  return type === "DORM" ? "宿舍目录" : "食堂目录";
}

function renderCatalogEntry(container, entry) {
  const form = adminElement("form", "admin-catalog-card");
  form.dataset.type = entry.targetType;
  form.dataset.key = entry.targetKey;
  const heading = adminElement("header");
  const title = adminElement("div");
  title.append(
    adminElement("strong", "", entry.name),
    adminElement("code", "", entry.targetKey),
  );
  heading.append(title, adminElement("span", "admin-catalog-type", catalogTypeName(entry.targetType)));

  const fields = adminElement("div", "admin-catalog-fields");
  const nameLabel = adminElement("label");
  const nameInput = adminElement("input");
  nameInput.name = "name";
  nameInput.value = entry.name;
  nameInput.maxLength = 40;
  nameInput.required = true;
  nameLabel.append(adminElement("span", "", "显示名称"), nameInput);

  const groupLabel = adminElement("label");
  const groupInput = adminElement("input");
  groupInput.name = "groupName";
  groupInput.value = entry.groupName;
  groupInput.maxLength = 40;
  groupInput.required = true;
  groupLabel.append(adminElement("span", "", "所属分组"), groupInput);

  const sortLabel = adminElement("label");
  const sortInput = adminElement("input");
  sortInput.name = "sortOrder";
  sortInput.type = "number";
  sortInput.min = "0";
  sortInput.max = "1000";
  sortInput.value = String(entry.sortOrder);
  sortInput.required = true;
  sortLabel.append(adminElement("span", "", "排序"), sortInput);
  fields.append(nameLabel, groupLabel, sortLabel);

  const footer = adminElement("footer");
  const enabledLabel = adminElement("label", "admin-catalog-enabled");
  const enabledInput = adminElement("input");
  enabledInput.type = "checkbox";
  enabledInput.name = "enabled";
  enabledInput.checked = entry.enabled;
  enabledLabel.append(enabledInput, adminElement("span", "", "公开启用"));
  const saveButton = adminElement("button");
  saveButton.type = "submit";
  saveButton.append(iconElement("save"), adminElement("span", "", "保存"));
  footer.append(enabledLabel, saveButton);
  form.append(heading, fields, footer);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!form.reportValidity()) return;
    setActionButtonsBusy(form, true);
    try {
      await adminRequest(`/api/admin/catalog/${entry.targetType}/${entry.targetKey}`, {
        method: "PATCH",
        body: JSON.stringify({
          name: nameInput.value,
          groupName: groupInput.value,
          sortOrder: Number(sortInput.value),
          enabled: enabledInput.checked,
        }),
      });
      setAdminStatus(`${entry.targetKey} 已保存。`, "success");
      await loadCatalog();
    } catch (error) {
      setAdminStatus(error.message, "error");
      setActionButtonsBusy(form, false);
    }
  });
  container.append(form);
}

async function loadCatalog() {
  adminCatalogList.replaceChildren();
  setAdminStatus("正在读取目录...");
  const result = await adminRequest("/api/admin/catalog");
  result.targets.forEach((entry) => renderCatalogEntry(adminCatalogList, entry));
  if (!result.targets.length) renderEmpty(adminCatalogList, "目录中没有可管理项目。");
  setAdminStatus(`已读取 ${result.targets.length} 个目录项目。`, "success");
  refreshIcons();
}

async function loadOperations() {
  adminOperationList.replaceChildren();
  setAdminStatus("正在读取操作记录...");
  const result = await adminRequest("/api/admin/operations?page=0");
  result.operations.forEach((operation) => {
    const article = adminElement("article", "admin-operation-card");
    const meta = adminElement("div", "admin-operation-meta");
    meta.append(
      adminElement("strong", "", actionLabels[operation.action] || operation.action),
      adminElement("span", "", operation.entityType),
      adminElement("code", "", operation.entityKey),
      adminElement("time", "", formatAdminTime(operation.createdAt)),
    );
    article.append(meta, adminElement("p", "", operation.summary));
    adminOperationList.append(article);
  });
  if (!result.operations.length) renderEmpty(adminOperationList, "当前没有管理员操作记录。");
  setAdminStatus(`已读取 ${result.operations.length} 条操作记录。`, "success");
}

async function loadActiveView() {
  try {
    if (activeView === "pending" || activeView === "managed") {
      await loadCommentsView(activeView);
    } else if (activeView === "catalog") {
      await loadCatalog();
    } else {
      await loadOperations();
    }
  } catch (error) {
    setAdminStatus(error.message, "error");
    const container = {
      pending: adminPendingList,
      managed: adminManagedList,
      catalog: adminCatalogList,
      operations: adminOperationList,
    }[activeView];
    renderEmpty(container, "数据读取失败，请检查管理员令牌或服务状态。");
  }
}

document.querySelectorAll("[data-admin-view]").forEach((button) => {
  button.addEventListener("click", async () => {
    activeView = button.dataset.adminView;
    document.querySelectorAll("[data-admin-view]").forEach((item) => {
      item.classList.toggle("active", item === button);
    });
    document.querySelectorAll("[data-admin-panel]").forEach((panel) => {
      const active = panel.dataset.adminPanel === activeView;
      panel.hidden = !active;
      panel.classList.toggle("active", active);
    });
    await loadActiveView();
  });
});

document.querySelectorAll("[data-admin-refresh]").forEach((button) => {
  button.addEventListener("click", loadActiveView);
});

adminForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!adminForm.reportValidity()) return;
  adminToken = adminTokenInput.value;
  adminWorkspace.hidden = false;
  await loadActiveView();
});

refreshIcons();
