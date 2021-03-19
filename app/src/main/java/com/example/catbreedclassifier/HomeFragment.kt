package com.example.catbreedclassifier

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.catbreedclassifier.ml.MobileCatBreeds
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.TensorLabel

class HomeFragment : Fragment() {

    private val pickImage = 100
    private var imageUri: Uri? = null

    private lateinit var navController: NavController

    private lateinit var model: MobileCatBreeds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = activity?.applicationContext?.let { MobileCatBreeds.newInstance(it) }!!

        navController = view.findNavController()

        val pickButton: Button = view.findViewById(R.id.pick_image_button)

        pickButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data

            val stream = data?.data?.let { activity?.contentResolver!!.openInputStream(it) }


            var photoImage = BitmapFactory.decodeStream(stream)

            photoImage = Bitmap.createScaledBitmap(photoImage, 224, 224, false)

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
            val labels = activity?.applicationContext?.let {
                FileUtil.loadLabels(it, "labels.txt")
            }

            // Gets the map of label and probability.
            val labeledProbability: Map<String, Float> =
                    labels?.let {
                        TensorLabel(
                                it,
                                probabilityProcessor.process(outputFeature0)
                        ).mapWithFloatValue
                    } as Map<String, Float>

            val result = labeledProbability.maxByOrNull { it.value }

            Log.i("model", output.toString())

            val action = result?.let {
                HomeFragmentDirections
                        .actionHomeFragmentToIdentifiedCatFragment2(it.key, photoImage)
            }

            if (action != null) {
                navController.navigate(action)
            }

        }
    }

}