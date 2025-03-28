package com.example.second_project.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.CertificationAdapter
import com.example.second_project.databinding.FragmentCertBinding
import com.example.second_project.viewmodel.CertViewModel


class CertFragment : Fragment() {

    private var _binding: FragmentCertBinding? = null
    private val binding get() = _binding!!
    private val viewModel:CertViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCertBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하드코딩 더미 (Pair<String, String>)
        val dummyCertList = listOf(
            Pair("데이터 교육", "데이터"),
            Pair("법률 교육", "법률"),
            Pair("마케팅 교육", "마케팅"),
            Pair("수학 교육", "수학"),
            Pair("생명과학 교육을 한번 들어보시겠습니까 저도 한번 들어보고 싶어서 참을 수 없네요", "생명과학")
        )

        val adapter = CertificationAdapter(dummyCertList) { position ->
            val action = CertFragmentDirections.actionCertFragmentToCertDetailFragment()
            findNavController().navigate(action)
        }

        binding.recyclerCertifications.adapter = adapter
        binding.recyclerCertifications.layoutManager = LinearLayoutManager(requireContext())

        binding.textNullCertification.visibility = View.GONE
        binding.recyclerCertifications.visibility = View.VISIBLE


        viewModel.text.observe(viewLifecycleOwner){

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}