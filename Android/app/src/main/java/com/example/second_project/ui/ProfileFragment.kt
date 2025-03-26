package com.example.second_project.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.second_project.R
import com.example.second_project.databinding.FragmentProfileBinding
import com.example.second_project.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    private  var _binding :FragmentProfileBinding? =null
    private val binding get() = _binding!!
    private val viewModel:ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.text.observe(viewLifecycleOwner){
//            binding.profileText.text = it
//        }

        binding.profileMenu1.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, MyWalletFragment())
            transaction.addToBackStack(null) // 뒤로 가기 버튼을 눌렀을 때 ProfileFragment로 돌아오도록 설정
            transaction.commit()
        }

        binding.chargeBtn.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, ChargeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }


        //화면 확인을 위한 임시 코드!!
        binding.profileMenu4.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, LectureDetailFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}