document.addEventListener("DOMContentLoaded", () => {
    const alertContainer = document.getElementById("candidateMasterAlertContainer");
    const fromDateEl = document.getElementById("fromDate");
    const toDateEl = document.getElementById("toDate");
    const applyFiltersBtn = document.getElementById("applyFiltersBtn");
    const downloadExcelBtn = document.getElementById("downloadExcelBtn");
    const statusFlags = document.querySelectorAll(".status-flag");

    const tableBody = document.getElementById("candidateMasterTableBody");
    const countBadge = document.getElementById("candidateMasterCountBadge");
    const pageInfo = document.getElementById("candidateMasterPageInfo");
    const pagination = document.getElementById("candidateMasterPagination");
    const resultStatusHeader = document.querySelector("th.result-status-col");

    const PAGE_SIZE = 10;
    let currentPage = 1;
    let totalPages = 1;
    let totalElements = 0;

    function showAlert(type, message) {
        if (!alertContainer) return;
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>`;
    }

    function getSelectedStatusMap() {
        const map = {};
        statusFlags.forEach(cb => {
            const key = cb.getAttribute("data-status");
            const val = cb.getAttribute("data-value") === "true";
            if (!map[key]) map[key] = { approved: null };
            if (cb.checked) {
                map[key].approved = val;
            }
        });
        return map;
    }

    // behave like radio per status row
    statusFlags.forEach(cb => {
        cb.addEventListener("change", () => {
            const status = cb.getAttribute("data-status");
            const value = cb.getAttribute("data-value");
            statusFlags.forEach(other => {
                if (other === cb) return;
                if (other.getAttribute("data-status") === status &&
                    other.getAttribute("data-value") !== value) {
                    other.checked = false;
                }
            });
        });
    });

    function choosePrimaryStatusForTable() {
        const map = getSelectedStatusMap();
        const order = ["RESULT_STATUS", "ATTENDANCE", "DOCUMENTS", "PHYSICAL_TEST", "RUNNING_NO"];
        for (const key of order) {
            const entry = map[key];
            if (entry && entry.approved !== null) return { statusType: key, approved: entry.approved };
        }
        return { statusType: null, approved: null };
    }

    async function loadPage(page) {
        const primary = choosePrimaryStatusForTable();
        const params = new URLSearchParams();
        if (fromDateEl?.value) params.append("fromDate", fromDateEl.value);
        if (toDateEl?.value) params.append("toDate", toDateEl.value);
        if (primary.statusType) {
            params.append("statusType", primary.statusType);
            params.append("approved", String(primary.approved));
        }
        params.append("page", String(page));
        params.append("pageSize", String(PAGE_SIZE));

        tableBody.innerHTML = `
            <tr>
                <td colspan="30" class="text-center text-secondary py-4">
                    <span class="spinner-border spinner-border-sm me-2"></span> Loading candidates...
                </td>
            </tr>`;

        try {
            const res = await fetch(`/api/v1/candidates/master?${params.toString()}`, {
                method: "GET",
                credentials: "same-origin"
            });
            if (!res.ok) throw new Error("Failed to load candidates");
            const data = await res.json();

            const content = Array.isArray(data.content) ? data.content : [];
            currentPage = data.number != null ? (data.number + 1) : page;
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || content.length;

            renderTable(content, primary.statusType === "RESULT_STATUS");
            renderPagination();
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to load candidates.");
        }
    }

    function renderTable(rows, showResultColumn) {
        if (!rows.length) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="30" class="text-center text-secondary py-4">
                        No records found for given filters.
                    </td>
                </tr>`;
            if (countBadge) countBadge.textContent = "0 records";
            if (pageInfo) pageInfo.textContent = "Showing 0 of 0";
            if (resultStatusHeader) resultStatusHeader.classList.toggle("d-none", !showResultColumn);
            return;
        }

        const start = (currentPage - 1) * PAGE_SIZE + 1;
        const end = Math.min(start + rows.length - 1, totalElements);

        if (countBadge) countBadge.textContent = `${totalElements} records`;
        if (pageInfo) pageInfo.textContent = `Showing ${start}–${end} of ${totalElements}`;
        if (resultStatusHeader) resultStatusHeader.classList.toggle("d-none", !showResultColumn);

        const html = rows.map(r => {
            const resultBase = r.resultStatus == null ? "" : (r.resultStatus ? "Pass" : "Fail");
            return `
                <tr>
                    <td>${r.srNo ?? ""}</td>
                    <td>${r.applicationNo ?? ""}</td>
                    <td>${r.post ?? ""}</td>
                    <td>${r.firstName ?? ""}</td>
                    <td>${r.fatherName ?? ""}</td>
                    <td>${r.surname ?? ""}</td>
                    <td>${r.motherName ?? ""}</td>
                    <td>${r.dob ?? ""}</td>
                    <td>${r.age ?? ""}</td>
                    <td>${r.gender ?? ""}</td>
                    <td>${r.category ?? ""}</td>
                    <td>${r.reservationType ?? ""}</td>
                    <td>${r.mobileNo ?? ""}</td>
                    <td>${resultBase}</td>
                    <td>${formatDateTime(r.hundredStartTime)}</td>
                    <td>${formatDateTime(r.hundredEndTime)}</td>
                    <td>${r.hundredTimeDiff ?? ""}</td>
                    <td>${r.hundredMarks ?? ""}</td>
                    <td>${formatDateTime(r.fiveKmStartTime)}</td>
                    <td>${formatDateTime(r.fiveKmEndTime)}</td>
                    <td>${r.fiveKmTimeDiff ?? ""}</td>
                    <td>${r.fiveKmMarks ?? ""}</td>
                    <td>${r.shotputAttempt1 ?? ""}</td>
                    <td>${r.shotputAttempt2 ?? ""}</td>
                    <td>${r.shotputAttempt3 ?? ""}</td>
                    <td>${r.shotputHighestDistance ?? ""}</td>
                    <td>${r.shotputMarks ?? ""}</td>
                    <td>${r.totalMarks ?? ""}</td>
                    <td class="result-status-col ${showResultColumn ? "" : "d-none"}">${resultBase}</td>
                </tr>`;
        }).join("");

        tableBody.innerHTML = html;
    }

    function formatDateTime(value) {
        if (!value) return "";
        const str = String(value);
        const parts = str.split("T");
        if (parts.length === 2) {
            // ISO LocalDateTime → HH:mm:ss (or HH:mm)
            const timePart = parts[1];
            return timePart.substring(0, 8);
        }
        // Fallback: if already "yyyy-MM-dd HH:mm:ss" just take time part
        const spaceIdx = str.indexOf(" ");
        if (spaceIdx > -1 && spaceIdx + 1 < str.length) {
            return str.substring(spaceIdx + 1, spaceIdx + 9);
        }
        return str;
    }

    function renderPagination() {
        if (!pagination) return;
        if (totalPages <= 1) {
            pagination.innerHTML = "";
            return;
        }
        const maxButtons = 5;
        let startPage = Math.max(1, currentPage - 2);
        let endPage = startPage + maxButtons - 1;
        if (endPage > totalPages) {
            endPage = totalPages;
            startPage = Math.max(1, endPage - maxButtons + 1);
        }

        let html = `
            <li class="page-item ${currentPage === 1 ? "disabled" : ""}">
                <button class="page-link" data-page="${currentPage - 1}">Previous</button>
            </li>`;

        for (let p = startPage; p <= endPage; p++) {
            html += `
                <li class="page-item ${p === currentPage ? "active" : ""}">
                    <button class="page-link" data-page="${p}">${p}</button>
                </li>`;
        }

        html += `
            <li class="page-item ${currentPage === totalPages ? "disabled" : ""}">
                <button class="page-link" data-page="${currentPage + 1}">Next</button>
            </li>`;

        pagination.innerHTML = html;
        pagination.querySelectorAll("button.page-link").forEach(btn => {
            btn.addEventListener("click", e => {
                const targetPage = parseInt(e.currentTarget.getAttribute("data-page"), 10);
                if (!isNaN(targetPage) && targetPage >= 1 && targetPage <= totalPages) {
                    loadPage(targetPage);
                }
            });
        });
    }

    if (applyFiltersBtn) {
        applyFiltersBtn.addEventListener("click", () => {
            loadPage(1);
        });
    }

    if (downloadExcelBtn) {
        downloadExcelBtn.addEventListener("click", () => {
            const map = getSelectedStatusMap();
            const entries = Object.entries(map).filter(([, v]) => v.approved !== null);
            if (!entries.length) {
                showAlert("warning", "Please select at least one status filter to download.");
                return;
            }
            const baseParams = new URLSearchParams();
            if (fromDateEl?.value) baseParams.append("fromDate", fromDateEl.value);
            if (toDateEl?.value) baseParams.append("toDate", toDateEl.value);

            entries.forEach(([statusType, v]) => {
                const params = new URLSearchParams(baseParams.toString());
                params.append("statusType", statusType);
                params.append("approved", String(v.approved));
                const url = `/api/v1/candidates/master/export?${params.toString()}`;
                window.open(url, "_blank", "noopener,noreferrer");
            });
        });
    }

    // initial load
    loadPage(1);
});

