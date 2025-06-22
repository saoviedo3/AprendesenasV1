package com.espe.aprendesenasv1.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.espe.aprendesenasv1.R

class LettersFragment : Fragment(R.layout.fragment_letters) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nav = findNavController()
        view.findViewById<Button>(R.id.btnA).setOnClickListener {
            nav.navigate(R.id.action_letters_to_detection, bundleOf("sign" to "A"))
        }
        view.findViewById<Button>(R.id.btnB).setOnClickListener {
            nav.navigate(R.id.action_letters_to_detection, bundleOf("sign" to "B"))
        }
    }
}
