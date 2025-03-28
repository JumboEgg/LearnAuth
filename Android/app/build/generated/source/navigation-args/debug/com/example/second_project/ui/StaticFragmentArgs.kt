package com.example.second_project.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class StaticFragmentArgs(
  public val lectureName: String = "",
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
    public fun fromBundle(bundle: Bundle): StaticFragmentArgs {
      bundle.setClassLoader(StaticFragmentArgs::class.java.classLoader)
      val __lectureName : String?
      if (bundle.containsKey("lectureName")) {
        __lectureName = bundle.getString("lectureName")
        if (__lectureName == null) {
          throw IllegalArgumentException("Argument \"lectureName\" is marked as non-null but was passed a null value.")
        }
      } else {
        __lectureName = ""
      }
      return StaticFragmentArgs(__lectureName)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): StaticFragmentArgs {
      val __lectureName : String?
      if (savedStateHandle.contains("lectureName")) {
        __lectureName = savedStateHandle["lectureName"]
        if (__lectureName == null) {
          throw IllegalArgumentException("Argument \"lectureName\" is marked as non-null but was passed a null value")
        }
      } else {
        __lectureName = ""
      }
      return StaticFragmentArgs(__lectureName)
    }
  }
}
