import ar.com.fdvs.dj.domain.constants.Font
import ar.com.fdvs.dj.domain.constants.Border
import java.awt.Color
import ar.com.fdvs.dj.domain.constants.Transparency
import ar.com.fdvs.dj.domain.constants.HorizontalAlign
import ar.com.fdvs.dj.domain.constants.VerticalAlign
import ar.com.fdvs.dj.domain.constants.Rotation
import ar.com.fdvs.dj.domain.constants.Stretching
import ar.com.fdvs.dj.domain.constants.Page

dynamicJasper {

    useFullPageWidth = true

    page = Page.Page_Legal_Portrait()

    isUsingImagesToAlign = true

    intPattern = '#0'
    floatPattern = '#0.00'
    datePattern = 'MM/dd/yyyy'

    titleStyle {
        font = new Font(18, Font._FONT_VERDANA,true)
        textColor = Color.black
        border = Border.NO_BORDER()
        borderColor = Color.darkGray
        horizontalAlign = HorizontalAlign.CENTER
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.white
        transparency = Transparency.OPAQUE
        transparent = false
        blankWhenNull = true
        padding = 2
        pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

    groupStyle {

    }

    subtitleStyle {
        font = new Font(14, Font._FONT_VERDANA, false)
        textColor = Color.black
        border = Border.NO_BORDER()
        borderColor = Color.darkGray
        horizontalAlign = HorizontalAlign.LEFT
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.white
        transparency = Transparency.OPAQUE
        transparent = false
        blankWhenNull = true
        padding = 2
        pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

    headerStyle {
        font = Font.ARIAL_MEDIUM_BOLD
        textColor = Color.black
        border = Border.THIN()
        borderColor = Color.darkGray
        horizontalAlign = HorizontalAlign.CENTER
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.decode("#f9fafb")
        transparency = Transparency.OPAQUE
        transparent = false
        blankWhenNull = true
        padding = 2
        pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

    detailStyle {
        font = Font.ARIAL_MEDIUM
        textColor = Color.black
        borderTop = Border.NO_BORDER()
        borderBottom = Border.THIN()
        borderLeft = Border.NO_BORDER()
        borderRight = Border.NO_BORDER()
        borderColor = Color.LIGHT_GRAY
        horizontalAlign = HorizontalAlign.LEFT
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.white
        transparency = Transparency.OPAQUE
        //transparent = false
        blankWhenNull = true
        padding = 4
        //pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

}
