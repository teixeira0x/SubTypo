package com.teixeira0x.subtypo.ui.fragment.dialog

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.teixeira0x.subtypo.databinding.LayoutProgressDialogBinding
import com.teixeira0x.subtypo.utils.Constants

class ProgressDialogFragment : DialogFragment() {

  companion object {

    @JvmStatic
    fun newInstance(message: String? = null): ProgressDialogFragment {
      return ProgressDialogFragment().apply {
        if (message != null) {
          arguments =
            Bundle().apply { putString(Constants.KEY_MESSAGE_ARG, message) }
        }
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val binding = LayoutProgressDialogBinding.inflate(layoutInflater)
    val message = arguments?.getString(Constants.KEY_MESSAGE_ARG)
    if (message != null) {
      binding.message.text = message
    }

    return AlertDialog.Builder(requireContext())
      .setView(binding.root)
      .create()
      .apply { window?.setBackgroundDrawable(GradientDrawable()) }
  }
}
