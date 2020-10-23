package fr.asdl.minder.sharing.files

import androidx.annotation.StringRes
import fr.asdl.minder.R

enum class FileCreationResult(@StringRes val str: Int, val hasTried: Boolean) {
    CREATED(R.string.shared_to_file_ok, true),
    ERROR(R.string.shared_to_file_exception, true),
    NOT_OPENED(0, false);
}