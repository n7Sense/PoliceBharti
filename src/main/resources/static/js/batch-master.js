document.addEventListener("DOMContentLoaded", () => {
    const alertContainer = document.getElementById("batchMasterAlertContainer");
    const tbody = document.getElementById("batchesTableBody");
    const countBadge = document.getElementById("batchCountBadge");

    const modalEl = document.getElementById("batchCandidatesModal");
    const modalTitleEl = document.getElementById("batchCandidatesModalTitle");
    const modalSubtitleEl = document.getElementById("batchCandidatesSubtitle");
    const candidatesTbody = document.getElementById("batchCandidatesTableBody");
    const printBtn = document.getElementById("printBatchCandidatesBtn");

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    let currentBatchId = null;
    let currentBatchCode = "";
    let bsModal = null;

    function showAlert(type, message) {
        if (!alertContainer) return;
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>`;
    }

    function safe(v) {
        return v == null ? "" : String(v);
    }

    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) headers[csrfHeaderName] = csrfToken;
        return headers;
    }

    function setLoadingBatches() {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-secondary py-4">
                    <span class="spinner-border spinner-border-sm me-2"></span> Loading batches...
                </td>
            </tr>`;
    }

    function setEmptyBatches() {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-secondary py-4">
                    No batches found.
                </td>
            </tr>`;
    }

    function renderBatches(batches) {
        countBadge.textContent = `${batches.length} records`;
        if (!batches.length) {
            setEmptyBatches();
            return;
        }
        tbody.innerHTML = "";
        for (const b of batches) {
            const locked = b.isLocked === true ? "Yes" : "No";
            const status = b.batchStatus === false ? "Full" : "Active";
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="fw-semibold">${safe(b.batchCode)}</td>
                <td>${safe(b.batchName)}</td>
                <td class="text-center">${safe(b.batchSize)}</td>
                <td class="text-center">${safe(b.assignedCount)}</td>
                <td class="text-center">${locked}</td>
                <td class="text-center">${status}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-primary view-batch-btn"
                            data-batch-id="${safe(b.id)}"
                            data-batch-code="${safe(b.batchCode)}">
                        <i class="bi bi-eye me-1"></i> View
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        }
    }

    async function loadBatches() {
        setLoadingBatches();
        try {
            const res = await fetch("/api/v1/batches", { method: "GET", credentials: "same-origin" });
            if (!res.ok) throw new Error("Failed to load batches");
            const data = await res.json();
            renderBatches(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
            showAlert("danger", "Failed to load batches. Please refresh the page.");
            setEmptyBatches();
            countBadge.textContent = "0 records";
        }
    }

    function openModal() {
        if (!modalEl) return;
        if (!bsModal && window.bootstrap?.Modal) bsModal = new bootstrap.Modal(modalEl);
        if (bsModal) bsModal.show();
    }

    function setCandidatesLoading() {
        candidatesTbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-secondary py-4">
                    <span class="spinner-border spinner-border-sm me-2"></span> Loading candidates...
                </td>
            </tr>`;
    }

    function setCandidatesEmpty() {
        candidatesTbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-secondary py-4">
                    No candidates found for this batch.
                </td>
            </tr>`;
    }

    function renderCandidates(rows) {
        if (!rows.length) {
            setCandidatesEmpty();
            return;
        }
        candidatesTbody.innerHTML = "";
        for (const r of rows) {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="text-center fw-semibold">${safe(r.runningNumber)}</td>
                <td>${safe(r.applicationNo)}</td>
                <td>${safe(r.name)}</td>
                <td>${safe(r.mobileNo)}</td>
                <td>${safe(r.post)}</td>
                <td>${safe(r.category)}</td>
            `;
            candidatesTbody.appendChild(tr);
        }
    }

    async function loadCandidates(batchId, batchCode) {
        currentBatchId = batchId;
        currentBatchCode = batchCode || "";
        modalTitleEl.textContent = `Batch ${currentBatchCode || ""} - Candidates`;
        modalSubtitleEl.textContent = "";
        setCandidatesLoading();
        openModal();

        try {
            const res = await fetch(`/api/v1/batches/${encodeURIComponent(batchId)}/candidates`, {
                method: "GET",
                credentials: "same-origin"
            });
            if (!res.ok) throw new Error("Failed to load candidates");
            const data = await res.json();
            const rows = Array.isArray(data) ? data : [];
            modalSubtitleEl.textContent = `${rows.length} candidates`;
            renderCandidates(rows);
        } catch (e) {
            console.error(e);
            showAlert("danger", "Failed to load batch candidates.");
            setCandidatesEmpty();
        }
    }

    tbody.addEventListener("click", (e) => {
        const btn = e.target.closest(".view-batch-btn");
        if (!btn) return;
        const batchId = btn.getAttribute("data-batch-id");
        const batchCode = btn.getAttribute("data-batch-code");
        if (!batchId) return;
        loadCandidates(batchId, batchCode);
    });

    printBtn.addEventListener("click", () => {
        if (!currentBatchId) {
            showAlert("warning", "Select a batch first.");
            return;
        }
        const url = `/api/v1/batches/${encodeURIComponent(currentBatchId)}/candidates/pdf`;
        window.open(url, "_blank", "noopener,noreferrer");
    });

    // initialize
    loadBatches();
});

