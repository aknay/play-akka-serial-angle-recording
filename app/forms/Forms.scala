package forms
import play.api.data.Form
import play.api.data.Forms._

object Forms {

    val dateForm = Form(single(
      "date" -> nonEmptyText
    ))

}
