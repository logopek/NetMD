package abv.logopek.netmd.netschoolapi

import kotlinx.datetime.DateTimeUnit
/*
open class NetSchoolApiSchema(){
    class Meta{
        val dateformat = "%Y-%m-%dT00:00:00"
    }
}

data class Attachment(
    val id: Int,
    val name: String,
    val description: String?
): NetSchoolApiSchema()

data class Author(
    val id: Int,
    val fullName: String,
    val nickname: String
): NetSchoolApiSchema()

data class Announcement(
    val name: String,
    val author: Author,
    val content: String,
    val postDate: DateTimeUnit,
    val attachments: List<Attachment>
): NetSchoolApiSchema()

data class Assigmnment(
    val id: Int,
    val comment: String,
    val type: String,
    val mark: Int,
    val isDuty: Boolean,
    val deadLine: DateTimeUnit
) : NetSchoolApiSchema() {
    fun unwrapMarks(){
        // TODO: Make Implementation
    }
}

data class Lesson(
    val day: DateTimeUnit,
    val start: DateTimeUnit,
    val end: DateTimeUnit,
    val room: String,
    val number: Int,
    val subject: String,
    val assignments: List<Assigmnment>
): NetSchoolApiSchema()

data class Day(
    val lessons: List<Lesson>,
    val day: DateTimeUnit,
): NetSchoolApiSchema()

data class ShortSchool(
    val name: String,
    val id: String,
    val address: String,
    ) : NetSchoolApiSchema()

data class School(
    val name: String,
    val about: String,
    val address: String,
    val email: String,
    val site: String,
    val phone: String,
    val director: String,
    val AHC: String,
    val IT: String,
    val UVR: String,
): NetSchoolApiSchema() {
    fun unwrapNestedDicts(){
        // TODO: Implement
    }
}
*/