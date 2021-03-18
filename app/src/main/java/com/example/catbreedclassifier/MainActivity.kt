package com.example.catbreedclassifier

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.catbreedclassifier.ml.MobileCatBreeds
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.TensorLabel
import java.util.*


class MainActivity : AppCompatActivity() {
    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var imageView: ImageView

    private lateinit var model: MobileCatBreeds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = MobileCatBreeds.newInstance(applicationContext)

        imageView = findViewById(R.id.cat_image)
        val pickButton: Button = findViewById(R.id.pick_image)

        pickButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data

            val stream = data?.data?.let { contentResolver!!.openInputStream(it) }


            var photoImage = BitmapFactory.decodeStream(stream)

            photoImage = Bitmap.createScaledBitmap(photoImage, 224, 224, false)

            imageView.setImageBitmap(photoImage)
            var tensorImage: TensorImage = TensorImage.fromBitmap(photoImage)
            tensorImage = TensorImage.createFrom(tensorImage, DataType.FLOAT32)

            // Runs model inference and gets result.
            val outputs = model.process(tensorImage.tensorBuffer)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            val output = outputFeature0.floatArray

            // Creates the post processor for the output probability.

            val normalizeOp = NormalizeOp(0.0f, 1.0f);

            // Creates the post processor for the output probability.
            val probabilityProcessor = TensorProcessor.Builder().add(normalizeOp).build()


            // Loads labels out from the label file.
            var labels = FileUtil.loadLabels(applicationContext, "labels.txt")


            // Gets the map of label and probability.
            val labeledProbability: Map<String, Float> =
                    TensorLabel(
                            labels,
                            probabilityProcessor.process(outputFeature0)
                    ).getMapWithFloatValue()

            val result = labeledProbability.maxByOrNull { it.value }

            Log.i("model", output.toString())
        }
    }
}