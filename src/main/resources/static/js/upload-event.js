// Page-specific logic for upload-event.html

document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const uploadForm = document.getElementById("eventUploadForm");
    const uploadButton = document.getElementById("eventUploadButton");
    const alertContainer = document.getElementById("eventUploadAlertContainer");
    const countBadge = document.getElementById("eventCountBadge");
    const eventsTableBody = document.getElementById("eventsTableBody");
    const searchRunningNumber = document.getElementById("searchRunningNumber");
    const paginationContainer = document.getElementById("eventsPagination");
    const pageInfo = document.getElementById("eventsPageInfo");

    const PAGE_SIZE = 10;
    let allEvents = [];
    let currentPage = 1;

    function showAlert(type, message) {
        if (!alertContainer) return;
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
    }

    function formatDateTime(value) {
        if (!value) return "";
        // Spring serializes LocalDateTime as "yyyy-MM-ddTHH:mm:ss"
        const str = String(value);
        if (str.includes("T")) {
            return str.replace("T", " ");
        }
        return str;
    }

    function formatEventName(evt) {
        if (!evt) return "";
        const name = evt.name != null ? evt.name : "";
        const unit = evt.unit != null ? evt.unit : "";
        if (!name && !unit) return "";
        return `${name} ${unit}`.trim();
    }

    function renderPage(page) {
        if (!eventsTableBody) return;

        const total = allEvents.length;
        const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

        currentPage = Math.min(Math.max(page, 1), totalPages);

        if (total === 0) {
            eventsTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center text-secondary py-4">
                        No event records found.
                    </td>
                </tr>
            `;
            if (countBadge) {
                countBadge.textContent = "0 records";
            }
            if (pageInfo) {
                pageInfo.textContent = "Showing 0 of 0";
            }
            if (paginationContainer) {
                paginationContainer.innerHTML = "";
            }
            return;
        }

        const startIndex = (currentPage - 1) * PAGE_SIZE;
        const endIndex = Math.min(startIndex + PAGE_SIZE, total);
        const pageItems = allEvents.slice(startIndex, endIndex);

        const rowsHtml = pageItems.map(e => `
            <tr>
                <td>${e.rfidNumber ?? ""}</td>
                <td>${e.runningNumber ?? ""}</td>
                <td>${formatDateTime(e.startTime)}</td>
                <td>${formatDateTime(e.endTime)}</td>
                <td>${e.timeDifference ?? ""}</td>
                <td>${e.marks ?? ""}</td>
                <td>${e.lap ?? 0}</td>
                <td>${formatEventName(e)}</td>
            </tr>
        `).join("");

        eventsTableBody.innerHTML = rowsHtml;

        if (countBadge) {
            countBadge.textContent = `${total} records`;
        }

        if (pageInfo) {
            pageInfo.textContent = `Showing ${startIndex + 1}–${endIndex} of ${total}`;
        }

        if (paginationContainer) {
            const totalPagesLimited = totalPages;

            let startPage = Math.max(1, currentPage - 2);
            let endPage = startPage + 4;
            if (endPage > totalPagesLimited) {
                endPage = totalPagesLimited;
                startPage = Math.max(1, endPage - 4);
            }

            let html = `
                <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                    <button class="page-link" data-page="${currentPage - 1}">Previous</button>
                </li>
            `;

            for (let p = startPage; p <= endPage; p++) {
                html += `
                    <li class="page-item ${p === currentPage ? 'active' : ''}">
                        <button class="page-link" data-page="${p}">${p}</button>
                    </li>
                `;
            }

            html += `
                <li class="page-item ${currentPage === totalPagesLimited ? 'disabled' : ''}">
                    <button class="page-link" data-page="${currentPage + 1}">Next</button>
                </li>
            `;

            paginationContainer.innerHTML = html;

            paginationContainer.querySelectorAll("button.page-link").forEach(btn => {
                btn.addEventListener("click", e => {
                    const targetPage = parseInt(e.currentTarget.getAttribute("data-page"), 10);
                    if (!isNaN(targetPage)) {
                        renderPage(targetPage);
                    }
                });
            });
        }
    }

    async function loadEvents() {
        const params = new URLSearchParams();
        const rnVal = searchRunningNumber?.value.trim();
        if (rnVal) {
            params.append("runningNumber", rnVal);
        }
        params.append("limit", "500");

        const url = "/api/v1/events" + (params.toString() ? ("?" + params.toString()) : "");

        try {
            const res = await fetch(url, { method: "GET" });
            if (!res.ok) {
                throw new Error("Failed to load event records.");
            }
            const data = await res.json();
            allEvents = Array.isArray(data) ? data : [];
            renderPage(1);
        } catch (e) {
            console.error("Error loading events:", e);
            showAlert("danger", "Error loading event records from server.");
        }
    }

    if (uploadForm) {
        uploadForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const eventKeyEl = document.getElementById("eventKey");
            const lapEl = document.getElementById("lap");
            const fileInput = document.getElementById("eventFile");

            if (!eventKeyEl || !eventKeyEl.value) {
                showAlert("warning", "Please select an event.");
                return;
            }

            if (!lapEl || lapEl.value === "") {
                showAlert("warning", "Please enter lap value (0 for non-lap events).");
                return;
            }

            if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                showAlert("warning", "Please select an Excel (.xls / .xlsx) file to upload.");
                return;
            }

            const file = fileInput.files[0];

            const lowerName = file.name.toLowerCase();
            if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx")) {
                showAlert("danger", "Invalid file type. Only .xls or .xlsx files are allowed.");
                return;
            }

            const formData = new FormData();
            formData.append("file", file);
            formData.append("eventKey", eventKeyEl.value);
            formData.append("lap", lapEl.value);

            const headers = {};
            if (csrfToken && csrfHeaderName) {
                headers[csrfHeaderName] = csrfToken;
            }

            if (uploadButton) {
                uploadButton.disabled = true;
                uploadButton.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Uploading...';
            }

            fetch("/api/v1/events/upload", {
                method: "POST",
                headers: headers,
                body: formData
            })
                .then(async (response) => {
                    const contentType = response.headers.get("content-type") || "";
                    let data = null;
                    if (contentType.includes("application/json")) {
                        data = await response.json();
                    }

                    if (!response.ok) {
                        const msg = (data && data.message) || "Upload failed. Please check the file and try again.";
                        throw new Error(msg);
                    }

                    const success = data && data.success;
                    const message = (data && data.message) || "File uploaded successfully.";
                    const savedCount = data && typeof data.savedCount === "number" ? data.savedCount : 0;
                    const errorCount = data && typeof data.errorCount === "number" ? data.errorCount : 0;

                    showAlert(success ? "success" : "warning", message);

                    if (countBadge) {
                        countBadge.textContent = `${savedCount} records (errors: ${errorCount})`;
                    }

                    if (success) {
                        loadEvents();
                    }
                })
                .catch((error) => {
                    console.error("Upload error:", error);
                    showAlert("danger", error.message || "Unexpected error while uploading file.");
                })
                .finally(() => {
                    if (uploadButton) {
                        uploadButton.disabled = false;
                        uploadButton.innerHTML = '<i class="bi bi-upload me-1"></i> Upload';
                    }
                });
        });
    }

    if (searchRunningNumber) {
        searchRunningNumber.addEventListener("input", () => loadEvents());
    }

    if (eventsTableBody) {
        loadEvents();
    }
});

