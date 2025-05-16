package abv.logopek.netmd.netschoolapi

open class NetSchoolApiError: Exception()
class AuthError: NetSchoolApiError()
class SchoolNotFoundError: NetSchoolApiError()
class NoResponseFromServer: NetSchoolApiError()