package com.cturner56.streetwise_toolbox.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cturner56.streetwise_toolbox.R
import com.cturner56.streetwise_toolbox.ui.theme.CooperativeDemo1DeviceStatisticsTheme

/**
 * The main composable which comprises the [AboutScreen].
 * Displays information retrieved from [AboutDevSection] and [AboutAppSection]
 */
@Composable
fun AboutScreen() {
    Column (
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ){
        AboutDevSection()
        AboutAppSection()
    }
}

/**
 * A card component which displays information about the developer.
 * The stringResource value is pulled from /res/values/strings.xml
 */
@Composable
fun AboutDevSection() {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(25.dp)) {
            Text(
                text = "About Developer",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(text = stringResource(id = R.string.about_developer_description))
        }
    }
}

/**
* A card component which displays information about the application.
* The stringResource value is pulled from /res/values/strings.xml
*/
@Composable
fun AboutAppSection() {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(25.dp)) {
            Text(
                text = "About Application",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(text = stringResource(id = R.string.about_application_description))
        }
    }
}

/**
 * A preview composable for the [AboutScreen].
 * Providing a means to visualize the screen without running the application.
 */
@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        AboutScreen()
    }
}