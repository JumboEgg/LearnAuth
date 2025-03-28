package com.example.second_project.ui

import androidx.navigation.NavDirections
import com.example.second_project.NavGraphDirections
import kotlin.String

public class ChargeFragmentDirections private constructor() {
  public companion object {
    public fun actionGlobalStaticFragment(lectureName: String = ""): NavDirections =
        NavGraphDirections.actionGlobalStaticFragment(lectureName)
  }
}
