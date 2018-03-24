package models

object BudgetEntryDirection {
  def fromString(value: String): Either[String, BudgetEntryDirection] = value match {
    case "income" => Right(Income)
    case "expense" => Right(Expense)
    case _ => Left(s"'$value' is not a valid status")
  }
}


sealed trait BudgetEntryDirection
case object Income extends BudgetEntryDirection {
  override def toString: String = "income"
}
case object Expense extends BudgetEntryDirection {
  override def toString: String = "expense"
}