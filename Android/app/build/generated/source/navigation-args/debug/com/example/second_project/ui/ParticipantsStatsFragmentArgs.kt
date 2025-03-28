package com.example.second_project.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class ParticipantsStatsFragmentArgs(
  public val lectureName: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("lectureName", this.lectureName)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("lectureName", this.lectureName)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): ParticipantsStatsFragmentArgs {
      bundle.setClassLoader(ParticipantsStatsFragmentArgs::class.java.classLoader)
      val __lectureName : String?
      if (bundle.containsKey("lectureName")) {
        __lectureName = bundle.getString("lectureName")
        if (__lectureName == null) {
          throw IllegalArgumentException("Argument \"lectureName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"lectureName\" is missing and does not have an android:defaultValue")
      }
      return ParticipantsStatsFragmentArgs(__lectureName)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        ParticipantsStatsFragmentArgs {
      val __lectureName : String?
      if (savedStateHandle.contains("lectureName")) {
        __lectureName = savedStateHandle["lectureName"]
        if (__lectureName == null) {
          throw IllegalArgumentException("Argument \"lectureName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"lectureName\" is missing and does not have an android:defaultValue")
      }
      return ParticipantsStatsFragmentArgs(__lectureName)
    }
  }
}
