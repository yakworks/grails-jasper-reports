package foo

import yakworks.reports.ReportFormat

class ReportsController {

    List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]

    def index() {
        render(view:"${params.id}.jrxml",model:[data:dataList, "ReportTitle":"Controller Report"])
    }

    def accept() {
        String format = response.format
        render format
    }

    def acceptFormat() {
        Map rptModel = [data:dataList, "ReportTitle":"Controller Report"]
        //render "hello"
        render(view:"testme.jrxml",model:rptModel)
    }

    def specifyFormat() {
        String format = ReportFormat.get(response.format)?:ReportFormat.HTML
        Map rptModel = [format:format, data:dataList, "ReportTitle":"Controller Report"]
        //render "hello"
        render(view:"testme.jrxml",model:rptModel)
    }
}
