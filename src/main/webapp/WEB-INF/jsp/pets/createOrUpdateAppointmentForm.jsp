<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>


<petclinic:layout pageName="owners">
    <jsp:attribute name="customScript">
        <script>
            $(function () {
                $("#appointmentDate").datepicker({dateFormat: 'yy/mm/dd'});
            });
        </script>
    </jsp:attribute>
    <jsp:body>
        <h2><c:if test="${appointment['new']}">New </c:if>Appointment</h2>

        <b>Pet</b>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Name</th>
                <th>Birth Date</th>
                <th>Type</th>
                <th>Owner</th>
            </tr>
            </thead>
            <tr>
                <td><c:out value="${appointment.pet.name}"/></td>
                <td><petclinic:localDate date="${appointment.pet.birthDate}" pattern="yyyy/MM/dd"/></td>
                <td><c:out value="${appointment.pet.type.name}"/></td>
                <td><c:out value="${appointment.pet.owner.firstName} ${appointment.pet.owner.lastName}"/></td>
            </tr>
        </table>

        <form:form modelAttribute="appointment" class="form-horizontal">
            <div class="form-group has-feedback">
                <petclinic:inputField label="Date" name="appointmentDate"/>
                <petclinic:inputField label="Description" name="description"/>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <input type="hidden" name="petId" value="${appointment.pet.id}"/>
                    <button class="btn btn-default" type="submit">New Appointment</button>
                </div>
            </div>
        </form:form>
    </jsp:body>

</petclinic:layout>
