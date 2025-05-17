package abv.logopek.netmd.ui.components

import abv.logopek.netmd.netschoolapi.Assignment
import abv.logopek.netmd.netschoolapi.Lesson
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowLesson(p: PaddingValues? = null, isEnabled: MutableState<Boolean>, lesson: Lesson){
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Если не нужен частично раскрытый режим
    )
    ModalBottomSheet(
        onDismissRequest = {isEnabled.value = false},
        sheetState = sheetState,

    ) {
        Column(modifier = Modifier.padding(12.dp, 12.dp)) {
            Text(lesson.subjectName)
            Spacer(Modifier.size(0.dp, 4.dp))
            for(assignment in lesson.assignments.sortedByDescending { it.typeId }){
                Box(Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(16.dp)
                    )){
                    if(assignment.typeId == 3){

                        Column(Modifier.padding(8.dp, 2.dp)) {
                            Text("Домашнее задание")
                            Text(assignment.assignmentName)
                        }

                    }
                    else{
                        Column(Modifier.padding(8.dp, 2.dp)) {
                            Text("Тема урока")
                            Text(assignment.assignmentName)
                        }

                    }
                }
                Spacer(Modifier.size(0.dp, 8.dp))
            }
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowAssignment(p: PaddingValues? = null, isEnabled: MutableState<Boolean>, assignment: Assignment){
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Если не нужен частично раскрытый режим
    )
    ModalBottomSheet(
        onDismissRequest = {isEnabled.value = false},
        sheetState = sheetState,

        ) {

        Column(modifier = Modifier.padding(12.dp, 12.dp)) {
            Text(assignment.assignmentName)
            Row(Modifier.wrapContentSize(Alignment.Center), verticalAlignment = Alignment.CenterVertically){
                SuggestionChip(
                    onClick = {},
                    label = {Text(assignment.mark?.mark.toString())},

                )
                Spacer(Modifier.padding(4.dp, 0.dp))
                Text("×")
                Spacer(Modifier.padding(4.dp,0.dp))
                SuggestionChip(
                    onClick = {},
                    label = {Text(assignment.weight.toString())},

                )
            }


        }

    }
}