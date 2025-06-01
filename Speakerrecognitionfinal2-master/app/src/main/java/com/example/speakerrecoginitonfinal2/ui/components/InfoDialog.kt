package com.example.speakerrecoginitonfinal2.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.speakerrecoginitonfinal2.R

/**
 * Genel amaçlı bir bilgi/hata dialog'u.
 *
 * @param title Dialog başlığı.
 * @param message Dialog mesajı.
 * @param icon Opsiyonel olarak dialog başlığında gösterilecek ikon.
 * @param onDismissRequest Dialog kapatıldığında çağrılacak fonksiyon.
 * @param confirmButtonText Onay butonu metni, varsayılan olarak "Tamam".
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    icon: ImageVector? = null,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = stringResource(id = R.string.dialog_close_button)
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(confirmButtonText)
            }
        }
    )
}