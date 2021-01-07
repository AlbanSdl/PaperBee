package fr.asdl.paperbee.activities.fragments

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.note.Note
import fr.asdl.paperbee.note.bindings.NoteAdapter
import fr.asdl.paperbee.note.NoteFolder
import fr.asdl.paperbee.note.NoteText
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.ROOT_ID
import fr.asdl.paperbee.storage.DatabaseProxy.Companion.TRASH_ID
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_EXTRA
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD
import fr.asdl.paperbee.view.options.Color
import fr.asdl.paperbee.view.options.ColorPicker
import fr.asdl.paperbee.view.sentient.SentientRecyclerView

class FolderFragment : NotableFragment<NoteFolder>(), View.OnClickListener {

    override lateinit var notable: NoteFolder
    override val layoutId: Int = R.layout.folder_content

    override fun attach(notable: NoteFolder): NotableFragment<NoteFolder> {
        this.menuLayoutId = when {
            notable.id!! == TRASH_ID -> R.menu.trash_menu
            notable.id!! == ROOT_ID -> R.menu.root_menu
            else -> R.menu.folder_menu
        }
        return super.attach(notable)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<NavigationView>(R.id.nav).apply {
            this.setCheckedItem(if (notable.id == TRASH_ID) R.id.goto_trash else R.id.goto_main)
        }
    }

    override fun onLayoutInflated(view: View) {
        // Toolbar setup
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.folder_toolbar))
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(notable.id != ROOT_ID)
        // No note message
        view.findViewById<TextView>(R.id.no_note).text = getString(when (notable.id) {
            ROOT_ID -> R.string.no_note_root
            TRASH_ID -> R.string.no_note_trash
            else -> R.string.no_note
        })
        // Folder name
        val title = view.findViewById<EditText>(R.id.folder_name)
        title.setText(notable.title)
        if (notable.id!! >= 0)
            title.addTextChangedListener(FolderTitleListener())
        else
            title.inputType = InputType.TYPE_NULL
        // Folder content
        val recycler = view.findViewById<SentientRecyclerView>(R.id.notes_recycler)
        recycler.adapter = NoteAdapter(notable)
        // Floating action buttons
        if (notable.id != TRASH_ID) {
            view.findViewById<FloatingActionButton>(R.id.add_note_button).setOnClickListener(this)
            view.findViewById<FloatingActionButton>(R.id.add_note_selector).setOnClickListener(this)
            view.findViewById<FloatingActionButton>(R.id.add_folder_selector).setOnClickListener(this)
        } else {
            val fab = view.findViewById<FloatingActionButton>(R.id.add_note_button)
            fab.scaleX = 0f
            fab.scaleY = 0f
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.empty_trash -> {
                if (activity != null) AlertDialog.Builder(requireActivity()).setTitle(R.string.trash_empty_confirm).setMessage(R.string.trash_empty_confirm_details).apply {
                    setPositiveButton(android.R.string.ok) { _, _ -> notable.clear(true) }
                    setNegativeButton(android.R.string.cancel) { _, _ -> }
                }.show()
            }
            R.id.set_color -> {
                ColorPicker(requireActivity(), listOf(*Color.values()), Color.getIndex(notable.color), false) {
                    notable.color = it
                    this.updateBackgroundTint()
                    notable.notifyDataChanged(COLUMN_NAME_EXTRA)
                    notable.save()
                }
            }
            R.id.share_icon -> (this.activity as? MainActivity)?.startSharing(this.notable)
            R.id.share_import_icon -> (this.activity as? MainActivity)?.openImport()
            else -> return false
        }
        return true
    }

    override fun shouldLockDrawer(): Boolean = false

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_note_selector -> {
                val note = Note()
                notable.add(note)
                note.add(NoteText(""))
                (this.activity as MainActivity).openNotable(note)
            }
            R.id.add_folder_selector -> {
                val fold = NoteFolder()
                notable.add(fold)
                (this.activity as MainActivity).openNotable(fold)
            }
            R.id.add_note_button -> {
                if (this.notable.id!! < -1) return
                val addNote = activity?.findViewById<View>(R.id.add_note_selector)
                val addFolder = activity?.findViewById<View>(R.id.add_folder_selector)
                if (addNote?.visibility == View.GONE) {
                    addNote.visibility = View.VISIBLE
                    addNote.isEnabled = true
                    addNote.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_in))
                    addFolder?.visibility = View.VISIBLE
                    addFolder?.isEnabled = true
                    addFolder?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_in))
                    v.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_rotate_clock))
                } else {
                    addNote?.visibility = View.GONE
                    addNote?.isEnabled = false
                    addNote?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_out))
                    addFolder?.visibility = View.GONE
                    addFolder?.isEnabled = false
                    addFolder?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_out))
                    v.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_rotate_anticlock))
                }
            }
        }
    }

    private inner class FolderTitleListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            this@FolderFragment.notable.title = s.toString()
            this@FolderFragment.notable.notifyDataChanged(COLUMN_NAME_PAYLOAD)
            this@FolderFragment.notable.save()
        }
    }

    override fun getTintBackgroundView(fragmentRoot: View): View? {
        return fragmentRoot.findViewById(R.id.folder_color)
    }

}