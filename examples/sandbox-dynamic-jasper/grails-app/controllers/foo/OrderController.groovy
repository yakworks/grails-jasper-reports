package foo

import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
class OrderController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Bills.list(params), model:[orderInstanceCount: Bills.count()]
    }

    def show(Bills orderInstance) {
        respond orderInstance
    }

    def create() {
        respond new Bills(params)
    }

    @Transactional
    def save(Bills orderInstance) {
        if (orderInstance == null) {
            notFound()
            return
        }

        if (orderInstance.hasErrors()) {
            respond orderInstance.errors, view:'create'
            return
        }

        orderInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'order.label', default: 'Order'), orderInstance.id])
                redirect orderInstance
            }
            '*' { respond orderInstance, [status: CREATED] }
        }
    }

    def edit(Bills orderInstance) {
        respond orderInstance
    }

    @Transactional
    def update(Bills orderInstance) {
        if (orderInstance == null) {
            notFound()
            return
        }

        if (orderInstance.hasErrors()) {
            respond orderInstance.errors, view:'edit'
            return
        }

        orderInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Order.label', default: 'Order'), orderInstance.id])
                redirect orderInstance
            }
            '*'{ respond orderInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Bills orderInstance) {

        if (orderInstance == null) {
            notFound()
            return
        }

        orderInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Order.label', default: 'Order'), orderInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
