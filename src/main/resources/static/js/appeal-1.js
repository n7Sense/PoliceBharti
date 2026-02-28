// Appeal 1 page logic (no biometric verification)

document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const alertContainer = document.getElementById("appeal1AlertContainer");
    const form = document.getElementById("appeal1Form");
    const resetBtn = document.getElementById("resetBtn");
    const saveAppeal1Btn = document.getElementById("saveAppeal1Btn");
    const downloadPdfBtn = document.getElementById("downloadPdfBtn");

    const applicationNoEl = document.getElementById("applicationNo");
    const postEl = document.getElementById("post");
    const genderEl = document.getElementById("gender");
    const dobEl = document.getElementById("dob");
    const applicationCategoryEl = document.getElementById("applicationCategory");
    const parallelReservationEl = document.getElementById("parallelReservation");
    const mobileNoEl = document.getElementById("mobileNo");

    const mainResultTextEl = document.getElementById("mainResultText");
    const mainReasonTextEl = document.getElementById("mainReasonText");

    const height1El = document.getElementById("height1");
    const chest1El = document.getElementById("chest1");
    const expandedChest1El = document.getElementById("expandedChest1");
    const rejectReason1El = document.getElementById("rejectReason1");

    let candidateLoaded = false;

    function showAlert(type, message) {
        if (!alertContainer) return;
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
    }

    function clearCandidateFields() {
        postEl.value = "";
        genderEl.value = "";
        dobEl.value = "";
        applicationCategoryEl.value = "";
        parallelReservationEl.value = "";
        mobileNoEl.value = "";
        candidateLoaded = false;
    }

    function clearAppealFields() {
        height1El.value = "";
        chest1El.value = "";
        expandedChest1El.value = "";
        rejectReason1El.value = "";
        if (mainResultTextEl) mainResultTextEl.textContent = "N/A";
        if (mainReasonTextEl) mainReasonTextEl.textContent = "N/A";
        if (downloadPdfBtn) downloadPdfBtn.disabled = true;
    }

    function formatDob(dob) {
        if (!dob) return "";
        if (typeof dob === "string") return dob;
        if (Array.isArray(dob) && dob.length >= 3) {
            const [y, m, d] = dob;
            if (!y || !m || !d) return "";
            return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        if (typeof dob === "object") {
            const y = dob.year;
            const m = dob.monthValue ?? dob.month;
            const d = dob.dayOfMonth ?? dob.day;
            if (!y || !m || !d) return "";
            return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        return "";
    }

    async function fetchPhysicalTest(appNo) {
        clearAppealFields();
        if (!appNo) return;

        try {
            const res = await fetch(`/api/v1/physical-tests?applicationNo=${encodeURIComponent(appNo)}`, { method: "GET" });
            if (res.status === 404) {
                return;
            }
            if (!res.ok) return;
            const data = await res.json();

            const mainPass = data.status === true;
            if (mainResultTextEl) mainResultTextEl.textContent = mainPass ? "PASS" : "REJECT";
            if (mainReasonTextEl) mainReasonTextEl.textContent = data.rejectReason || (mainPass ? "PASS" : "Failed limits");

            height1El.value = data.height1 ?? "";
            chest1El.value = data.chest1 ?? "";
            expandedChest1El.value = data.expandedChest1 ?? "";
            rejectReason1El.value = data.rejectReason1 ?? "";

            if (downloadPdfBtn) downloadPdfBtn.disabled = false;
        } catch (e) {
            console.error(e);
        }
    }

    async function fetchCandidateByApplicationNo(appNo) {
        clearCandidateFields();
        clearAppealFields();

        if (!appNo) {
            showAlert("danger", "Application No is required.");
            return;
        }

        try {
            const res = await fetch(`/api/v1/candidates/details?applicationNo=${encodeURIComponent(appNo)}`, {
                method: "GET"
            });

            if (res.status === 404) {
                const data = await res.json().catch(() => null);
                showAlert("warning", (data && data.message) ? data.message : "Invalid Application Number. Candidate not found.");
                return;
            }

            if (!res.ok) {
                throw new Error("Failed to fetch candidate details.");
            }

            const data = await res.json();

            postEl.value = data.post ?? "";
            genderEl.value = data.gender ?? "";
            dobEl.value = formatDob(data.dob);
            applicationCategoryEl.value = data.applicationCategory ?? "";
            parallelReservationEl.value = data.parallelReservation ?? "";
            mobileNoEl.value = data.mobileNo ?? "";

            candidateLoaded = true;
            showAlert("success", "Candidate details loaded. Now record Appeal 1 measurements.");
            if (saveAppeal1Btn) saveAppeal1Btn.disabled = false;

            await fetchPhysicalTest(appNo);
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to fetch candidate details. Please try again.");
        }
    }

    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) headers[csrfHeaderName] = csrfToken;
        return headers;
    }

    async function saveAppeal1() {
        const appNoText = applicationNoEl.value.trim();
        if (!appNoText) {
            showAlert("danger", "Application No is required.");
            applicationNoEl.focus();
            return;
        }
        if (!candidateLoaded) {
            showAlert("warning", "Please fetch candidate details first (press Enter or Tab).");
            return;
        }

        const payload = {
            applicationNo: Number(appNoText),
            height1: height1El.value !== "" ? Number(height1El.value) : null,
            chest1: chest1El.value !== "" ? Number(chest1El.value) : null,
            expandedChest1: expandedChest1El.value !== "" ? Number(expandedChest1El.value) : null,
            rejectReason1: rejectReason1El.value || null
        };

        try {
            saveAppeal1Btn.disabled = true;
            saveAppeal1Btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Saving...';

            const res = await fetch("/api/v1/physical-tests", {
                method: "POST",
                headers: buildCsrfHeaders(),
                body: JSON.stringify(payload)
            });

            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = (data && data.message) ? data.message : "Failed to save appeal 1.";
                throw new Error(msg);
            }

            showAlert("success", (data && data.message) ? data.message : "Appeal 1 saved successfully");
            if (downloadPdfBtn) downloadPdfBtn.disabled = false;
            await fetchPhysicalTest(appNoText);
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to save appeal 1.");
        } finally {
            saveAppeal1Btn.disabled = false;
            saveAppeal1Btn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Save Appeal 1';
        }
    }

    if (applicationNoEl) {
        applicationNoEl.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                const appNo = applicationNoEl.value.trim();
                fetchCandidateByApplicationNo(appNo);
            }
        });

        applicationNoEl.addEventListener("blur", () => {
            const appNo = applicationNoEl.value.trim();
            if (appNo) {
                fetchCandidateByApplicationNo(appNo);
            }
        });
    }

    if (form) {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
        });
    }

    if (saveAppeal1Btn) {
        saveAppeal1Btn.addEventListener("click", () => saveAppeal1());
    }

    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener("click", () => {
            const appNoText = applicationNoEl.value.trim();
            if (!appNoText) {
                showAlert("danger", "Application No is required.");
                return;
            }
            window.location.href = `/api/v1/physical-tests/report?applicationNo=${encodeURIComponent(appNoText)}`;
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            applicationNoEl.value = "";
            clearCandidateFields();
            clearAppealFields();
            if (saveAppeal1Btn) saveAppeal1Btn.disabled = true;
            showAlert("info", "Form reset.");
        });
    }
});

