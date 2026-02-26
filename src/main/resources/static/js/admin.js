// Admin UI behaviors: sidebar toggle, upload handling, table filtering

document.addEventListener("DOMContentLoaded", function () {
    // Read CSRF token & header name from meta tags (Spring Security)
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    const sidebar = document.getElementById("sidebar");
    const sidebarToggle = document.getElementById("sidebarToggle");

    // Sidebar toggle for mobile
    if (sidebar && sidebarToggle) {
        sidebarToggle.addEventListener("click", function () {
            sidebar.classList.toggle("sidebar-open");
        });
    }

    // Candidate upload form (calls REST /api/v1/candidates/upload)
    const uploadForm = document.getElementById("candidateUploadForm");
    const uploadButton = document.getElementById("uploadButton");
    const uploadAlertContainer = document.getElementById("uploadAlertContainer");
    const candidateCountBadge = document.getElementById("candidateCountBadge");
    const candidatesTableBody = document.getElementById("candidatesTableBody");

    function showAlert(type, message) {
        if (!uploadAlertContainer) return;
        uploadAlertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
    }

    function renderCandidatesTable(rows) {
        if (!candidatesTableBody) return;

        if (!rows || rows.length === 0) {
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
            return;
        }

        const html = rows.map(c => `
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

        candidatesTableBody.innerHTML = html;

        if (candidateCountBadge) {
            candidateCountBadge.textContent = `${rows.length} records`;
        }
    }

    async function loadCandidatesFromBackend() {
        const searchApplicationNo = document.getElementById("searchApplicationNo");
        const searchName = document.getElementById("searchName");
        const searchMobile = document.getElementById("searchMobile"); // may not exist yet

        const params = new URLSearchParams();
        const appVal = searchApplicationNo?.value.trim();
        const nameVal = searchName?.value.trim();
        const mobileInput = document.getElementById("searchMobile");
        const mobileVal = mobileInput && mobileInput.value ? mobileInput.value.trim() : "";

        if (appVal) params.append("applicationNo", appVal);
        if (nameVal) params.append("name", nameVal);
        if (mobileVal) params.append("mobileNo", mobileVal);

        params.append("limit", "200");

        const url = "/api/v1/candidates" + (params.toString() ? ("?" + params.toString()) : "");

        try {
            const response = await fetch(url, { method: "GET" });
            if (!response.ok) {
                throw new Error("Failed to load candidates.");
            }
            const data = await response.json();
            renderCandidatesTable(data);
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

            // Build headers with CSRF (if available) so Spring Security accepts the POST
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

                    // Expecting ExcelUploadResponse JSON from backend
                    const success = data && data.success;
                    const message = (data && data.message) || "File uploaded successfully.";
                    const savedCount = data && typeof data.savedCount === "number" ? data.savedCount : 0;
                    const errorCount = data && typeof data.errorCount === "number" ? data.errorCount : 0;

                    showAlert(success ? "success" : "warning", message);

                    if (candidateCountBadge) {
                        candidateCountBadge.textContent = `${savedCount} records (errors: ${errorCount})`;
                    }

                    // Reload candidates table after successful upload
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

    // Table search/filter for uploaded candidates
    const searchApplicationNo = document.getElementById("searchApplicationNo");
    const searchName = document.getElementById("searchName");
    const searchTokenNo = document.getElementById("searchTokenNo");
    const searchMobile = document.getElementById("searchMobile");

    const triggerSearch = () => {
        loadCandidatesFromBackend();
    };

    [searchApplicationNo, searchName, searchTokenNo, searchMobile].forEach(input => {
        if (input) {
            input.addEventListener("input", triggerSearch);
        }
    });

    // Initial load of recent candidates when page opens
    if (candidatesTableBody) {
        loadCandidatesFromBackend();
    }
});

