package models

import java.time.LocalDate

case class BudgetEntry(budgetEntryId: Long,
                       event: Long,
                       date: LocalDate,
                       desc: String,
                       who: Int,
                       done: Boolean,
                       amount: Double,
                       direction: BudgetEntryDirection,
                       by: Int)