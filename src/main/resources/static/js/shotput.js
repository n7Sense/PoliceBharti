document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const alertContainer = document.getElementById("shotputAlertContainer");

    const form = document.getElementById("shotputForm");
    const resetBtn = document.getElementById("resetBtn");
    const saveBtn = document.getElementById("saveShotputBtn");
    const printBtn = document.getElementById("printShotputBtn");

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

    const attempt1El = document.getElementById("attempt1");
    const attempt2El = document.getElementById("attempt2");
    const attempt3El = document.getElementById("attempt3");
    const marks1El = document.getElementById("marks1");
    const marks2El = document.getElementById("marks2");
    const marks3El = document.getElementById("marks3");
    const highestDistanceEl = document.getElementById("highestDistance");
    const highestMarksEl = document.getElementById("highestMarks");

    let candidateLoaded = false;
    let shotputLoaded = false;

    function showAlert(type, message) {
        if (!alertContainer) return;
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>`;
    }

    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) headers[csrfHeaderName] = csrfToken;
        return headers;
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

    function clearShotputFields() {
        attempt1El.value = "";
        attempt2El.value = "";
        attempt3El.value = "";
        marks1El.value = "";
        marks2El.value = "";
        marks3El.value = "";
        highestDistanceEl.value = "";
        highestMarksEl.value = "";
        shotputLoaded = false;
        if (saveBtn) saveBtn.disabled = true;
        if (printBtn) printBtn.disabled = true;
    }

    function formatDob(dob) {
        if (!dob) return "";
        if (typeof dob === "string") return dob;
        if (Array.isArray(dob) && dob.length >= 3) {
            const [y, m, d] = dob;
            return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        if (typeof dob === "object" && dob !== null) {
            const y = dob.year;
            const m = dob.monthValue ?? dob.month;
            const d = dob.dayOfMonth ?? dob.day;
            if (y != null && m != null && d != null) {
                return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
            }
        }
        return "";
    }

    function computeMarks(distance) {
        if (distance == null || isNaN(distance)) return "";
        const d = Number(distance);
        if (d >= 8.50) return 15;
        if (d >= 7.90) return 12;
        if (d >= 7.30) return 10;
        if (d >= 6.70) return 8;
        if (d >= 6.10) return 6;
        if (d >= 5.50) return 5;
        if (d >= 4.90) return 4;
        if (d >= 4.30) return 3;
        if (d >= 3.70) return 2;
        if (d >= 3.10) return 1;
        if (d > 0) return 0;
        return "";
    }

    function recalcMarksAndHighest() {
        const a1 = attempt1El.value !== "" ? Number(attempt1El.value) : null;
        const a2 = attempt2El.value !== "" ? Number(attempt2El.value) : null;
        const a3 = attempt3El.value !== "" ? Number(attempt3El.value) : null;

        const m1 = computeMarks(a1);
        const m2 = computeMarks(a2);
        const m3 = computeMarks(a3);

        marks1El.value = m1;
        marks2El.value = m2;
        marks3El.value = m3;

        let highestDistance = 0;
        let highestMarks = 0;

        if (a1 != null && !isNaN(a1) && a1 > highestDistance) {
            highestDistance = a1;
            highestMarks = m1 !== "" ? Number(m1) : 0;
        }
        if (a2 != null && !isNaN(a2) && a2 > highestDistance) {
            highestDistance = a2;
            highestMarks = m2 !== "" ? Number(m2) : 0;
        }
        if (a3 != null && !isNaN(a3) && a3 > highestDistance) {
            highestDistance = a3;
            highestMarks = m3 !== "" ? Number(m3) : 0;
        }

        highestDistanceEl.value = highestDistance > 0 ? highestDistance.toFixed(2) : "";
        highestMarksEl.value = highestDistance > 0 ? highestMarks : "";

        if (candidateLoaded) {
            saveBtn.disabled = false;
        }
    }

    async function fetchShotput(appNo) {
        clearShotputFields();
        if (!appNo) return;
        try {
            const res = await fetch(`/api/v1/shotput?applicationNo=${encodeURIComponent(appNo)}`, {
                method: "GET",
                credentials: "same-origin"
            });
            if (res.status === 404) return;
            if (!res.ok) return;
            const data = await res.json();
            if (data.attempt1 != null) attempt1El.value = data.attempt1;
            if (data.attempt2 != null) attempt2El.value = data.attempt2;
            if (data.attempt3 != null) attempt3El.value = data.attempt3;
            if (data.marks1 != null) marks1El.value = data.marks1;
            if (data.marks2 != null) marks2El.value = data.marks2;
            if (data.marks3 != null) marks3El.value = data.marks3;
            if (data.highestDistance != null) highestDistanceEl.value = data.highestDistance;
            if (data.highestMarks != null) highestMarksEl.value = data.highestMarks;
            shotputLoaded = true;
            if (printBtn) printBtn.disabled = false;
        } catch (e) {
            console.error(e);
        }
    }

    async function fetchCandidateByApplicationNo(appNo) {
        clearCandidateFields();
        clearShotputFields();

        if (!appNo) {
            showAlert("danger", "Application No is required.");
            return;
        }

        try {
            const res = await fetch(`/api/v1/candidates/details?applicationNo=${encodeURIComponent(appNo)}`, {
                method: "GET",
                credentials: "same-origin"
            });

            if (res.status === 404) {
                const data = await res.json().catch(() => null);
                showAlert("warning", (data && data.message) ? data.message : "Invalid Application Number. Candidate not found.");
                return;
            }
            if (!res.ok) throw new Error("Failed to fetch candidate details.");

            const data = await res.json();
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
            showAlert("success", "Candidate details loaded. Enter shotput attempts and save.");
            await fetchShotput(appNo);
            if (candidateLoaded) {
                saveBtn.disabled = false;
            }
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to fetch candidate details. Please try again.");
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
            if (appNo) fetchCandidateByApplicationNo(appNo);
        });
    }

    [attempt1El, attempt2El, attempt3El].forEach(el => {
        if (!el) return;
        el.addEventListener("input", recalcMarksAndHighest);
    });

    async function saveShotput() {
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
            attempt1: attempt1El.value !== "" ? Number(attempt1El.value) : null,
            attempt2: attempt2El.value !== "" ? Number(attempt2El.value) : null,
            attempt3: attempt3El.value !== "" ? Number(attempt3El.value) : null
        };

        try {
            saveBtn.disabled = true;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Saving...';

            const res = await fetch("/api/v1/shotput", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin",
                body: JSON.stringify(payload)
            });
            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = (data && data.message) ? data.message : "Failed to save shotput record.";
                throw new Error(msg);
            }

            showAlert("success", (data && data.message) ? data.message : "Shotput record saved successfully");
            if (printBtn) printBtn.disabled = false;
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to save shotput record.");
        } finally {
            saveBtn.disabled = false;
            saveBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Save Shotput';
        }
    }

    if (form) {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            saveShotput();
        });
    }

    if (saveBtn) {
        saveBtn.addEventListener("click", () => saveShotput());
    }

    if (printBtn) {
        printBtn.addEventListener("click", () => {
            const appNoText = applicationNoEl.value.trim();
            if (!appNoText) {
                showAlert("danger", "Application No is required.");
                return;
            }
            const url = `/api/v1/shotput/report?applicationNo=${encodeURIComponent(appNoText)}`;
            window.open(url, "_blank", "noopener,noreferrer");
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            applicationNoEl.value = "";
            clearCandidateFields();
            clearShotputFields();
            showAlert("info", "Form reset.");
        });
    }
});

