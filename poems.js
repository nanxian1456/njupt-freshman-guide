document.addEventListener("DOMContentLoaded", () => {
  const pages = [...document.querySelectorAll("[data-poem-page]")];
  const pagination = document.querySelector("#poem-pagination");
  const previousButton = document.querySelector("#poem-prev");
  const nextButton = document.querySelector("#poem-next");
  const currentLabel = document.querySelector("#poem-current");
  const totalLabel = document.querySelector("#poem-total");
  let activeIndex = 0;
  let touchStartX = 0;

  const formatPage = (value) => String(value).padStart(2, "0");
  const renderPage = (index) => {
    activeIndex = Math.max(0, Math.min(index, pages.length - 1));
    pages.forEach((page, pageIndex) => {
      const isActive = pageIndex === activeIndex;
      page.classList.toggle("is-active", isActive);
      page.setAttribute("aria-hidden", String(!isActive));
    });
    currentLabel.textContent = formatPage(activeIndex + 1);
    previousButton.disabled = activeIndex === 0;
    nextButton.disabled = activeIndex === pages.length - 1;
  };

  totalLabel.textContent = formatPage(pages.length);
  pagination.hidden = pages.length <= 1;
  previousButton.addEventListener("click", () => renderPage(activeIndex - 1));
  nextButton.addEventListener("click", () => renderPage(activeIndex + 1));
  document.addEventListener("keydown", (event) => {
    if (event.key === "ArrowLeft") renderPage(activeIndex - 1);
    if (event.key === "ArrowRight") renderPage(activeIndex + 1);
  });
  document.querySelector(".poem-book").addEventListener("touchstart", (event) => {
    touchStartX = event.changedTouches[0].clientX;
  }, { passive: true });
  document.querySelector(".poem-book").addEventListener("touchend", (event) => {
    const distance = event.changedTouches[0].clientX - touchStartX;
    if (Math.abs(distance) < 48) return;
    renderPage(activeIndex + (distance < 0 ? 1 : -1));
  }, { passive: true });

  renderPage(0);
  window.lucide?.createIcons();
});
