package com.guilherme.santana.genui2.data

import com.guilherme.santana.genui2.sdui.spec.ScreenSpec
import com.guilherme.santana.genui2.sdui.spec.parseScreenSpec
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal class ScreenApiException(val detail: String?) : IOException(detail)

internal data class ScreenPreview(val screen: ScreenSpec, val draftJson: String? = null)

internal fun requestScreen(prompt: String): ScreenPreview =
    postPreview("preview", JSONObject().put("prompt", prompt))

internal fun reviseScreen(draftJson: String, feedback: String): ScreenPreview =
    postPreview("revise", JSONObject().put("draft", JSONObject(draftJson)).put("prompt", feedback))

private fun postPreview(route: String, request: JSONObject): ScreenPreview {
    val endpoint = URL("http://127.0.0.1:8000/screens/$route")
    val connection = endpoint.openConnection() as HttpURLConnection
    try {
        val payload = request.toString().toByteArray(Charsets.UTF_8)
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
        val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        return ScreenPreview(
            screen = parseScreenSpec(response.getJSONObject("screen").toString()),
            draftJson = response.getJSONObject("draft").toString(),
        )
    } finally {
        connection.disconnect()
    }
}
