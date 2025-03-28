package com.example.second_project.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.second_project.NavGraphDirections
import com.example.second_project.R
import kotlin.String

public class CertFragmentDirections private constructor() {
  public companion object {
    public fun actionCertFragmentToCertDetailFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_certFragment_to_certDetailFragment)

    public fun actionGlobalStaticFragment(lectureName: String = ""): NavDirections =
        NavGraphDirections.actionGlobalStaticFragment(lectureName)
  }
}
