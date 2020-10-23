package fr.asdl.minder.sharing.permissions

interface PermissionAccessor {

    fun usePermission(
        permission: String,
        callback: (Boolean) -> Unit,
        rationale: PermissionRationale? = null
    )

}