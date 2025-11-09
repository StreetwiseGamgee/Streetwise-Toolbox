package com.cturner56.cooperative_demo_1_device_statistics.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.cturner56.cooperative_demo_1_device_statistics.R
import com.cturner56.cooperative_demo_1_device_statistics.ui.theme.CooperativeDemo1DeviceStatisticsTheme

@Composable
fun FeedbackScreen(){
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(25.dp)) {
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(text = stringResource(id = R.string.feedback_welcome_description))
        }
    }
}

//@Composable
// // https://developer.android.com/guide/components/intents-common POTENTIAL IMPLEMENTATION NEEDS REFACTORING
//fun composeEmail(addresses: Array<String>, subject: String, attachment: Uri) {
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = "*/*"
//        putExtra(Intent.EXTRA_EMAIL, addresses)
//        putExtra(Intent.EXTRA_SUBJECT, subject)
//        putExtra(Intent.EXTRA_STREAM, attachment)
//    }
//    if (intent.resolveActivity(packageManager) != null) {
//        startActivity(intent)
//    }
//}

@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        FeedbackScreen()
    }
}