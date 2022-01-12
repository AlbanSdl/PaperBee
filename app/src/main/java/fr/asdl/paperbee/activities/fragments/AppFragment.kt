package fr.asdl.paperbee.activities.fragments

import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.annotation.TransitionRes
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import fr.asdl.paperbee.PaperBeeApplication
import fr.asdl.paperbee.R
import fr.asdl.paperbee.nfc.NfcTag
import fr.asdl.paperbee.sharing.FileAccessor
import fr.asdl.paperbee.sharing.PermissionAccessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class AppFragment : Fragment(), FileAccessor, PermissionAccessor, DrawerLock {

    private var pendingNfcTag: NfcTag? = null
    private var fileCreatorData: ByteArray? = null
    private var fileCreatorBreakpoint: Continuation<Boolean>? = null
    private var fileReaderBreakpoint: Continuation<ByteArray?>? = null
    private var permissionAccessorBreakpoint: Continuation<Boolean>? = null

    private val fileCreator = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        uri -> if (fileCreatorBreakpoint != null && uri == null) {
            fileCreatorBreakpoint!!.resume(false)
            fileCreatorBreakpoint = null
        } else if (fileCreatorData != null && fileCreatorBreakpoint != null)
        getScope().launch(Dispatchers.IO) {
            var success = false
            try {
                requireContext().applicationContext.contentResolver.openFileDescriptor(uri, "w")
                    ?.use {
                        FileOutputStream(it.fileDescriptor).write(fileCreatorData!!)
                        success = true
                    }
            } finally {
                fileCreatorData = null
                fileCreatorBreakpoint!!.resume(success)
                fileCreatorBreakpoint = null
            }
        }
    }
    private val fileReader = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            uri -> if (fileReaderBreakpoint != null && uri == null) {
                fileReaderBreakpoint!!.resume(null)
                fileReaderBreakpoint = null
            } else if (fileReaderBreakpoint != null) getScope().launch(Dispatchers.IO) {
                try {
                    requireContext().applicationContext.contentResolver.openFileDescriptor(uri, "r")?.use { desc ->
                        fileReaderBreakpoint!!.resume(FileInputStream(desc.fileDescriptor).readBytes())
                        fileReaderBreakpoint = null
                    }
                } catch (exception: Exception) {
                    fileReaderBreakpoint!!.resume(null)
                    fileReaderBreakpoint = null
                }

        }
    }
    private val permissionAccessor = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (permissionAccessorBreakpoint != null) {
            permissionAccessorBreakpoint!!.resume(granted)
            permissionAccessorBreakpoint = null
        }
    }

    open fun restoreState(savedInstanceState: Bundle) {}
    open fun saveState(savedInstanceState: Bundle) {}

    abstract val layoutId: Int
    open var menuLayoutId: Int? = null
    open val styleId: Int? = null

    @TransitionRes
    open val transitionIn: Int? = null
    @TransitionRes
    open val transitionOut: Int? = null

    abstract fun onLayoutInflated(view: View)

    @Deprecated("Don't use shouldRetainInstance anymore")
    open val shouldRetainInstance = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        this.retainInstance = this.shouldRetainInstance
        if (savedInstanceState != null) this.restoreState(savedInstanceState)
        this.setHasOptionsMenu(true)
        val layoutInflater = LayoutInflater.from(if (styleId != null) ContextThemeWrapper(
            activity,
            styleId!!,
        ) else inflater.context)

        val root = layoutInflater.inflate(this.layoutId, container, false)
        this.onLayoutInflated(root)
        return root
    }

    @CallSuper
    override fun onResume() {
        this.updateDrawerLock()
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

    final override suspend fun createFile(fileName: String, content: ByteArray): Boolean {
        return suspendCoroutine {
            fileCreatorData = content
            fileCreatorBreakpoint = it
            fileCreator.launch(fileName)
        }
    }

    final override suspend fun readFile(fileType: String?): ByteArray? {
        return suspendCoroutine {
            fileReaderBreakpoint = it
            fileReader.launch(arrayOf(fileType ?: "application/octet-stream"))
        }
    }

    override suspend fun usePermission(permission: String): Boolean {
        return suspendCoroutine {
            permissionAccessorBreakpoint = it
            permissionAccessor.launch(permission)
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

    fun getScope(): CoroutineScope {
        return (requireActivity().application as PaperBeeApplication).paperScope
    }

}