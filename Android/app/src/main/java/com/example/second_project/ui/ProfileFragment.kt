package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.second_project.R
import com.example.second_project.databinding.FragmentProfileBinding
import com.example.second_project.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // profileMenu1 -> MyWalletFragment 이동
        binding.profileMenu1.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myWalletFragment)
        }

        // profileMenu2 -> MyLectureFragment 이동 (새로 추가)
        binding.profileMenu2.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myLectureFragment)
        }

        binding.profileMenu3.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_declarationFragment)
        }

        // 충전하기 -> ChargeFragment 이동
        binding.chargeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_chargeFragment)
        }

//
//        //화면 확인을 위한 임시 코드!!
//        binding.profileMenu4.setOnClickListener {
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.nav_host_fragment, LectureDetailFragment())
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
