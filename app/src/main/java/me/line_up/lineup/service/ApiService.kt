package me.line_up.lineup.service

import android.net.Uri
import android.os.Build
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.InlineDataPart
import com.github.kittinunf.fuel.jackson.responseObject
import me.line_up.lineup.App
import me.line_up.lineup.util.MediaImages


class ApiService(val authToken: String, val comment: String, val photos: List<Uri>) {
    val baseUrl = "${App.apiURL}/contents/posts"

    lateinit var onSuccess: () -> Unit
    lateinit var onError: (Throwable) -> Unit
    lateinit var appVersion: String

    fun post() {
//        Log.d(App.TAG, "Token: $authToken, Comment: $comment, Photos: $photos")

        App.shared.applicationContext.apply {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            appVersion = packageInfo.versionName
        }

        prepare()
    }

    private fun prepare() {
        Fuel.post("$baseUrl/").apply {
            fillCommonHeaders(authToken, appVersion)
            responseObject<PreparedContentResponse> { _, _, result ->
                val (body, err) = result
                if (err != null) {
                    onError(err.exception)
                } else if (body != null) {
                    Log.d(App.TAG, "Response-1: $body")
                    uploadAttachments(body.uid)
                }
            }
        }
    }

    private fun uploadAttachments(contentId: String) {
        Fuel.upload("$baseUrl/$contentId/attachments/").apply {
            fillCommonHeaders(authToken, appVersion)

            val mediaHandler = MediaImages()
            photos.forEachIndexed { index, uri ->
                val file = mediaHandler.contentUriToFile(uri)

                add(InlineDataPart(content = "$index", name = "order", contentType = "text/plain"))
                add(FileDataPart(file,
                    name = "content",
                    filename = "$index.jpg",
                    contentType = "image/jpeg"
                ))
            }
            responseString { _, _, result ->
                val (body, err) = result
                if (err != null) {
                    onError(err.exception)
                } else if (body != null) {
                    // [{"post":"90413bca-e3e2-4e9c-a7ae-55c4b0526308",
                    //   "content":"https://lineup-staging-user-assets.s3-ap-northeast-2.amazonaws.com/uploads/p/9/0/4/1/3bcae3e24e9ca7ae55c4b0526308.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJLW5JGO3XFTCKQHQ%2F20190922%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Date=20190922T073521Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=5bb16db957227fbaed6cf15b0c7b7a4c6e7ca7660cbc789dd60069da9d658831",
                    //   "order":0,
                    //   "kind":"original"},
                    Log.d(App.TAG, "Response-2: $body")
                    comment(contentId)
                }
            }
        }
    }

    private fun comment(contentId: String) {
        Fuel.put("$baseUrl/$contentId/", listOf("content" to comment)).apply {
            fillCommonHeaders(authToken, appVersion)
            responseString { _, _, result ->
                val (body, err) = result
                if (err != null) {
                    onError(err.exception)
                } else if (body != null) {
                    // {"uid":"f630edae-98de-4051-b730-a8c2ff9bb743","user":"rath_460",
                    //  "nickname":null,"content":"<p>ㅋㅋㅋ</p>",
                    //  "tags":[{"name":"line-up","post_number":137}],
                    //  "updated_at":"2019-09-22T07:50:33.552107Z",
                    //  "images":["https://staging-api.line-up.me/contents/assets/7f9d972a86284eb4af03f1308d39ade9/"],"thumbnails":[{"url":"https://staging-api.line-up.me/contents/assets/28f07b0d02ce4579bb8e743302752704/","width":640,"height":640},{"url":"https://staging-api.line-up.me/contents/assets/383b299885ec42e6adff12e3e661959d/","width":750,"height":750},{"url":"https://staging-api.line-up.me/contents/assets/1f7722bfe54c436c86b6f8131ee2a450/","width":1080,"height":1080}],
                    //  "likes":[],"comments":[],"user_image":null,"orig_content":"ㅋㅋㅋ","restrict_code":null}
                    Log.d(App.TAG, "Response-3: $body")
                    publish(contentId)
                }
            }
        }
    }

    private fun publish(contentId: String) {
        Fuel.post("$baseUrl/$contentId/publish/").apply {
            fillCommonHeaders(authToken, appVersion)
            responseString { _, _, result ->
                val (body, err) = result
                if (err != null) {
                    onError(err.exception)
                } else if (body != null) {
                    // {"uid":"f630edae-98de-4051-b730-a8c2ff9bb743","user":"rath_460",
                    //  "nickname":null,"content":"<p>ㅋㅋㅋ</p>",
                    //  "tags":[{"name":"line-up","post_number":137}],
                    //  "updated_at":"2019-09-22T07:50:33.552107Z",
                    //  "images":["https://staging-api.line-up.me/contents/assets/7f9d972a86284eb4af03f1308d39ade9/"],"thumbnails":[{"url":"https://staging-api.line-up.me/contents/assets/28f07b0d02ce4579bb8e743302752704/","width":640,"height":640},{"url":"https://staging-api.line-up.me/contents/assets/383b299885ec42e6adff12e3e661959d/","width":750,"height":750},{"url":"https://staging-api.line-up.me/contents/assets/1f7722bfe54c436c86b6f8131ee2a450/","width":1080,"height":1080}],
                    //  "likes":[],"comments":[],"user_image":null,"orig_content":"ㅋㅋㅋ","restrict_code":null}
                    Log.d(App.TAG, "Response-4: $body")
                    onSuccess()
                }
            }
        }
    }
}

data class PreparedContentResponse(var uid: String)

fun Request.fillCommonHeaders(authToken: String, appVersion: String) {
    headers.apply {
        append("Authorization", "Token $authToken")
        append("X-App-Version", appVersion)
        append("X-OS-Type", "Android")
        append("X-OS-Version", "${Build.VERSION.SDK_INT}")
    }
}