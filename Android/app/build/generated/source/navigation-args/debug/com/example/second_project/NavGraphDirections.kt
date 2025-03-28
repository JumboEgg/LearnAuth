package com.example.second_project

import android.os.Bundle
import androidx.navigation.NavDirections
import kotlin.Int
import kotlin.String

public class NavGraphDirections private constructor() {
  private data class ActionGlobalStaticFragment(
    public val lectureName: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_staticFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("lectureName", this.lectureName)
        return result
      }
  }

  public companion object {
    public fun actionGlobalStaticFragment(lectureName: String = ""): NavDirections =
        ActionGlobalStaticFragment(lectureName)
  }
}
