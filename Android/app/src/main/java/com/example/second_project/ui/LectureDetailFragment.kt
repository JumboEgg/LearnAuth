package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.second_project.R
import com.example.second_project.databinding.FragmentLectureDetailBinding
import com.example.second_project.databinding.FragmentOwnedLectureDetailBinding

private const val TAG = "LectureDetailFragment_야옹"
class LectureDetailFragment: Fragment() {

    private var _binding: FragmentLectureDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLectureDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buyBtn.setOnClickListener {
            Log.d(TAG, "onViewCreated: 클릭")
            showChargeDialog()
        }

    }

    fun showChargeDialog() {
        Log.d(TAG, "showChargeDialog:")

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_charge, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_radius_20)

            val params = attributes
            params.width =
                (resources.displayMetrics.widthPixels * 0.6).toInt() // 화면 너비의 60%로 설정? (반영될지 안될지는..)
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        val cancelBtn: Button = dialogView.findViewById(R.id.chargeNoBtn)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}