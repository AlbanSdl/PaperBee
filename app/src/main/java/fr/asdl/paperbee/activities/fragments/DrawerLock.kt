package fr.asdl.paperbee.activities.fragments

import androidx.drawerlayout.widget.DrawerLayout

/**
 * Indicates that the Object has direct control over a DrawerLayout.
 * It can lock it (disable it actually) or enable it at any time.
 */
interface DrawerLock {

    /**
     * Retrieves the Drawer to control. The return value of this method is not supposed to be
     * constant as you can control multiple drawer layouts with one single fragment (eg. left and
     * right drawer)
     */
    fun getDrawer(): DrawerLayout?

    /**
     * Retrieves whether the drawer should be locked (disabled/hidden).
     * This value is not supposed to be final as you may control multiple drawer layouts with
     * this DrawerLock.
     */
    fun shouldLockDrawer(): Boolean

    /**
     * Updates the lock mode of the chosen drawer.
     */
    fun updateDrawerLock() {
        this.getDrawer()?.setDrawerLockMode(
            if (shouldLockDrawer()) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            else DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

}