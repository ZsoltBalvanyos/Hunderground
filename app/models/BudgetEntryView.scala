package models

import java.time.LocalDate

case class BudgetEntryView(budgetEntryId: Long,
                           event: Long,
                           eventType: String,
                           eventDesc: String,
                           eventDate: LocalDate,
                           inputted: LocalDate,
                           desc: String,
                           who: String,
                           payment: String,
                           amount: Double,
                           direction: String,
                           by: Int)