package com.example.second_project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.adapter.TransactionAdapter
import com.example.second_project.databinding.FragmentMywalletBinding

class MyWalletFragment : Fragment() {

    private var _binding: FragmentMywalletBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMywalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        val transactionList = listOf(
            listOf("1", "데이터 분석 기초", "2025 / 03 / 25", "400 CAT"),
            listOf("2", "일상 생활 관리", "2025 / 03 / 24", "300 CAT"),
            listOf("3", "기본 법률 상식", "2025 / 03 / 23", "500 CAT"),
            listOf("4", "스포츠 심리학", "2025 / 03 / 22", "600 CAT"),
            listOf("5", "마케팅 전략", "2025 / 03 / 21", "450 CAT")
        )

        transactionAdapter = TransactionAdapter(transactionList)
        binding.transactionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }

        binding.chargeBtn.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, ChargeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
