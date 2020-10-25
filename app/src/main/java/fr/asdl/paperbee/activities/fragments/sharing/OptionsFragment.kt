package fr.asdl.paperbee.activities.fragments.sharing

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.fragments.SharingFragment
import fr.asdl.paperbee.note.Note
import fr.asdl.paperbee.view.StatefulExtendedFloatingActionButton

class OptionsFragment : ShareBaseFragment() {

    override val layoutId: Int = R.layout.share_options

    override fun onLayoutInflated(view: View) {
        val orig = this.parentFragment as SharingFragment
        this.setToolBarIsClose(orig.getOpenedFrom() is Note)

        // Configuring method choice
        val selectorFile = view.findViewById<RadioButton>(R.id.share_method_selector_file)
        val selectorNfc = view.findViewById<RadioButton>(R.id.share_method_selector_nfc)
        when (orig.shareOptions.method) {
            SharingMethod.FILE -> selectorFile.isChecked = true
            SharingMethod.NFC -> {
                selectorNfc.isChecked = true
                view.findViewById<View>(R.id.share_file_group).visibility = View.GONE
            }
        }
        selectorFile.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view.findViewById<View>(R.id.share_file_group).visibility = View.VISIBLE
                orig.shareOptions.method = SharingMethod.FILE
            }
        }
        selectorNfc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view.findViewById<View>(R.id.share_file_group).visibility = View.GONE
                orig.shareOptions.method = SharingMethod.NFC
            }
        }

        // Configuring file options
        val passwordField = view.findViewById<EditText>(R.id.share_password_field)
        passwordField.setText(orig.shareOptions.password)
        passwordField.addTextChangedListener(SimpleWatcher {
            orig.shareOptions.password = it
        })

        // Configuring next button
        view.findViewById<StatefulExtendedFloatingActionButton>(R.id.next).apply {
            this.setStateChanged(true)
            this.setOnClickListener {
                if (orig.shareOptions.isCorrect())
                    orig.displayFragment(ShareProcessFragment(), "processShare${orig.getNotableId()}")
            }
        }
    }

    override fun getSharedViews(): List<View> {
        if (this.view == null) return super.getSharedViews()
        return listOf(this.view!!.findViewById(R.id.next))
    }

    private class SimpleWatcher(private val listener: (String) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            listener.invoke(s.toString())
        }
    }

}