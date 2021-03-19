package com.example.catbreedclassifier

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.catbreedclassifier.ml.MobileCatBreeds

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

        model = activity?.applicationContext?.let { MobileCatBreeds.newInstance(it) }!!

        navController = view.findNavController()

        val pickDifferentButton: Button = view.findViewById(R.id.pick_different_button)

        pickDifferentButton.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_previewImage)
        }

        val keepButton: Button = view.findViewById(R.id.keep_button)

        keepButton.setOnClickListener {
            navController.navigate(R.id.action_previewImage_to_identifiedCatFragment2)
        }
    }
}