package abv.logopek.netmd.ui.components

import abv.logopek.netmd.netschoolapi.Assignment
import abv.logopek.netmd.netschoolapi.Diary
import abv.logopek.netmd.netschoolapi.Lesson
import android.util.Log
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun LessonView(lesson: Lesson){
    var isLessonModalShown = remember { mutableStateOf(false) }
    val isAssignmentModalShown = remember { mutableStateOf(false) }
    var assignmentToShow: Assignment? by remember { mutableStateOf(null) }
    var assignments = lesson.assignments
    Box(Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(8.dp, 8.dp).clickable {
        isLessonModalShown.value = true
    }){
        Row(Modifier.fillMaxSize()) {
            Column {
                Text(lesson.subjectName, fontSize = TextUnit(18f, TextUnitType.Sp))
                Text("${lesson.startTime} - ${lesson.endTime}")
            }
            Box(Modifier.fillMaxSize()){
                LazyRow(Modifier.align(Alignment.CenterEnd)) {
                    items(assignments){ assignment ->
                        if (assignment.mark?.mark != null){
                            AssistChip(
                                onClick = {
                                    Log.d("NS", "Set assignment to new!")
                                    assignmentToShow = assignment
                                    isAssignmentModalShown.value = true
                                },
                                label = {
                                    Text(assignment.mark.mark.toString())
                                },
                            )
                        }

                    }
                }
            }

        }
    }
    if(isLessonModalShown.value){
        FloatingWindowLesson(null, isLessonModalShown, lesson)
    }
    if(isAssignmentModalShown.value && assignmentToShow != null){
        FloatingWindowAssignment(null, isAssignmentModalShown, assignmentToShow!!)
    }
}
