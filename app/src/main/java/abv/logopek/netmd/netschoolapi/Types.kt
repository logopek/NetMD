package abv.logopek.netmd.netschoolapi

import kotlinx.datetime.LocalDate
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
    val code: Int? = null,
    val timeOut: Int,
    val accessToken: String,
    val refreshToken: String,
    val accountInfo: AccountInfo,
    val tokenType: String,
    val entryPoint: String,
    val requestData: RequestData,
    val errorMessage: String? = null
)

@Serializable
data class AccountInfo(
    val activeToken: String? = null,
    val secureActiveToken: String,
    val currentOrganization: Organization,
    val user: User,
    val userRoles: List<Role>,
    val organizations: List<Organization>,
    val loginTime: String,
    val active: Boolean,
    val canLogin: Boolean,
    val storeTokens: Boolean,
    val accessToken: String
)

@Serializable
data class Organization(
    val id: Int,
    val name: String,
    val provinceId: Int? = null,
    val cityId: Int? = null,
    val inn: String? = null,
    val orgn: String? = null,
    val address: String? = null,
    val shortName: String? = null,
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
    val weekStart: String,
    val yaClass: Boolean,
    val yaClassAuthUrl: String? = null,
    val newDiskToken: String,
    val newDiskWasRequest: Boolean,
    val ttsuRl: String,
    val externalUrl: String,
    val weight: Boolean,
    val version: String? = null,
    val maxMark: Int,
    val withLaAssigns: Boolean
)

@Serializable
data class Student(
    val studentId: Int,
    val nickName: String,
    val className: String? = null,
    val classId: Int,
    val iupGrade: Int
)

@Serializable
data class SchoolYearResponse(
    val globalYearId: Int,
    val schoolId: Int,
    val startDate: String,
    val endDate: String,
    val name: String,
    val archiveStatus: ArchiveStatus,
    val status: String,
    val weekEndSet: Int,
    val id: Int
)

@Serializable
data class ArchiveStatus(
    val status: Int,
    val date: String? = null
)


@Serializable
data class Diary(
    val weekStart: LocalDateTime,
    val weekEnd: LocalDateTime,
    val weekDays: List<WeekDay>,
    val laAssigns: List<Assignment>,
    val termName: String? = null,
    val className: String? = null
)


@Serializable
data class WeekDay(
    val date: LocalDateTime,
    val lessons: List<Lesson>
)


@Serializable
data class Lesson(
    val assignments: List<Assignment>,
    val isDistanceLesson: Boolean,
    val isEaLesson: Boolean,
    val classmeetingId: Long,
    val day: LocalDateTime,
    val number: Int,
    val relay: Int,
    val room: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val subjectName: String,
    val issueClassMeetingId: Long? = null
)


@Serializable
data class Assignment(
    val id: Long,
    val typeId: Int,
    val assignmentName: String,
    val weight: Int,
    val dueDate: LocalDateTime,
    val classAssignment: Boolean,
    val classMeetingId: Long,
    val mark: Mark? = null,
    val issueClassMeetingId: Long? = null
)


@Serializable
data class Mark(
    val id: Long,
    val studentId: Long,
    val mark: Int? = null,
    val resultScore: Int? = null,
    val dutyMark: Boolean,
    val assignmentId: Long
)