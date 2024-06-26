package fr.asdl.paperbee.activities.fragments.receiving

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.activities.fragments.ImportFragment
import fr.asdl.paperbee.activities.fragments.sharing.SharingMethod
import fr.asdl.paperbee.exceptions.WrongPasswordException
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.note.bindings.DirectoryTree
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.ROOT_ID
import fr.asdl.paperbee.view.tree.TreeView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReceptionOptionsFragment : ReceptionBaseFragment() {

    override val layoutId: Int = R.layout.share_import_options

    override fun onLayoutInflated(view: View) {
        val orig = this.parentFragment as ImportFragment
        this.setToolBarIsClose(false)

        if (orig.method == SharingMethod.FILE) {
            if (orig.content == null)
                view.findViewById<View>(R.id.share_file_group).visibility = View.VISIBLE
            view.findViewById<TreeView>(R.id.share_selector_tree).attachData(
                DirectoryTree((activity as MainActivity).dbProxy.findElementById(ROOT_ID) as NoteFolder, {
                    orig.destination = it
                    view.findViewById<TextView>(R.id.selected_name).text = getString(R.string.share_import_selected, it.title)
                }, resources.getDimension(R.dimen.smallText) / resources.displayMetrics.scaledDensity)
            )
            view.findViewById<View>(R.id.next).setOnClickListener {
                fun callback() {
                    if (orig.content != null) {
                        orig.destination!!.db!!.import(orig.content!!, orig.destination!!)
                        activity?.supportFragmentManager?.popBackStack(
                            orig.tag,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }
                }
                if (orig.destination == null) {
                    Toast.makeText(
                        requireContext(),
                        R.string.share_import_required_destination,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                it.isEnabled = false
                if (orig.content == null) {
                    val passwordField = view.findViewById<TextView>(R.id.share_password_field)
                    GlobalScope.launch {
                        try {
                            orig.content = orig.shareProcess.decryptFromFile(
                                passwordField.text.toString(),
                                orig.shareData!!
                            )
                            requireActivity().runOnUiThread {
                                callback()
                            }
                        } catch (e: WrongPasswordException) {
                            requireActivity().runOnUiThread {
                                it.isEnabled = true
                                passwordField.error = getString(R.string.share_import_wrong_password)
                            }
                        }
                    }
                }
                callback()
            }
        } else if (orig.method == SharingMethod.NFC) {
            view.findViewById<View>(R.id.share_nfc_group).visibility = View.VISIBLE
        }
    }

    override fun getSharedViews(): List<View> {
        if (this.view == null) return super.getSharedViews()
        return listOf(this.requireView().findViewById(R.id.next))
    }

}