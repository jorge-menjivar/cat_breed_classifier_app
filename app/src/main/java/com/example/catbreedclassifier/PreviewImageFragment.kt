package com.example.catbreedclassifier

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.catbreedclassifier.ml.MobileCatBreeds
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.TensorLabel

class PreviewImageFragment: Fragment() {
    private lateinit var navController: NavController

    private lateinit var model: MobileCatBreeds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmapImage: Bitmap? = arguments?.getParcelable<Bitmap>("bitmap")

        val image : ImageView = view.findViewById(R.id.image_preview)

        image.setImageBitmap(bitmapImage)

        navController = view.findNavController()

        val pickDifferentButton: Button = view.findViewById(R.id.pick_different_button)

        pickDifferentButton.setOnClickListener {
            navController.popBackStack()
        }

        val keepButton: Button = view.findViewById(R.id.keep_button)

        keepButton.setOnClickListener {

            model = activity?.applicationContext?.let { MobileCatBreeds.newInstance(it) }!!

            val inputImageBuffer = TensorImage(DataType.FLOAT32)
            if (bitmapImage != null) {
                inputImageBuffer.load(bitmapImage)
            }

            val cropSize: Int? = bitmapImage?.width?.coerceAtMost(bitmapImage.height)
            val imageProcessor: ImageProcessor = ImageProcessor.Builder()
                .add(
                    cropSize?.let { it1 ->
                        ResizeWithCropOrPadOp(
                            it1,
                            it1
                        )
                    }
                )
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(NormalizeOp(0.0f, 255.0f))
                .build()

            val tensorImage = imageProcessor.process(inputImageBuffer)

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

            val action = result?.key?.let { it1 ->
                PreviewImageFragmentDirections.actionPreviewImageToIdentifiedCatFragment2(
                    it1, bitmapImage!!)
            }

            if (action != null) {
                navController.navigate(action)
            }

            model.close()

        }
    }
}