package me.line_up.lineup

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import androidx.core.content.ContextCompat
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import com.sangcomz.fishbun.define.Define
import me.line_up.lineup.service.ApiService


class PostActivity : AppCompatActivity() {
    private val photoAdapter = PhotoAdapter().apply {
        chooseCallback = {
            choosePhoto()
        }
    }
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this

        val token = intent.getStringExtra("token")
        Log.e(App.TAG, "Token: $token")

        setContentView(R.layout.upload_main)

        val listView = findViewById<RecyclerView>(R.id.selected_photos)
        listView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = RecyclerView.HORIZONTAL
            }
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                this.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider)!!)
            })
            adapter = photoAdapter
        }


        setupActionBar()
    }

    private fun setupActionBar() {
        val header = layoutInflater.inflate(R.layout.upload_actionbar, null)
        header.findViewById<Button>(R.id.actionbar_close).setOnClickListener {
            close()
        }
        header.findViewById<Button>(R.id.actionbar_upload).setOnClickListener {
            upload()
        }

        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            title = "글쓰기"
            setCustomView(header, ActionBar.LayoutParams(
                MATCH_PARENT, MATCH_PARENT, Gravity.CENTER
            ))
            setDisplayHomeAsUpEnabled(false)
            show()
        }
    }

    private fun close() {
        finish()
        overridePendingTransition(R.anim.slide_still,  R.anim.slide_exit)
    }

    private fun choosePhoto() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 1)
            }
        } else {
            choosePhotoImpl()
        }

    }

    private fun choosePhotoImpl() {
        FishBun.with(this)
            .setImageAdapter(GlideAdapter())
            .setPickerSpanCount(3)
            .setMinCount(1)
            .setMaxCount(20)
            .setSelectedImages(photoAdapter.selectedPhotos)
            .setCamera(false)
            .exceptGif(true)
            .isStartInAllView(true)
            .startAlbum()
    }

    private fun upload() {
        val photos = photoAdapter.selectedPhotos
        val comment = findViewById<EditText>(R.id.editor).text.toString().trim()
        if (comment.isEmpty()) {
            Toast.makeText(this, R.string.warn_require_comment, Toast.LENGTH_SHORT).show()
            return
        }
        if (photos.isEmpty()) {
            Toast.makeText(this, R.string.warn_require_photo, Toast.LENGTH_SHORT).show()
            return
        }
        val token = intent.getStringExtra("token")!!


        val context = this
        val progressDialog = AlertDialog.Builder(context).apply {
            setCancelable(true)
            setView(R.layout.upload_progress)
        }.create()

        progressDialog.show()

        ApiService(authToken = token, comment = comment, photos = photos).apply {
            onSuccess = {
                progressDialog.dismiss()
                close()
            }
            onError = { error ->
                Log.e(App.TAG, "Upload failed", error)
                progressDialog.dismiss()
                Toast.makeText(context, R.string.error_upload, Toast.LENGTH_SHORT).show()
            }
            post()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhotoImpl()
            } else {
                Toast.makeText(this, R.string.warn_require_photo_permission, Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Define.ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            val uris = data?.getParcelableArrayListExtra<Uri>(Define.INTENT_PATH)
            if (uris != null) {
                photoAdapter.selectedPhotos = uris
                photoAdapter.notifyDataSetChanged()
            }
        }
    }
}

class PhotoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var chooseCallback: (() -> Unit)? = null
    var selectedPhotos: ArrayList<Uri> = ArrayList()

    class AddHolder(button: ImageButton) : RecyclerView.ViewHolder(button)
    class PhotoHolder(photoView: ImageView) : RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val addButton = ImageButton(parent.context).apply {
                setBackgroundColor(Color.WHITE)
                setImageResource(R.drawable.ic_img_add_n)
                setOnClickListener {
                    chooseCallback?.invoke()
                }
            }
            return AddHolder(addButton)
        }
        return PhotoHolder(ImageView(parent.context))
    }

    override fun getItemCount(): Int {
        return 1 + selectedPhotos.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val size = App.shared.dp(74)

        if (position == 0) {
            val view = holder.itemView
            view.layoutParams = TableRow.LayoutParams(size, size)
        } else {
            val view = holder.itemView as ImageView
            view.layoutParams = TableRow.LayoutParams(size, size)
            val uri = selectedPhotos[position-1]
            Glide.with(view.context).load(uri).centerCrop().override(size).into(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }
}