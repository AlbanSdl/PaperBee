package fr.asdl.paperbee.activities.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import fr.asdl.paperbee.IntAllocator
import fr.asdl.paperbee.R
import fr.asdl.paperbee.sharing.files.*
import fr.asdl.paperbee.sharing.permissions.PermissionAccessor
import fr.asdl.paperbee.sharing.permissions.PermissionRationale

abstract class AppFragment : Fragment(), FileAccessor, PermissionAccessor, DrawerLock {

    private val activityResultCodes = IntAllocator()
    private val pendingFileAccesses = hashMapOf<Int, FutureFileAccess>()
    private val pendingPermissionUses = hashMapOf<Int, (Boolean) -> Unit>()
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
        val fragmentInflater = if (styleId != null) inflater.cloneInContext(
            ContextThemeWrapper(
                activity,
                styleId!!
            )
        ) else inflater

        val view = fragmentInflater.inflate(this.layoutId, container, false)
        this.onLayoutInflated(view)
        return view
    }

    @CallSuper
    override fun onResume() {
        this.updateDrawerLock()
        requireView().requestApplyInsets()
        super.onResume()
    }

    @CallSuper
    final override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (this.menuLayoutId != null) inflater.inflate(this.menuLayoutId!!, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    @CallSuper
    final override fun onSaveInstanceState(outState: Bundle) {
        this.saveState(outState)
    }

    final override fun createFile(
        fileName: String,
        fileType: String?,
        content: ByteArray,
        callback: FileCreationCallBack
    ) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType ?: "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        val code = activityResultCodes.allocate()
        this.pendingFileAccesses[code] = FutureFileCreation(content, callback)
        startActivityForResult(intent, code)
    }

    final override fun readFile(fileType: String?, callback: FileOpeningCallBack) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType ?: "application/octet-stream"
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
                    futureFileAccess.onAccessed.invoke(
                        this.requireActivity(),
                        FileAccess(FileAccessResult.ACCESSED, uri)
                    )
                } catch (io: Exception) {
                    futureFileAccess.onAccessed.invoke(
                        this.requireActivity(),
                        FileAccess(FileAccessResult.ERROR)
                    )
                }
            }
            else futureFileAccess.onAccessed.invoke(
                this.requireActivity(),
                FileAccess(FileAccessResult.CANCELLED)
            )
        }
    }

    override fun usePermission(
        permission: String,
        callback: (Boolean) -> Unit,
        rationale: PermissionRationale?
    ) {
        val activity = activity ?: return
        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fun openPermissionRequestDialog() {
                val code = activityResultCodes.allocate()
                this.pendingPermissionUses[code] = callback
                ActivityCompat.requestPermissions(activity, arrayOf(permission), code)
            }
            if (rationale != null && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            )
                rationale.setCallback { openPermissionRequestDialog() }.display(this.requireContext())
            else
                openPermissionRequestDialog()
        } else {
            callback.invoke(true)
        }
    }

    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionUse = this.pendingPermissionUses.remove(requestCode)
        if (permissionUse != null) {
            this.activityResultCodes.release(requestCode)
            permissionUse.invoke(
                grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
            )
        }
    }

    override fun getDrawer(): DrawerLayout? = activity?.findViewById(R.id.main)

}