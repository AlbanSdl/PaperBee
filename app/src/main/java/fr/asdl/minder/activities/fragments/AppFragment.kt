package fr.asdl.minder.activities.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import fr.asdl.minder.IntAllocator
import fr.asdl.minder.sharing.files.*

abstract class AppFragment : Fragment(), FileAccessor {

    private val activityResultCodes = IntAllocator()
    private val pendingFileAccesses = hashMapOf<Int, FutureFileAccess>()
    open fun restoreState(savedInstanceState: Bundle) {}
    open fun saveState(savedInstanceState: Bundle) {}

    abstract val layoutId: Int
    open var menuLayoutId: Int? = null
    open val styleId: Int? = null
    open val shouldRetainInstance = false
    abstract fun onLayoutInflated(view: View)

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.retainInstance = this.shouldRetainInstance
        if (savedInstanceState != null) this.restoreState(savedInstanceState)
        this.setHasOptionsMenu(true)
        val fragmentInflater = if (styleId != null) inflater.cloneInContext(ContextThemeWrapper(activity, styleId!!)) else inflater

        val statusBarColor = TypedValue()
        fragmentInflater.context.theme.resolveAttribute(android.R.attr.statusBarColor, statusBarColor, true)
        activity?.window?.statusBarColor = statusBarColor.data

        val view = fragmentInflater.inflate(this.layoutId, container, false)
        this.onLayoutInflated(view)
        return view
    }

    final override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (this.menuLayoutId != null) inflater.inflate(this.menuLayoutId!!, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    final override fun onSaveInstanceState(outState: Bundle) {
        this.saveState(outState)
    }

    final override fun createFile(fileName: String, fileType: String, content: ByteArray, callback: FileCreationCallBack) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        val code = activityResultCodes.allocate()
        this.pendingFileAccesses[code] = FutureFileCreation(content, callback)
        startActivityForResult(intent, code)
    }

    final override fun readFile(fileType: String, callback: FileOpeningCallBack) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType
        }
        val code = activityResultCodes.allocate()
        this.pendingFileAccesses[code] = FutureFileOpening(callback)
        startActivityForResult(intent, code)
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val futureFileAccess = this.pendingFileAccesses.remove(requestCode)
        if (futureFileAccess != null) {
            this.activityResultCodes.release(requestCode)
            if (resultCode == Activity.RESULT_OK) data?.data?.also { uri ->
                try {
                    futureFileAccess.onAccessed.invoke(this.activity!!, FileAccess(FileAccessResult.ACCESSED, uri))
                } catch (io: Exception) {
                    futureFileAccess.onAccessed.invoke(this.activity!!, FileAccess(FileAccessResult.ERROR))
                }
            }
            else futureFileAccess.onAccessed.invoke(this.activity!!, FileAccess(FileAccessResult.CANCELLED))
        }
    }

}