package com.example.second_project.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.second_project.NavGraphDirections
import com.example.second_project.R
import kotlin.Int
import kotlin.String

public class SearchFragmentDirections private constructor() {
  private data class ActionNavSearchToQuizFragment(
    public val lectureId: Int = 0,
    public val lectureTitle: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_nav_search_to_quizFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("lectureId", this.lectureId)
        result.putString("lectureTitle", this.lectureTitle)
        return result
      }
  }

  public companion object {
    public fun actionNavSearchToQuizFragment(lectureId: Int = 0, lectureTitle: String = ""):
        NavDirections = ActionNavSearchToQuizFragment(lectureId, lectureTitle)

    public fun actionGlobalStaticFragment(lectureName: String = ""): NavDirections =
        NavGraphDirections.actionGlobalStaticFragment(lectureName)
  }
}
