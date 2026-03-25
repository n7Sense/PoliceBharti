// Appeal 2 page logic (no biometric verification)

document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const alertContainer = document.getElementById("appeal2AlertContainer");
    const form = document.getElementById("appeal2Form");
    const resetBtn = document.getElementById("resetBtn");
    const saveAppeal2Btn = document.getElementById("saveAppeal2Btn");
    const downloadPdfBtn = document.getElementById("downloadPdfBtn");

    const applicationNoEl = document.getElementById("applicationNo");
    const nameEl = document.getElementById("name");
    const postEl = document.getElementById("post");
    const genderEl = document.getElementById("gender");
    const dobEl = document.getElementById("dob");
    const ageEl = document.getElementById("age");
    const emailEl = document.getElementById("email");
    const religionEl = document.getElementById("religion");
    const applicationCategoryEl = document.getElementById("applicationCategory");
    const parallelReservationEl = document.getElementById("parallelReservation");
    const mobileNoEl = document.getElementById("mobileNo");

    const mainResultTextEl = document.getElementById("mainResultText");
    const mainReasonTextEl = document.getElementById("mainReasonText");
    const mainHeightEl = document.getElementById("mainHeight");
    const mainChestEl = document.getElementById("mainChest");
    const mainExpandedChestEl = document.getElementById("mainExpandedChest");
    const mainReasonEl = document.getElementById("mainReason");

    const appeal1ResultTextEl = document.getElementById("appeal1ResultText");
    const appeal1ReasonTextEl = document.getElementById("appeal1ReasonText");
    const appeal1HeightEl = document.getElementById("appeal1Height");
    const appeal1ChestEl = document.getElementById("appeal1Chest");
    const appeal1ExpandedChestEl = document.getElementById("appeal1ExpandedChest");
    const appeal1ReasonEl = document.getElementById("appeal1Reason");

    const height2El = document.getElementById("height2");
    const chest2El = document.getElementById("chest2");
    const expandedChest2El = document.getElementById("expandedChest2");
    const rejectReason2El = document.getElementById("rejectReason2");

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
        if (nameEl) nameEl.value = "";
        postEl.value = "";
        genderEl.value = "";
        dobEl.value = "";
        if (ageEl) ageEl.value = "";
        if (emailEl) emailEl.value = "";
        if (religionEl) religionEl.value = "";
        applicationCategoryEl.value = "";
        parallelReservationEl.value = "";
        mobileNoEl.value = "";
        candidateLoaded = false;
    }

    function clearAppealFields() {
        height2El.value = "";
        chest2El.value = "";
        expandedChest2El.value = "";
        rejectReason2El.value = "";
        if (mainResultTextEl) mainResultTextEl.textContent = "N/A";
        if (mainReasonTextEl) mainReasonTextEl.textContent = "N/A";
        if (mainHeightEl) mainHeightEl.value = "";
        if (mainChestEl) mainChestEl.value = "";
        if (mainExpandedChestEl) mainExpandedChestEl.value = "";
        if (mainReasonEl) mainReasonEl.value = "";
        if (appeal1ResultTextEl) appeal1ResultTextEl.textContent = "N/A";
        if (appeal1ReasonTextEl) appeal1ReasonTextEl.textContent = "N/A";
        if (appeal1HeightEl) appeal1HeightEl.value = "";
        if (appeal1ChestEl) appeal1ChestEl.value = "";
        if (appeal1ExpandedChestEl) appeal1ExpandedChestEl.value = "";
        if (appeal1ReasonEl) appeal1ReasonEl.value = "";
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

            if (mainHeightEl) mainHeightEl.value = data.height != null ? String(data.height) : "";
            if (mainChestEl) mainChestEl.value = data.chest != null ? String(data.chest) : "";
            if (mainExpandedChestEl) mainExpandedChestEl.value = data.expandedChest != null ? String(data.expandedChest) : "";
            if (mainReasonEl) mainReasonEl.value = data.rejectReason ?? "";

            const a1Pass = data.status1 === true;
            if (appeal1ResultTextEl) appeal1ResultTextEl.textContent = data.status1 == null ? "N/A" : (a1Pass ? "PASS" : "REJECT");
            if (appeal1ReasonTextEl) appeal1ReasonTextEl.textContent = data.rejectReason1 || (data.status1 == null ? "N/A" : (a1Pass ? "PASS" : "Failed limits"));

            if (appeal1HeightEl) appeal1HeightEl.value = data.height1 != null ? String(data.height1) : "";
            if (appeal1ChestEl) appeal1ChestEl.value = data.chest1 != null ? String(data.chest1) : "";
            if (appeal1ExpandedChestEl) appeal1ExpandedChestEl.value = data.expandedChest1 != null ? String(data.expandedChest1) : "";
            if (appeal1ReasonEl) appeal1ReasonEl.value = data.rejectReason1 ?? "";

            height2El.value = data.height2 ?? "";
            chest2El.value = data.chest2 ?? "";
            expandedChest2El.value = data.expandedChest2 ?? "";
            rejectReason2El.value = data.rejectReason2 ?? "";

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

            if (data.physicalTestStatus === true) {
                showAlert("warning", "Candidate passed the test. Appeal is not allowed.");
                return;
            }

            if (nameEl) nameEl.value = data.name ?? "";
            postEl.value = data.post ?? "";
            genderEl.value = data.gender ?? "";
            dobEl.value = formatDob(data.dob);
            if (ageEl) ageEl.value = data.age != null ? String(data.age) : "";
            if (emailEl) emailEl.value = data.email ?? "";
            if (religionEl) religionEl.value = data.religion ?? "";
            applicationCategoryEl.value = data.applicationCategory ?? "";
            parallelReservationEl.value = data.parallelReservation ?? "";
            mobileNoEl.value = data.mobileNo ?? "";

            candidateLoaded = true;
            showAlert("success", "Candidate details loaded. Now record Appeal 2 measurements.");
            if (saveAppeal2Btn) saveAppeal2Btn.disabled = false;

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

    async function saveAppeal2() {
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
            height2: height2El.value !== "" ? Number(height2El.value) : null,
            chest2: chest2El.value !== "" ? Number(chest2El.value) : null,
            expandedChest2: expandedChest2El.value !== "" ? Number(expandedChest2El.value) : null,
            rejectReason2: rejectReason2El.value || null
        };

        try {
            saveAppeal2Btn.disabled = true;
            saveAppeal2Btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Saving...';

            const res = await fetch("/api/v1/physical-tests", {
                method: "POST",
                headers: buildCsrfHeaders(),
                body: JSON.stringify(payload)
            });

            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = (data && data.message) ? data.message : "Failed to save appeal 2.";
                throw new Error(msg);
            }

            showAlert("success", (data && data.message) ? data.message : "Appeal 2 saved successfully");
            if (downloadPdfBtn) downloadPdfBtn.disabled = false;
            await fetchPhysicalTest(appNoText);
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to save appeal 2.");
        } finally {
            saveAppeal2Btn.disabled = false;
            saveAppeal2Btn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Save Appeal 2';
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

    if (saveAppeal2Btn) {
        saveAppeal2Btn.addEventListener("click", () => saveAppeal2());
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
            if (saveAppeal2Btn) saveAppeal2Btn.disabled = true;
            showAlert("info", "Form reset.");
        });
    }
});

