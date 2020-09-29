package fr.asdl.minder.activities.fragments

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.asdl.minder.R
import fr.asdl.minder.activities.MainActivity
import fr.asdl.minder.note.*
import fr.asdl.minder.note.NoteManager.Companion.ROOT_ID
import fr.asdl.minder.note.NoteManager.Companion.TRASH_ID
import fr.asdl.minder.view.sentient.SentientRecyclerView

class FolderFragment(private val folder: NoteFolder) : MinderFragment(), View.OnClickListener {

    override val layoutId: Int = R.layout.folder_content
    override val menuLayoutId: Int? = if (folder.id!! == TRASH_ID) R.menu.trash_menu else R.menu.folder_menu

    override fun onLayoutInflated(view: View) {
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.folder_toolbar))
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(folder.id != ROOT_ID)
        val title = view.findViewById<EditText>(R.id.folder_name)
        title.setText(folder.title)
        if (folder.id!! >= 0)
            title.addTextChangedListener(FolderTitleListener())
        else
            title.inputType = InputType.TYPE_NULL
        val recycler = view.findViewById<SentientRecyclerView>(R.id.notes_recycler)
        recycler.adapter = NoteAdapter(folder)
        if (folder.id != TRASH_ID) {
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
            R.id.goto_trash -> (activity as? MainActivity)?.openNotable(folder.noteManager?.findElementById(TRASH_ID) as NoteFolder)
            R.id.empty_trash -> {
                if (activity != null) AlertDialog.Builder(activity!!).setTitle(R.string.trash_empty_confirm).setMessage(R.string.trash_empty_confirm_details).apply {
                    setPositiveButton(android.R.string.ok) { _, _ -> folder.clear() }
                    setNegativeButton(android.R.string.cancel) { _, _ -> }
                }.show()
            }
            else -> return false
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_note_selector -> {
                val note = Note("", folder.noteManager, idAllocator = folder.idAllocator, parentId = folder.id)
                folder.add(note)
                note.add(NoteText(""))
                (this.activity as MainActivity).openNotable(note)
            }
            R.id.add_folder_selector -> {
                val fold = NoteFolder("", folder.noteManager, idAllocator = folder.idAllocator, parentId = folder.id)
                folder.add(fold)
                (this.activity as MainActivity).openNotable(fold)
            }
            R.id.add_note_button -> {
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
            this@FolderFragment.folder.title = s.toString()
            this@FolderFragment.folder.save()
        }
    }

}