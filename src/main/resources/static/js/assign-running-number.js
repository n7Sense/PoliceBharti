/**
 * Assign Running Number page:
 * - Load batches and current running number; if user has locked batch show only that
 * - Lock/Unlock batch; if lock fails (locked by another user) offer to create next batch
 * - Fetch candidate by application no (only when batch locked), verify photo + biometric, then assign
 */
document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    const alertContainer = document.getElementById("assignRnAlertContainer");
    const batchSelect = document.getElementById("batchSelect");
    const lockBtn = document.getElementById("lockBtn");
    const unlockBtn = document.getElementById("unlockBtn");
    const currentRunningNumberDisplay = document.getElementById("currentRunningNumberDisplay");
    const form = document.getElementById("assignRunningNumberForm");
    const applicationNoEl = document.getElementById("applicationNo");
    const mobileNoEl = document.getElementById("mobileNo");
    const postEl = document.getElementById("post");
    const genderEl = document.getElementById("gender");
    const dobEl = document.getElementById("dob");
    const applicationCategoryEl = document.getElementById("applicationCategory");
    const parallelReservationEl = document.getElementById("parallelReservation");
    const resetBtn = document.getElementById("resetBtn");
    const assignBtn = document.getElementById("assignBtn");

    const storedPhotoPreviewEl = document.getElementById("storedPhotoPreview");
    const storedPhotoPlaceholderEl = document.getElementById("storedPhotoPlaceholder");
    const photoPreviewEl = document.getElementById("photoPreview");
    const photoPlaceholderEl = document.getElementById("photoPlaceholder");
    const photoBase64El = document.getElementById("photoBase64");
    const photoVerifiedCheckEl = document.getElementById("photoVerifiedCheck");
    const openCameraBtn = document.getElementById("openCameraBtn");
    const capturePhotoBtn = document.getElementById("capturePhotoBtn");
    const closeCameraBtn = document.getElementById("closeCameraBtn");
    const webcamVideo = document.getElementById("webcamVideo");
    const webcamCanvas = document.getElementById("webcamCanvas");
    const biometric1El = document.getElementById("biometric1");
    const biometric2El = document.getElementById("biometric2");
    const captureLeftThumbBtn = document.getElementById("captureLeftThumbBtn");
    const captureRightThumbBtn = document.getElementById("captureRightThumbBtn");
    const verifyCandidateBtn = document.getElementById("verifyCandidateBtn");
    const verificationStatusEl = document.getElementById("verificationStatus");

    let pageData = { batches: [], currentRunningNumber: 1 };
    let candidateLoaded = false;
    let verificationPassed = false;
    let storedBiometric1 = "";
    let storedBiometric2 = "";
    let webcamStream = null;

    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) headers[csrfHeaderName] = csrfToken;
        return headers;
    }

    function showAlert(type, message) {
        if (!alertContainer) return;
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>`;
    }

    function clearCandidateFields() {
        if (mobileNoEl) mobileNoEl.value = "";
        if (postEl) postEl.value = "";
        if (genderEl) genderEl.value = "";
        if (dobEl) dobEl.value = "";
        if (applicationCategoryEl) applicationCategoryEl.value = "";
        if (parallelReservationEl) parallelReservationEl.value = "";
        candidateLoaded = false;
    }

    function clearVerification() {
        verificationPassed = false;
        storedBiometric1 = "";
        storedBiometric2 = "";
        if (verificationStatusEl) verificationStatusEl.textContent = "Not verified";
        if (photoVerifiedCheckEl) photoVerifiedCheckEl.checked = false;
        if (storedPhotoPreviewEl) { storedPhotoPreviewEl.style.display = "none"; storedPhotoPreviewEl.src = ""; }
        if (storedPhotoPlaceholderEl) storedPhotoPlaceholderEl.style.display = "block";
        if (biometric1El) biometric1El.value = "";
        if (biometric2El) biometric2El.value = "";
        if (photoBase64El) photoBase64El.value = "";
        if (photoPreviewEl) { photoPreviewEl.style.display = "none"; photoPreviewEl.src = ""; }
        if (photoPlaceholderEl) photoPlaceholderEl.style.display = "block";
        if (assignBtn) assignBtn.disabled = true;
    }

    function formatDob(dob) {
        if (!dob) return "";
        if (typeof dob === "string") return dob;
        if (Array.isArray(dob) && dob.length >= 3) {
            const [y, m, d] = dob;
            return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        if (typeof dob === "object" && dob !== null) {
            const y = dob.year; const m = dob.monthValue ?? dob.month; const d = dob.dayOfMonth ?? dob.day;
            if (y != null && m != null && d != null) return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        return "";
    }

    function getSelectedBatch() {
        const id = batchSelect && batchSelect.value ? batchSelect.value : "";
        if (!id) return null;
        return pageData.batches.find(b => String(b.id) === String(id)) || null;
    }

    function updateLockUnlockVisibility() {
        const batch = getSelectedBatch();
        if (!batch) {
            if (lockBtn) lockBtn.style.display = "";
            if (unlockBtn) unlockBtn.style.display = "none";
            return;
        }
        const isLocked = batch.isLocked === true;
        if (lockBtn) lockBtn.style.display = isLocked ? "none" : "";
        if (unlockBtn) {
            unlockBtn.style.display = isLocked ? "" : "none";
            unlockBtn.disabled = false;
        }
    }

    async function loadPageData() {
        try {
            const res = await fetch("/api/v1/assign-running-number/page-data", { method: "GET", credentials: "same-origin" });
            if (!res.ok) throw new Error("Failed to load page data");
            pageData = await res.json();
            pageData.batches = pageData.batches || [];
            pageData.currentRunningNumber = pageData.currentRunningNumber != null ? pageData.currentRunningNumber : 1;

            batchSelect.innerHTML = '<option value="">-- Select Batch --</option>';
            pageData.batches.forEach(b => {
                const opt = document.createElement("option");
                opt.value = b.id;
                opt.textContent = b.batchCode + " (" + (b.assignedCount || 0) + "/" + (b.batchSize || 20) + ")";
                batchSelect.appendChild(opt);
            });
            if (pageData.batches.length === 1) batchSelect.value = pageData.batches[0].id;
            currentRunningNumberDisplay.textContent = pageData.currentRunningNumber;
            updateLockUnlockVisibility();
        } catch (e) {
            console.error(e);
            showAlert("danger", "Failed to load batches. Please refresh the page.");
        }
    }

    async function refreshCurrentNumber() {
        try {
            const res = await fetch("/api/v1/assign-running-number/current-number", { method: "GET", credentials: "same-origin" });
            if (res.ok) {
                const data = await res.json();
                pageData.currentRunningNumber = data.currentRunningNumber != null ? data.currentRunningNumber : 1;
                currentRunningNumberDisplay.textContent = pageData.currentRunningNumber;
            }
        } catch (e) {
            console.error(e);
        }
    }

    lockBtn.addEventListener("click", async () => {
        const batch = getSelectedBatch();
        if (!batch) {
            showAlert("warning", "Please select a batch first.");
            return;
        }
        try {
            const res = await fetch("/api/v1/assign-running-number/lock", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin",
                body: JSON.stringify({ batchId: batch.id })
            });
            const data = await res.json().catch(() => ({}));
            if (data.success) {
                showAlert("success", "Batch locked. You can now enter Application No and assign running numbers.");
                await loadPageData();
                return;
            }
            const lockedBy = data.lockedByUserName || "Another user";
            const code = data.batchCode || batch.batchCode || "Batch";
            const createNext = confirm(code + " is locked by " + lockedBy + ". Do you want to create the next batch?");
            if (!createNext) return;
            const createRes = await fetch("/api/v1/assign-running-number/create-batch", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin"
            });
            if (!createRes.ok) throw new Error("Failed to create batch");
            const newBatch = await createRes.json();
            await loadPageData();
            batchSelect.value = newBatch.id;
            updateLockUnlockVisibility();
            const lockRes = await fetch("/api/v1/assign-running-number/lock", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin",
                body: JSON.stringify({ batchId: newBatch.id })
            });
            const lockData = await lockRes.json().catch(() => ({}));
            if (lockData.success) {
                showAlert("success", "New batch " + (newBatch.batchCode || "") + " created and locked.");
                await loadPageData();
            } else {
                showAlert("warning", "Batch created but could not lock it. Please lock it manually.");
                await loadPageData();
            }
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to lock batch.");
        }
    });

    unlockBtn.addEventListener("click", async () => {
        const batch = getSelectedBatch();
        if (!batch) return;
        try {
            const res = await fetch("/api/v1/assign-running-number/unlock", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin",
                body: JSON.stringify({ batchId: batch.id })
            });
            const data = await res.json().catch(() => ({}));
            if (data.success) {
                showAlert("info", "Batch unlocked.");
                await loadPageData();
            } else {
                showAlert("warning", "You can only unlock a batch you locked.");
            }
        } catch (e) {
            console.error(e);
            showAlert("danger", "Failed to unlock batch.");
        }
    });

    batchSelect.addEventListener("change", () => {
        updateLockUnlockVisibility();
        refreshCurrentNumber();
    });

    async function fetchCandidateByApplicationNo(appNo) {
        clearCandidateFields();
        clearVerification();
        if (!appNo) {
            showAlert("danger", "Application No is required.");
            return;
        }
        const batch = getSelectedBatch();
        if (!batch) {
            showAlert("warning", "Please select a batch first.");
            return;
        }
        if (!batch.isLocked) {
            showAlert("warning", "Please lock the batch first before entering Application No.");
            return;
        }
        try {
            const res = await fetch("/api/v1/candidates/details?applicationNo=" + encodeURIComponent(appNo), { method: "GET", credentials: "same-origin" });
            if (res.status === 404) {
                const data = await res.json().catch(() => null);
                showAlert("warning", (data && data.message) ? data.message : "Candidate not found.");
                return;
            }
            if (!res.ok) throw new Error("Failed to fetch candidate");
            const data = await res.json();
            postEl.value = data.post ?? "";
            genderEl.value = data.gender ?? "";
            dobEl.value = formatDob(data.dob);
            applicationCategoryEl.value = data.applicationCategory ?? "";
            parallelReservationEl.value = data.parallelReservation ?? "";
            mobileNoEl.value = data.mobileNo ?? "";
            candidateLoaded = true;
            showAlert("success", "Candidate loaded. Verify photo and biometrics, then click Assign Running Number.");
            const verRes = await fetch("/api/v1/candidates/verification-data?applicationNo=" + encodeURIComponent(appNo), { method: "GET", credentials: "same-origin" });
            if (verRes.ok) {
                const verData = await verRes.json();
                if (verData.photo && storedPhotoPreviewEl) {
                    storedPhotoPreviewEl.src = verData.photo.startsWith("data:") ? verData.photo : ("data:image/jpeg;base64," + verData.photo);
                    storedPhotoPreviewEl.style.display = "block";
                    if (storedPhotoPlaceholderEl) storedPhotoPlaceholderEl.style.display = "none";
                }
                storedBiometric1 = verData.biometric1 || "";
                storedBiometric2 = verData.biometric2 || "";
                if (!storedBiometric1 || !storedBiometric2) showAlert("warning", "Stored biometrics not found. Enroll candidate first.");
            }
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to fetch candidate details.");
        }
    }

    applicationNoEl.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            fetchCandidateByApplicationNo(applicationNoEl.value.trim());
        }
    });
    applicationNoEl.addEventListener("blur", () => {
        const v = applicationNoEl.value.trim();
        if (v) fetchCandidateByApplicationNo(v);
    });

    function openWebcam() {
        navigator.mediaDevices.getUserMedia({ video: true, audio: false }).then(stream => {
            webcamStream = stream;
            webcamVideo.srcObject = stream;
            webcamVideo.style.display = "block";
            capturePhotoBtn.disabled = false;
            closeCameraBtn.disabled = false;
            showAlert("info", "Webcam opened. Click Capture.");
        }).catch(e => {
            console.error(e);
            showAlert("danger", "Unable to access webcam.");
        });
    }
    function closeWebcam() {
        if (webcamStream) {
            webcamStream.getTracks().forEach(t => t.stop());
            webcamStream = null;
        }
        webcamVideo.srcObject = null;
        webcamVideo.style.display = "none";
        capturePhotoBtn.disabled = true;
        closeCameraBtn.disabled = true;
    }
    function capturePhoto() {
        if (!webcamVideo.srcObject) return;
        const w = 600, h = 720;
        webcamCanvas.width = w;
        webcamCanvas.height = h;
        webcamCanvas.getContext("2d").drawImage(webcamVideo, 0, 0, w, h);
        const dataUrl = webcamCanvas.toDataURL("image/jpeg", 0.9);
        photoBase64El.value = dataUrl;
        photoPreviewEl.src = dataUrl;
        photoPreviewEl.style.display = "block";
        photoPlaceholderEl.style.display = "none";
        showAlert("success", "Photo captured. Capture fingerprints and click Verify Candidate.");
    }
    if (openCameraBtn) openCameraBtn.addEventListener("click", openWebcam);
    if (closeCameraBtn) closeCameraBtn.addEventListener("click", closeWebcam);
    if (capturePhotoBtn) capturePhotoBtn.addEventListener("click", capturePhoto);

    function hookCaptureCallback() {
        if (typeof client === "undefined" || client.__ufrs_assign_hooked) return;
        if (typeof client.OnCaptureFingerData !== "function") return;
        client.__ufrs_assign_hooked = true;
        const orig = client.OnCaptureFingerData;
        client.OnCaptureFingerData = function (code, msg, image) {
            try {
                if (code === 0 && image && typeof flag !== "undefined") {
                    if (flag === 1 && biometric1El) biometric1El.value = image.feature_data || (typeof feature1 !== "undefined" ? feature1 : "") || "";
                    if (flag === 2 && biometric2El) biometric2El.value = image.feature_data || (typeof feature2 !== "undefined" ? feature2 : "") || "";
                }
            } catch (e) {}
            if (typeof orig === "function") return orig(code, msg, image);
        };
    }
    if (document.readyState === "complete") setTimeout(hookCaptureCallback, 500);
    else window.addEventListener("load", () => setTimeout(hookCaptureCallback, 500));

    if (captureLeftThumbBtn) captureLeftThumbBtn.addEventListener("click", () => { try { if (typeof capturefinger1 === "function") capturefinger1(); showAlert("info", "Capture left thumb."); } catch (e) { showAlert("danger", "Device not ready."); } });
    if (captureRightThumbBtn) captureRightThumbBtn.addEventListener("click", () => { try { if (typeof capturefinger2 === "function") capturefinger2(); showAlert("info", "Capture right thumb, then Verify."); } catch (e) { showAlert("danger", "Device not ready."); } });

    function verifyFeaturePair(stored, captured, timeoutMs = 8000) {
        return new Promise((resolve, reject) => {
            if (typeof client === "undefined" || typeof client.Verify !== "function") { reject(new Error("SDK not ready")); return; }
            const prev = client.OnVerify;
            const t = setTimeout(() => { client.OnVerify = prev; reject(new Error("timeout")); }, timeoutMs);
            client.OnVerify = function (code, msg, score, result) {
                clearTimeout(t);
                client.OnVerify = prev;
                if (code === 0) resolve({ result: !!result, score });
                else reject(new Error(msg || "Match failed"));
            };
            try { client.Verify(stored, captured, typeof SecurityLevel !== "undefined" ? SecurityLevel.Level_4 : 4); } catch (e) { clearTimeout(t); client.OnVerify = prev; reject(e); }
        });
    }

    async function verifyCandidate() {
        if (!applicationNoEl.value.trim()) { showAlert("danger", "Application No required."); return; }
        if (!candidateLoaded) { showAlert("warning", "Fetch candidate first."); return; }
        if (!storedBiometric1 || !storedBiometric2) { showAlert("danger", "Stored biometrics missing. Enroll candidate first."); return; }
        if (!photoBase64El.value) { showAlert("danger", "Capture photo first."); return; }
        if (!biometric1El.value || !biometric2El.value) { showAlert("danger", "Capture both thumbs."); return; }
        if (photoVerifiedCheckEl && !photoVerifiedCheckEl.checked) { showAlert("warning", "Confirm photo matches."); return; }
        try {
            verificationStatusEl.textContent = "Verifying...";
            verifyCandidateBtn.disabled = true;
            const left = await verifyFeaturePair(storedBiometric1, biometric1El.value);
            const right = await verifyFeaturePair(storedBiometric2, biometric2El.value);
            if (left.result && right.result) {
                verificationPassed = true;
                verificationStatusEl.textContent = "Verified";
                showAlert("success", "Candidate verified. You can now Assign Running Number.");
                assignBtn.disabled = false;
            } else {
                verificationPassed = false;
                verificationStatusEl.textContent = "Not verified";
                showAlert("danger", "Fingerprint mismatch.");
                assignBtn.disabled = true;
            }
        } catch (e) {
            console.error(e);
            verificationPassed = false;
            verificationStatusEl.textContent = "Verification failed";
            showAlert("danger", "Verification failed. Check device.");
            assignBtn.disabled = true;
        } finally {
            verifyCandidateBtn.disabled = false;
        }
    }
    if (verifyCandidateBtn) verifyCandidateBtn.addEventListener("click", verifyCandidate);

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const batch = getSelectedBatch();
        const appNo = applicationNoEl.value.trim();
        if (!batch) { showAlert("warning", "Select a batch."); return; }
        if (!batch.isLocked) { showAlert("warning", "Lock the batch first."); return; }
        if (!appNo) { showAlert("danger", "Application No required."); return; }
        if (!candidateLoaded) { showAlert("warning", "Fetch candidate first."); return; }
        if (!verificationPassed) { showAlert("danger", "Verify candidate (photo + biometrics) before assigning."); return; }
        try {
            assignBtn.disabled = true;
            assignBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Assigning...';
            const res = await fetch("/api/v1/assign-running-number/assign", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin",
                body: JSON.stringify({ batchId: batch.id, applicationNo: Number(appNo) })
            });
            const data = await res.json().catch(() => ({}));
            if (data.success) {
                showAlert("success", data.message || "Running number " + (data.assignedRunningNumber || "") + " assigned.");
                await refreshCurrentNumber();
                applicationNoEl.value = "";
                clearCandidateFields();
                clearVerification();
                const b = getSelectedBatch();
                if (b) {
                    b.assignedCount = (b.assignedCount || 0) + 1;
                    batchSelect.querySelector("option:checked").textContent = b.batchCode + " (" + b.assignedCount + "/" + (b.batchSize || 20) + ")";
                }
            } else {
                const msg = data.message || "Assign failed.";
                if (msg.toLowerCase().includes("full")) {
                    const createNext = confirm("This batch is full. Do you want to create the next batch?");
                    if (createNext) {
                        const createRes = await fetch("/api/v1/assign-running-number/create-batch", {
                            method: "POST",
                            headers: buildCsrfHeaders(),
                            credentials: "same-origin"
                        });
                        if (createRes.ok) {
                            const newBatch = await createRes.json();
                            showAlert("info", "Next batch " + (newBatch.batchCode || "") + " created. Select it and lock to continue.");
                            await loadPageData();
                        }
                    } else {
                        showAlert("warning", msg);
                    }
                } else {
                    showAlert("danger", msg);
                }
            }
        } catch (err) {
            console.error(err);
            showAlert("danger", err.message || "Assign failed.");
        } finally {
            assignBtn.disabled = !verificationPassed;
            assignBtn.innerHTML = '<i class="bi bi-hash me-1"></i> Assign Running Number';
        }
    });

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            applicationNoEl.value = "";
            clearCandidateFields();
            clearVerification();
            closeWebcam();
            showAlert("info", "Form reset.");
        });
    }

    loadPageData();
});
