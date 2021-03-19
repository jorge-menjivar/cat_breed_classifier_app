package com.example.catbreedclassifier

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.findNavController

class IdentifiedCatFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cat_identified, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val breed: String? = arguments?.getString("breed")
        val bitmap: Bitmap? = arguments?.getParcelable<Bitmap>("bitmap")

        val breedTextView: TextView = view.findViewById(R.id.identified_breed)
        breedTextView.text = breed

        val image: ImageView = view.findViewById(R.id.identified_image)
        image.setImageBitmap(bitmap)

        val againButton: Button = view.findViewById(R.id.identified_again_button)

        val navController: NavController = view.findNavController()
        againButton.setOnClickListener {
            navController.navigate(R.id.action_identifiedCatFragment2_to_homeFragment)
        }

    }

}