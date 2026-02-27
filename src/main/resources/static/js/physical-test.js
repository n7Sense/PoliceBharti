// Page-specific logic for add-candidate.html:
// - fetch candidate by applicationNo on Enter/Tab
// - webcam capture to Base64
// - integrate existing Aratek biometric scripts (/biometric/app.js, /biometric/Client.js)
// - validate and save (photo + biometrics) back to Candidate table

document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const alertContainer = document.getElementById("physicalTestAlertContainer");

    const form = document.getElementById("physicalTestForm");
    const resetBtn = document.getElementById("resetBtn");
    const saveBtn = document.getElementById("saveBtn");

    const applicationNoEl = document.getElementById("applicationNo");
    const postEl = document.getElementById("post");
    const genderEl = document.getElementById("gender");
    const dobEl = document.getElementById("dob");
    const applicationCategoryEl = document.getElementById("applicationCategory");
    const parallelReservationEl = document.getElementById("parallelReservation");
    const mobileNoEl = document.getElementById("mobileNo");

    const photoBase64El = document.getElementById("photoBase64");

    let candidateLoaded = false;
    let webcamStream = null;

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


    function formatDob(dob) {
        if (!dob) return "";
        // expecting yyyy-MM-dd from JSON
        return dob;
    }

    async function fetchCandidateByApplicationNo(appNo) {
        clearCandidateFields();

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
            showAlert("success", "Candidate details loaded. Now capture photo and thumbs.");
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to fetch candidate details. Please try again.");
        }
    }

    // Trigger fetch on Enter, or when leaving the field (Tab causes blur)
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


    // Biometric integration using existing scripts
    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) headers[csrfHeaderName] = csrfToken;
        return headers;
    }

    // Ensure hook runs after app.js has attached its handlers (window.onload)
    if (document.readyState === "complete") {
        setTimeout(hookCaptureCallback, 0);
    } else {
        window.addEventListener("load", function () {
            // small delay to allow app.js onload to finish
            setTimeout(hookCaptureCallback, 0);
        });
    }


    // Save
    async function saveCandidate() {
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

        const photo = photoBase64El.value;
        const biometric1 = biometric1El.value;
        const biometric2 = biometric2El.value;

        if (!photo) {
            showAlert("danger", "Photo is mandatory before submit.");
            return;
        }
        if (!biometric1 || !biometric2) {
            showAlert("danger", "Both biometric thumbs are mandatory before submit.");
            return;
        }

        const payload = {
            applicationNo: Number(appNoText),
            photo: photo,
            biometric1: biometric1,
            biometric2: biometric2
        };

        try {
            saveBtn.disabled = true;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Saving...';

            const res = await fetch("/api/v1/candidates/enroll", {
                method: "POST",
                headers: buildCsrfHeaders(),
                body: JSON.stringify(payload)
            });

            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = (data && data.message) ? data.message : "Failed to save candidate.";
                throw new Error(msg);
            }

            showAlert("success", (data && data.message) ? data.message : "Candidate Added Successfully");
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to save candidate.");
        } finally {
            saveBtn.disabled = false;
            saveBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Save Candidate';
        }
    }

    if (form) {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            saveCandidate();
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            applicationNoEl.value = "";
            clearCandidateFields();
            showAlert("info", "Form reset.");
        });
    }
});

