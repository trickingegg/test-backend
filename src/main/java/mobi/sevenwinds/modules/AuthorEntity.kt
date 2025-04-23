import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

object AuthorTable : Table("Author") {
    val id = integer("ID").autoIncrement()
    val fio = varchar("FIO", 255)
    val creationDate = datetime("creationDate").defaultExpression(CurrentDateTime())

    override val primaryKey = PrimaryKey(id)
}

data class AuthorEntity(
    val id: Int,
    val fio: String,
    val creationDate: LocalDateTime
)