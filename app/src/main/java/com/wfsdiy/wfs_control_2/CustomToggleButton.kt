import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomToggleButton(
    text: String,
    isOn: Boolean,
    onColor: Color,
    offColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mainContentBackgroundColor = if (isOn) onColor else offColor
    val textColor = Color.White
    val outerShape = RoundedCornerShape(8.dp)
    val innerCornerRadius = 3.dp

    val outerBorderThickness = 2.dp
    val blackLineThickness = 3.dp
    val textContentPadding = 4.dp

    Box( // Outer Box for border and black line
        modifier = modifier
            .aspectRatio(1.5f)
            .fillMaxWidth()
            .clip(outerShape)
            .clickable(onClick = onClick)
            .border(width = outerBorderThickness, color = borderColor, shape = outerShape)
            .padding(outerBorderThickness)
            .background(Color.Black)
            .padding(blackLineThickness),
        contentAlignment = Alignment.Center
    ) {
        // Inner Box for the main content background and text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(innerCornerRadius))
                .background(mainContentBackgroundColor)
                .padding(textContentPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 14.sp
            )
        }
    }
}
