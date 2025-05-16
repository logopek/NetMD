package abv.logopek.netmd.netschoolapi

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val lt: String,
    val ver: String,
    val salt: String
)

@Serializable
data class LoginGet(
    val loginType: String,
    val scid: String,
    val un: String,
    val pw2: String,
    val lt: String,
    val ver: String
)

@Serializable
data class AuthResponse(
    val at: String,
    val code: Int? = null, // Use Int? for nullable integer
    val timeOut: Int, // 3600000 fits in Int
    val accessToken: String,
    val refreshToken: String,
    val accountInfo: AccountInfo,
    val tokenType: String,
    val entryPoint: String,
    val requestData: RequestData,
    val errorMessage: String? = null // Use String? for nullable string
)

@Serializable
data class AccountInfo(
    val activeToken: String? = null, // Use String? for nullable string
    val secureActiveToken: String,
    val currentOrganization: Organization,
    val user: User,
    val userRoles: List<Role>, // Assuming userRoles are objects with id and name like organizations
    val organizations: List<Organization>,
    val loginTime: String, // Can use kotlinx.datetime for better date/time handling if needed
    val active: Boolean,
    val canLogin: Boolean,
    val storeTokens: Boolean,
    val accessToken: String // Note: accessToken appears here and at the top level in the JSON
)

@Serializable
data class Organization(
    val id: Int,
    val name: String
)

@Serializable
data class User(
    val id: Int,
    val name: String
)

@Serializable
data class Role(
    val id: Int,
    val name: String
)

@Serializable
data class RequestData(
    val warnType: String
)


@Serializable
data class DiaryResponse(
    val students: List<Student>,
    val currentStudentId: Int,
    val weekStart: String, // Можно использовать kotlinx.datetime.Instant или LocalDate если нужна работа с датами
    val yaClass: Boolean,
    val yaClassAuthUrl: String? = null, // Поле может быть null
    val newDiskToken: String,
    val newDiskWasRequest: Boolean,
    val ttsuRl: String, // Исправлено на ttsuRl согласно JSON
    val externalUrl: String,
    val weight: Boolean,
    val version: String? = null, // Поле может быть null
    val maxMark: Int,
    val withLaAssigns: Boolean
)

@Serializable
data class Student(
    val studentId: Int,
    val nickName: String,
    val className: String? = null, // Поле может быть null
    val classId: Int,
    val iupGrade: Int
)

@Serializable
data class SchoolYearResponse(
    val globalYearId: Int,
    val schoolId: Int,
    val startDate: String, // Можно использовать kotlinx.datetime.LocalDate/Instant
    val endDate: String,   // Можно использовать kotlinx.datetime.LocalDate/Instant
    val name: String,
    val archiveStatus: ArchiveStatus,
    val status: String,
    val weekEndSet: Int,
    val id: Int
)

@Serializable
data class ArchiveStatus(
    val status: Int,
    val date: String? = null // Поле может быть null
)


@Serializable
data class Diary(
    val weekStart: LocalDateTime, // Соответствует "2025-05-12T00:00:00"
    val weekEnd: LocalDateTime,   // Соответствует "2025-05-18T00:00:00"
    val weekDays: List<WeekDay>,
    val laAssigns: List<Assignment>, // Предполагаем, что структура такая же, как у Assignment
    val termName: String,
    val className: String
)

// Класс для одного дня недели
@Serializable
data class WeekDay(
    val date: LocalDateTime, // Соответствует "2025-05-12T00:00:00"
    val lessons: List<Lesson>
)

// Класс для одного урока
@Serializable
data class Lesson(
    val assignments: List<Assignment>,
    val isDistanceLesson: Boolean,
    val isEaLesson: Boolean,
    val classmeetingId: Long, // Используем Long для больших ID
    val day: LocalDateTime, // Дублирует WeekDay.date, но оставляем как в JSON
    val number: Int,
    val relay: Int,
    val room: String,
    val startTime: LocalTime, // Соответствует "08:30"
    val endTime: LocalTime,   // Соответствует "09:00"
    val subjectName: String,
    val issueClassMeetingId: Long? = null // Может отсутствовать, делаем nullable
)

// Класс для одного задания
@Serializable
data class Assignment(
    val id: Long,
    val typeId: Int,
    val assignmentName: String,
    val weight: Int,
    val dueDate: LocalDateTime, // Соответствует "2025-05-12T00:00:00"
    val classAssignment: Boolean,
    val classMeetingId: Long,
    val mark: Mark? = null, // Может отсутствовать (например, если еще не выставлена), делаем nullable
    val issueClassMeetingId: Long? = null // Может отсутствовать
)

// Класс для оценки (mark)
@Serializable
data class Mark(
    val id: Long,
    val studentId: Long,
    val mark: Int, // Или String/Double, если оценка может быть не целым числом или другим форматом
    val resultScore: Int, // Или Double
    val dutyMark: Boolean,
    val assignmentId: Long
)