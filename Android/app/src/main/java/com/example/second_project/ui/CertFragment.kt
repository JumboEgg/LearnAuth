package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.UserSession
import com.example.second_project.databinding.FragmentCertBinding
import com.example.second_project.adapter.CertificationAdapter
import com.example.second_project.viewmodel.CertViewModel

class CertFragment : Fragment() {

    private var _binding: FragmentCertBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CertViewModel by viewModels()
    private lateinit var adapter: CertificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadCertificateList()
    }

    private fun setupRecyclerView() {
        adapter = CertificationAdapter(emptyList()) { certificateData ->
            val userId = UserSession.userId
            val lectureId = certificateData.lectureId
            val action = CertFragmentDirections.actionCertFragmentToCertDetailFragment(
                userId = userId,
                lectureId = lectureId
            )
            findNavController().navigate(action)
        }
        binding.recyclerCertifications.adapter = adapter
        binding.recyclerCertifications.layoutManager = LinearLayoutManager(requireContext())

        // 뷰모델에서 수료증 리스트를 관찰하여 UI 업데이트
        viewModel.certificateList.observe(viewLifecycleOwner) { certificateList ->
            if (certificateList.isNullOrEmpty()) {
                binding.textNullCertification.visibility = View.VISIBLE
                binding.recyclerCertifications.visibility = View.GONE
            } else {
                binding.textNullCertification.visibility = View.GONE
                binding.recyclerCertifications.visibility = View.VISIBLE
                adapter.updateList(certificateList)
            }
        }
    }

    private fun loadCertificateList() {
        val userId = UserSession.userId
        viewModel.fetchCertificates(userId)
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 보일 때마다 데이터 새로고침
        loadCertificateList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
