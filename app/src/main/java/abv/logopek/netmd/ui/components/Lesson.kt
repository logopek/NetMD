package abv.logopek.netmd.ui.components

import abv.logopek.netmd.netschoolapi.Assignment
import abv.logopek.netmd.netschoolapi.Lesson
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType

@Composable
fun LessonView(lesson: Lesson){
    var assignments = lesson.assignments
    Box(){
        Row() {
            Column {
                Text(lesson.subjectName, fontSize = TextUnit(18f, TextUnitType.Sp))
                Text("${lesson.startTime} - ${lesson.endTime}")
            }

            LazyRow {
                items(assignments){ assignment ->
                    if (assignment.mark?.mark != null){
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(assignment.mark.mark.toString())
                            }
                        )
                    }

                }
            }
        }
    }
}
