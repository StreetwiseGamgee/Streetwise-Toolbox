package com.cturner56.streetwise_toolbox.screens

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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.cturner56.streetwise_toolbox.R

/**
 * Main composable which is responsible managing the state and logic for the feedback screen.
 * Users can type their feedback here, and have it submitted in their desired email client.
 */
@Composable
fun FeedbackScreen(){
    var subject by remember { mutableStateOf("")}
    var feedbackBody by remember { mutableStateOf("")}
    val context = LocalContext.current

    val onSubmit = {
        composeEmail(
            context = context,
            addresses = arrayOf("cturner56@academic.rrc.ca"),
            subject = subject,
            extraText = feedbackBody
        )
    }

    FeedbackScreenContents(
        subject = subject,
        onSubjectChange = { subject = it },
        feedbackBody = feedbackBody,
        onFeedbackBodyChange = { feedbackBody = it },
        onSubmit = onSubmit
    )
}

/**
 * A re-usable stateless composable which is responsible for displaying the UI of the feedback screen.
 *
 * @param subject The current text which is entered in the subject field of the feedback form.
 * @param onSubjectChange A lambda function which is responsible for updating the [subject] state.
 * @param feedbackBody The current text which is entered in the feedback body field of the feedback form.
 * @param onFeedbackBodyChange A lambda function which is responsible for updating the [feedbackBody] state.
 */
@Composable
fun FeedbackScreenContents(
    subject: String,
    onSubjectChange: (String) -> Unit,
    feedbackBody: String,
    onFeedbackBodyChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
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
            HorizontalDivider(modifier = Modifier.padding(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { OutlinedTextField(
                value = subject,
                onValueChange = onSubjectChange,
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
                OutlinedTextField(
                    value = feedbackBody,
                    onValueChange = onFeedbackBodyChange,
                    label = { Text("Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 6,
                    maxLines = 12
                )
                Button(onClick = onSubmit) {
                    Text("Submit Feedback")
                }
            }
        }
    }
}

/**
 * A function which is responsible for launching an email intent.
 * Allowing users to submit their feedback through their preferred email client.
 *
 * The function constructs an [Intent] using [Intent.ACTION_SEND] to open the email application chosen.
 * From there it pre-populates the respective recipient address (myself), and the user's feedback.
 *
 * @param context The context which is used to start the activity to open an email client.
 * @param addresses An array of recipient email addresses.
 * @param subject The subject line of the composed email.
 * @param extraText The main body of the composed email.
 */
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