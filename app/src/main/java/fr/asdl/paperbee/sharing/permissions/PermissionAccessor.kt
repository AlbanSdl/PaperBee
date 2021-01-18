package fr.asdl.paperbee.sharing.permissions

/**
 * Indicates that this Object can use App Permissions.
 * To ensure the app will not crash because a permission has not been granted, use this method
 * every time you need a permission.
 */
interface PermissionAccessor {

    /**
     * The Permission Checker function.
     *
     * @param permission the android/app permission id such as [android.Manifest.permission.NFC]
     * @param rationale if the permission you ask is not obvious for the end user, you should
     * explain why you're asking for a such permission. In that case, use a [PermissionRationale]
     * where you can give more details to the user.
     *
     * @return whether the permission has been granted, thus check it before executing
     * the permission-relative code.
     */
    suspend fun usePermission(
        permission: String,
        rationale: PermissionRationale? = null
    ): Boolean

}