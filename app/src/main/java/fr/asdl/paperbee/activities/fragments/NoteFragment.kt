package fr.asdl.paperbee.activities.fragments

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.doOnDetach
import com.google.android.material.snackbar.Snackbar
import fr.asdl.paperbee.R
import fr.asdl.paperbee.activities.MainActivity
import fr.asdl.paperbee.note.*
import fr.asdl.paperbee.note.bindings.NotePartAdapter
import fr.asdl.paperbee.note.bindings.NotePartDecoration
import fr.asdl.paperbee.note.bindings.NotePartEditor
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_EXTRA
import fr.asdl.paperbee.storage.v1.NotableContract.NotableContractInfo.COLUMN_NAME_PAYLOAD
import fr.asdl.paperbee.view.RichTextSpan
import fr.asdl.paperbee.view.options.ColorPicker
import fr.asdl.paperbee.view.options.FontColor
import fr.asdl.paperbee.view.options.NoteColor
import fr.asdl.paperbee.view.rounded.RoundedImageView
import fr.asdl.paperbee.view.sentient.SentientRecyclerView

class NoteFragment : NotableFragment<Note>(), View.OnClickListener {

    override val layoutId: Int = R.layout.note_editor
    override lateinit var notable: Note
    override var menuLayoutId: Int? = R.menu.editor_menu
    override val styleId: Int = R.style.EditorTheme
    private var focusedNote: NotePart? = null
    private var currentEditor: NotePartEditor? = null
    private val focusWatcher = (object : ViewTreeObserver.OnGlobalFocusChangeListener {
        override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
            val rec = view?.findViewById<SentientRecyclerView>(R.id.note_editor_elements) ?: return
            focusedNote = if (newFocus != null) (rec.adapter as NotePartEditorAdapter).getHeldItem(
                rec.findContainingViewHolder(newFocus)?.adapterPosition ?: -1
            ) else null
            updateContextToolbarEnabled(rec.rootView)
        }
    })

    override fun onLayoutInflated(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val editorToolbar: LinearLayout = view.findViewById(R.id.editor_toolbar)
        ((LayoutInflater.from(editorToolbar.context).inflate(
            R.layout.editor_toolbar,
            editorToolbar
        ) as ViewGroup).getChildAt(0) as ViewGroup).children.filterIsInstance<RoundedImageView>()
            .forEach {
                it.setOnClickListener(this)
            }
        this.updateContextToolbarEnabled(editorToolbar)

        val editorFormatToolbar = view.findViewById<LinearLayout>(R.id.editor_format_toolbar)
        ((LayoutInflater.from(editorFormatToolbar.context).inflate(
            R.layout.editor_format_toolbar,
            editorFormatToolbar
        ) as ViewGroup).getChildAt(1) as ViewGroup).children.filterIsInstance<RoundedImageView>()
            .forEach {
                it.setOnClickListener(this)
            }

        // We add the note contents
        (view.findViewById<EditText>(R.id.note_editor_title)).setText(notable.title)
        (view.findViewById<EditText>(R.id.note_editor_title)).addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                notable.title = s.toString()
                notable.notifyDataChanged(COLUMN_NAME_PAYLOAD)
                notable.save()
            }
        })

        val rec = (view.findViewById<SentientRecyclerView>(R.id.note_editor_elements))
        rec.visibility = View.VISIBLE
        rec.addItemDecoration(NotePartDecoration())
        val adapter = NotePartEditorAdapter(notable)
        rec.adapter = adapter
        rec.viewTreeObserver.addOnGlobalFocusChangeListener(focusWatcher)
        rec.doOnDetach { it.viewTreeObserver.removeOnGlobalFocusChangeListener(focusWatcher) }
    }

    private fun updateContextToolbarEnabled(viewFrom: View) {
        viewFrom.findViewById<RoundedImageView>(R.id.moveIn)?.isEnabled =
            this.focusedNote != null && this.focusedNote?.hasAbove() == true
        viewFrom.findViewById<RoundedImageView>(R.id.moveOut)?.isEnabled =
            this.focusedNote != null && this.focusedNote?.getParentPart() != null
    }

    private fun updateFormatToolbarVisible(visible: Boolean) {
        requireView().findViewById<LinearLayout>(R.id.editor_format_toolbar).apply {
            if (!visible && visibility != View.GONE) {
                animate().cancel()
                animate().alpha(0f).withEndAction {
                    visibility = View.GONE
                }
            } else if (visible && visibility == View.GONE) {
                visibility = View.VISIBLE
                animate().cancel()
                animate().alpha(1f)
            }
        }
    }

    fun onUserSelection(hasSelection: Boolean, editor: NotePartEditor) {
        this.updateFormatToolbarVisible(hasSelection)
        this.currentEditor = if (hasSelection) editor else null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.set_color -> {
                ColorPicker(
                    requireActivity(),
                    NoteColor.values(),
                    notable.color,
                    false
                ) {
                    notable.color = it
                    this.updateBackgroundTint()
                    notable.notifyDataChanged(COLUMN_NAME_EXTRA)
                    notable.save()
                }
            }
            R.id.share_icon -> (this.activity as? MainActivity)?.startSharing(this.notable)
            else -> return false
        }
        return true
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.add_text_element -> notable.add(NoteText())
            R.id.add_checkbox_element -> notable.add(NoteCheckBoxable())
            R.id.moveIn -> this.focusedNote?.moveIn()
            R.id.moveOut -> this.focusedNote?.moveOut()
            R.id.bold, R.id.italic, R.id.underline -> this.currentEditor?.applyButtonSpan(v.id)
            R.id.background_color, R.id.font_color -> {
                val spanType = RichTextSpan.findSpanType { it.id == v.id } ?: return
                val current = this.currentEditor?.getCurrentSelectionFullSpan(spanType)
                var currentColor = current?.extra as FontColor?
                ColorPicker(
                    requireContext(), FontColor.values(), currentColor, false,
                    colorContext = RichTextSpan.getColorTheme(requireContext(), spanType)
                ) {
                    currentColor = it ?: currentColor
                    this.currentEditor?.applyButtonSpanWithExtra(v.id, currentColor)
                }
            }
            R.id.insert_link -> {
                val current =
                    this.currentEditor?.getCurrentSelectionFullSpan(RichTextSpan.findSpanType { it.id == v.id })
                lateinit var dialog: AlertDialog
                dialog = AlertDialog.Builder(requireContext(), R.style.ColorPickerTheme)
                    .setTitle(R.string.format_insert_link_dialog_name)
                    .setView(R.layout.editor_format_link_insert)
                    .setNegativeButton(R.string.format_insert_link_dialog_remove) { dial, _ ->
                        dial.dismiss(); this.currentEditor?.applyButtonSpanWithExtra(
                        v.id,
                        null
                    )
                    }
                    .setNeutralButton(android.R.string.cancel) { dial, _ -> dial.dismiss() }
                    .setPositiveButton(R.string.format_insert_link_dialog_apply) { dial, _ ->
                        this.currentEditor?.applyButtonSpanWithExtra(
                            v.id,
                            dialog.findViewById<EditText>(R.id.insert_link_edit)!!.text.toString()
                        ); dial.dismiss()
                    }
                    .show()
                dialog.findViewById<EditText>(R.id.insert_link_edit)
                    ?.setText(current?.extra as String?)
            }
        }
    }

    override fun shouldLockDrawer(): Boolean = true

    override fun getTintBackgroundView(fragmentRoot: View): View? {
        return fragmentRoot.findViewById(R.id.transitionBackground)
    }

    private inner class NotePartEditorAdapter(note: Note) : NotePartAdapter(note) {

        private var currentlyMoving: NotePart? = null

        override fun onBindViewHolder(holder: ViewHolder, content: NotePart) {
            super.onBindViewHolder(holder, content)
            if (content is TextNotePart) {
                val textView = (holder.findViewById(R.id.note_text) as? NotePartEditor)
                textView?.attach(content)
                textView?.setSelectionListener(this@NoteFragment::onUserSelection)
            }
            if (content is CheckableNotePart) {
                val checkBox = (holder.findViewById(R.id.note_checkbox) as? CheckBox)
                checkBox?.setOnClickListener {
                    content.checked = checkBox.isChecked; content.notifyDataChanged(
                    COLUMN_NAME_EXTRA
                ); content.save(
                    false
                )
                }
            }
        }

        override fun onViewRecycled(holder: ViewHolder) {
            (holder.findViewById(R.id.note_text) as? NotePartEditor)?.detach()
        }

        override fun getLayoutId(): Int {
            return R.layout.note_part_editor_layout
        }

        override fun onMoved(content: NotePart): Boolean = content.updateParentId()

        override fun onMoveChange(content: NotePart?) {
            if (content != null && this.currentlyMoving == null)
                notable.collapse(content) // Collapses the NotePart
            if (content == null && this.currentlyMoving != null)
                notable.expand(this.currentlyMoving) // Expands the NotePart when no longer dragged
            this.currentlyMoving = content // Updates values
        }

        override fun onSwipeRight(context: Context, content: NotePart) {
            data class PartHierarchy(val target: NotePart, val parent: NotePart?, val order: Int)

            val removed = arrayListOf<PartHierarchy>()
            fun saveRestore(part: NotePart) {
                part.getChildren().forEach { saveRestore(it) }
                removed.add(PartHierarchy(part, part.getParentPart(), part.order))
            }
            saveRestore(content)
            notable.remove(content)
            Snackbar.make(
                activity!!.findViewById(R.id.transitionContents),
                R.string.note_part_deleted,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.restore) {
                    notable.db!!.reMapIds(removed.map { it.target })
                    removed.reverse()
                    removed.forEach { this.getDataHolder().add(it.target, it.order) }
                    removed.forEach { if (it.parent != null) it.target.parentId = it.parent.id }
                    removed.clear()
                }.show()
        }

    }

}