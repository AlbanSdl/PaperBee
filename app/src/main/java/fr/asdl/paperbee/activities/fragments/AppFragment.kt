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
import fr.asdl.paperbee.activities.fragments.sharing.NfcTag
import fr.asdl.paperbee.sharing.files.FileAccess
import fr.asdl.paperbee.sharing.files.FileAccessResult
import fr.asdl.paperbee.sharing.files.FileAccessor
import fr.asdl.paperbee.sharing.files.AccessedFile
import fr.asdl.paperbee.sharing.permissions.PermissionAccessor
import fr.asdl.paperbee.sharing.permissions.PermissionRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class AppFragment : Fragment(), FileAccessor, PermissionAccessor, DrawerLock {

    private var pendingNfcTag: NfcTag? = null
    private val activityResultCodes = IntAllocator()
    private val pendingFileAccesses = hashMapOf<Int, Continuation<FileAccess>>()
    private val pendingPermissionUses = hashMapOf<Int, Continuation<Boolean>>()
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

    final override suspend fun createFile(
            fileName: String,
            fileType: String?,
            content: ByteArray
    ): AccessedFile {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType ?: "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        val code = activityResultCodes.allocate()
        val res: FileAccess = suspendCoroutine {
            this.pendingFileAccesses[code] = it
            startActivityForResult(intent, code)
        }
        return suspendCoroutine {
            GlobalScope.launch(Dispatchers.IO) {
                if (res.result.success)
                    requireContext().applicationContext.contentResolver.openFileDescriptor(res.uri!!, "w")?.use {
                        FileOutputStream(it.fileDescriptor).write(content)
                    }
                it.resume(AccessedFile(res.result, null))
            }
        }
    }

    final override suspend fun readFile(fileType: String?): AccessedFile {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType ?: "application/octet-stream"
        }
        val code = activityResultCodes.allocate()
        val res: FileAccess = suspendCoroutine {
            this.pendingFileAccesses[code] = it
            startActivityForResult(intent, code)
        }
        return suspendCoroutine {
            GlobalScope.launch(Dispatchers.IO) {
                if (res.result.success) {
                    requireContext().applicationContext.contentResolver.openFileDescriptor(res.uri!!, "r")?.use { desc ->
                        it.resume(AccessedFile(res.result, FileInputStream(desc.fileDescriptor).readBytes()))
                    }
                } else {
                    it.resume(AccessedFile(res.result, null))
                }
            }
        }
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val futureFileAccess = this.pendingFileAccesses.remove(requestCode)
        if (futureFileAccess != null) {
            this.activityResultCodes.release(requestCode)
            if (resultCode == Activity.RESULT_OK) data?.data?.also { uri ->
                try {
                    futureFileAccess.resume(FileAccess(FileAccessResult.ACCESSED, uri))
                } catch (io: Exception) {
                    futureFileAccess.resume(FileAccess(FileAccessResult.ERROR))
                }
            }
            else futureFileAccess.resume(FileAccess(FileAccessResult.CANCELLED))
        }
    }

    override suspend fun usePermission(
            permission: String,
            rationale: PermissionRationale?
    ): Boolean {
        val activity = activity ?: return false
        suspend fun openPermissionRequestDialog(): Boolean {
            val code = activityResultCodes.allocate()
            return suspendCoroutine {
                this.pendingPermissionUses[code] = it
                ActivityCompat.requestPermissions(activity, arrayOf(permission), code)
            }
        }
        return if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
            if (rationale != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                if (rationale.display(this.requireContext())) openPermissionRequestDialog()
                else false
            else openPermissionRequestDialog()
        else true
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
            permissionUse.resume(grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
    }

    override fun getDrawer(): DrawerLayout? = activity?.findViewById(R.id.main)

    @CallSuper
    override fun onStart() {
        super.onStart()
        if (this is FragmentContainer<*> && this.pendingNfcTag != null) {
            (this.childFragmentManager.findFragmentById(
                    this.getFragmentContainerId()
            ) as? AppFragment)?.onNdefMessage(this.pendingNfcTag)
            this.pendingNfcTag = null
        }
    }

    /**
     * Called when Nfc events are triggered by android.
     * This function will be called when a nfc tag is detected nearby. It may contain data,
     * otherwise it's a write only tag. (Content will be empty with a zero length list)
     * This method propagates to [SubFragment]s.
     * @param nfcTag the detected nfc tag, if it corresponds to a supported tech
     */
    @CallSuper
    open fun onNdefMessage(nfcTag: NfcTag?) {
        if (this is FragmentContainer<*>) {
            if (this.isVisible) {
                (this.childFragmentManager.findFragmentById(
                        this.getFragmentContainerId()
                ) as? AppFragment)?.onNdefMessage(nfcTag)
            } else {
                this.pendingNfcTag = nfcTag
            }
        }
    }

}