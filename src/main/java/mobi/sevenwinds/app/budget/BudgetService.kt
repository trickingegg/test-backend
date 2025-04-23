package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord, authorId: Int?): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = authorId?.let { AuthorEntity.findById(it) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam, fioFilter: String?): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable.innerJoin(AuthorTable, { BudgetTable.authorId }, { AuthorTable.id })
                .select { BudgetTable.year eq param.year }

            fioFilter?.let {
                query.andWhere { AuthorTable.fio.lowerCase() like "%${fioFilter}%" }
            }

            val total = query.count()
            val totalByType = query.groupBy { it[BudgetTable.type].toString() }
                .mapValues { (_, rows) -> rows.sumOf { it[BudgetTable.amount] } }

            val paginatedQuery = query
                .orderBy(BudgetTable.month to true, BudgetTable.amount to false)
                .limit(param.limit, param.offset)

            val data = BudgetEntity.wrapRows(paginatedQuery).map {
                val author = it.authorId?.let { author ->
                    mapOf("fio" to author.fio, "creationDate" to author.creationDate)
                }
                it.toResponse().copy(author = author)
            }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = totalByType,
                items = data
            )
        }
    }
}