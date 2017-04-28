package nine.jasper.spring

import grails.util.Holders
import nine.reports.ReportFormat

/**
 * An abstract representation of a Jasper report.
 */
class JasperReportDef implements Serializable {

  /**
   * The name of the report file
   * The file can be in jrxml- or jasper-format.
   */
  String name

  /**
   * The data source used to fill the report.
   * <p>
   * This is a list of java beans.
   */
  Collection reportData

  /**
   * The target file format.
   */
  ReportFormat fileFormat = ReportFormat.PDF

  /**
   * The generated report as OutputStream.
   */
  ByteArrayOutputStream contentStream

  /**
   * Additional parameters.
   */
  Map parameters = [:]

  /**
   * Locale setting.
   */
  Locale locale


  private getApplicationContext() {
    return Holders.grailsApplication.mainContext
  }

  /**
   * Add a parameter to the parameter map.
   * @param key , the key
   * @param value , the value
   */
  void addParameter(key, value) {
    parameters.put(key, value)
  }
}
