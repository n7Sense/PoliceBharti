// Page-specific logic for document-verification.html
// - fetch candidate by applicationNo on Enter/Tab
// - build certificate checklist based on candidate flags + mandatory rules
// - validate required certificates and submit decision to backend

document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    const alertContainer = document.getElementById("documentVerificationAlertContainer");

    const form = document.getElementById("documentVerificationForm");
    const resetBtn = document.getElementById("resetBtn");
    const submitBtn = document.getElementById("submitVerificationBtn");

    const applicationNoEl = document.getElementById("applicationNo");
    const nameEl = document.getElementById("name");
    const naxaliteAreaEl = document.getElementById("naxaliteArea");
    const documentStatusEl = document.getElementById("documentStatus");

    const genderEl = document.getElementById("gender");
    const mobileNoEl = document.getElementById("mobileNo");
    const dobEl = document.getElementById("dob");
    const ageEl = document.getElementById("age");
    const emailEl = document.getElementById("email");
    const postEl = document.getElementById("post");
    const religionEl = document.getElementById("religion");
    const applicationCategoryEl = document.getElementById("applicationCategory");
    const parallelReservationEl = document.getElementById("parallelReservation");

    const candidatePhotoEl = document.getElementById("candidatePhoto");
    const candidatePhotoPlaceholderEl = document.getElementById("candidatePhotoPlaceholder");

    const mandatoryContainer = document.getElementById("mandatoryCertificatesContainer");
    const dynamicContainer = document.getElementById("dynamicCertificatesContainer");

    let candidateLoaded = false;
    let lastFetchedAppNo = null;

    function buildCsrfHeaders() {
        const headers = { "Content-Type": "application/json" };
        if (csrfToken && csrfHeaderName) {
            headers[csrfHeaderName] = csrfToken;
        }
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
        if (nameEl) nameEl.value = "";
        if (naxaliteAreaEl) naxaliteAreaEl.value = "";
        if (documentStatusEl) documentStatusEl.value = "";
        if (genderEl) genderEl.value = "";
        if (mobileNoEl) mobileNoEl.value = "";
        if (dobEl) dobEl.value = "";
        if (ageEl) ageEl.value = "";
        if (emailEl) emailEl.value = "";
        if (postEl) postEl.value = "";
        if (religionEl) religionEl.value = "";
        if (applicationCategoryEl) applicationCategoryEl.value = "";
        if (parallelReservationEl) parallelReservationEl.value = "";
        if (candidatePhotoEl) {
            candidatePhotoEl.src = "";
            candidatePhotoEl.style.display = "none";
        }
        if (candidatePhotoPlaceholderEl) {
            candidatePhotoPlaceholderEl.style.display = "block";
        }
        candidateLoaded = false;
        lastFetchedAppNo = null;
    }

    function clearCertificates() {
        if (mandatoryContainer) mandatoryContainer.innerHTML = "";
        if (dynamicContainer) dynamicContainer.innerHTML = "";
    }

    function createCertificateRow(id, label, required) {
        const wrapper = document.createElement("div");
        wrapper.className = "certificate-check";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.id = id;
        checkbox.dataset.required = required ? "true" : "false";
        checkbox.className = "certificate-checkbox";

        const visual = document.createElement("div");
        visual.className = "certificate-check-visual";
        visual.innerHTML = '<i class="bi bi-check-lg"></i>';

        const labelEl = document.createElement("label");
        labelEl.className = "certificate-check-label";
        labelEl.htmlFor = id;

        const pill = document.createElement("span");
        pill.className = "certificate-pill";
        pill.innerHTML = '<i class="bi bi-file-earmark-text"></i><span>Certificate Verified</span>';

        const titleSpan = document.createElement("span");
        titleSpan.textContent = label;

        labelEl.appendChild(titleSpan);
        labelEl.appendChild(document.createElement("br"));
        labelEl.appendChild(pill);

        wrapper.appendChild(checkbox);
        wrapper.appendChild(visual);
        wrapper.appendChild(labelEl);

        return wrapper;
    }

    function buildMandatoryCertificates(naxaliteAreaFlag) {
        if (!mandatoryContainer) return;
        mandatoryContainer.innerHTML = "";

        if (naxaliteAreaFlag === true) {
            // Special rule – only Seventh Result Marksheet (Mandatory)
            const seventhRow = createCertificateRow(
                "cert_seventh_result",
                "Seventh Result Marksheet (Mandatory)",
                true
            );
            mandatoryContainer.appendChild(seventhRow);
        } else {
            const sscRow = createCertificateRow(
                "cert_ssc_result",
                "SSC Result (Mandatory)",
                true
            );
            const hscRow = createCertificateRow(
                "cert_hsc_result",
                "HSC Result (Mandatory)",
                true
            );
            mandatoryContainer.appendChild(sscRow);
            mandatoryContainer.appendChild(hscRow);
        }
    }

    function buildDynamicCertificates(candidateDto) {
        if (!dynamicContainer) return;
        dynamicContainer.innerHTML = "";

        const mapping = [
            { field: "exSoldier", id: "cert_ex_soldier", label: "Ex-Soldier Certificate" },
            { field: "nonCremelayer", id: "cert_non_cremelayer", label: "Non-Creamy Layer Certificate" },
            { field: "maharashtraDomicile", id: "cert_maharashtra_domicile", label: "Maharashtra Domicile Certificate" },
            { field: "karnatakaDomicile", id: "cert_karnataka_domicile", label: "Karnataka Domicile Certificate" },
            { field: "homeGuard", id: "cert_home_guard", label: "Home Guard Certificate" },
            { field: "prakalpgrast", id: "cert_prakalpgrast", label: "Prakalpgrast Certificate" },
            { field: "bhukampgrast", id: "cert_bhukampgrast", label: "Bhukampgrast Certificate" },
            { field: "sportsperson", id: "cert_sportsperson", label: "Sports Certificate" },
            { field: "femaleReservation", id: "cert_female_reservation", label: "Female Reservation Certificate" },
            { field: "parentInPolice", id: "cert_parent_in_police", label: "Parent in Police Certificate" },
            { field: "anath", id: "cert_anath", label: "Anath Certificate" },
            { field: "exServiceDependent", id: "cert_ex_service_dependent", label: "Ex-Service Dependent Certificate" },
            { field: "isNcc", id: "cert_is_ncc", label: "NCC Certificate" },
            { field: "naxaliteArea", id: "cert_naxalite_area", label: "Naxalite Area Certificate" },
            { field: "smallVehicle", id: "cert_small_vehicle", label: "Small Vehicle License" },
            { field: "workOnContract", id: "cert_work_on_contract", label: "Work on Contract Certificate" },
            { field: "mscit", id: "cert_mscit", label: "MSCIT Certificate" },
            { field: "isFarmerSuicide", id: "cert_farmer_suicide", label: "Farmer Suicide Dependent Certificate" }
        ];

        mapping.forEach(m => {
            const flag = candidateDto[m.field];
            if (flag === true) {
                const row = createCertificateRow(m.id, m.label, true);
                dynamicContainer.appendChild(row);
            }
        });

        // Application Category based certificate: ST Certificate only for ST category
        const appCat = (candidateDto.applicationCategory || "").trim().toUpperCase();
        if (appCat === "ST") {
            const stRow = createCertificateRow(
                "cert_st_category",
                "ST Category Certificate",
                true
            );
            dynamicContainer.appendChild(stRow);
        }
    }

    function formatDob(dob) {
        if (!dob) return "";
        if (typeof dob === "string") return dob;
        if (Array.isArray(dob) && dob.length >= 3) {
            const [y, m, d] = dob;
            if (!y || !m || !d) return "";
            return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        if (typeof dob === "object" && dob !== null) {
            const y = dob.year;
            const m = dob.monthValue ?? dob.month;
            const d = dob.dayOfMonth ?? dob.day;
            if (!y || !m || !d) return "";
            return `${String(y).padStart(4, "0")}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
        return "";
    }

    async function fetchDocumentVerificationData(appNo) {
        clearCandidateFields();
        clearCertificates();

        if (!appNo) {
            showAlert("danger", "Application No is required.");
            return;
        }

        try {
            const res = await fetch(`/api/v1/candidates/document-verification?applicationNo=${encodeURIComponent(appNo)}`, {
                method: "GET",
                credentials: "same-origin"
            });

            if (res.status === 404) {
                const data = await res.json().catch(() => null);
                showAlert("warning", (data && data.message) ? data.message : "Invalid Application Number. Candidate not found.");
                return;
            }

            if (!res.ok) {
                const data = await res.json().catch(() => null);
                const msg = (data && data.message) ? data.message : "Failed to fetch document verification data.";
                showAlert("danger", msg);
                return;
            }

            const data = await res.json();

            if (nameEl) nameEl.value = data.name ?? "";
            if (genderEl) genderEl.value = data.gender ?? "";
            if (mobileNoEl) mobileNoEl.value = data.mobileNo ?? "";
            if (dobEl) dobEl.value = formatDob(data.dob);
            if (ageEl) ageEl.value = data.age != null ? String(data.age) : "";
            if (emailEl) emailEl.value = data.email ?? "";
            if (postEl) postEl.value = data.post ?? "";
            if (religionEl) religionEl.value = data.religion ?? "";
            if (applicationCategoryEl) applicationCategoryEl.value = data.applicationCategory ?? "";
            if (parallelReservationEl) parallelReservationEl.value = data.parallelReservation ?? "";

            if (candidatePhotoEl) {
                if (data.photo) {
                    candidatePhotoEl.src = data.photo.startsWith("data:")
                        ? data.photo
                        : ("data:image/jpeg;base64," + data.photo);
                    candidatePhotoEl.style.display = "block";
                    if (candidatePhotoPlaceholderEl) candidatePhotoPlaceholderEl.style.display = "none";
                } else {
                    candidatePhotoEl.src = "";
                    candidatePhotoEl.style.display = "none";
                    if (candidatePhotoPlaceholderEl) candidatePhotoPlaceholderEl.style.display = "block";
                }
            }
            if (naxaliteAreaEl) naxaliteAreaEl.value = data.naxaliteArea === true ? "Yes" : "No";
            if (documentStatusEl) {
                if (data.documentStatus === true) {
                    documentStatusEl.value = "Verified";
                } else if (data.documentStatus === false) {
                    documentStatusEl.value = "Rejected";
                } else {
                    documentStatusEl.value = "Pending";
                }
            }

            buildMandatoryCertificates(data.naxaliteArea === true);
            buildDynamicCertificates(data);

            candidateLoaded = true;
            lastFetchedAppNo = data.applicationNo || Number(appNo);

            showAlert("success", "Candidate loaded. Please verify all required certificates and click Submit.");
        } catch (e) {
            console.error(e);
            showAlert("danger", "Unable to fetch document verification data. Please try again.");
        }
    }

    if (applicationNoEl) {
        applicationNoEl.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                const appNo = applicationNoEl.value.trim();
                fetchDocumentVerificationData(appNo);
            }
        });

        applicationNoEl.addEventListener("blur", () => {
            const appNo = applicationNoEl.value.trim();
            if (appNo) {
                fetchDocumentVerificationData(appNo);
            }
        });
    }

    function collectMissingCertificates() {
        const missing = [];
        const allCheckboxes = document.querySelectorAll(".certificate-checkbox");
        allCheckboxes.forEach(chk => {
            const required = chk.dataset.required === "true";
            if (required && !chk.checked) {
                const wrapper = chk.parentElement;
                const labelEl = wrapper?.querySelector(".certificate-check-label span");
                const name = labelEl ? labelEl.textContent || "" : chk.id;
                if (name) {
                    // Remove trailing "(Mandatory)" from display list
                    missing.push(name.replace(/\s*\(Mandatory\)\s*$/i, "").trim());
                }
            }
        });
        return missing;
    }

    async function submitDecision() {
        const appNoText = applicationNoEl?.value.trim();
        if (!appNoText) {
            showAlert("danger", "Application No is required.");
            applicationNoEl?.focus();
            return;
        }
        if (!candidateLoaded || !lastFetchedAppNo) {
            showAlert("warning", "Please fetch candidate details first (press Enter or Tab).");
            return;
        }

        const missing = collectMissingCertificates();
        const allVerified = missing.length === 0;

        if (!allVerified) {
            const list = missing.map(m => `- ${m}`).join("\n");
            const msg = `Missing Certificate:\n${list}\n\nAre you sure you want to reject this candidate?`;
            const confirmReject = window.confirm(msg);
            if (!confirmReject) {
                return;
            }
        }

        const payload = {
            applicationNo: lastFetchedAppNo,
            allRequiredVerified: allVerified
        };

        try {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Submitting...';

            const res = await fetch("/api/v1/candidates/document-verification", {
                method: "POST",
                headers: buildCsrfHeaders(),
                credentials: "same-origin",
                body: JSON.stringify(payload)
            });

            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = (data && data.message) ? data.message : "Failed to submit document verification.";
                throw new Error(msg);
            }

            if (documentStatusEl) {
                documentStatusEl.value = allVerified ? "Verified" : "Rejected";
            }

            showAlert("success", (data && data.message) ? data.message : "Document verification submitted successfully.");
        } catch (e) {
            console.error(e);
            showAlert("danger", e.message || "Failed to submit document verification.");
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Submit Verification';
        }
    }

    if (form) {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            submitDecision();
        });
    }

    if (submitBtn) {
        submitBtn.addEventListener("click", () => submitDecision());
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            if (applicationNoEl) applicationNoEl.value = "";
            clearCandidateFields();
            clearCertificates();
            showAlert("info", "Form reset.");
        });
    }
});

