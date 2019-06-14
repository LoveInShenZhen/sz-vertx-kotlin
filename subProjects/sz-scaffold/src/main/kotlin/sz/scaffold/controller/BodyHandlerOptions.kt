package sz.scaffold.controller

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.BodyHandler

class BodyHandlerOptions {
    var bodyLimit: Long = BodyHandler.DEFAULT_BODY_LIMIT

    var uploadsDirectory: String = BodyHandler.DEFAULT_UPLOADS_DIRECTORY

    var mergeFormAttributes: Boolean = false //BodyHandler.DEFAULT_MERGE_FORM_ATTRIBUTES

    var deleteUploadedFilesOnEnd: Boolean = BodyHandler.DEFAULT_DELETE_UPLOADED_FILES_ON_END

    constructor()

    constructor(json: JsonObject) : this() {
        if (json.getValue("bodyLimit") is Number) {
            this.bodyLimit = (json.getValue("bodyLimit") as Number).toLong()
        }

        if (json.getValue("mergeFormAttributes") is Boolean) {
            this.mergeFormAttributes = json.getValue("mergeFormAttributes") as Boolean
        }

        if (json.getValue("deleteUploadedFilesOnEnd") is Boolean) {
            this.deleteUploadedFilesOnEnd = json.getValue("deleteUploadedFilesOnEnd") as Boolean
        }

        if (json.getValue("uploadsDirectory") is String) {
            this.uploadsDirectory = json.getValue("uploadsDirectory") as String
        }
    }
}