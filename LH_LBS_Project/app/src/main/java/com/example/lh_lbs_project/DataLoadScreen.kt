package com.example.lh_lbs_project

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.example.lh_lbs_project.DrainpipeInfo
import java.io.File
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader

import java.nio.charset.Charset

@Composable
fun DataLoadScreen(onDataLoaded: (List<LatLng>, List<DrainpipeInfo>, List<NoiseLevelInfo>, List<SensorInfo>) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var loadingMessage by remember { mutableStateOf("데이터 로딩 중...") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // context 선언 위치 변경

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            loadingMessage = "공사 현장 데이터 로딩 중..."
            val parsedConstructionSites = getSeoulData()?.let { parseIncompleteSites(it) } ?: emptyList()

             loadingMessage = "하수관로 데이터 로딩 중..."
             val allDrainpipeInfo = getDrainpipeData()

             loadingMessage = "하수관로 위치 정보 변환 중..."
             allDrainpipeInfo.forEachIndexed { index, info ->
                 val latLng = geocodeAddress(info.address)
                 if (latLng != null) {
                     info.location = latLng
                 }
                 loadingMessage = "하수관로 위치 정보 변환 중... (${index + 1}/${allDrainpipeInfo.size})"
             }
            val allGeocodedDrainpipes = allDrainpipeInfo

            loadingMessage = "센서 데이터 로딩 중..."
            val allSensorInfo = loadSensorDataFromCsv(context)

            loadingMessage = "소음도 데이터 로딩 중..."
            val allNoiseLevelInfo = getNoiseLevelData(allSensorInfo)

            isLoading = false
            onDataLoaded(
                parsedConstructionSites as List<LatLng>,
                allGeocodedDrainpipes as List<DrainpipeInfo>,
                allNoiseLevelInfo as List<NoiseLevelInfo>,
                allSensorInfo as List<SensorInfo>
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(loadingMessage, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// ✅ 서울시 공공데이터 호출
private fun getSeoulData(): String? {
    val url = "http://openapi.seoul.go.kr:8088/${BuildConfig.API_CLIENT_KEY}/xml/ListOnePMISBizInfo/1/1000/"

    Log.d("API_CALL", "Calling getSeoulData API. URL: $url")

    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("SEOUL_API_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                Log.e("API_CALL", "getSeoulData API Response (Error): ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string()
            Log.d("API_CALL", "getSeoulData API Response (Success): ${responseBody?.take(500)}...") // Log first 500 chars
            responseBody
        }
    } catch (e: Exception) {
        Log.e("SEOUL_API_ERROR", "예외 발생: ${e.message}", e)
        null
    }
}

// ✅ Fetch Seoul drainpipe data
private fun getDrainpipeData(): List<DrainpipeInfo> {
    Log.d("DRAINPIPE_DEBUG", "getDrainpipeData() called.")
    Log.d("DRAINPIPE_DEBUG", "API_CLIENT_KEY: ${BuildConfig.API_CLIENT_KEY}")
    return try {
        val allDrainpipeInfos = mutableSetOf<DrainpipeInfo>()
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyyMMddHH", Locale.getDefault())

    val currentTime = calendar.time
    val currentFormattedTime = dateFormat.format(currentTime)

    calendar.add(Calendar.HOUR_OF_DAY, -1)
    val oneHourAgoTime = calendar.time
    val oneHourAgoFormattedTime = dateFormat.format(oneHourAgoTime)

    val startTime = oneHourAgoFormattedTime
    val endTime = currentFormattedTime

    for (i in 1..25) {
        val seCd = String.format("%02d", i)
        val url = "http://openAPI.seoul.go.kr:8088/${BuildConfig.API_CLIENT_KEY}/xml/DrainpipeMonitoringInfo/1/1000/$seCd/$startTime/$endTime"

        Log.d("API_CALL", "Calling getDrainpipeData API for seCd=$seCd. URL: $url")

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("DRAINPIPE_API_ERROR", "HTTP Error (seCd=$seCd): ${response.code}, ${response.message}")
                } else {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val parsedInfos = parseDrainpipeSites(responseBody)
                        Log.d("DRAINPIPE_DEBUG", "seCd=$seCd parsed ${parsedInfos.size} drainpipes.")
                        allDrainpipeInfos.addAll(parsedInfos)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DRAINPIPE_API_ERROR", "Exception (seCd=$seCd): ${e.message}", e)
        }
    }
    Log.d("DRAINPIPE_DEBUG", "Total unique drainpipes collected: ${allDrainpipeInfos.size}")
    allDrainpipeInfos.toList()
    } catch (e: Exception) {
        Log.e("DRAINPIPE_API_ERROR", "Unhandled exception in getDrainpipeData: ${e.message}", e)
        emptyList()
    }
}

// ✅ Fetch Seoul noise level data
private fun getNoiseLevelData(allSensorInfo: List<SensorInfo>): List<NoiseLevelInfo> {
    Log.d("NOISE_DEBUG", "getNoiseLevelData() called.")
    Log.d("NOISE_DEBUG", "API_CLIENT_KEY: ${BuildConfig.API_CLIENT_KEY}")
    return try {
        val allNoiseLevelInfos = mutableListOf<NoiseLevelInfo>()
        val url = "http://openapi.seoul.go.kr:8088/${BuildConfig.API_CLIENT_KEY}/xml/IotVdata017/1/1000/"

        Log.d("API_CALL", "Calling getNoiseLevelData API. URL: $url")

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("NOISE_API_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
            } else {
                val responseBody = response.body?.string()
                Log.d("API_CALL", "getNoiseLevelData API Full Response (Success): $responseBody")
                if (responseBody != null) {
                    Log.d("API_CALL", "getNoiseLevelData API Response (Success): ${responseBody.take(500)}...")
                    val parsedInfos = parseNoiseLevelSites(responseBody, allSensorInfo)
                    Log.d("NOISE_DEBUG", "Parsed ${parsedInfos.size} noise level entries.")
                    allNoiseLevelInfos.addAll(parsedInfos)
                }
            }
        }
        Log.d("NOISE_DEBUG", "Total noise level entries collected: ${allNoiseLevelInfos.size}")
        allNoiseLevelInfos
    } catch (e: Exception) {
        Log.e("NOISE_API_ERROR", "Unhandled exception in getNoiseLevelData: ${e.message}", e)
        emptyList()
    }
}

// ✅ XML에서 공사 미완료(LAT, LOT)만 파싱
fun parseIncompleteSites(xml: String): List<LatLng> {
    val list = mutableListOf<LatLng>()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var lat: Double? = null
    var lot: Double? = null
    var isIncomplete = false
    var tagName: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> tagName = parser.name
            XmlPullParser.TEXT -> {
                when (tagName) {
                    "CMCN_YN1" -> isIncomplete = parser.text.trim() == "0"
                    "LAT" -> lat = parser.text.toDoubleOrNull()
                    "LOT" -> lot = parser.text.toDoubleOrNull()
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "row") {
                    if (isIncomplete && lat != null && lot != null) {
                        list += LatLng(lat, lot)
                    }
                    isIncomplete = false
                    lat = null
                    lot = null
                }
                tagName = null
            }
        }
        eventType = parser.next()
    }
    return list
}

// ✅ Parse drainpipe data from XML
fun parseDrainpipeSites(xml: String): List<DrainpipeInfo> {
    val list = mutableListOf<DrainpipeInfo>()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var pstnInfo: String? = null
    var meaWal: Double? = null
    var tagName: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> tagName = parser.name
            XmlPullParser.TEXT -> {
                when (tagName) {
                    "PSTN_INFO" -> pstnInfo = parser.text.trim()
                    "MSRMT_WATL" -> meaWal = parser.text.toDoubleOrNull()
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "row") {
                    if (pstnInfo != null && meaWal != null) {
                        list.add(DrainpipeInfo(address = pstnInfo, waterLevel = meaWal))
                    }
                    pstnInfo = null
                    meaWal = null
                }
                tagName = null
            }
            else -> {}
        }
        eventType = parser.next()
    }
    return list
}

private fun geocodeAddress(address: String): LatLng? {
    val encodedAddress = URLEncoder.encode(address, "UTF-8")
    val url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=$encodedAddress"

    Log.d("API_CALL", "Calling geocodeAddress API. URL: $url")

    val request = Request.Builder()
        .url(url)
        .addHeader("x-ncp-apigw-api-key-id", BuildConfig.NAVER_CLIENT_ID)
        .addHeader("x-ncp-apigw-api-key", BuildConfig.NAVER_CLIENT_SECRET)
        .addHeader("Accept", "application/json")
        .build()

    val client = OkHttpClient()
    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GEOCODE_ERROR", "HTTP 오류: ${response.code}, ${response.message}")
                Log.e("API_CALL", "geocodeAddress API Response (Error): ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            Log.d("API_CALL", "geocodeAddress API Response (Success): ${responseBody.take(500)}...") // Log first 500 chars
            val json = JSONObject(responseBody)

            val addressesArray = json.getJSONArray("addresses")
            if (addressesArray.length() > 0) {
                val firstAddress = addressesArray.getJSONObject(0)
                val x = firstAddress.getDouble("x") // longitude
                val y = firstAddress.getDouble("y") // latitude
                LatLng(y, x) // LatLng(latitude, longitude)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        Log.e("GEOCODE_ERROR", "예외 발생: ${e.message}", e)
        return null
    }
}
fun parseNoiseLevelSites(xml: String, allSensorInfo: List<SensorInfo>): List<NoiseLevelInfo> {
    val list = mutableListOf<NoiseLevelInfo>()
    val sensorMap = allSensorInfo.associateBy { it.serialNo.trim() } // map으로 만들기
    val csvSerialSet = sensorMap.keys

    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var apiSensorIdentifier: String? = null
    var noiseLevel: Double? = null
    var tagName: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> tagName = parser.name
            XmlPullParser.TEXT -> {
                when (tagName) {
                    "SERIAL_NO" -> apiSensorIdentifier = parser.text.trim()
                    "AVG_NOISE" -> noiseLevel = parser.text.toDoubleOrNull()
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "row") {
                    val serial = apiSensorIdentifier
                    if (serial != null && noiseLevel != null) {
                        val sensor = sensorMap[serial]
                        if (sensor != null) {
                            list.add(
                                NoiseLevelInfo(
                                    address = "",
                                    noiseLevel = noiseLevel,
                                    location = sensor.location
                                )
                            )
                        } else {
                            Log.w("NOISE_PARSE_WARNING", "센서 위치 못 찾음: $serial")
                        }
                    }
                    apiSensorIdentifier = null
                    noiseLevel = null
                }
                tagName = null
            }
        }
        eventType = parser.next()
    }
    return list
}


private fun loadSensorDataFromCsv(context: Context): List<SensorInfo> {
    val sensorList = mutableListOf<SensorInfo>()
    val csvFileName = "data.csv"
    Log.d("CSV_LOAD", "Attempting to load sensor data from assets: $csvFileName")
    try {
        context.assets.open(csvFileName).bufferedReader(Charset.forName("UTF-8")).useLines { lines: Sequence<String> ->
            for (line in lines.drop(1)) { // Skip header row
                Log.d("CSV_PARSE_DETAIL", "Processing line: $line")
                val columns = line.split(',')
                Log.d("CSV_PARSE_DETAIL", "Columns size: ${columns.size}")
                if (columns.size >= 7) {
                    try {
                        val serialNo = columns[1].trim()             // V02Q1940059
                        val modelName = "Unknown"                    // CSV에 없음 → 기본값 할당
                        val address = columns[2].trim()              // 서울특별시 강남구 일원동 688
                        val latitude = columns[4].toDouble()         // 37.4895...
                        val longitude = columns[5].toDouble()        // 127.0753...

                        val sensor = SensorInfo(serialNo, modelName, address, latitude, longitude, LatLng(latitude, longitude))
                        Log.d("CSV_PARSE_DEBUG", "Loaded sensor serialNo from CSV: ${sensor.serialNo}, modelName: ${sensor.modelName}")
                        sensorList.add(sensor)
                    } catch (e: NumberFormatException) {
                        Log.e("CSV_PARSE_ERROR", "Error parsing numbers in line: $line, Error: ${e.message}")
                    } catch (e: Exception) {
                        Log.e("CSV_PARSE_ERROR", "Error parsing line: $line, Error: ${e.message}")
                    }
                } else {
                    Log.w("CSV_PARSE_WARNING", "Skipping malformed line (columns size ${columns.size} < 7): $line")
                }
            }
        }
    } catch (e: Exception) {
        Log.e("CSV_LOAD_ERROR", "Error loading CSV file: ${e.message}", e)
    }
    Log.d("CSV_LOAD_SUCCESS", "Loaded ${sensorList.size} sensor entries from CSV.")
    return sensorList
}
