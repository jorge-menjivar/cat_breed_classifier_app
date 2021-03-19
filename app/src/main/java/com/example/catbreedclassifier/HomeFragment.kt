package com.example.catbreedclassifier

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.catbreedclassifier.ml.MobileCatBreeds


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

        savedInstanceState?.putParcelableArray("states", null);

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

            val bitmapImage = BitmapFactory.decodeStream(stream)

            val action = HomeFragmentDirections
                        .actionHomeFragmentToPreviewImage(bitmapImage)

            navController.navigate(action)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

}