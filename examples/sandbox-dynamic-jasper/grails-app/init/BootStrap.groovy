import yakworks.reports.SeedData

class BootStrap {
    static Random rand = new Random()

    def init = { servletContext ->

        SeedData.seed()

    }
    def destroy = {
    }

}
