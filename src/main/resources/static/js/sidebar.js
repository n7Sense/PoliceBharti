document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.getElementById("sidebar");
    if (!sidebar) return;

    const brandToggle = document.querySelector(".navbar-brand");
    const links = sidebar.querySelectorAll(".nav-link");

    const LOCKED_CLASS = "sidebar-locked";
    const ICON_ONLY_CLASS = "sidebar-icons-only";
    const STORAGE_KEY = "ufrs.sidebar.locked";

    function setLockState(locked) {
        if (locked) {
            sidebar.classList.add(LOCKED_CLASS);
            sidebar.classList.remove(ICON_ONLY_CLASS);
            document.body.classList.remove("sidebar-icons-only-mode");
        } else {
            sidebar.classList.remove(LOCKED_CLASS);
            sidebar.classList.add(ICON_ONLY_CLASS);
            document.body.classList.add("sidebar-icons-only-mode");
        }
        localStorage.setItem(STORAGE_KEY, locked ? "true" : "false");
    }

    // Initial state from storage (default: locked = true => icons + labels)
    const saved = localStorage.getItem(STORAGE_KEY);
    const initiallyLocked = saved === null || saved === "true";
    setLockState(initiallyLocked);

    // Hover behavior: when unlocked (ICON_ONLY), expand labels on hover
    sidebar.addEventListener("mouseenter", () => {
        if (sidebar.classList.contains(ICON_ONLY_CLASS) &&
            !sidebar.classList.contains(LOCKED_CLASS)) {
            sidebar.classList.remove(ICON_ONLY_CLASS);
            document.body.classList.remove("sidebar-icons-only-mode");
        }
    });

    sidebar.addEventListener("mouseleave", () => {
        const locked = sidebar.classList.contains(LOCKED_CLASS);
        if (!locked) {
            sidebar.classList.add(ICON_ONLY_CLASS);
            document.body.classList.add("sidebar-icons-only-mode");
        }
    });

    // Use the "Police Bharti" brand as toggle
    if (brandToggle) {
        brandToggle.addEventListener("click", (e) => {
            e.preventDefault();
            const locked = sidebar.classList.contains(LOCKED_CLASS);
            // If currently locked => unlock (icon only)
            setLockState(!locked);
        });
    }

    // Minimal tooltip: use title attribute when collapsed
    links.forEach(link => {
        const label = link.querySelector(".sidebar-label");
        if (label && !link.getAttribute("title")) {
            link.setAttribute("title", label.textContent.trim());
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const sidebar = document.getElementById("sidebar");
    const toggleBtn = document.getElementById("sidebarToggle");
    const toggleIcon = document.getElementById("sidebarToggleIcon");
    const collapsedClass = "sidebar-collapsed";
    const storageKey = "ufrs.sidebar.collapsed";

    function applyState(collapsed) {
        if (collapsed) {
            sidebar.classList.add(collapsedClass);
            if (toggleIcon) {
                toggleIcon.classList.remove("bi-chevron-double-left");
                toggleIcon.classList.add("bi-chevron-double-right");
            }
        } else {
            sidebar.classList.remove(collapsedClass);
            if (toggleIcon) {
                toggleIcon.classList.remove("bi-chevron-double-right");
                toggleIcon.classList.add("bi-chevron-double-left");
            }
        }
    }

    const saved = localStorage.getItem(storageKey);
    if (saved === "true") {
        applyState(true);
    }

    if (toggleBtn) {
        toggleBtn.addEventListener("click", function () {
            const collapsed = !sidebar.classList.contains(collapsedClass);
            applyState(collapsed);
            localStorage.setItem(storageKey, String(collapsed));
        });
    }

    // Setup tooltips on nav links using their label text
    const links = sidebar.querySelectorAll(".nav-link");
    links.forEach(link => {
        const label = link.querySelector(".sidebar-label");
        if (label && !link.getAttribute("title")) {
            link.setAttribute("title", label.textContent.trim());
        }
    });
    if (window.bootstrap && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(sidebar.querySelectorAll(".nav-link[title]"));
        tooltipTriggerList.forEach(function (el) {
            new bootstrap.Tooltip(el);
        });
    }
});