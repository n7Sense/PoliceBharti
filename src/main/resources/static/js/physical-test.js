// Page-specific logic for physical-test.html:
// - fetch candidate by applicationNo on Enter/Tab
// - fetch existing physical test (if any)
// - save height/chest/expandedChest as 1:1 PhysicalTest linked to Candidate


document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const alertContainer = document.getElementById("physicalTestAlertContainer");

    const form = document.getElementById("physicalTestForm");
    const resetBtn = document.getElementById("resetBtn");
    const saveBtn = document.getElementById("saveBtn"); // legacy button in left form (kept)

    const applicationNoEl = document.getElementById("applicationNo");
    const postEl = document.getElementById("post");
    const genderEl = document.getElementById("gender");
    const dobEl = document.getElementById("dob");
    const applicationCategoryEl = document.getElementById("applicationCategory");
    const parallelReservationEl = document.getElementById("parallelReservation");
    const mobileNoEl = document.getElementById("mobileNo");

    // Photo capture
    const photoBase64El = document.getElementById("photoBase64");
    const photoPreviewEl = document.getElementById("photoPreview");
    const photoPlaceholderEl = document.getElementById("photoPlaceholder");
    const storedPhotoPreviewEl = document.getElementById("storedPhotoPreview");
    const storedPhotoPlaceholderEl = document.getElementById("storedPhotoPlaceholder");
    const photoVerifiedCheckEl = document.getElementById("photoVerifiedCheck");

    const openCameraBtn = document.getElementById("openCameraBtn");
    const capturePhotoBtn = document.getElementById("capturePhotoBtn");
    const closeCameraBtn = document.getElementById("closeCameraBtn");
    const webcamVideo = document.getElementById("webcamVideo");
    const webcamCanvas = document.getElementById("webcamCanvas");

    // Biometrics capture (Aratek)
    const biometric1El = document.getElementById("biometric1");
    const biometric2El = document.getElementById("biometric2");
    const captureLeftThumbBtn = document.getElementById("captureLeftThumbBtn");
    const captureRightThumbBtn = document.getElementById("captureRightThumbBtn");
    const verifyCandidateBtn = document.getElementById("verifyCandidateBtn");
    const verificationStatusEl = document.getElementById("verificationStatus");

    const heightEl = document.getElementById("height");
    const chestEl = document.getElementById("chest");
    const expandedChestEl = document.getElementById("expandedChest");
    const savePhysicalTestBtn = document.getElementById("savePhysicalTestBtn");
    const downloadPdfBtn = document.getElementById("downloadPdfBtn");

    const rejectReasonEl = document.getElementById("rejectReason");

    let candidateLoaded = false;
    let verificationPassed = false;
    let webcamStream = null;

    // Stored DB templates for matching
    let storedBiometric1 = "";
    let storedBiometric2 = "";

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

    function clearPhysicalFields() {
        if (heightEl) heightEl.value = "";
        if (chestEl) chestEl.value = "";
        if (expandedChestEl) expandedChestEl.value = "";
        if (rejectReasonEl) rejectReasonEl.value = "";
    }

    function clearVerification() {
        verificationPassed = false;
        storedBiometric1 = "";
        storedBiometric2 = "";
        if (verificationStatusEl) verificationStatusEl.textContent = "Not verified";
        if (photoVerifiedCheckEl) photoVerifiedCheckEl.checked = false;
        if (storedPhotoPreviewEl) {
            storedPhotoPreviewEl.style.display = "none";
            storedPhotoPreviewEl.src = "";
        }
        if (storedPhotoPlaceholderEl) storedPhotoPlaceholderEl.style.display = "block";
        if (biometric1El) biometric1El.value = "";
        if (biometric2El) biometric2El.value = "";
        if (photoBase64El) photoBase64El.value = "";
        if (photoPreviewEl) {
            photoPreviewEl.style.display = "none";
            photoPreviewEl.src = "";
        }
        if (photoPlaceholderEl) photoPlaceholderEl.style.display = "block";
        if (savePhysicalTestBtn) savePhysicalTestBtn.disabled = true;
        if (downloadPdfBtn) downloadPdfBtn.disabled = true;
    }

    function formatDob(dob) {
        if (!dob) return "";
        // Can be "yyyy-MM-dd" OR [yyyy,mm,dd] OR {year,month,day}
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
        clearPhysicalFields();
        if (!appNo) return;

        try {
            const res = await fetch(`/api/v1/physical-tests?applicationNo=${encodeURIComponent(appNo)}`, { method: "GET" });
            if (res.status === 404) {
                return; // no physical test yet
            }
            if (!res.ok) return;
            const data = await res.json();
            if (heightEl) heightEl.value = data.height ?? "";
            if (chestEl) chestEl.value = data.chest ?? "";
            if (expandedChestEl) expandedChestEl.value = data.expandedChest ?? "";
            if (rejectReasonEl) rejectReasonEl.value = data.rejectReason ?? "";

            if (downloadPdfBtn) downloadPdfBtn.disabled = false;
        } catch (e) {
            console.error(e);
        }
    }

    async function fetchCandidateByApplicationNo(appNo) {
        clearCandidateFields();
        clearPhysicalFields();
        clearVerification();

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
            showAlert("success", "Candidate details loaded. Now verify candidate (photo + fingerprints) before saving physical test.");
            await fetchPhysicalTest(appNo);
            await fetchVerificationData(appNo);
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

    // Webcam capture (same pattern as add-candidate)
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
        if (webcamVideo) {
            webcamVideo.srcObject = null;
            webcamVideo.style.display = "none";
        }
        if (capturePhotoBtn) capturePhotoBtn.disabled = true;
        if (closeCameraBtn) closeCameraBtn.disabled = true;
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
        if (photoBase64El) photoBase64El.value = dataUrl;
        if (photoPreviewEl) {
            photoPreviewEl.src = dataUrl;
            photoPreviewEl.style.display = "block";
        }
        if (photoPlaceholderEl) photoPlaceholderEl.style.display = "none";
        showAlert("success", "Photo captured successfully. Now capture fingerprints and click Verify.");
    }

    if (openCameraBtn) openCameraBtn.addEventListener("click", openWebcam);
    if (closeCameraBtn) closeCameraBtn.addEventListener("click", closeWebcam);
    if (capturePhotoBtn) capturePhotoBtn.addEventListener("click", capturePhoto);

    // Hook capture callback so biometric1/2 inputs get filled from SDK results
    function hookCaptureCallback() {
        if (typeof client === "undefined") return;
        if (client.__ufrs_hooked) return;
        if (typeof client.OnCaptureFingerData !== "function") return;

        client.__ufrs_hooked = true;
        const original = client.OnCaptureFingerData;
        client.OnCaptureFingerData = function (code, msg, image) {
            try {
                if (code === 0 && image) {
                    if (typeof flag !== "undefined") {
                        if (flag === 1) {
                            if (biometric1El) biometric1El.value = image.feature_data || feature1 || "";
                        } else if (flag === 2) {
                            if (biometric2El) biometric2El.value = image.feature_data || feature2 || "";
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

    if (document.readyState === "complete") {
        setTimeout(hookCaptureCallback, 0);
    } else {
        window.addEventListener("load", function () {
            setTimeout(hookCaptureCallback, 0);
        });
    }

    async function captureLeftThumb() {
        try {
            if (typeof capturefinger1 !== "function") throw new Error("capturefinger1 not available");
            capturefinger1();
            showAlert("info", "Left thumb capturing. After image appears, capture right thumb.");
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to start left thumb capture. Make sure device is connected.");
        }
    }

    async function captureRightThumb() {
        try {
            if (typeof capturefinger2 !== "function") throw new Error("capturefinger2 not available");
            capturefinger2();
            showAlert("info", "Right thumb capturing. Then click Verify Candidate.");
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to start right thumb capture. Make sure device is connected.");
        }
    }

    if (captureLeftThumbBtn) captureLeftThumbBtn.addEventListener("click", captureLeftThumb);
    if (captureRightThumbBtn) captureRightThumbBtn.addEventListener("click", captureRightThumb);

    async function fetchVerificationData(appNo) {
        try {
            const res = await fetch(`/api/v1/candidates/verification-data?applicationNo=${encodeURIComponent(appNo)}`, {
                method: "GET"
            });
            if (!res.ok) return;
            const data = await res.json();

            // stored photo (for visual match)
            if (data.photo && storedPhotoPreviewEl) {
                storedPhotoPreviewEl.src = data.photo.startsWith("data:") ? data.photo : ("data:image/jpeg;base64," + data.photo);
                storedPhotoPreviewEl.style.display = "block";
                if (storedPhotoPlaceholderEl) storedPhotoPlaceholderEl.style.display = "none";
            }

            storedBiometric1 = data.biometric1 || "";
            storedBiometric2 = data.biometric2 || "";

            if (!storedBiometric1 || !storedBiometric2) {
                showAlert("warning", "Stored biometrics not found for this candidate. Please enroll candidate first.");
            }
        } catch (e) {
            console.error(e);
        }
    }

    function verifyFeaturePair(storedFeature, capturedFeature, timeoutMs = 8000) {
        return new Promise((resolve, reject) => {
            if (typeof client === "undefined" || typeof client.Verify !== "function") {
                reject(new Error("Biometric SDK not ready"));
                return;
            }

            const prev = client.OnVerify;
            const timer = setTimeout(() => {
                client.OnVerify = prev;
                reject(new Error("verify timeout"));
            }, timeoutMs);

            client.OnVerify = function (code, msg, score, result) {
                clearTimeout(timer);
                client.OnVerify = prev;
                if (code === 0) {
                    resolve({ result: !!result, score });
                } else {
                    reject(new Error(msg || ("Match failed: " + code)));
                }
            };

            try {
                // Use a strict security level
                client.Verify(storedFeature, capturedFeature, SecurityLevel.Level_4);
            } catch (e) {
                clearTimeout(timer);
                client.OnVerify = prev;
                reject(e);
            }
        });
    }

    async function verifyCandidate() {
        const appNoText = applicationNoEl.value.trim();
        if (!appNoText) {
            showAlert("danger", "Application No is required.");
            return;
        }
        if (!candidateLoaded) {
            showAlert("warning", "Please fetch candidate details first.");
            return;
        }

        const capturedPhoto = photoBase64El?.value || "";
        const capturedB1 = biometric1El?.value || "";
        const capturedB2 = biometric2El?.value || "";

        if (!storedBiometric1 || !storedBiometric2) {
            showAlert("danger", "Stored biometrics not available in DB. Please enroll candidate first.");
            return;
        }
        if (!capturedPhoto) {
            showAlert("danger", "Please capture live photo first.");
            return;
        }
        if (!capturedB1 || !capturedB2) {
            showAlert("danger", "Please capture both thumbs first.");
            return;
        }
        if (photoVerifiedCheckEl && !photoVerifiedCheckEl.checked) {
            showAlert("warning", "Please confirm photo matches stored candidate photo.");
            return;
        }

        try {
            if (verificationStatusEl) verificationStatusEl.textContent = "Verifying...";
            if (verifyCandidateBtn) verifyCandidateBtn.disabled = true;

            const left = await verifyFeaturePair(storedBiometric1, capturedB1);
            const right = await verifyFeaturePair(storedBiometric2, capturedB2);

            if (left.result && right.result) {
                verificationPassed = true;
                if (verificationStatusEl) verificationStatusEl.textContent = `Verified (L:${left.score}, R:${right.score})`;
                showAlert("success", "Candidate verified successfully. You can now save Physical Test.");
                if (savePhysicalTestBtn) savePhysicalTestBtn.disabled = false;
            } else {
                verificationPassed = false;
                if (verificationStatusEl) verificationStatusEl.textContent = `Not verified (L:${left.score}, R:${right.score})`;
                showAlert("danger", "Fingerprint mismatch. Candidate NOT verified.");
                if (savePhysicalTestBtn) savePhysicalTestBtn.disabled = true;
            }
        } catch (e) {
            console.error(e);
            verificationPassed = false;
            if (verificationStatusEl) verificationStatusEl.textContent = "Verification failed";
            showAlert("danger", "Verification failed. Ensure Aratek Assistant is running and device is connected.");
            if (savePhysicalTestBtn) savePhysicalTestBtn.disabled = true;
        } finally {
            if (verifyCandidateBtn) verifyCandidateBtn.disabled = false;
        }
    }

    if (verifyCandidateBtn) verifyCandidateBtn.addEventListener("click", verifyCandidate);

    async function savePhysicalTest() {
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
        if (!verificationPassed) {
            showAlert("danger", "Please verify candidate (photo + fingerprints) before saving physical test.");
            return;
        }

        const payload = {
            applicationNo: Number(appNoText),
            height: heightEl && heightEl.value !== "" ? Number(heightEl.value) : null,
            chest: chestEl && chestEl.value !== "" ? Number(chestEl.value) : null,
            expandedChest: expandedChestEl && expandedChestEl.value !== "" ? Number(expandedChestEl.value) : null,
            rejectReason: rejectReasonEl ? (rejectReasonEl.value || null) : null
        };

        try {
            if (savePhysicalTestBtn) {
                savePhysicalTestBtn.disabled = true;
                savePhysicalTestBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Saving...';
            }

            const res = await fetch("/api/v1/physical-tests", {
                method: "POST",
                headers: buildCsrfHeaders(),
                body: JSON.stringify(payload)
            });

            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = (data && data.message) ? data.message : "Failed to save candidate.";
                throw new Error(msg);
            }

            showAlert("success", (data && data.message) ? data.message : "Physical test saved successfully");
            if (downloadPdfBtn) downloadPdfBtn.disabled = false;
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to save physical test.");
        } finally {
            if (savePhysicalTestBtn) {
                savePhysicalTestBtn.disabled = false;
                savePhysicalTestBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Save Physical Test';
            }
        }
    }

    if (form) {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            savePhysicalTest();
        });
    }

    if (savePhysicalTestBtn) {
        savePhysicalTestBtn.addEventListener("click", () => savePhysicalTest());
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
            clearPhysicalFields();
            clearVerification();
            closeWebcam();
            showAlert("info", "Form reset.");
        });
    }
});

