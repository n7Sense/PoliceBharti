var operate_model;
var client = new Client();
var device_id;
var dpi;
var feature1;
var feature2;
var feature3;
var flag = 0;
var bmpdata;
var imagewidth = 0;
var imageheight = 0;
var LED = 0;
var Status = 0;
var begin_cap = false;
var show_img_index = 0;
var CID_img_1;
var CID_img_2;
var CID_fea_1;
var CID_fea_2;
var ImageMatch = false;

window.onload = function () {

    client.OnOpenDevice = function (code, msg) {

        if(code === 0) {
            console.log("Open Device Success : "+ code + " "+ msg);
        } else {
            alert("Open Device Failed : "+ code + " "+ msg);
        }
        //alert(code === 0 ? "Open Device Success" : "Open Device Failed: " + code + " " + msg);
    };

    client.OnCloseDevice = function (code, msg) {
        alert(code === 0 ? "Close Device Success" : "Close Device Failed: " + code + " " + msg);
    };

    client.OnGetDeviceCount = function (code, msg, count) {

        if(code === 0) {
            console.log("Device Count: "+ count + " "+ msg);
        } else {
            alert("Get Device Count Failed: "+ code + " "+ msg);
        }
        //alert(code === 0 ? "Device Count: " + count : "Get Device Count Failed: " + code + " " + msg);
    };

    client.OnGetDeviceDescription = function (code, msg, info) {
        if (code === 0) {

            console.log(
                "Serial Number: " + info.sn + "\n" +
                "Manufacturer: " + info.manufacturer + "\n" +
                "Product Name: " + info.productname
            );
            /*alert(
                "Serial Number: " + info.sn + "\n" +
                "Manufacturer: " + info.manufacturer + "\n" +
                "Product Name: " + info.productname
            );*/
            device_id = info.device_id;
            imagewidth = info.image_width;
            imageheight = info.iamge_height;
            dpi = info.dpi;
        } else {
            alert("Get Device Description Failed: " + code + " " + msg);
        }
    };

    // client.OnCaptureFingerData = function (code, msg, image) {
    //     if (code !== 0) {
    //         alert("Capture Failed: " + code + " " + msg);
    //         return;
    //     }

    //     if (image.image_format === ImageFormatType.IBmp) {
    //         var img =
    //             flag % 3 === 1 ? "img1" :
    //             flag % 3 === 2 ? "img2" : "img3";

    //         document.getElementById(img).src =
    //             "data:image/bmp;base64," + image.image_data;

    //         bmpdata = image.image_data;
    //     }

    //     if (flag % 3 === 1) feature1 = image.feature_data;
    //     else if (flag % 3 === 2) feature2 = image.feature_data;
    //     else feature3 = image.feature_data;
    // };
    client.OnCaptureFingerData = function (code, msg, image) {
    if (code !== 0) {
        alert("Capture Failed: " + code + " " + msg);
        return;
    }

    // Show image
    if (image.image_format === ImageFormatType.IBmp) {
        var imgId =
            flag === 1 ? "img1" :
            flag === 2 ? "img2" : "img3";

        document.getElementById(imgId).src =
            "data:image/bmp;base64," + image.image_data;

        bmpdata = image.image_data;
    }

    // Store feature
    if (flag === 1) feature1 = image.feature_data;
    else if (flag === 2) feature2 = image.feature_data;
    else if (flag === 3) feature3 = image.feature_data;

    // 👉 Convert BMP → RAW to get quality
    client.BmpToRaw(image.image_data, imagewidth, imageheight);
};



    client.OnStartCapture = function (code, msg, image) {
        if (code === 0 || code === -211) {
            document.getElementById("img1").src =
                "data:image/bmp;base64," + image.image_data;
        } else {
            alert("Start Preview Failed: " + code + " " + msg);
        }
    };

    client.OnStopCapture = function (code) {
        if (code !== 0) alert("Stop Preview Failed: " + code);
    };

    client.OnGeneralizeTemplate = function (code, msg, template) {
        if (code === 0) {
            alert("Template Generated Successfully");
            document.getElementById("imgdata").value = template;
        } else {
            alert("Template Generation Failed: " + code + " " + msg);
        }
    };

    client.OnVerify = function (code, msg, score, result) {
        if (code === 0) {
            alert(result ? "Match Success. Score: " + score : "Mismatch");
        } else {
            alert("Match Failed: " + code + " " + msg);
        }
    };

    client.OnCID_Alg_GetQualityScore = function (code, msg, score) {
    if (code !== 1) {
        alert("Get Quality Score Failed: " + code + " " + msg);
        return;
    }

    /*
      score object usually contains:
      score.NFIQ
      score.QualityScore
      (field names depend on SDK, see console.log)
    */

    console.log("Quality Score Object:", score);

    alert(
        "Capture Image " + flag + " Successful\n\n" +
        "NFIQ Score : " + score.NFIQ + "\n" +
        "Quality Score : " + score.QualityScore
    );
};

    client.OnBmpToRaw = function (code, msg, imagedata) {
        if (code !== 0) {
            alert("Format Conversion Failed: " + code + " " + msg);
            return;
        }

        if (operate_model === 1) {
            if (ImageMatch) {
                client.CID_Alg_ImageMatch(imagedata, CID_fea_2);
                ImageMatch = false;
            } else {
                client.CID_Alg_FeatureExtract(ID_FingerPosition.UNKNOWN, imagedata);
            }
        } else {
            alert("Format Conversion Success");
            document.getElementById("imgdata").value = imagedata;
        }
        // Use the converted RAW image data returned by SDK
        client.CID_Alg_GetQualityScore(imagedata);
    };

    client.OnCID_Alg_FeatureExtract = function (code, msg, featuredata) {
        if (code === 1) {
            if (show_img_index === 1) {
                CID_fea_1 = featuredata;
                document.getElementById("Fea1").value = featuredata;
            } else if (show_img_index === 2) {
                CID_fea_2 = featuredata;
                document.getElementById("Fea2").value = featuredata;
            }
            alert("Feature Extracted Successfully");
        } else {
            alert("Feature Extraction Failed: " + code);
        }
    };

    client.OnCID_Alg_FeatureMatch = function (code, msg, similarity) {
        alert(
            code === 1
                ? "Match Success. Similarity: " + similarity
                : "Match Failed: " + code
        );
    };

    client.OnCID_Alg_ImageMatch = function (code, msg, similarity) {
        alert(
            code === 1
                ? "Match Success. Similarity: " + similarity
                : "Match Failed: " + code
        );
    };
};

/* ---------- COMMON HELPERS ---------- */

function ensureConnected() {
    if (!client.isConnect) {
        alert("Assistant Program is not connected");
        return false;
    }
    return true;
}

/* ---------- DEVICE OPERATIONS ---------- */

function connect() { client.Connect_Server(); }

function open_device() {
    if (ensureConnected()) client.OpenDevice(0);
}

function close_device() {
    if (ensureConnected()) client.CloseDevice();
}

function get_device_count() {
    if (ensureConnected()) client.GetDeviceCount();
}

function get_device_desc() {
    if (ensureConnected()) client.GetDeviceDescription(0);
}

/* ---------- TRUSTFINGER ---------- */

function capturefinger1() { flag = 1; capture(); }
function capturefinger2() { flag = 2; capture(); }
function capturefinger3() { flag = 3; capture(); }

function capture() {
    if (!ensureConnected()) return;
    if (!device_id) return alert("Get Device Description first");

    client.CaptureFingerData(
        ImageFormatType.IBmp,
        FeatureFormatType.FBione,
        3,
        FingerPosition.UNKNOWN,
        CompressionType.UnCompressed,
        device_id
    );
}

function generalizetemp() {
    if (!ensureConnected()) return;
    if (!feature1 || !feature2 || !feature3)
        return alert("Capture three fingerprints first");

    client.GeneralizeTemplate(feature1, feature2, feature3);
}

function verify() {
    if (!ensureConnected()) return;
    if (!feature2 || !feature3)
        return alert("Capture fingerprint 2 and 3 first");

    client.Verify(feature2, feature3, SecurityLevel.Level_4);
}

/* ---------- CID ---------- */

function LIVESCAN_Init() {
    if (ensureConnected()) client.CID_Init(CIDDeviceType.CID4000);
}

function LIVESCAN_Close() {
    if (ensureConnected()) client.CID_Close();
}

function LIVESCAN_GetChannelCount() {
    if (ensureConnected()) client.CID_GetChannelCount();
}

function LIVESCAN_GetDesc() {
    if (ensureConnected()) client.CID_GetDesc();
}

function CID_Cap1() { cidCapture(1); }
function CID_Cap2() { cidCapture(2); }

function cidCapture(index) {
    if (!ensureConnected()) return;
    begin_cap = false;
    show_img_index = index;
    client.CID_BeginCapture(0);
}

function CID_Ext1() { cidExtract(1, CID_img_1); }
function CID_Ext2() { cidExtract(2, CID_img_2); }

function cidExtract(index, img) {
    if (!ensureConnected()) return;
    if (!img) return alert("Capture image first");

    show_img_index = index;
    operate_model = 1;
    client.BmpToRaw(img, 256, 360);
}

function FP_FeatureMatch() {
    if (!ensureConnected()) return;
    if (!CID_fea_1 || !CID_fea_2)
        return alert("Extract both features first");

    client.CID_Alg_FeatureMatch(CID_fea_1, CID_fea_2);
}

function FP_ImageMatch() {
    if (!ensureConnected()) return;
    if (!CID_img_1 || !CID_fea_2)
        return alert("Capture image and extract feature first");

    operate_model = 1;
    ImageMatch = true;
    client.BmpToRaw(CID_img_1, 256, 360);
}
