package com.example.libraryrental.logic

sealed interface ReservationResult {
    data object Success : ReservationResult
    data object BookUnavailable : ReservationResult
    data object InvalidName : ReservationResult
    data object InvalidDuration : ReservationResult
}

sealed interface RenewResult {
    data object Renewed : RenewResult
    data object NotActive : RenewResult
    data object InvalidDuration : RenewResult
    data object ExceedsMaxLoan : RenewResult
}
