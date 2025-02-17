package com.example.tzpoc.helper

object Constants {


    const val SHARED_PREF = "shared_pref"
    const val Key_LOCATION_ID="locationId"
    const val ANTENNA_POWER = "antenna_power"
    const val SERVER_IP = "server_ip"
    const val SERVER_IP_2 = "server_ip_2"
    const val ISFIRSTTIME = "is_first_time"
    const val PORT = "port"
    const val PORT_2 = "port_2"
    const val KEY_HTTP = "http"
    const val KEY_SERVER_IP = "serverIp"
    const val SERVER_IP_SHARED = "192.168.1.205"
    const val DEFAULT_PORT = 5500
    const val GET = 1
    const val POST = 2
    const val HTTP_OK = 200
    const val HTTP_CREATED = 201
    const val HTTP_EXCEPTION = 202
    const val HTTP_FOUND = 302
    const val HTTP_NOT_FOUND = 404
    const val HTTP_CONFLICT = 409
    const val HTTP_INTERNAL_SERVER_ERROR = 500
    const val HTTP_ERROR = 400
    const val REGISTER_VEHICLE = 1
    const val GET_LOCATIONS = 2
    const val GET_EMP_NAME = 3
    const val ENTRY_EXIT_VEHICLE = 4
    const val GET_ALL_DAMAGE_TYPE = 5
    const val ADD_DAMAGE = 6
    const val ADD_REPAIR = 7
    const val GET_DAMAGE_ID_BY_RES_ID = 8
    const val SESSION_EXPIRE = "Session Expired ! Please relogin"
    const val NO_INTERNET = "No Internet Connection"
    const val NETWORK_FAILURE = "Network Failure"
    const val CONFIG_ERROR = "Please configure network details"
    const val INCOMPLETE_DETAILS = "Please fill the required details"
    const val EXCEPTION_ERROR = "No Data Found"
    const val SHARED_BASE_URL = "base_url"
    const val SHARED_BASE_URL_ALT = "base_url_alt"
    const val KEY_USER_NAME = "userName"
    var BASE_URL = ""
    var BASE_URL_2 = ""
    const val IS_CONFIGURED = "isConfigured"
    const val IS_VERIFIED = "isVerified"
    const val IS_REGISTERED = "isRegistered"
    const val SETTINGS_BASE_URL = "baseUrl"

    //public static final int GET_CLOTH_BY_RES_ID = 9;
    //public static final int GET_EXISTING_PLATE_CLOTH_MAPPING = 10;
    /*  const val UPDATE_RESOURCE_MAPPING_CS = 11
      const val UPDATE_RESOURCE_MAPPING_MS = 12
      const val GET_CLOTH_BY_PLATE_ID = 13
      const val GET_ALL_RES_MFG = 14
      const val CYCLE_COUNT = 15
      const val CHECK_EXISTING_CLOTH_MS = 16
      const val CHECK_EXISTING_CLOTH_CS = 17
      const val POST_MAPPING_NEW = 18
      const val IS_MAPPING_EXIST = 19
      const val LOGIN = 20
      const val UPDATE_PLATE = 21
      const val GET_ALL_FILTER_DATA = 22
      const val UPDATE_FILTER_DATA = 23
      const val GET_EOL_DATA = 24
      const val RESET_TAG = 25
      const val RDT_ID = "rdtId"
      const val KEY_TERMINAL = "terminal"
      const val KEY_ISLOGGEDIN = "isLoggedIn"
      const val KEY_JWT_TOKEN = "jwtToken"
      const val KEY_REFRESH_TOKEN = "refreshToken"
  */


    const val RFID_MAP_DEMAP = "/rfid/mapping-demapping"
    const val GET_LOCATION_CODES = "/location-codes"
    const val TAG_NEW_POSITION = "/tagNewPosition"

    const val GET_ASSETS_BY_ASSET_TYPE = "/api/v1/assetmaster/AssetIdByAssetType"
    const val GET_ASSET_BY_RFID = " /api/v1/assetmaster/AssetByRfId"
    const val SUBMIT_ASSET_TO_RFID_MAPPING = "/api/v1/assetmaster/AssetToRfIdMapping"
    const val SUBMIT_ASSET_TO_RFID_DEMAPPING = "/api/v1/assetmaster/AssetToRfIdDemapping"

    //     const val  baseurl="https://kemar.in/service/api/"
   // const val baseurl = "http://192.168.1.205:8892/service/api/"
   const val baseurl = "http://192.168.1.231:5000/api/"  // rajesh local
//const val baseurl ="http://74.225.231.186:1007/service/api/"
//        const val  baseurl="http://192.168.1.148:5000/api/"
//    const val baseUrl = "http://52.140.53.57/api/"
    //const val baseUrl2 = "http://192.168.1.18:5000/api/"
    //const val baseUrl = "https://52.140.53.57:1880/"
//    const val baseUrl = "https://20.235.176.51:1880/"
    const val LOGGEDIN = "loggedIn"
    const val TOKEN_TYPE = "token_type"
    const val ACCESS_TOKEN = "access_token"
    const val HTTP_HEADER_AUTHORIZATION = "Authorization"
    const val HTTP_ERROR_MESSAGE = "message"

    const val SET_BASE_URL = "base_url"
    const val SET_ANTENNA_POWER = "antenna_power"
    const val LOCATION_CODE_LIST = "location_list_key"

    //API URL
    const val LOGIN_URL = "UserManagement/authenticate"
    const val GET_VEHICLE_JOB_DETAILS = "MobileApp/GetVehiclewithUnAllocatedContainer"
    const val POST_GATE_ENTRY_EXIT = "MobileApp/GateEntryExit"
    const val GET_CONTAINER_DETAIL_ON_TAG = "MobileApp/GetContainerDetailsOnTag"
    const val GET_ALL_LOCATION_DEVICE_MAPPING = "DeviceLocationMapping/GetAllLocationDeviceMapping"
    const val GET_VEHICLE_BY_LOCATION = "MobileApp/GetVehicleByLocation"
    const val POST_RFID_VERIFY_MAP="MobileApp/PostRFIDVerifyMap"

}