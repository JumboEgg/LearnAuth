package com.example.second_project.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmStatic

public data class QuizFragmentArgs(
  public val lectureId: Int = 0,
  public val lectureTitle: String = "",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("lectureId", this.lectureId)
    result.putString("lectureTitle", this.lectureTitle)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("lectureId", this.lectureId)
    result.set("lectureTitle", this.lectureTitle)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): QuizFragmentArgs {
      bundle.setClassLoader(QuizFragmentArgs::class.java.classLoader)
      val __lectureId : Int
      if (bundle.containsKey("lectureId")) {
        __lectureId = bundle.getInt("lectureId")
      } else {
        __lectureId = 0
      }
      val __lectureTitle : String?
      if (bundle.containsKey("lectureTitle")) {
        __lectureTitle = bundle.getString("lectureTitle")
        if (__lectureTitle == null) {
          throw IllegalArgumentException("Argument \"lectureTitle\" is marked as non-null but was passed a null value.")
        }
      } else {
        __lectureTitle = ""
      }
      return QuizFragmentArgs(__lectureId, __lectureTitle)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): QuizFragmentArgs {
      val __lectureId : Int?
      if (savedStateHandle.contains("lectureId")) {
        __lectureId = savedStateHandle["lectureId"]
        if (__lectureId == null) {
          throw IllegalArgumentException("Argument \"lectureId\" of type integer does not support null values")
        }
      } else {
        __lectureId = 0
      }
      val __lectureTitle : String?
      if (savedStateHandle.contains("lectureTitle")) {
        __lectureTitle = savedStateHandle["lectureTitle"]
        if (__lectureTitle == null) {
          throw IllegalArgumentException("Argument \"lectureTitle\" is marked as non-null but was passed a null value")
        }
      } else {
        __lectureTitle = ""
      }
      return QuizFragmentArgs(__lectureId, __lectureTitle)
    }
  }
}
