package com.guilherme.santana.genui2.data

import com.guilherme.santana.genui2.sdui.spec.ScreenSpec
import com.guilherme.santana.genui2.sdui.spec.parseScreenSpec
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal class ScreenApiException(val detail: String?) : IOException(detail)

internal fun requestScreen(prompt: String): ScreenSpec {
    val endpoint = URL("http://127.0.0.1:8000/screens/generate")
    val connection = endpoint.openConnection() as HttpURLConnection
    try {
        val payload = JSONObject().put("prompt", prompt).toString().toByteArray(Charsets.UTF_8)
        connection.requestMethod = "POST"
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 5_000
        connection.readTimeout = 120_000
        connection.doOutput = true
        connection.setFixedLengthStreamingMode(payload.size)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(payload) }
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            val body = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            val detail = try {
                body?.let { JSONObject(it).opt("detail") as? String }
            } catch (_: JSONException) {
                null
            }
            throw ScreenApiException(detail)
        }
        return connection.inputStream.bufferedReader(Charsets.UTF_8).use { parseScreenSpec(it.readText()) }
    } finally {
        connection.disconnect()
    }
}
