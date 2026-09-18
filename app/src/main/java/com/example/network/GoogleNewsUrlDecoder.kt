package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.net.URLEncoder
import java.util.regex.Pattern

object GoogleNewsUrlDecoder {
    private const val TAG = "GoogleNewsUrlDecoder"
    private val sgPattern = Pattern.compile("data-n-a-sg=\"([^\"]+)\"")
    private val tsPattern = Pattern.compile("data-n-a-ts=\"([^\"]+)\"")

    suspend fun decodeUrl(
        client: OkHttpClient,
        sourceUrl: String,
        userAgent: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (!sourceUrl.contains("news.google.com")) {
                return@withContext sourceUrl
            }

            // Extract base64 token
            val base64Str = extractBase64Token(sourceUrl) ?: return@withContext sourceUrl

            // 1. Fetch decoding parameters from Google News article splash page
            val splashUrl = "https://news.google.com/rss/articles/$base64Str"
            val splashRequest = Request.Builder()
                .url(splashUrl)
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-IN,en;q=0.9,hi;q=0.8")
                .build()

            val splashResponse = client.newCall(splashRequest).execute()
            val html = splashResponse.body?.string().orEmpty()
            splashResponse.close()

            val sgMatcher = sgPattern.matcher(html)
            val tsMatcher = tsPattern.matcher(html)

            if (!sgMatcher.find() || !tsMatcher.find()) {
                Log.w(TAG, "Could not find signature/timestamp attributes in Google News splash page")
                return@withContext sourceUrl
            }

            val signature = sgMatcher.group(1) ?: return@withContext sourceUrl
            val timestamp = tsMatcher.group(1) ?: return@withContext sourceUrl

            // 2. Perform batchexecute RPC to decode URL
            val batchUrl = "https://news.google.com/_/DotsSplashUi/data/batchexecute"
            val innerPayload = "[\"garturlreq\",[[\"X\",\"X\",[\"X\",\"X\"],null,null,1,1,\"US:en\",null,1,null,null,null,null,null,0,1],\"X\",\"X\",1,[1,1,1],1,1,null,0,0,null,0],\"$base64Str\",$timestamp,\"$signature\"]"

            val outerArray = JSONArray().apply {
                val itemArray = JSONArray().apply {
                    put("Fbv4je")
                    put(innerPayload)
                    put(null)
                    put("generic")
                }
                val batchArray = JSONArray().apply {
                    put(itemArray)
                }
                put(batchArray)
            }

            val encodedReq = "f.req=" + URLEncoder.encode(outerArray.toString(), "UTF-8")
            val mediaType = "application/x-www-form-urlencoded;charset=UTF-8".toMediaType()

            val batchRequest = Request.Builder()
                .url(batchUrl)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("User-Agent", userAgent)
                .header("Accept", "*/*")
                .post(encodedReq.toRequestBody(mediaType))
                .build()

            val batchResponse = client.newCall(batchRequest).execute()
            val batchBody = batchResponse.body?.string().orEmpty()
            batchResponse.close()

            val parts = batchBody.split("\n\n")
            if (parts.size >= 2) {
                val jsonPart = parts[1]
                val rootArray = JSONArray(jsonPart)
                if (rootArray.length() > 0) {
                    val firstItem = rootArray.getJSONArray(0)
                    val innerStr = firstItem.getString(2)
                    val innerJson = JSONArray(innerStr)
                    val realUrl = innerJson.getString(1)
                    if (realUrl.startsWith("http://") || realUrl.startsWith("https://")) {
                        Log.d(TAG, "Decoded Google News URL successfully: $realUrl")
                        return@withContext realUrl
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error during Google News URL decoding: ${e.message}")
        }
        return@withContext sourceUrl
    }

    private fun extractBase64Token(url: String): String? {
        val marker = "/articles/"
        val idx = url.indexOf(marker)
        if (idx == -1) return null
        val after = url.substring(idx + marker.length)
        val qIdx = after.indexOf('?')
        return if (qIdx != -1) after.substring(0, qIdx) else after
    }
}
