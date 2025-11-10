package com.cturner56.cooperative_demo_1_device_statistics.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

            HorizontalDivider(modifier = Modifier .padding(12.dp))

            FeedbackSections()
        }
    }
}

@Composable
fun FeedbackSections() {
    var subject by remember { mutableStateOf("")}
    var extraText by remember { mutableStateOf("")}
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)

    ) {
        OutlinedTextField(
            value = subject,
            onValueChange =  {subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = extraText,
            onValueChange =  {extraText = it },
            label = { Text("Feedback") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 6,
            maxLines = 12
        )
        Button(
            onClick = {
                composeEmail(
                    context = context,
                    addresses = arrayOf("cturner56@academic.rrc.ca"),
                    subject = subject,
                    extraText = extraText
                )
            },
        ) {
            Text("Submit Feedback")
        }
    }
}

fun composeEmail(context: Context, addresses: Array<String>, subject: String, extraText: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_EMAIL, addresses)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, extraText)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        startActivity(context, intent, null)
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        FeedbackScreen()
    }
}