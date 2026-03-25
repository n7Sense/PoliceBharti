// Page-specific logic for upload-candidate.html

document.addEventListener("DOMContentLoaded", function () {
    // CSRF from meta tags
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const uploadForm = document.getElementById("candidateUploadForm");
    const uploadButton = document.getElementById("uploadButton");
    const uploadAlertContainer = document.getElementById("uploadAlertContainer");
    const candidateCountBadge = document.getElementById("candidateCountBadge");
    const candidatesTableBody = document.getElementById("candidatesTableBody");

    const searchApplicationNo = document.getElementById("searchApplicationNo");
    const searchName = document.getElementById("searchName");
    const searchMobile = document.getElementById("searchMobile");

    const paginationContainer = document.getElementById("paginationContainer");
    const pageInfo = document.getElementById("pageInfo");

    const PAGE_SIZE = 10;
    let allCandidates = [];
    let currentPage = 1;

    function showAlert(type, message) {
        if (!uploadAlertContainer) return;
        uploadAlertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
    }

    function renderPage(page) {
        if (!candidatesTableBody) return;

        const total = allCandidates.length;
        const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

        currentPage = Math.min(Math.max(page, 1), totalPages);

        if (total === 0) {
            candidatesTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-secondary py-4">
                        No candidates found. Upload a file or change search criteria.
                    </td>
                </tr>
            `;
            if (candidateCountBadge) {
                candidateCountBadge.textContent = "0 records";
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
        const pageItems = allCandidates.slice(startIndex, endIndex);

        const rowsHtml = pageItems.map(c => `
            <tr>
                <td class="col-app-no">${c.applicationNo ?? ""}</td>
                <td class="col-name">${c.name ?? ""}</td>
                <td class="col-mobile">${c.mobileNo ?? ""}</td>
                <td>${c.category ?? ""}</td>
                <td class="col-token-no">${c.tokenNo ?? ""}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-info" disabled>
                        <i class="bi bi-pencil-square me-1"></i>Edit
                    </button>
                </td>
            </tr>
        `).join("");

        candidatesTableBody.innerHTML = rowsHtml;

        if (candidateCountBadge) {
            candidateCountBadge.textContent = `${total} records`;
        }

        if (pageInfo) {
            pageInfo.textContent = `Showing ${startIndex + 1}–${endIndex} of ${total}`;
        }

        if (paginationContainer) {
            const maxButtons = 5;
            let startPage = Math.max(1, currentPage - 2);
            let endPage = startPage + maxButtons - 1;
            if (endPage > totalPages) {
                endPage = totalPages;
                startPage = Math.max(1, endPage - maxButtons + 1);
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
                <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
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

    async function loadCandidatesFromBackend() {
        const params = new URLSearchParams();
        const appVal = searchApplicationNo?.value.trim();
        const nameVal = searchName?.value.trim();
        const mobileVal = searchMobile && searchMobile.value ? searchMobile.value.trim() : "";

        if (appVal) params.append("applicationNo", appVal);
        if (nameVal) params.append("name", nameVal);
        if (mobileVal) params.append("mobileNo", mobileVal);

        params.append("limit", "100000"); // effectively "all" for this UI

        const url = "/api/v1/candidates" + (params.toString() ? ("?" + params.toString()) : "");

        try {
            const response = await fetch(url, { method: "GET" });
            if (!response.ok) {
                throw new Error("Failed to load candidates.");
            }
            const data = await response.json();
            allCandidates = Array.isArray(data) ? data : [];
            renderPage(1);
        } catch (e) {
            console.error("Error loading candidates:", e);
            showAlert("danger", "Error loading candidates from server.");
        }
    }

    if (uploadForm) {
        uploadForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const fileInput = document.getElementById("file");
            if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                showAlert("warning", "Please select an Excel (.xlsx) file to upload.");
                return;
            }

            const file = fileInput.files[0];
            if (!file.name.toLowerCase().endsWith(".xlsx")) {
                showAlert("danger", "Invalid file type. Only .xlsx files are allowed.");
                return;
            }

            const formData = new FormData();
            formData.append("file", file);

            if (uploadButton) {
                uploadButton.disabled = true;
                uploadButton.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Uploading...';
            }

            const headers = {};
            if (csrfToken && csrfHeaderName) {
                headers[csrfHeaderName] = csrfToken;
            }

            fetch("/api/v1/candidates/upload", {
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

                    if (candidateCountBadge) {
                        candidateCountBadge.textContent = `${savedCount} records (errors: ${errorCount})`;
                    }

                    if (success) {
                        loadCandidatesFromBackend();
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

    const triggerSearch = () => {
        loadCandidatesFromBackend();
    };

    [searchApplicationNo, searchName, searchMobile].forEach(input => {
        if (input) {
            input.addEventListener("input", triggerSearch);
        }
    });

    if (candidatesTableBody) {
        loadCandidatesFromBackend();
    }
});

