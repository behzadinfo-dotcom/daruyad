package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class MedicineSearchResult(
    val title: String,
    val imageUrl: String,
    val source: String,
    val isVerified: Boolean = false
)

/**
 * جستجوی عکس واقعی دارو از ویکی‌مدیا کامنز (API رسمی و باز، با اطلاعات مجوز).
 * هیچ عکس ساختگی یا کاتالوگ ثابتی وجود ندارد. اگر نتیجه‌ای نباشد، لیست خالی برمی‌گردد
 * و کاربر می‌تواند با دوربین یا گالری عکس قرص/بسته‌بندی خودش را بگذارد.
 */
class MedicineImageSearchService {

    private val englishNames = mapOf(
        "استامینوفن" to "Paracetamol",
        "پاراستامول" to "Paracetamol",
        "ایبوپروفن" to "Ibuprofen",
        "ژلوفن" to "Ibuprofen",
        "آموکسی سیلین" to "Amoxicillin",
        "آموکسی‌سیلین" to "Amoxicillin",
        "امپرازول" to "Omeprazole",
        "پنتوپرازول" to "Pantoprazole",
        "لوزارتان" to "Losartan",
        "متفورمین" to "Metformin",
        "آسپرین" to "Aspirin",
        "سیتریزین" to "Cetirizine",
        "آتورواستاتین" to "Atorvastatin",
        "سالبوتامول" to "Salbutamol inhaler",
        "دیفن هیدرامین" to "Diphenhydramine",
        "شربت" to "cough syrup"
    )

    // تصاویر غیردارویی که معمولاً در نتایج کامنز ظاهر می‌شوند
    private val excludedTitleWords = listOf(
        "structure", "formula", "skeletal", "molecule", "chemical",
        "diagram", "logo", "icon", "drawing", "map"
    )

    suspend fun searchMedicineImages(query: String): List<MedicineSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val english = englishNames.entries
            .firstOrNull { trimmed.contains(it.key, ignoreCase = true) }?.value
            ?: trimmed

        val searchTerm = URLEncoder.encode("$english pill medicine", "UTF-8")
        val url = "https://commons.wikimedia.org/w/api.php?action=query&format=json" +
                "&generator=search&gsrnamespace=6&gsrlimit=12&gsrsearch=$searchTerm" +
                "&prop=imageinfo&iiprop=url|mime|extmetadata&iiurlwidth=600" +
                "&iiextmetadatafilter=LicenseShortName"

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            // ویکی‌مدیا برای درخواست‌های API به User-Agent معرفی‌شده نیاز دارد
            setRequestProperty("User-Agent", "DaruyadApp/2.0 (Android medication reminder; contact via GitHub)")
        }
        if (conn.responseCode != 200) {
            throw IllegalStateException("HTTP ${conn.responseCode}")
        }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages")
            ?: return@withContext emptyList()

        val results = mutableListOf<Pair<Int, MedicineSearchResult>>()
        val keys = pages.keys()
        while (keys.hasNext()) {
            val page = pages.getJSONObject(keys.next())
            val rawTitle = page.optString("title")
            val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue

            val mime = info.optString("mime")
            val thumb = info.optString("thumburl")
            if ((mime != "image/jpeg" && mime != "image/png") || thumb.isEmpty()) continue
            if (excludedTitleWords.any { rawTitle.contains(it, ignoreCase = true) }) continue

            val license = info.optJSONObject("extmetadata")
                ?.optJSONObject("LicenseShortName")
                ?.optString("value").orEmpty()

            val displayTitle = rawTitle.removePrefix("File:")
                .substringBeforeLast('.')
                .replace('_', ' ')

            results.add(
                page.optInt("index", 999) to MedicineSearchResult(
                    title = displayTitle,
                    imageUrl = thumb,
                    source = if (license.isNotEmpty()) "ویکی‌مدیا کامنز • مجوز: $license" else "ویکی‌مدیا کامنز",
                    isVerified = false
                )
            )
        }

        results.sortedBy { it.first }.map { it.second }.take(8)
    }
}
