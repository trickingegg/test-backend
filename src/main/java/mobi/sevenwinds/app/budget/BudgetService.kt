package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val allRecordsQuery = BudgetTable.select { BudgetTable.year eq param.year }

            val total = allRecordsQuery.count()
            val totalByType = allRecordsQuery.groupBy { it[BudgetTable.type] }
                .mapValues { (_, rows) -> rows.sumOf { it[BudgetTable.amount] } }

            val paginatedQuery = BudgetTable
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month to true, BudgetTable.amount to false)
                .limit(param.limit, param.offset)

            val data = BudgetEntity.wrapRows(paginatedQuery).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = totalByType,
                items = data
            )
        }
    }
}