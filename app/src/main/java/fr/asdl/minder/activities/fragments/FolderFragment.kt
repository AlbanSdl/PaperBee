package fr.asdl.minder.activities.fragments

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
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.Note
import fr.asdl.minder.note.bindings.NoteAdapter
import fr.asdl.minder.note.NoteFolder
import fr.asdl.minder.note.NoteManager.Companion.ROOT_ID
import fr.asdl.minder.note.NoteManager.Companion.TRASH_ID
import fr.asdl.minder.note.NoteText
import fr.asdl.minder.view.options.Color
import fr.asdl.minder.view.options.ColorPicker
import fr.asdl.minder.view.sentient.SentientRecyclerView

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
            R.id.goto_trash -> (activity as? MainActivity)?.openNotable(notable.noteManager?.findElementById(TRASH_ID) as NoteFolder)
            R.id.empty_trash -> {
                if (activity != null) AlertDialog.Builder(activity!!).setTitle(R.string.trash_empty_confirm).setMessage(R.string.trash_empty_confirm_details).apply {
                    setPositiveButton(android.R.string.ok) { _, _ -> notable.clear() }
                    setNegativeButton(android.R.string.cancel) { _, _ -> }
                }.show()
            }
            R.id.set_color -> {
                ColorPicker(activity!!, listOf(*Color.values()), Color.getIndex(notable.color), false) {
                    notable.color = it
                    this.updateBackgroundTint()
                    notable.save()
                }
            }
            else -> return false
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_note_selector -> {
                val note = Note("", notable.noteManager, idAllocator = notable.idAllocator, parentId = notable.id)
                notable.add(note)
                note.add(NoteText(""))
                (this.activity as MainActivity).openNotable(note)
            }
            R.id.add_folder_selector -> {
                val fold = NoteFolder("", notable.noteManager, idAllocator = notable.idAllocator, parentId = notable.id)
                notable.add(fold)
                (this.activity as MainActivity).openNotable(fold)
            }
            R.id.add_note_button -> {
                if (this.notable.id!! < -1) return
                val addNote = activity?.findViewById<View>(R.id.add_note_selector)
                val addFolder = activity?.findViewById<View>(R.id.add_folder_selector)
                if (addNote?.visibility == View.GONE) {
                    addNote.visibility = View.VISIBLE
                    addNote.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_anim_out))
                    addFolder?.visibility = View.VISIBLE
                    addFolder?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_anim_out))
                    v.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_rotate_clock))
                } else {
                    addNote?.visibility = View.GONE
                    addNote?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_anim_in))
                    addFolder?.visibility = View.GONE
                    addFolder?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fab_anim_in))
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
            this@FolderFragment.notable.save()
        }
    }

    override fun getTintBackgroundView(fragmentRoot: View): View? {
        return fragmentRoot.findViewById(R.id.folder_color)
    }

}