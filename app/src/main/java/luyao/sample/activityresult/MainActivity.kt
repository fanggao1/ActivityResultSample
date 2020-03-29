package luyao.sample.activityresult

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var takePhotoObserver: TakePhotoObserver
    private lateinit var takePhotoLiveData: TakePhotoLiveData

    private val startActivity =
        prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            toast(result?.data?.getStringExtra("value") ?: "")
        }

    private val dial = prepareCall(ActivityResultContracts.Dial()) { result ->
        toast("dial $result")
    }

    private val requestPermission =
        prepareCall(ActivityResultContracts.RequestPermission()) { result ->
            toast("request permission $result")
        }

    private val requestPermissions =
        prepareCall(ActivityResultContracts.RequestPermissions()) { result ->
        }

    private val takePicture = prepareCall(ActivityResultContracts.TakePicture()) { result ->
        toast("take picture : $result")
        photo.setImageBitmap(result)
    }

    private class TakePicDrawable : ActivityResultContract<Void, Drawable>() {

        override fun createIntent(input: Void?): Intent {
            return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Drawable? {
            if (resultCode != Activity.RESULT_OK || intent == null) return null
            val bitmap = intent.getParcelableExtra<Bitmap>("data")
            return BitmapDrawable(bitmap)
        }
    }

    private val takePictureCustom = prepareCall(TakePicDrawable()) { result ->
        toast("take picture : $result")
        photo.setImageDrawable(result)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        takePhotoLiveData = TakePhotoLiveData(activityResultRegistry)
        takePhotoLiveData.observeForever(Observer { bitmap ->
            photo.setImageBitmap(bitmap)
        })

        takePhotoObserver = TakePhotoObserver(activityResultRegistry) { bitmap ->
            photo.setImageBitmap(bitmap)
        }
        lifecycle.addObserver(takePhotoObserver)

        jumpBt.setOnClickListener { startActivity.launch(Intent(this, SecondActivity::class.java)) }
        permissionBt.setOnClickListener { requestPermission.launch(Manifest.permission.READ_PHONE_STATE) }
        dialBt.setOnClickListener { dial("123456789") }
        pictureBt.setOnClickListener { takePicture() }
        pictureCustomBt.setOnClickListener { takePictureCustom() }
        customObserverBt.setOnClickListener { takePhotoObserver.takePicture() }
        liveDataBt.setOnClickListener { takePhotoLiveData.takePhotoLauncher() }
    }

}
