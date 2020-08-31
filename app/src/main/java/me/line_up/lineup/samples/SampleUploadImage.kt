package me.line_up.lineup.samples

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.jackson.responseObject
import me.line_up.lineup.App
import java.io.File

data class TestResponse(
    var hello: String = "",
    var world: String = ""
)

class SampleUploadImage {

    fun testUpload(context: Context) {
        Fuel.get(
            "https://images.sftcdn.net/images/t_app-cover-l,f_auto/p/" +
                    "ce2ece60-9b32-11e6-95ab-00163ed833e7/260663710/the-test-fun-for-friends-screenshot.jpg"
        ).response { result ->
            val (content, _) = result

            val dir = context.filesDir
            if (content != null) {
                File(dir, "test1.jpg").writeBytes(content)
                File(dir, "test2.jpg").writeBytes(content)
                uploadImages(dir)
            }
        }
    }

    private fun uploadImages(dir: File) {
        val file1 = FileDataPart.from(File(dir, "test1.jpg").absolutePath, name = "image")
        val file2 = FileDataPart.from(File(dir, "test2.jpg").absolutePath, name = "image")

        Fuel.upload("http://172.26.120.84:8000/api/upload/test")
            .add(file1)
            .add(file2)
            .responseObject<TestResponse> { _, _, result ->
                val (test, err) = result
                if (err != null) {
                    Log.e(App.TAG, "Response error: $err")
                }
                if (test != null) {
                    Log.d(App.TAG, "Response from server: ${test.hello}, ${test.world}")
                }
            }
    }
}

