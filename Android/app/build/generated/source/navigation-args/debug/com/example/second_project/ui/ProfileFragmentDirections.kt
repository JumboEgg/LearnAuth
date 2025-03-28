package com.example.second_project.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.second_project.NavGraphDirections
import com.example.second_project.R
import kotlin.String

public class ProfileFragmentDirections private constructor() {
  public companion object {
    public fun actionProfileFragmentToMyWalletFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_profileFragment_to_myWalletFragment)

    public fun actionProfileFragmentToChargeFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_profileFragment_to_chargeFragment)

    public fun actionProfileFragmentToMyLectureFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_profileFragment_to_myLectureFragment)

    public fun actionProfileFragmentToDeclarationFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_profileFragment_to_declarationFragment)

    public fun actionGlobalStaticFragment(lectureName: String = ""): NavDirections =
        NavGraphDirections.actionGlobalStaticFragment(lectureName)
  }
}
