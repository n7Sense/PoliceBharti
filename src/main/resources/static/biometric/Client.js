var CommandType;
(function (CommandType) {
    CommandType[CommandType["OpenDevice_Type"] = 1] = "OpenDevice_Type";
    CommandType[CommandType["CloseDevice_Type"] = 2] = "CloseDevice_Type";
    CommandType[CommandType["GetDeviceCount_Type"] = 3] = "GetDeviceCount_Type";
    CommandType[CommandType["GetDeviceDescription_Type"] = 4] = "GetDeviceDescription_Type";
    CommandType[CommandType["CaptureFingerData_Type"] = 5] = "CaptureFingerData_Type";
    CommandType[CommandType["StartCapture_Type"] = 6] = "StartCapture_Type";
    CommandType[CommandType["StopCapture_Type"] = 7] = "StopCapture_Type";
    CommandType[CommandType["GeneralizeTemplate_Type"] = 8] = "GeneralizeTemplate_Type";
    CommandType[CommandType["Verify_Type"] = 9] = "Verify_Type";
    CommandType[CommandType["SetOption_Type"] = 10] = "SetOption_Type";
    CommandType[CommandType["BmpToRaw_Type"] = 11] = "BmpToRaw_Type";
    CommandType[CommandType["BmpToWSQ_Type"] = 12] = "BmpToWSQ_Type";
    CommandType[CommandType["BmpToISO_Type"] = 13] = "BmpToISO_Type";
    CommandType[CommandType["BmpToANSI_Type"] = 14] = "BmpToANSI_Type";
    CommandType[CommandType["CID_Init_Type"] = 15] = "CID_Init_Type";
    CommandType[CommandType["CID_Close_Type"] = 16] = "CID_Close_Type";
    CommandType[CommandType["CID_GetChannelCount_Type"] = 17] = "CID_GetChannelCount_Type";
    CommandType[CommandType["CID_BeginCapture_Type"] = 18] = "CID_BeginCapture_Type";
    CommandType[CommandType["CID_GetFPRawData_Type"] = 19] = "CID_GetFPRawData_Type";
    CommandType[CommandType["CID_GetFPBmpData_Type"] = 20] = "CID_GetFPBmpData_Type";
    CommandType[CommandType["CID_EndCapture_Type"] = 21] = "CID_EndCapture_Type";
    CommandType[CommandType["CID_GetVersion_Type"] = 22] = "CID_GetVersion_Type";
    CommandType[CommandType["CID_GetDesc_Type"] = 23] = "CID_GetDesc_Type";
    CommandType[CommandType["CID_Alg_GetVersion_Type"] = 25] = "CID_Alg_GetVersion_Type";
    CommandType[CommandType["CID_Alg_Begin_Type"] = 26] = "CID_Alg_Begin_Type";
    CommandType[CommandType["CID_Alg_FeatureExtract_Type"] = 27] = "CID_Alg_FeatureExtract_Type";
    CommandType[CommandType["CID_Alg_FeatureMatch_Type"] = 28] = "CID_Alg_FeatureMatch_Type";
    CommandType[CommandType["CID_Alg_ImageMatch_Type"] = 29] = "CID_Alg_ImageMatch_Type";
    CommandType[CommandType["CID_Alg_GetQualityScore_Type"] = 32] = "CID_Alg_GetQualityScore_Type";
    CommandType[CommandType["CID_Alg_End_Type"] = 35] = "CID_Alg_End_Type";
})(CommandType || (CommandType = {}));
var FingerPosition;
(function (FingerPosition) {
    FingerPosition[FingerPosition["UNKNOWN"] = 0] = "UNKNOWN";
    FingerPosition[FingerPosition["RIGHT_THUMB"] = 1] = "RIGHT_THUMB";
    FingerPosition[FingerPosition["RIGHT_INDEX"] = 2] = "RIGHT_INDEX";
    FingerPosition[FingerPosition["RIGHT_MIDDLE"] = 3] = "RIGHT_MIDDLE";
    FingerPosition[FingerPosition["RIGHT_RING"] = 4] = "RIGHT_RING";
    FingerPosition[FingerPosition["RIGHT_LITTLE"] = 5] = "RIGHT_LITTLE";
    FingerPosition[FingerPosition["LEFT_THUMB"] = 6] = "LEFT_THUMB";
    FingerPosition[FingerPosition["LEFT_INDEX"] = 7] = "LEFT_INDEX";
    FingerPosition[FingerPosition["LEFT_MIDDLE"] = 8] = "LEFT_MIDDLE";
    FingerPosition[FingerPosition["LEFT_RING"] = 9] = "LEFT_RING";
    FingerPosition[FingerPosition["LEFT_LITTLE"] = 10] = "LEFT_LITTLE";
})(FingerPosition || (FingerPosition = {}));
var ID_FingerPosition;
(function (ID_FingerPosition) {
    ID_FingerPosition[ID_FingerPosition["UNKNOWN"] = 99] = "UNKNOWN";
    ID_FingerPosition[ID_FingerPosition["RIGHT_THUMB"] = 11] = "RIGHT_THUMB";
    ID_FingerPosition[ID_FingerPosition["RIGHT_INDEX"] = 12] = "RIGHT_INDEX";
    ID_FingerPosition[ID_FingerPosition["RIGHT_MIDDLE"] = 13] = "RIGHT_MIDDLE";
    ID_FingerPosition[ID_FingerPosition["RIGHT_RING"] = 14] = "RIGHT_RING";
    ID_FingerPosition[ID_FingerPosition["RIGHT_LITTLE"] = 15] = "RIGHT_LITTLE";
    ID_FingerPosition[ID_FingerPosition["LEFT_THUMB"] = 16] = "LEFT_THUMB";
    ID_FingerPosition[ID_FingerPosition["LEFT_INDEX"] = 17] = "LEFT_INDEX";
    ID_FingerPosition[ID_FingerPosition["LEFT_MIDDLE"] = 18] = "LEFT_MIDDLE";
    ID_FingerPosition[ID_FingerPosition["LEFT_RING"] = 19] = "LEFT_RING";
    ID_FingerPosition[ID_FingerPosition["LEFT_LITTLE"] = 20] = "LEFT_LITTLE";
})(ID_FingerPosition || (ID_FingerPosition = {}));
var CompressionType;
(function (CompressionType) {
    CompressionType[CompressionType["UnCompressed"] = 0] = "UnCompressed";
    CompressionType[CompressionType["BitPacked"] = 1] = "BitPacked";
    CompressionType[CompressionType["WSQ"] = 2] = "WSQ";
    CompressionType[CompressionType["JPEG"] = 3] = "JPEG";
    CompressionType[CompressionType["JPEG2000"] = 4] = "JPEG2000";
    CompressionType[CompressionType["PNG"] = 5] = "PNG";
})(CompressionType || (CompressionType = {}));
var SecurityLevel;
(function (SecurityLevel) {
    SecurityLevel[SecurityLevel["Level_1"] = 1] = "Level_1";
    SecurityLevel[SecurityLevel["Level_2"] = 2] = "Level_2";
    SecurityLevel[SecurityLevel["Level_3"] = 3] = "Level_3";
    SecurityLevel[SecurityLevel["Level_4"] = 4] = "Level_4";
    SecurityLevel[SecurityLevel["Level_5"] = 5] = "Level_5";
})(SecurityLevel || (SecurityLevel = {}));
var ImageFormatType;
(function (ImageFormatType) {
    ImageFormatType[ImageFormatType["IRaw"] = 1] = "IRaw";
    ImageFormatType[ImageFormatType["IBmp"] = 2] = "IBmp";
    ImageFormatType[ImageFormatType["IISO"] = 3] = "IISO";
    ImageFormatType[ImageFormatType["IANSI"] = 4] = "IANSI";
})(ImageFormatType || (ImageFormatType = {}));
var FeatureFormatType;
(function (FeatureFormatType) {
    FeatureFormatType[FeatureFormatType["FBione"] = 1] = "FBione";
    FeatureFormatType[FeatureFormatType["FISO"] = 2] = "FISO";
    FeatureFormatType[FeatureFormatType["FANSI"] = 3] = "FANSI";
})(FeatureFormatType || (FeatureFormatType = {}));
var CIDDeviceType;
(function (CIDDeviceType) {
    CIDDeviceType[CIDDeviceType["CID4000"] = 1] = "CID4000";
})(CIDDeviceType || (CIDDeviceType = {}));
var NOT_CONNECT = -1;
var Client = /** @class */ (function () {
    function Client() {
        this.url = 'ws://127.0.0.1:4397';
        this.ws = null;
        this.isConnect = false;
    }
    Client.prototype.SendMessage = function (msg) {
        if (this.isConnect) {
            this.ws.send(msg);
        }
    };
    Client.prototype.OpenDevice = function (index) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.OpenDevice_Type,
            "Index": index
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CloseDevice = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CloseDevice_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.GetDeviceCount = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.GetDeviceCount_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.GetDeviceDescription = function (index) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.GetDeviceDescription_Type,
            "Index": index
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CaptureFingerData = function (image_format, feature_format, timeout, fingerposition, compression_type, device_id) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CaptureFingerData_Type,
            "Image_Format": image_format,
            "Feature_Format": feature_format,
            "TimeOut": timeout,
            "Finger_Position": fingerposition,
            "Compression_Type": compression_type,
            "Device_ID": device_id
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.StartCapture = function (feature_format, fingerposition) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.StartCapture_Type,
            "Feature_Format": feature_format,
            "Finger_Position": fingerposition
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.StopCapture = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.StopCapture_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.GeneralizeTemplate = function (fea1, fea2, fea3) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.GeneralizeTemplate_Type,
            "Feature_1": fea1,
            "Feature_2": fea2,
            "Feature_3": fea3
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.Verify = function (fea1, fea2, security_level) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.Verify_Type,
            "Feature_1": fea1,
            "Feature_2": fea2,
            "Security_Level": security_level
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.SetOption = function (LED_index, LED_status) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.SetOption_Type,
            "LED_Index": LED_index,
            "LED_Status": LED_status
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.BmpToRaw = function (bmp_imagedata, image_width, image_height) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.BmpToRaw_Type,
            "Bmp_ImageData": bmp_imagedata,
            "Image_Width": image_width,
            "Image_Height": image_height
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.BmpToWSQ = function (bmp_imagedata, image_width, image_height, dpi, bitrate) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.BmpToWSQ_Type,
            "Bmp_ImageData": bmp_imagedata,
            "Image_Width": image_width,
            "Image_Height": image_height,
            "DPI": dpi,
            "Bitrate": bitrate
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.BmpToISO = function (bmp_imagedata, image_width, image_height, fingerposition, compression_type, deviceid) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.BmpToISO_Type,
            "Bmp_ImageData": bmp_imagedata,
            "Image_Width": image_width,
            "Image_Height": image_height,
            "Finger_Position": fingerposition,
            "Compression_Type": compression_type,
            "Device_ID": deviceid
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.BmpToANSI = function (bmp_imagedata, image_width, image_height, fingerposition, compression_type, deviceid) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.BmpToANSI_Type,
            "Bmp_ImageData": bmp_imagedata,
            "Image_Width": image_width,
            "Image_Height": image_height,
            "Finger_Position": fingerposition,
            "Compression_Type": compression_type,
            "Device_ID": deviceid
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Init = function (devicetype) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Init_Type,
            "DeviceType": devicetype
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Close = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Close_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_GetChannelCount = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_GetChannelCount_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_BeginCapture = function (nChannel) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_BeginCapture_Type,
            "nChannel": nChannel
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_GetFPRawData = function (nChannel) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_GetFPRawData_Type,
            "nChannel": nChannel
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_GetFPBmpData = function (nChannel) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_GetFPBmpData_Type,
            "nChannel": nChannel
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_EndCapture = function (nChannel) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_EndCapture_Type,
            "nChannel": nChannel
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_GetVersion = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_GetVersion_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_GetDesc = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_GetDesc_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_GetVersion = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_GetVersion_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_Begin = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_Begin_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_FeatureExtract = function (fingerposition, image_rawdata) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_FeatureExtract_Type,
            "FingerPosition": fingerposition,
            "Image_RawData": image_rawdata
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_FeatureMatch = function (featuredata1, featuredata2) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_FeatureMatch_Type,
            "FeatureData1": featuredata1,
            "FeatureData2": featuredata2
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_ImageMatch = function (image_rawdata, featuredata) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_ImageMatch_Type,
            "RawImageData": image_rawdata,
            "FeatureData": featuredata
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_GetQualityScore = function (image_rawdata) {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_GetQualityScore_Type,
            "RawImageData": image_rawdata
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.CID_Alg_End = function () {
        if (this.isConnect == false) {
            console.log("Unconnected service");
            return NOT_CONNECT;
        }
        var cmd_json = {
            "RequestType": CommandType.CID_Alg_End_Type
        };
        this.SendMessage(JSON.stringify(cmd_json));
    };
    Client.prototype.Connect_Server = function () {
        var thisws = this;
        //if (this.isConnect == true) {
        //    this.ws.onclose = function () {
        //        console.log("Close Connect");
        //        thisws.isConnect = false;
        //    }
        //}
        this.ws = new WebSocket(this.url);
        this.ws.onopen = function () {
            console.log("Connect Success");
            //alert("Connect Success");
            thisws.isConnect = true;
        };
        this.ws.onmessage = function (ev) {
            var data = JSON.parse(ev.data);
            var responsetype = data.ResponseType;
            switch (responsetype) {
                case CommandType.OpenDevice_Type:
                    var opencode = data.ErrorCode;
                    var openmsg = data.ErrorMessage;
                    if (thisws.OnOpenDevice) {
                        thisws.OnOpenDevice(opencode, openmsg);
                    }
                    break;
                case CommandType.CloseDevice_Type:
                    var closecode = data.ErrorCode;
                    var closemsg = data.ErrorMessage;
                    if (thisws.OnCloseDevice) {
                        thisws.OnCloseDevice(closecode, closemsg);
                    }
                    break;
                case CommandType.GetDeviceCount_Type:
                    var countcode = data.ErrorCode;
                    var countmsg = data.ErrorMessage;
                    var count = data.Count;
                    if (thisws.OnGetDeviceCount) {
                        thisws.OnGetDeviceCount(countcode, countmsg, count);
                    }
                    break;
                case CommandType.GetDeviceDescription_Type:
                    var desccode = data.ErrorCode;
                    var descmsg = data.ErrorMessage;
                    var deviceinfo = {
                        sn: data.SN,
                        manufacturer: data.Manufacturer,
                        productname: data.ProductName,
                        productmodel: data.ProductModel,
                        fw_version: data.FW_Version,
                        hw_version: data.HW_Version,
                        image_width: data.Image_Width,
                        iamge_height: data.Image_Height,
                        dpi: data.DPI,
                        device_id: data.Device_ID
                    };
                    if (thisws.OnGetDeviceDescription) {
                        thisws.OnGetDeviceDescription(desccode, descmsg, deviceinfo);
                    }
                    break;
                case CommandType.CaptureFingerData_Type:
                    var capcode = data.ErrorCode;
                    var capmsg = data.ErrorMessage;
                    var capimage = {
                        image_format: data.Image_Format,
                        image_data: data.Image_Data,
                        feature_format: data.Feature_Format,
                        feature_data: data.Feature_Data,
                        quality: data.Image_Quality,
                        NFIQ: data.Image_NFIQ
                    };
                    if (thisws.OnCaptureFingerData) {
                        thisws.OnCaptureFingerData(capcode, capmsg, capimage);
                    }
                    break;
                case CommandType.StartCapture_Type:
                    var startcode = data.ErrorCode;
                    var startmsg = data.ErrorMessage;
                    var startimage = {
                        image_format: ImageFormatType.IBmp,
                        image_data: data.Image_Data,
                        feature_format: data.Feature_Format,
                        feature_data: data.Feature_Data,
                        quality: data.Image_Quality,
                        NFIQ: data.Image_NFIQ
                    };
                    if (thisws.OnStartCapture) {
                        thisws.OnStartCapture(startcode, startmsg, startimage);
                    }
                    break;
                case CommandType.StopCapture_Type:
                    var stopcode = data.ErrorCode;
                    var stopmsg = data.ErrorMessage;
                    if (thisws.OnStopCapture) {
                        thisws.OnStopCapture(stopcode, stopmsg);
                    }
                    break;
                case CommandType.GeneralizeTemplate_Type:
                    var tempcode = data.ErrorCode;
                    var tempmsg = data.ErrorMessage;
                    var temp = data.Template_Data;
                    if (thisws.OnGeneralizeTemplate) {
                        thisws.OnGeneralizeTemplate(tempcode, tempmsg, temp);
                    }
                    break;
                case CommandType.Verify_Type:
                    var verifycode = data.ErrorCode;
                    var verifymsg = data.ErrorMessage;
                    var verifyscore = data.Verify_Score;
                    var verifyresult = data.Verify_Result;
                    if (thisws.OnVerify) {
                        thisws.OnVerify(verifycode, verifymsg, verifyscore, verifyresult);
                    }
                    break;
                case CommandType.SetOption_Type:
                    var setcode = data.ErrorCode;
                    var setmsg = data.ErrorMessage;
                    if (thisws.OnSetOption) {
                        thisws.OnSetOption(setcode, setmsg);
                    }
                    break;
                case CommandType.BmpToRaw_Type:
                    var b2rcode = data.ErrorCode;
                    var b2rmsg = data.ErrorMessage;
                    var b2rimage = data.Raw_ImageData;
                    if (thisws.OnBmpToRaw) {
                        thisws.OnBmpToRaw(b2rcode, b2rmsg, b2rimage);
                    }
                    break;
                case CommandType.BmpToWSQ_Type:
                    var b2wsqcode = data.ErrorCode;
                    var b2wsqmsg = data.ErrorMessage;
                    var b2wsqimage = data.WSQ_ImageData;
                    if (thisws.OnBmpToWSQ) {
                        thisws.OnBmpToWSQ(b2wsqcode, b2wsqmsg, b2wsqimage);
                    }
                    break;
                case CommandType.BmpToISO_Type:
                    var b2isocode = data.ErrorCode;
                    var b2isomsg = data.ErrorMessage;
                    var b2isoimage = data.ISO_ImageData;
                    if (thisws.OnBmpToISO) {
                        thisws.OnBmpToISO(b2isocode, b2isomsg, b2isoimage);
                    }
                    break;
                case CommandType.BmpToANSI_Type:
                    var b2ansicode = data.ErrorCode;
                    var b2ansimsg = data.ErrorMessage;
                    var b2ansiimage = data.ANSI_ImageData;
                    if (thisws.OnBmpToANSI) {
                        thisws.OnBmpToANSI(b2ansicode, b2ansimsg, b2ansiimage);
                    }
                    break;
                case CommandType.CID_Init_Type:
                    var CID_initcode = data.ErrorCode;
                    var CID_initmsg = data.ErrorMessage;
                    if (thisws.OnCID_Init) {
                        thisws.OnCID_Init(CID_initcode, CID_initmsg);
                    }
                    break;
                case CommandType.CID_Close_Type:
                    var CID_closecode = data.ErrorCode;
                    var CID_closemsg = data.ErrorMessage;
                    if (thisws.OnCID_Close) {
                        thisws.OnCID_Close(CID_closecode, CID_closemsg);
                    }
                    break;
                case CommandType.CID_GetChannelCount_Type:
                    var CID_countcode = data.ErrorCode;
                    var CID_countmsg = data.ErrorMessage;
                    var CID_count = data.Count;
                    if (thisws.OnCID_GetCount) {
                        thisws.OnCID_GetCount(CID_countcode, CID_countmsg, CID_count);
                    }
                    break;
                case CommandType.CID_BeginCapture_Type:
                    var CID_begincode = data.ErrorCode;
                    var CID_beginmsg = data.ErrorMessage;
                    if (thisws.OnCID_BeginCapture) {
                        thisws.OnCID_BeginCapture(CID_begincode, CID_beginmsg);
                    }
                    break;
                case CommandType.CID_GetFPRawData_Type:
                    var CID_rawcode = data.ErrorCode;
                    var CID_rawmsg = data.ErrorMessage;
                    var CID_rawdata = data.RawData;
                    if (thisws.OnCID_GetRawData) {
                        thisws.OnCID_GetRawData(CID_rawcode, CID_rawmsg, CID_rawdata);
                    }
                    break;
                case CommandType.CID_GetFPBmpData_Type:
                    var CID_bmpcode = data.ErrorCode;
                    var CID_bmpmsg = data.ErrorMessage;
                    var CID_bmpdata = data.BmpData;
                    if (thisws.OnCID_GetBmpData) {
                        thisws.OnCID_GetBmpData(CID_bmpcode, CID_bmpmsg, CID_bmpdata);
                    }
                    break;
                case CommandType.CID_EndCapture_Type:
                    var CID_endcode = data.ErrorCode;
                    var CID_endmsg = data.ErrorMessage;
                    if (thisws.OnCID_EndCapture) {
                        thisws.OnCID_EndCapture(CID_endcode, CID_endmsg);
                    }
                    break;
                case CommandType.CID_GetVersion_Type:
                    var CID_versioncode = data.ErrorCode;
                    var CID_versionmsg = data.ErrorMessage;
                    var CID_version = data.Version;
                    if (thisws.OnCID_GetVersion) {
                        thisws.OnCID_GetVersion(CID_versioncode, CID_versionmsg, CID_version);
                    }
                    break;
                case CommandType.CID_GetDesc_Type:
                    var CID_desccode = data.ErrorCode;
                    var CID_descmsg = data.ErrorMessage;
                    var CID_description = data.Description;
                    if (thisws.OnCID_GetDesc) {
                        thisws.OnCID_GetDesc(CID_desccode, CID_descmsg, CID_description);
                    }
                    break;
                case CommandType.CID_Alg_GetVersion_Type:
                    var CID_alg_versioncode = data.ErrorCode;
                    var CID_alg_versionmsg = data.ErrorMessage;
                    var CID_alg_version = data.Version;
                    if (thisws.OnCID_Alg_GetVersion) {
                        thisws.OnCID_Alg_GetVersion(CID_alg_versioncode, CID_alg_versionmsg, CID_alg_version);
                    }
                    break;
                case CommandType.CID_Alg_Begin_Type:
                    var CID_alg_begincode = data.ErrorCode;
                    var CID_alg_beginmsg = data.ErrorMessage;
                    if (thisws.OnCID_Alg_Begin) {
                        thisws.OnCID_Alg_Begin(CID_alg_begincode, CID_alg_beginmsg);
                    }
                    break;
                case CommandType.CID_Alg_FeatureExtract_Type:
                    var CID_alg_extractcode = data.ErrorCode;
                    var CID_alg_extractmsg = data.ErrorMessage;
                    var CID_alg_featuredata = data.FeatureData;
                    if (thisws.OnCID_Alg_FeatureExtract) {
                        thisws.OnCID_Alg_FeatureExtract(CID_alg_extractcode, CID_alg_extractmsg, CID_alg_featuredata);
                    }
                    break;
                case CommandType.CID_Alg_FeatureMatch_Type:
                    var CID_alg_fmatchcode = data.ErrorCode;
                    var CID_alg_fmatchmsg = data.ErrorMessage;
                    var CID_alg_fsimilarity = data.Similarity;
                    if (thisws.OnCID_Alg_FeatureMatch) {
                        thisws.OnCID_Alg_FeatureMatch(CID_alg_fmatchcode, CID_alg_fmatchmsg, CID_alg_fsimilarity);
                    }
                    break;
                case CommandType.CID_Alg_ImageMatch_Type:
                    var CID_alg_imatchcode = data.ErrorCode;
                    var CID_alg_imatchmsg = data.ErrorMessage;
                    var CID_alg_isimilarity = data.Similarity;
                    if (thisws.OnCID_Alg_ImageMatch) {
                        thisws.OnCID_Alg_ImageMatch(CID_alg_imatchcode, CID_alg_imatchmsg, CID_alg_isimilarity);
                    }
                    break;
                case CommandType.CID_Alg_GetQualityScore_Type:
                    var CID_alg_qualitycode = data.ErrorCode;
                    var CID_alg_qualitymsg = data.ErrorMessage;
                    var CID_alg_quality = data.Score;
                    if (thisws.OnCID_Alg_GetQualityScore) {
                        thisws.OnCID_Alg_GetQualityScore(CID_alg_qualitycode, CID_alg_qualitymsg, CID_alg_quality);
                    }
                    break;
                case CommandType.CID_Alg_End_Type:
                    var CID_alg_endcode = data.ErrorCode;
                    var CID_alg_endmsg = data.ErrorMessage;
                    if (thisws.OnCID_Alg_End) {
                        thisws.OnCID_Alg_End(CID_alg_endcode, CID_alg_endmsg);
                    }
                    break;
                default:
                    if (thisws.OnOtherError) {
                        thisws.OnOtherError();
                    }
                    break;
            }
        };
        this.ws.onclose = function () {
            thisws.isConnect = false;
            console.log("Disconnect");
        };
    };
    return Client;
}());
//# sourceMappingURL=Client.js.map