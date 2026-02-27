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
    const photoPreviewEl = document.getElementById("photoPreview");
    const photoPlaceholderEl = document.getElementById("photoPlaceholder");

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

    function clearCapturedData() {
        // photo
        photoBase64El.value = "";
        photoPreviewEl.style.display = "none";
        photoPreviewEl.src = "";
        photoPlaceholderEl.style.display = "block";

        // biometrics templates
        biometric1El.value = "";
        biometric2El.value = "";
    }

    function formatDob(dob) {
        if (!dob) return "";
        // expecting yyyy-MM-dd from JSON
        return dob;
    }

    async function fetchCandidateByApplicationNo(appNo) {
        clearCandidateFields();
        clearCapturedData();

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

    // Webcam capture
    async function openWebcam() {
        try {
            webcamStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
            webcamVideo.srcObject = webcamStream;
            webcamVideo.style.display = "block";
            capturePhotoBtn.disabled = false;
            closeCameraBtn.disabled = false;
            showAlert("info", "Webcam opened. Click Capture.");
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to access webcam. Please allow camera permission.");
        }
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
        if (!webcamVideo || !webcamStream) return;

        const w = 600;
        const h = 720;
        webcamCanvas.width = w;
        webcamCanvas.height = h;
        const ctx = webcamCanvas.getContext("2d");
        ctx.drawImage(webcamVideo, 0, 0, w, h);

        const dataUrl = webcamCanvas.toDataURL("image/jpeg", 0.9);
        photoBase64El.value = dataUrl;

        photoPreviewEl.src = dataUrl;
        photoPreviewEl.style.display = "block";
        photoPlaceholderEl.style.display = "none";

        showAlert("success", "Photo captured successfully.");
    }

    if (openCameraBtn) openCameraBtn.addEventListener("click", openWebcam);
    if (closeCameraBtn) closeCameraBtn.addEventListener("click", closeWebcam);
    if (capturePhotoBtn) capturePhotoBtn.addEventListener("click", capturePhoto);

    // Biometric integration using existing scripts
    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) headers[csrfHeaderName] = csrfToken;
        return headers;
    }

    function waitFor(conditionFn, timeoutMs = 8000, pollMs = 200) {
        return new Promise((resolve, reject) => {
            const start = Date.now();
            const timer = setInterval(() => {
                if (conditionFn()) {
                    clearInterval(timer);
                    resolve(true);
                    return;
                }
                if (Date.now() - start > timeoutMs) {
                    clearInterval(timer);
                    reject(new Error("timeout"));
                }
            }, pollMs);
        });
    }

    async function prepareBiometricDevice() {
        // app.js exposes global: client, connect(), open_device(), get_device_desc()
        if (typeof client === "undefined" || typeof connect !== "function") {
            throw new Error("Biometric scripts not loaded.");
        }

        if (!client.isConnect) {
            connect();
        }
        await waitFor(() => client.isConnect === true, 6000);

        if (typeof open_device === "function") {
            open_device();
        }

        if (typeof get_device_desc === "function") {
            get_device_desc();
        }

        await waitFor(() => typeof device_id !== "undefined" && device_id, 6000);
    }

    function hookCaptureCallback() {
        if (typeof client === "undefined") return;
        if (client.__ufrs_hooked) return;
        if (typeof client.OnCaptureFingerData !== "function") {
            // underlying handler from app.js not ready yet
            return;
        }

        client.__ufrs_hooked = true;

        const original = client.OnCaptureFingerData;
        client.OnCaptureFingerData = function (code, msg, image) {
            try {
                // app.js already sets global: feature1/feature2 based on flag
                if (code === 0 && image) {
                    if (typeof flag !== "undefined") {
                        if (flag === 1) {
                            biometric1El.value = image.feature_data || feature1 || "";
                        } else if (flag === 2) {
                            biometric2El.value = image.feature_data || feature2 || "";
                        }
                    }
                }
            } catch (e) {
                console.error("capture hook error", e);
            }
            if (typeof original === "function") {
                return original(code, msg, image);
            }
        };
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

    async function captureLeftThumb() {
        try {
            if (typeof capturefinger1 !== "function") throw new Error("capturefinger1 not available");
            capturefinger1();
            showAlert("info", "Left thumb capturing. After image appears, place your Right thumb and click 'Capture Right'.");

        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to start left thumb capture. Make sure device is connected via Open Assistant.");
        }
    }

    async function captureRightThumb() {
        try {
            if (typeof capturefinger2 !== "function") throw new Error("capturefinger2 not available");
            capturefinger2();
            showAlert("info", "Right thumb capturing. Both thumbs will be saved with candidate.");
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to start right thumb capture. Make sure device is connected via Open Assistant.");
        }
    }

    if (captureLeftThumbBtn) captureLeftThumbBtn.addEventListener("click", captureLeftThumb);
    if (captureRightThumbBtn) captureRightThumbBtn.addEventListener("click", captureRightThumb);

    if (openAssistantLink) {
        openAssistantLink.addEventListener("click", async (e) => {
            // Let OS handle AratekFMA://4397, but also prepare device via SDK
            e.preventDefault();
            try {
                await prepareBiometricDevice();
                showAlert("success", "Device ready. Place your Left finger on the scanner and click 'Capture Left'.");
            } catch (err) {
                console.error(err);
                showAlert("danger", "Unable to prepare biometric device. Ensure Aratek Assistant is running, then try again.");
            }

            // Attempt to open the Assistant protocol as well
            try {
                window.location.href = "AratekFMA://4397";
            } catch (ignored) {
                // ignore
            }
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
            clearCapturedData();
            closeWebcam();
            showAlert("info", "Form reset.");
        });
    }
});

